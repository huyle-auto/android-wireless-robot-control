package com.example.myrobotapp.Class.Bluetooth;

import static com.example.myrobotapp.Class.ProtocolReceive.bytesToHex;
import static com.example.myrobotapp.Fragments.Features.HistoryFragment.recordOperation;
import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.globalSelectedBTDevice;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.ArraySet;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.myrobotapp.Class.Communication.PriorityCommunication;
import com.example.myrobotapp.Class.Dialog.InfoDialog;
import com.example.myrobotapp.Class.ProtocolSend;
import com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment;
import com.example.myrobotapp.HomeScreenActivity;
import com.google.android.material.snackbar.Snackbar;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Queue;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

public class BluetoothHelper {
    private static final byte STX = 0x02, ETX = 0x03, DLE = 0x10;
    private static final int CRC_POLYNOMIAL = 0x1021; // Polynomial for CRC-16-CCITT (XModem)
    private static final int CRC_INITIAL = 0x0000; // Initial CRC value

    final int MAX_RETRIES = 1;
    final int TIMEOUT_MS = 2000;

    public static final UUID BLE_SERVICE_UUID = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");
    public static final UUID BLE_CHARACTERISTIC_UUID = UUID.fromString("00001801-0000-1000-8000-00805f9b34fb");
    public static final UUID RFCOMM_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");

    private static final String TAG = "Bluetooth";
    private final BluetoothAdapter bluetoothAdapter;
    public static final Map<BluetoothDevice, BluetoothSocket> socketMap = new HashMap<>();
    private final Context context;
    public static final Map<BluetoothDevice, BluetoothGatt> gattMap = new HashMap<>();
    private final ByteArrayOutputStream baos = new ByteArrayOutputStream();
    private ExecutorService btSendExecutor;

    private final List<BluetoothConnectionListener> connectionListeners = new ArrayList<>();

    private static final List<BluetoothFrameListener> bluetoothFrameListeners = new ArrayList<>();
    private final Map<BluetoothDevice, ExecutorService> frameReceiverExecutors = new ConcurrentHashMap<>();
    private final Map<BluetoothDevice, Boolean> isReceivingFrames = new ConcurrentHashMap<>();
    private final ByteArrayOutputStream frameBuffer = new ByteArrayOutputStream();

    public static Queue<Long> btSendTimes = new ConcurrentLinkedQueue<>();
    public static AtomicInteger btSentCounter = new AtomicInteger(0);

    public enum ConnectionState {
        CONNECTED,
        DISCONNECTED
    }

    public enum Disconnector {
        SELF,
        PARTNER,
        NONE
    }

    Handler handler = new Handler();

    // ***************************** ON CONNECTED DEVICES CHANGE ****************************
    // Register listener
    public void registerConnectionListener(BluetoothConnectionListener listener) {
        if (!connectionListeners.contains(listener)) {
            connectionListeners.add(listener);
        }
    }

    // Remove listener
    public void removeConnectionListener(BluetoothConnectionListener listener) {
        connectionListeners.remove(listener);
    }

    // Notify listener
    public void notifyConnectionChanged(BluetoothHelper.ConnectionState connectionState, Disconnector disconnector) {
        for (BluetoothConnectionListener listener : connectionListeners) {
            listener.onConnectedDevicesChanged(getConnectedDevices(), connectionState, disconnector);  // Notify listeners and send them new Set
        }
    }

    // ***************************** ON BT FRAME RECEIVED ***************************************
    public interface BluetoothFrameListener {
        void onBTFrameReceived(BluetoothDevice device, byte[] frame);
    }

    public static void registerBluetoothFrameListener(BluetoothFrameListener listener) {
        if (!bluetoothFrameListeners.contains(listener)) {
            bluetoothFrameListeners.add(listener);
        }
    }

    public static void unregisterBluetoothFrameListener(BluetoothFrameListener listener) {
        bluetoothFrameListeners.remove(listener);
    }

    public void notifyBluetoothFrameReceived(BluetoothDevice device, byte[] frame) {
        for (BluetoothFrameListener listener : bluetoothFrameListeners) {
            listener.onBTFrameReceived(device, frame);
        }
    }

    // Permission request codes
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSIONS = 2;

    public BluetoothHelper(Context context) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
    }

    public String getLocalDeviceName() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                == PackageManager.PERMISSION_GRANTED) {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null && adapter.isEnabled()) {
                String deviceName = adapter.getName();
                return deviceName != null ? deviceName : "Unknown Device";
            }
        }
        return "None";
    }

    // Check if Bluetooth is supported and enabled
    public boolean isBluetoothSupported() {
        return bluetoothAdapter != null;
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter.isEnabled();
    }

    // Enable Bluetooth if not enabled
    public void enableBluetooth() {
        if (!bluetoothAdapter.isEnabled()) {
            if (!checkPermissions()) {
                requestConnectPermission();
                return;
            }

            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            try {
                enableBtIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(enableBtIntent);
            } catch (SecurityException e) {
                Toast.makeText(context, "Bluetooth enable request failed", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void requestConnectPermission() {
        // VER 2: No UI lags. Deny is the same to "DON'T ASK AGAIN". If then, ask to redirect to App Info
        if (!(context instanceof Activity)) {
            return;
        }

        Activity activity = (Activity) context;

        if (Build.VERSION.SDK_INT >= 31) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT)
                    != PackageManager.PERMISSION_GRANTED) {

                // Avoid repeat request if system will auto-block dialog (e.g. "Don't ask again")
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.BLUETOOTH_CONNECT)) {
                    Log.i(TAG, "Permission previously denied with 'Don't ask again'");

                    InfoDialog infoDialog = new InfoDialog(context, "OPEN_APP_INFO");
                    infoDialog.setInfoHint("Open App Info to set Bluetooth permissions");
                    infoDialog.showNumpadPopup((Activity) context);

                    return;
                }

                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{Manifest.permission.BLUETOOTH_CONNECT},
                        REQUEST_PERMISSIONS
                );
            }
        }
        else if (Build.VERSION.SDK_INT >= 24) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                // Avoid repeat request if system will auto-block dialog (e.g. "Don't ask again")
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Log.i(TAG, "Permission previously denied with 'Don't ask again'");

                    InfoDialog infoDialog = new InfoDialog(context, "OPEN_APP_INFO");
                    infoDialog.setInfoHint("Open App Info to set Bluetooth permissions");
                    infoDialog.showNumpadPopup((Activity) context);

                    return;
                }

                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSIONS
                );
            }
        }
    }

    private void requestScanPermission() {
        // VER 2: No UI lags. Deny is the same to "DON'T ASK AGAIN". If then, ask to redirect to App Info
        if (!(context instanceof Activity)) return;

        Activity activity = (Activity) context;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN)
                    != PackageManager.PERMISSION_GRANTED) {

                //if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.BLUETOOTH_SCAN)) {
                if (!checkPermissions()) {
                    Log.i(TAG, "Permission previously denied with 'Don't ask again'");

                    InfoDialog infoDialog = new InfoDialog(context, "OPEN_APP_INFO");
                    infoDialog.setInfoHint("Permissions missing\nOpen App Info to set Bluetooth permissions");
                    infoDialog.showNumpadPopup((Activity) context);

                    return;  // Skip to prevent UI issues
                }

                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_SCAN},
                        REQUEST_PERMISSIONS
                );
            }
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                    != PackageManager.PERMISSION_GRANTED) {

                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, Manifest.permission.ACCESS_FINE_LOCATION)) {
                    Log.i(TAG, "Permission previously denied with 'Don't ask again'");

                    InfoDialog infoDialog = new InfoDialog(context, "OPEN_APP_INFO");
                    infoDialog.setInfoHint("Permissions missing\nOpen App Info to set Bluetooth permissions");
                    infoDialog.showNumpadPopup((Activity) context);

                    return;
                }

                ActivityCompat.requestPermissions(
                        activity,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        REQUEST_PERMISSIONS
                );
            }
        }
    }

    public void openAppInfo(View view) {
        Snackbar.make(view, "Grant permissions in App Info", Snackbar.LENGTH_INDEFINITE)
                .setAction("Open", v -> {
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                    intent.setData(uri);
                    if (!(context instanceof Activity)) {
                        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    }
                    context.startActivity(intent);
                }).show();
    }   // NOT USED

    public void disableBluetooth() {
        if (bluetoothAdapter.isEnabled()) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }
            bluetoothAdapter.disable();
            Log.i(TAG, "Bluetooth disabled.");
        }
    }

    // Discover nearby Bluetooth devices
    public void startDiscovery() {
        // Check runtime permissions for discovery
        if (Build.VERSION.SDK_INT >= 31) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                // If permission is not granted, request permissions
                requestScanPermission();
                return; // Don't proceed with discovery until permission is granted
            }
        }
        else if (Build.VERSION.SDK_INT >= 24) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // If permission is not granted, request permissions
                requestScanPermission();
                return; // Don't proceed with discovery until permission is granted
            }
        }

        // Start Bluetooth device discovery
        if (!bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.startDiscovery();
            Log.i(TAG, "Discovery started...");
        }
    }

    // Stop discovery to save resources
    public void stopDiscovery() {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            // If permission is not granted, request permissions
            requestPermissions();
            return; // Don't proceed with discovery until permission is granted
        }
        if (bluetoothAdapter.isDiscovering()) {
            bluetoothAdapter.cancelDiscovery();
            Log.i(TAG, "Discovery stopped...");
        }
    }

    // Register a BroadcastReceiver to detect devices
    public void registerReceiver(BroadcastReceiver receiver) {
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        context.registerReceiver(receiver, filter);
    }

    // Unregister BroadcastReceiver
    public void unregisterReceiver(BroadcastReceiver receiver) {
        context.unregisterReceiver(receiver);
    }

    public Set<BluetoothDevice> getPairedDevices() {
        Set<BluetoothDevice> pairedDevices = new ArraySet<>();

        if (Build.VERSION.SDK_INT >= 31) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) {
                pairedDevices = bluetoothAdapter.getBondedDevices();
                return pairedDevices;
            }
        }
        else if (Build.VERSION.SDK_INT >= 24) {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                pairedDevices = bluetoothAdapter.getBondedDevices();
                return pairedDevices;
            }
        }

        return pairedDevices;
    }

    public void unpairDevice(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
        }

        // Unpair if paired
        if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
            try {
                Method m = device.getClass().getMethod("removeBond", (Class[]) null);
                m.invoke(device, (Object[]) null);
            } catch (Exception e) {
                Log.e(TAG, "Unpair device Exception: " + e.getMessage());
            }
        }
    }

    public void connect(BluetoothDevice device) {
        if (device == null) {
            Toast.makeText(context, "Invalid device selected", Toast.LENGTH_SHORT).show();
            return;
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
        }

//        showToast("Connecting to " + device.getName());
//        Toast.makeText(context, "Connecting to " + device.getName(), Toast.LENGTH_SHORT).show();

        // Try Classic Bluetooth RFCOMM first (currently not blocking to return isConnected properly)
        // Objective:
        // 1. Ensure each connection to any device use their own thread
        // 2, Ensure synchronous result (using Future)
        ExecutorService connectionExecutor = Executors.newSingleThreadExecutor();
        connectionExecutor.execute(() -> {

            ExecutorService singleConnectExecutor = Executors.newSingleThreadExecutor();
            Future<Boolean> future = singleConnectExecutor.submit(() -> {
                return connectRFCOMM(device);
            });

            try {
                boolean isConnected = future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);

                if (isConnected) {
                    // showToast("Connected to " + device.getName() + " through RFCOMM!");
                } else {
                    showToast("Connection to " + device.getName() + " failed");
                }
            } catch (Exception e) {
                Log.e(TAG, "connect() method failed with: " + e.getMessage());
            } finally {
                singleConnectExecutor.shutdownNow();
            }
        });
    }

    public Set<BluetoothDevice> getConnectedDevices() {
        Set<BluetoothDevice> connectedDevices = new HashSet<>();

        // Add all BLE-connected devices
        connectedDevices.addAll(gattMap.keySet());

        // Add all RFCOMM-connected devices (with additional state check)
        for (Map.Entry<BluetoothDevice, BluetoothSocket> entry : socketMap.entrySet()) {
            if (entry.getValue().isConnected()) {  // Check if the socket is still connected
                connectedDevices.add(entry.getKey());
            }
        }

        return connectedDevices;
    }

    public void disconnect(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
        }

        if (device == null) return;

        stopBTGetRegularData();

        try {
            if (socketMap.containsKey(device)) {
                BluetoothSocket socket = socketMap.get(device);
                socket.getInputStream().close();
                socket.getOutputStream().close();
                socket.close();                     // Close both input & output first --> instant socket.close()
                Log.i(TAG, "Called close socket");
                socketMap.remove(device);
                stopReceiveFrame(device);           // Free corresponding receiving thread
                notifyConnectionChanged(ConnectionState.DISCONNECTED, Disconnector.SELF);   // Notify
                showToast("Disconnected from " + device.getName());
                return;
            }

//            if (gattMap.containsKey(device)) {
//                gattMap.get(device).disconnect();
//                gattMap.get(device).close();
//                gattMap.remove(device);
//                notifyConnectionChanged();  // Notify
//                System.out.println("Disconnected through BLE.");
//                Toast.makeText(context, "Disconnected from " + device.getName(), Toast.LENGTH_SHORT).show();
//                return;
//            }

            // If the device is bonded but not actively connected, force unpair
//            if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
//                Method removeBondMethod = device.getClass().getMethod("removeBond");
//                boolean result = (Boolean) removeBondMethod.invoke(device);
//                Log.i(TAG, result ? "Successfully removed bond" : "Failed to remove bond");
//            }

        } catch (Exception e) {
            Toast.makeText(context, "Error disconnecting", Toast.LENGTH_SHORT).show();
        }

        // Re-check
        if (!socketMap.containsKey(device) || !gattMap.containsKey(device)) {
            Toast.makeText(context, "Disconnected from " + device.getName(), Toast.LENGTH_SHORT).show();
        }
    }

    private void sendFrame(BluetoothDevice device, byte[] frame) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
        }

        if (device == null) {
            return;
        }

        if (btSendExecutor == null || btSendExecutor.isShutdown()) {
            btSendExecutor = Executors.newSingleThreadExecutor();
        }

        btSendExecutor.submit(() -> {
            sendFrameRFCOMM(device, (frame));    // Send with CRC-16 bit Little-Endian
        });
        // sendFrameRFCOMM(device, (frame));
    }

    public void startReceiveFrame(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
        }

        if (!socketMap.containsKey(device)) {
            Log.e(TAG, "Cannot receive: " + device.getName() + " not connected");
            return;
        }

        BluetoothSocket socket = socketMap.get(device);
        if (socket == null || !socket.isConnected()) {
            Log.e(TAG, "Socket to " + device.getName() + " NULL");
            return;
        }

        if (frameReceiverExecutors.containsKey(device)) {
            return; // Already assigned device a thread for receiving
        }

        ExecutorService executor = Executors.newSingleThreadExecutor();
        frameReceiverExecutors.put(device, executor);
        isReceivingFrames.put(device, true);

        executor.execute(() -> {
            final byte DLE = 0x10;
            final byte STX = 0x02;
            final byte ETX = 0x03;

            final int MAX_DATA_SIZE = 506;
            final int FRAME_OVERHEAD = 2 + 2 + 2; // DLE + STX + CMD (2 bytes) + DLE + ETX
            final int MAX_FRAME_SIZE = MAX_DATA_SIZE + FRAME_OVERHEAD;

            boolean inFrame = false;
            boolean dleDetected = false; // Tracks a DLE byte for start/end detection

            try {
                InputStream inputStream = socket.getInputStream();  // Get new socket every time because multiple devices
                byte[] buffer = new byte[1024];
                int bytesRead;

                Log.i(TAG, "Started receiving frames from " + device.getName());

                while (Boolean.TRUE.equals(isReceivingFrames.get(device)) && !Thread.currentThread().isInterrupted()) {
                    bytesRead = inputStream.read(buffer);  // Blocks until data is available
                    if (bytesRead == -1) break;  // Connection closed

                    for (int i = 0; i < bytesRead; i++) {
                        byte b = buffer[i];

                        // Detect DLE byte
                        if (b == DLE) {
                            dleDetected = true;
                            frameBuffer.write(b);
                            continue;
                        }

                        if (dleDetected) {
                            // If DLE was detected, check for STX or ETX
                            if (!inFrame && b == STX) {
                                inFrame = true;
                                frameBuffer.write(b);
                            } else if (inFrame && b == ETX) {
                                // End of frame detected
                                frameBuffer.write(b);
                                byte[] completeFrame = frameBuffer.toByteArray();
                                frameBuffer.reset();

                                Log.i(TAG, "Frame received: " + bytesToHex(completeFrame));
                                notifyBluetoothFrameReceived(device, completeFrame);

                                inFrame = false;
                            } else {
                                frameBuffer.write(b);
                            }
                            dleDetected = false;
                            continue;
                        }

                        // Write to buffer if inside a frame
                        if (inFrame) {
                            frameBuffer.write(b);
                            // Prevent buffer overflow
                            if (frameBuffer.size() > MAX_FRAME_SIZE) {
                                Log.e(TAG, "Frame exceeded maximum size, discarding.");
                                frameBuffer.reset();
                                inFrame = false;
                            }
                        }
                    }
                }
            } catch (IOException e) {
                if (Boolean.TRUE.equals(isReceivingFrames.get(device))) {  // Only log if not stopped intentionally
                    Log.e(TAG, "Error receiving frames: " + e.getMessage());
                }
            } finally {   // Handle unexpected closed connection (bytesRead == -1)
                isReceivingFrames.remove(device);
                if (socketMap.containsKey(device)) {
                    BluetoothSocket disconnectedSocket = socketMap.get(device);
                    try {
                        if (disconnectedSocket != null) {
                            disconnectedSocket.getInputStream().close();
                            disconnectedSocket.getOutputStream().close();
                            disconnectedSocket.close();                     // Close both input & output first --> instant socket.close()
                        }
                    } catch (IOException e) {
                        Log.i(TAG, "Error closing resources (unexpected disconnection): " + e.getMessage());
                    }

                    socketMap.remove(device);
                    stopReceiveFrame(device);           // Free corresponding receiving thread
                    notifyConnectionChanged(ConnectionState.DISCONNECTED, Disconnector.PARTNER);   // Notify
                    Log.i(TAG, "Notify partner disconnected");
                }
                Log.i(TAG, "Frame receiving stopped for " + device.getName());
                showToast(device.getName() + " disconnected");
            }
        });
    }

    public void stopReceiveFrame(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
        }

        if (!frameReceiverExecutors.containsKey(device)) {
            Log.w(TAG, "No receiving thread found for device: " + device.getName());
            return;
        }

        isReceivingFrames.put(device, false);

        ExecutorService executor = frameReceiverExecutors.remove(device);   // Returns the value assigned with <key>
        if (executor != null) {
            executor.shutdownNow();
        }

        isReceivingFrames.remove(device);
        Log.i(TAG, "Stopped receiving frame for " + device.getName());
    }

    public void stopAllReceiveFrames() {
        for (BluetoothDevice device : frameReceiverExecutors.keySet()) {
            stopReceiveFrame(device);
        }
        Log.i(TAG, "Shut down all receiving frames...");
    }

    // ************************************ CLASSIC BLUETOOTH (RFCOMM) **************************************
    private boolean connectRFCOMM(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
        }
        ExecutorService executor = Executors.newSingleThreadExecutor();

        Future<Boolean> future = executor.submit(() -> {
            // Connect with retries and timeout
            for (int attempt = 1; attempt <= MAX_RETRIES; attempt++) {
                Log.i(TAG, "RFCOMM attempt: " + attempt);

//                if (connectRFCOMMStandard(device, TIMEOUT_MS)) {
//                    showToast("Connected to " + device.getName());
//                    Log.i(TAG, "Connected to " + device.getName());
//                    startReceiveFrame(device);
//                    return true;
//                }

                if (connectRFCOMMReflection(device, TIMEOUT_MS)) {
                    showToast("Connected to " + device.getName());
                    Log.i(TAG, "Connected to " + device.getName());
                    startReceiveFrame(device);
                    startBTGetRegularData();
                    return true;
                }

                Log.i(TAG, "Both RFCOMM methods failed on attempt: " + attempt);
            }

            return false;
        });

        try {
            return future.get(TIMEOUT_MS, TimeUnit.MILLISECONDS);
        } catch (TimeoutException e) {
            Log.e(TAG, "RFCOMM connection timed out: " + e.getMessage());
        } catch (Exception e) {
            Log.e(TAG, "Error future waiting for RFCOMM connection: " + e.getMessage());
        } finally {
            executor.shutdown();
        }

        return false;
    }

    private boolean connectRFCOMMStandard(BluetoothDevice device, int timeout) {
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions();
            }

            // Check if socket already exist
            if (socketMap.containsKey(device) && Objects.requireNonNull(socketMap.get(device)).isConnected()) {
                return true;
            }

            BluetoothSocket socket = device.createRfcommSocketToServiceRecord(RFCOMM_UUID);
            if (bluetoothAdapter.isDiscovering()) {
                stopDiscovery();
            }

            try {
                socket.connect();
            } catch (IOException e) {
                unpairDevice(device);
                socket = device.createRfcommSocketToServiceRecord(RFCOMM_UUID);
                socket.connect();   // Reconnect after unpairing
            }

            if (socket.isConnected()) {
                socketMap.put(device, socket);  // Add connection to collection
                notifyConnectionChanged(ConnectionState.CONNECTED, Disconnector.NONE);
                return true;
            }

            socket.close();
        } catch (Exception e) {
            Log.e(TAG, "Error in Standard RFCOMM connection: " + e.getMessage());
        }

        return false;
    }

    private boolean connectRFCOMMReflection(BluetoothDevice device, int timeout) {
        try {
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions();
            }

            // Check if socket already exist
            if (socketMap.containsKey(device) && Objects.requireNonNull(socketMap.get(device)).isConnected()) {
                return true;
            }

            Method method = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
            BluetoothSocket socket = (BluetoothSocket) method.invoke(device, 1);
            if (bluetoothAdapter.isDiscovering()) {
                stopDiscovery();
            }

            try {
                socket.connect();
            } catch (IOException e) {
                // unpairDevice(device); // Might cause force unpair for reconnection attempts
                method = device.getClass().getMethod("createRfcommSocket", new Class[]{int.class});
                socket = (BluetoothSocket) method.invoke(device, 1);
                socket.connect();    // Retry after unpairing
            }

            if (socket.isConnected()) {
                socketMap.put(device, socket);  // Add connection to collection
                notifyConnectionChanged(ConnectionState.CONNECTED, Disconnector.NONE);
                return true;
            }

            socket.close();
        } catch (Exception e) {
            Log.e(TAG, "Error in Reflection RFCOMM connection: " + e.getMessage());
        }

        return false;
    }

    // Send frame over RFCOMM
    private boolean sendFrameRFCOMM(BluetoothDevice device, byte[] frame) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
        }

        if (!socketMap.containsKey(device)) {
            Log.e(TAG, "Device not connected: " + device.getName());
            return false;
        }

        BluetoothSocket socket = socketMap.get(device);
        if (socket == null || !socket.isConnected()) {
            Log.e(TAG, "Lost connection to: " + device.getName());
            showToast("Bluetooth connection lost. Trying to reconnect...");

            // Attempt reconnection
            if (!connectRFCOMM(device)) {
                Log.e(TAG, "Reconnection failed for: " + device.getName());
                showToast("Reconnection failed.");
                return false;
            }

            // Update socket after reconnection
            socket = socketMap.get(device);
            if (socket == null || !socket.isConnected()) {
                Log.e(TAG, "Socket still unavailable after reconnecting.");
                showToast("Failed to establish a connection.");
                return false;
            }
        }

        try {
            OutputStream outputStream = socket.getOutputStream();

            long startTime = System.nanoTime();

            outputStream.write(frame);
            outputStream.flush();

            long endTime = System.nanoTime();

            double timeTakenSeconds = (endTime - startTime) / 1e9;
            double baudRate = frame.length / timeTakenSeconds;

            Log.i(TAG, "\nFrame sent successfully to " + device.getName()
                    + ": " + Arrays.toString(frame)
                    + " with baud rate " + baudRate + " bps");

            // Record sendTime to static variable
            // CAUTION: Must add frameId to match with receive frameId for accurate latency calculation
            btSendTimes.add(System.currentTimeMillis());
            btSentCounter.getAndIncrement();

            return true;
        } catch (IOException e) {
            Log.e(TAG, "Error sending frame: " + e.getMessage());
            showToast("Error sending frame: " + e.getMessage());
            return false;
        }
    }

    private void startBTGetRegularData() {
        if (globalSelectedBTDevice == null) {
            showToast("Bluetooth server not found");
            return;
        }

        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                // Your repetitive code here
                PriorityCommunication.ChannelType channelType = PriorityCommunication.checkAvailableChannel();
                if (channelType == PriorityCommunication.ChannelType.Bluetooth) {
                    ConnectionFragment.bluetoothHelper.getRegularData(globalSelectedBTDevice);
                }

                // Schedule the next execution
                handler.postDelayed(this, 200);
            }
        }, 200);
    }

    private void stopBTGetRegularData() {
        handler.removeCallbacksAndMessages(null);
    }

    private void showToast(String message) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        );
    }

    // ******************************************************************************************************


    // ************************************ BLUETOOTH LOW ENERGY (BLE) **************************************
    private boolean connectBLE(BluetoothDevice device) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            requestPermissions();
        }

        BluetoothGatt gatt = device.connectGatt(context, false, new BluetoothGattCallback() {
            @Override
            public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
                if (newState == BluetoothProfile.STATE_CONNECTED) {
                    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    gatt.discoverServices();    // Scan for BLE services
                    Toast.makeText(context, "Scanning for services...", Toast.LENGTH_SHORT).show();
                } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                    Log.d("Bluetooth", "Disconnected via BLE.");
                    gattMap.remove(device);
                    notifyConnectionChanged(ConnectionState.DISCONNECTED, Disconnector.PARTNER);
                }
            }

            @Override
            public void onServicesDiscovered(BluetoothGatt gatt, int status) {
                if (status == BluetoothGatt.GATT_SUCCESS) {
                    for (BluetoothGattService service : gatt.getServices()) {
                        System.out.println("BLE Service UUID: " + service.getUuid().toString());
                        for (BluetoothGattCharacteristic characteristic : service.getCharacteristics()) {
                            System.out.println(" Characteristic UUID: " + characteristic.getUuid().toString());
                        }
                    }
                }
            }
        });

        if (gatt != null) {
            gattMap.put(device, gatt);  // Add connection to collection
            notifyConnectionChanged(ConnectionState.CONNECTED, Disconnector.NONE);
            return true;
        }

        return false;
    }

    // Send frame over BLE
    private boolean sendFrameBLE(BluetoothDevice device, byte[] frame) {
        // Get device
        BluetoothGatt gatt = gattMap.get(device);
        if (gatt == null) {
            return false;
        }

        // Get service
        BluetoothGattService service = gatt.getService(BLE_SERVICE_UUID);  // service UUID
        if (service == null) {
            return false;
        }

        // Get characteristic
        BluetoothGattCharacteristic characteristic = service.getCharacteristic(BLE_CHARACTERISTIC_UUID);  // characteristic UUID
        if (characteristic == null) {
            return false;
        }

        characteristic.setValue(frame); // Set frame
        characteristic.setWriteType(BluetoothGattCharacteristic.WRITE_TYPE_DEFAULT);

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions();
        }

        boolean success = gatt.writeCharacteristic(characteristic); // Send frame
        if (success) {
            System.out.println("Frame sent (over BLE) is: " + Arrays.toString(frame));
        }

        return success;
    }

    // ******************************************************************************************************


    // ************************************* ROBOT OPERATION PROCESSING ********************************** //
    public void getRegularData(BluetoothDevice device) {
        baos.reset();
        byte[] regDataFrame = ProtocolSend.operate(ProtocolSend.Operation.GET_REG_DATA, baos);
        sendFrame(device, regDataFrame);
    }

    public void rebootFirst(BluetoothDevice device) {
        baos.reset();
        byte[] rebootFirstFrame = ProtocolSend.operate(ProtocolSend.Operation.REBOOT_1ST_SIG, baos);
        sendFrame(device, rebootFirstFrame);
    }

    public void resetErrors(BluetoothDevice device) {
        baos.reset();
        byte[] resetErrorFrame = ProtocolSend.operate(ProtocolSend.Operation.RESET_ERROR, baos);
        sendFrame(device, resetErrorFrame);
    }

    public void compileProgram(BluetoothDevice device) {
        baos.reset();
        byte[] regDataFrame = ProtocolSend.operate(ProtocolSend.Operation.COMPILE, baos);
        sendFrame(device, regDataFrame);
    }

    public void checkCompileResult(BluetoothDevice device) {
        baos.reset();
        byte[] regDataFrame = ProtocolSend.operate(ProtocolSend.Operation.CHECK_COMPILE_RESULT, baos);
        sendFrame(device, regDataFrame);
    }

    public void runRobotProgram(BluetoothDevice device) {
        baos.reset();
        byte[] regDataFrame = ProtocolSend.operate(ProtocolSend.Operation.RUN_PROG, baos, 10, "", "", "");
        sendFrame(device, regDataFrame);
    }

    public void stopRobotProgram(BluetoothDevice device) {
        baos.reset();
        byte[] stopRobotProgramFrame = ProtocolSend.operate(ProtocolSend.Operation.STOP_PROG, baos);
        sendFrame(device, stopRobotProgramFrame);
    }

    // SET SERVO 1:ON / 2:OFF
    public void setServo(BluetoothDevice device, int axes, int state) {
        baos.reset();
        byte[] setServoFrame = ProtocolSend.operate(ProtocolSend.Operation.SET_SERVO, baos, axes, state);
        sendFrame(device, setServoFrame);
    }

    // SET AUTO/MANUAL MODE: 0x01 = MAN, 0x02 = AUTO
    public void setMode(BluetoothDevice device, int mode) {
        baos.reset();
        byte[] setModeFrame = ProtocolSend.operate(ProtocolSend.Operation.SET_CONTROL_MODE, baos, mode);
        sendFrame(device, setModeFrame);
    }

    // MANUAL JOG
    public void jog(BluetoothDevice device, int coordinate, int axis, int buttonState, int direction, float velocity) {
        baos.reset();
        byte[] jogFrame = ProtocolSend.operate(ProtocolSend.Operation.JOG_MANUAL, baos, coordinate, axis, buttonState, direction, velocity);
        sendFrame(device, jogFrame);
    }

    // REBOOT CTRL. 2ND SIGNAL (CONFIRMATION)
    public void rebootSecond(BluetoothDevice device, char confirm) {
        baos.reset();
        byte[] rebootSecondFrame = ProtocolSend.operate(ProtocolSend.Operation.REBOOT_2ND_SIG, baos, confirm);
        sendFrame(device, rebootSecondFrame);
    }

    // MOVE TO HOME
    public void moveToHome(BluetoothDevice device, int homePosIndex, float speedLevel) {
        baos.reset();
        byte[] moveToHomeFrame = ProtocolSend.operate(ProtocolSend.Operation.MOVE_HOME, baos, homePosIndex, speedLevel);
        sendFrame(device, moveToHomeFrame);
    }

    // MOVE TO POSITION
    public void moveToPosition(BluetoothDevice device, int coordinateState, float speedLevel, String[] position) {
        baos.reset();
        byte[] moveToPositionFrame = ProtocolSend.operate(ProtocolSend.Operation.MOVE_TO_POSITION, baos, coordinateState, speedLevel, position);
        sendFrame(device, moveToPositionFrame);
    }

    // SEND PROGRAM FILE PART
    public void sendProgramPart(BluetoothDevice device, int partIndex, String programName, Context context) {
        baos.reset();
        byte[] sendProgramPartFrame = ProtocolSend.operate(ProtocolSend.Operation.SEND_PROG_PART, baos, partIndex, programName, context);
        sendFrame(device, sendProgramPartFrame);
    }

    // SEND PROGRAM END PART
    public void sendProgramEndPart(BluetoothDevice device, int endPartIndex, String programName, Context context, String miscellaneous) {
        baos.reset();
        byte[] sendProgramPartFrame = ProtocolSend.operate(ProtocolSend.Operation.SEND_PROG_END_PART, baos, endPartIndex, programName, context, miscellaneous);
        sendFrame(device, sendProgramPartFrame);
    }

    // SEND CONFIG FILE PART
    public void sendConfigPart(BluetoothDevice device, int partIndex, String configFileName, Context context, boolean isCustom) {
        baos.reset();
        byte[] sendConfigPartFrame = ProtocolSend.operate(ProtocolSend.Operation.SEND_PROG_PART, baos, partIndex, configFileName, context, isCustom);
        sendFrame(device, sendConfigPartFrame);
    }

    // SEND CONFIG END PART
    public void sendConfigEndPart(BluetoothDevice device, int endPartIndex, String configFileName, Context context, boolean isCustom, String miscellaneous) {
        baos.reset();
        byte[] sendConfigEndPartFrame = ProtocolSend.operate(ProtocolSend.Operation.SEND_PROG_END_PART, baos, endPartIndex, configFileName, context, isCustom, miscellaneous);
        sendFrame(device, sendConfigEndPartFrame);
    }

    // SAVE AXES ZERO VALUE
    public void saveAxesZeroValue(BluetoothDevice device, int axisQuantity, int[] zeroValues) {
        baos.reset();
        byte[] saveAxesZeroValueFrame = ProtocolSend.operate(ProtocolSend.Operation.SAVE_AXES_ZERO_VALUE, baos, axisQuantity, zeroValues);
        sendFrame(device, saveAxesZeroValueFrame);
    }

    // SAVE ROBOT HOME VALUE
    public void saveRobotHomeValue(BluetoothDevice device, int numAxis, String[] home1Position, String[] home2Position) {
        baos.reset();
        byte[] saveRobotHomeValueFrame = ProtocolSend.operate(ProtocolSend.Operation.SAVE_HOME, baos, numAxis, home1Position, home2Position);
        sendFrame(device, saveRobotHomeValueFrame);
    }

    // SET OUTPUT VALUE
    public void setOutputValue(BluetoothDevice device, int group, int value) {
        baos.reset();
        byte[] sendProgramPartFrame = ProtocolSend.operate(ProtocolSend.Operation.SET_OUTPUT_VALUE, baos, group, value, "");
        sendFrame(device, sendProgramPartFrame);
    }

    // Add DLE escaping in DATA section
    public static byte[] refineSendFrame(byte[] frame) {
        List<Byte> refinedFrameList = new ArrayList<>();

        refinedFrameList.add(STX);

        // Start from second byte and end at (last - 1) byte
        for (int i = 1; i < frame.length - 1; i++) {
            if (frame[i] == STX || frame[i] == ETX) {
                refinedFrameList.add(DLE);
            }
            refinedFrameList.add(frame[i]);
        }

        refinedFrameList.add(ETX);

        byte[] refinedFrame = new byte[refinedFrameList.size()];

        for (int i = 0; i < refinedFrameList.size(); i++) {
            refinedFrame[i] = refinedFrameList.get(i);
        }

        Log.i(TAG, "Refined frame is: " + Arrays.toString(refinedFrame));
        return refinedFrame;
    }

    // Add CRC-16-CCITT (XMODEM)
    public static byte[] appendCRC16(byte[] frame, boolean littleEndian) {
        int stxIndex = 0;
        int cmdIndex = 1;  // CMD starts after STX
        int etxIndex = frame.length - 1; // ETX is at the last position

        // Extract CMD + DATA for CRC calculation
        byte[] cmdData = new byte[etxIndex - cmdIndex];
        System.arraycopy(frame, cmdIndex, cmdData, 0, cmdData.length);

        // Compute CRC
        int crc = computeCRC16CCITT(cmdData);

        // Prepare new frame with space for CRC
        byte[] result = new byte[frame.length + 2];

        // Copy original frame up to ETX
        System.arraycopy(frame, 0, result, 0, etxIndex);

        // Insert CRC before ETX
        if (littleEndian) {
            result[etxIndex] = (byte) (crc & 0xFF);  // Low byte first (Little-endian)
            result[etxIndex + 1] = (byte) (crc >> 8); // High byte second
        } else {
            result[etxIndex] = (byte) (crc >> 8);  // High byte first (Big-endian, default)
            result[etxIndex + 1] = (byte) (crc & 0xFF); // Low byte second
        }

        // Append ETX at the end
        result[etxIndex + 2] = frame[etxIndex];

        return result;
    }

    private static int computeCRC16CCITT(byte[] data) {
        int crc = CRC_INITIAL;
        for (byte b : data) {
            crc ^= (b << 8);
            for (int i = 0; i < 8; i++) {
                if ((crc & 0x8000) != 0) {
                    crc = (crc << 1) ^ CRC_POLYNOMIAL;
                } else {
                    crc <<= 1;
                }
            }
        }
        return crc & 0xFFFF; // Return 16-bit CRC
    }

    // Check permissions for Bluetooth scanning
    private boolean checkPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {   // API >= 31. Only BLUETOOTH_SCAN and BLUETOOTH_CONNECT
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_SCAN) == PackageManager.PERMISSION_GRANTED
                    && ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED;
                    // && ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { // 23 <= API < 31. Only ACCESS_FINE_LOCATION
            return ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
        }

        return true;
    }

    // Request the necessary permissions
    private void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {   // API >= 31
            ActivityCompat.requestPermissions((HomeScreenActivity) context, new String[]{
                    Manifest.permission.BLUETOOTH_SCAN,
                    Manifest.permission.BLUETOOTH_CONNECT
            }, REQUEST_PERMISSIONS);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {    // 23 <= API < 31
            ActivityCompat.requestPermissions((HomeScreenActivity) context, new String[]{
                    Manifest.permission.ACCESS_FINE_LOCATION
            }, REQUEST_PERMISSIONS);
        }
    }
}


