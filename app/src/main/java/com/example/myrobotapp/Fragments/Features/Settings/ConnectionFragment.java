package com.example.myrobotapp.Fragments.Features.Settings;

import static com.example.myrobotapp.Class.Bluetooth.BluetoothHelper.btSendTimes;
import static com.example.myrobotapp.Class.Bluetooth.BluetoothHelper.btSentCounter;
import static com.example.myrobotapp.Class.ProtocolReceive.btFrameIdCounter;
import static com.example.myrobotapp.Class.ProtocolReceive.tcpFrameIdCounter;
import static com.example.myrobotapp.Class.TCP.TCPClient.tcpSendTimes;
import static com.example.myrobotapp.Class.TCP.TCPClient.tcpSentCounter;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.os.Looper;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.myrobotapp.Class.Bluetooth.BluetoothConnectionListener;
import com.example.myrobotapp.Class.Bluetooth.BluetoothDeviceAdapter;
import com.example.myrobotapp.Class.Bluetooth.BluetoothHelper;
import com.example.myrobotapp.Class.Bluetooth.BluetoothConnectedDeviceAdapter;
import com.example.myrobotapp.Class.Bluetooth.BluetoothPairedDeviceAdapter;
import com.example.myrobotapp.Class.Communication.FrameTracker;
import com.example.myrobotapp.Class.Communication.PriorityCommunication;
import com.example.myrobotapp.Class.Dialog.InfoDialog;
import com.example.myrobotapp.Class.GlobalData;
import com.example.myrobotapp.Class.ProtocolReceive;
import com.example.myrobotapp.Class.TCP.TCPClient;
import com.example.myrobotapp.Class.TCP.TCPConnectedDeviceAdapter;
import com.example.myrobotapp.Class.UserInput.NumpadPopup;
import com.example.myrobotapp.Fragments.Features.HistoryFragment;
import com.example.myrobotapp.R;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConnectionFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConnectionFragment extends Fragment implements BluetoothConnectionListener, TCPClient.TCPConnectionListener {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public ConnectionFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ConnectionFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ConnectionFragment newInstance(String param1, String param2) {
        ConnectionFragment fragment = new ConnectionFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    public static final String TAG = "ConnectionFragment";
    private static final int REQUEST_ENABLE_BT = 1;
    private static final int REQUEST_PERMISSIONS = 2;

    // TCP Socket
    Button connectButton;
    Button disconnectButton;
    public EditText editText_IPAddress;
    public EditText editText_Port;
    TextView TextView_status;
    TextView TextView_activeConnections;
    public static String activeConnectionKey = "";
    public static final int MAX_TCP_RETRIES = 4;

    // Executor service to handle multiple connections concurrently
    private ExecutorService executorService;

    // A map to keep track of active connections (IP + port -> TCPClient)
    public static ConcurrentHashMap<String, TCPClient> activeConnections = new ConcurrentHashMap<>();
    RecyclerView tcpConnectedDevicesRecyclerView;
    TCPConnectedDeviceAdapter tcpConnectedDeviceAdapter;
    String selectedConnectedTCPDevice;
    List<String> connectedTCPDevicesList = new ArrayList<>();
    Runnable tcpGetRegularDataTask;

    // Handler for repetitively requesting REGULAR DATA
    Handler handler = new Handler();
    private final int delay = 400;
    public static FrameTracker tcpFrameTracker = new FrameTracker(FrameTracker.ChannelType.TCP);
    Runnable frameLoggingTask;

    // Bluetooth
    public static short rssi; // Received Signal Strength Indicator
    private final List<BluetoothDevice> discoveredBTDevices = new ArrayList<>();
    private final BroadcastReceiver bluetoothReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action == null) return;

            if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

                // UPDATE LIST OF DISCOVERED BT DEVICES
                String deviceName;
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    deviceName = (device != null && ActivityCompat.checkSelfPermission(context, Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED) ? device.getName() : "Permission Required for API > 31 in fragment";
                } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    deviceName = (device != null && device.getName() != null) ? device.getName() : "Unnamed Device";
                }

                String deviceAddress = (device != null) ? device.getAddress() : "Unknown Address";

                if (!discoveredBTDevices.contains(device)) {
                    discoveredBTDevices.add(device);
                    bluetoothDeviceAdapter.notifyDataSetChanged();
                    // Toast.makeText(context, "Found: " + deviceName + " (" + deviceAddress + ")", Toast.LENGTH_SHORT).show();
                }

                // UPDATE RSSI (Received Signal Strength Indicator)
                /*
                    >-60 dBm: Great

                    -70 dBm: Okay

                    <-80 dBm: Weak
                 */

                rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE);
            }
            if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                int bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.BOND_NONE);

                if (bondState == BluetoothDevice.BOND_BONDED) {
                    bluetoothHelper.connect(device);  // Reattempt connection after pairing
                }
            } else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
                Toast.makeText(context, "Scanning complete", Toast.LENGTH_SHORT).show();
            }
        }
    };

    public static BluetoothHelper bluetoothHelper;
    SwitchCompat bluetoothPowerSwitch, bluetoothCommunicationSwitch;
    Button bluetoothConnectButton, bluetoothDisconnectButton, bluetoothScanButton, bluetoothUnpairButton;
    EditText localDeviceNameEditText;
    RecyclerView nearbyDevicesRecyclerView;
    boolean scanPressed;
    BluetoothDeviceAdapter bluetoothDeviceAdapter;
    BluetoothPairedDeviceAdapter bluetoothPairedDeviceAdapter;
    RecyclerView connectedDevicesRecyclerView, pairedDevicesRecyclerView;
    BluetoothConnectedDeviceAdapter bluetoothConnectedDeviceAdapter;

    private BluetoothDevice selectedNearbyBTDevice = null;
    private BluetoothDevice selectedConnectedBTDevice = null;
    private BluetoothDevice selectedPairedBTDevice = null;
    Set<BluetoothDevice> connectedDevicesSet = new ArraySet<>();
    List<BluetoothDevice> connectedDevicesList = new ArrayList<>();
    Set<BluetoothDevice> pairedDevicesSet = new ArraySet<>();
    List<BluetoothDevice> pairedDevicesList = new ArrayList<>();
    private ImageView rotateIcon;
    private ObjectAnimator rotateAnimator;
    public static FrameTracker btFrameTracker = new FrameTracker(FrameTracker.ChannelType.Bluetooth);

    public static BluetoothDevice globalSelectedBTDevice = null;

    // BLUETOOTH PERMISSION HANDLER
    public static ActivityResultLauncher<String[]> permissionLauncher;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_connection, container, false);

        // Initialize connection components
        // TCP Socket
        connectButton = view.findViewById(R.id.connectButton);
        disconnectButton = view.findViewById(R.id.disconnectButton);
        editText_IPAddress = view.findViewById(R.id.IP_Address);
        editText_IPAddress.setShowSoftInputOnFocus(false);
        editText_IPAddress.setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(requireContext(), editText_IPAddress, 0);
            numpadPopup.showNumpadPopup(this.requireActivity());
        });
        editText_Port = view.findViewById(R.id.Port);
        editText_Port.setShowSoftInputOnFocus(false);
        editText_Port.setOnClickListener(v -> {
            NumpadPopup numpadPopup = new NumpadPopup(requireContext(), editText_Port, 0);
            numpadPopup.showNumpadPopup(this.requireActivity());
        });
        TextView_status = view.findViewById(R.id.status);

        // TCP Connected Device Recycler View population
        tcpConnectedDevicesRecyclerView = view.findViewById(R.id.tcpConnectedDevicesRecyclerView);
        tcpConnectedDevicesRecyclerView.setNestedScrollingEnabled(false);
        tcpConnectedDevicesRecyclerView.setLayoutManager(new LinearLayoutManager(this.requireContext()));
        tcpConnectedDeviceAdapter = new TCPConnectedDeviceAdapter(this.requireContext(), connectedTCPDevicesList, device -> {
            selectedConnectedTCPDevice = device; // Store selected device
        });
        tcpConnectedDevicesRecyclerView.setAdapter(tcpConnectedDeviceAdapter);

        monitorConnection();
        TCPClient.registerTCPFrameListener(new ProtocolReceive());

        // Bluetooth
        bluetoothPowerSwitch = view.findViewById(R.id.bluetoothPowerSwitch);
        bluetoothCommunicationSwitch = view.findViewById(R.id.bluetoothCommunicationSwitch);
        bluetoothConnectButton = view.findViewById(R.id.bluetoothConnectButton);
        bluetoothConnectButton = view.findViewById(R.id.bluetoothConnectButton);
        bluetoothDisconnectButton = view.findViewById(R.id.bluetoothDisconnectButton);
        bluetoothScanButton = view.findViewById(R.id.bluetoothScanButton);
        bluetoothUnpairButton = view.findViewById(R.id.bluetoothUnpairButton);
        bluetoothUnpairButton.setOnClickListener(v -> {
            // Test send
            if (bluetoothHelper != null && globalSelectedBTDevice != null) {
                // May add runtime permission request for 24 <= API < 31 here

                bluetoothHelper.unpairDevice(globalSelectedBTDevice);
            }
        });

        // Nearby Device Recycler View population
        nearbyDevicesRecyclerView = view.findViewById(R.id.nearbyDevicesRecyclerView);
        nearbyDevicesRecyclerView.setNestedScrollingEnabled(false);
        nearbyDevicesRecyclerView.setLayoutManager(new LinearLayoutManager(this.requireContext()));
        bluetoothDeviceAdapter = new BluetoothDeviceAdapter(this.requireContext(), discoveredBTDevices, device -> {
            globalSelectedBTDevice = device; // Store selected device

            if (bluetoothDeviceAdapter != null) {
                bluetoothDeviceAdapter.notifyDataSetChanged();
                bluetoothConnectedDeviceAdapter.notifyDataSetChanged();
                bluetoothPairedDeviceAdapter.notifyDataSetChanged();
            }
        });
        nearbyDevicesRecyclerView.setAdapter(bluetoothDeviceAdapter);

        localDeviceNameEditText = view.findViewById(R.id.localDeviceNameEditText);

        // Connected Device Recycler View population
        connectedDevicesRecyclerView = view.findViewById(R.id.connectedDevicesRecyclerView);
        connectedDevicesRecyclerView.setNestedScrollingEnabled(false);
        connectedDevicesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        bluetoothConnectedDeviceAdapter = new BluetoothConnectedDeviceAdapter(this.requireContext(), connectedDevicesList, connectedDevice -> {
            globalSelectedBTDevice = connectedDevice;    // Store selected device

            if (bluetoothConnectedDeviceAdapter != null) {
                bluetoothDeviceAdapter.notifyDataSetChanged();
                bluetoothConnectedDeviceAdapter.notifyDataSetChanged();
                bluetoothPairedDeviceAdapter.notifyDataSetChanged();
            }
        });
        connectedDevicesRecyclerView.setAdapter(bluetoothConnectedDeviceAdapter);

        // Paired Device Recycler View population
        pairedDevicesRecyclerView = view.findViewById(R.id.pairedDevicesRecyclerView);
        pairedDevicesRecyclerView.setNestedScrollingEnabled(false);
        pairedDevicesRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));

        bluetoothPairedDeviceAdapter = new BluetoothPairedDeviceAdapter(this.requireContext(), pairedDevicesList, pairedDevice -> {
            globalSelectedBTDevice = pairedDevice;    // Store selected device

            if (bluetoothPairedDeviceAdapter != null) {
                bluetoothDeviceAdapter.notifyDataSetChanged();
                bluetoothConnectedDeviceAdapter.notifyDataSetChanged();
                bluetoothPairedDeviceAdapter.notifyDataSetChanged();
            }
        });
        pairedDevicesRecyclerView.setAdapter(bluetoothPairedDeviceAdapter);

        // Create swing animation
        rotateIcon = view.findViewById(R.id.rotateIcon);
        rotateAnimator = ObjectAnimator.ofFloat(rotateIcon, "rotation", 0f, 360f);
        rotateAnimator.setDuration(1500);
        rotateAnimator.setRepeatMode(ValueAnimator.RESTART);
        rotateAnimator.setRepeatCount(ValueAnimator.INFINITE);

        bluetoothInit();

        // Initialize ExecutorService and activeConnections
        executorService = Executors.newCachedThreadPool();
        activeConnections = new ConcurrentHashMap<>();

        // Press connectButton --> connect to TCP servers
        connectButton.setOnClickListener(v -> connectToServers());
        disconnectButton.setOnClickListener(v -> disconnectToServer());

        // Link the existing HashMap to GlobalData
        GlobalData.getInstance().setActiveConnections(activeConnections);

        // Register onFrameReceived for ProtocolReceive class
        BluetoothHelper.registerBluetoothFrameListener(new ProtocolReceive());

        // Frame tracking
        // createFrameLoggingTask();

        // Initiate permission launcher

        return view;
    }

    public static boolean validateIPAddress(final String ip) {
        String PATTERN = "^((0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)\\.){3}(0|1\\d?\\d?|2[0-4]?\\d?|25[0-5]?|[3-9]\\d?)$";

        return ip.matches(PATTERN);
    }

    public void connectToServers() {
        // Handling invalid IPv4
        String ipAddress = editText_IPAddress.getText().toString().trim();
        if (!validateIPAddress(ipAddress)) {
            TextView_status.setText("Status: Invalid IPv4 address !");
            return;
        }

        // Handling port number Exception
        int port;
        try {
            port = Integer.parseInt(editText_Port.getText().toString());
        } catch (NumberFormatException e) {
            TextView_status.setText("Status: Invalid Port number !");
            return;
        }

        // Create a unique key for each connection using IP + Port
        String connectionKey = ipAddress + ":" + port;

        // Check if a connection to this server already exists
        if (activeConnections.containsKey(connectionKey) && activeConnections.get(connectionKey).isConnected()) {
            String status = "Status: Already connected to " + connectionKey;
            TextView_status.setText(status);
            return;
        }

        // If not connected, run connection in the background
        executorService.submit(() -> {
            TCPClient tcpClient = new TCPClient();
            tcpClient.registerTCPConnectionListener(this);
            try {
                tcpClient.startConnection(ipAddress, port);
                activeConnections.put(connectionKey, tcpClient); // Save the connection in the map
                activeConnectionKey = connectionKey;
                requireActivity().runOnUiThread(() -> TextView_status.setText("Status: Connected to host " + connectionKey));

                // Keep requesting for regular data as soon as connection established
                createTCPGetRegularDataTask();
                startGetRegularData();
                // startFrameLogging();
            } catch (IOException e) {
                requireActivity().runOnUiThread(() -> TextView_status.setText("Status: Failed to connect to host " + connectionKey));
            }
        });

        handler.postDelayed(() -> {
            if (activeConnections.containsKey(connectionKey) && activeConnections.get(connectionKey).isConnected()) {
                HistoryFragment.recordOperation("TCP: Manually connected to ROBOT CONTROLLER");
            }
        }, 1000);
    }

    public void disconnectToServer() {
        // DEBUG
        System.out.println("Requested disconnect to server");
        // Handling invalid IPv4
        String ipAddress = editText_IPAddress.getText().toString().trim();
        if (!validateIPAddress(ipAddress)) {
            requireActivity().runOnUiThread(() -> TextView_status.setText("Status: Invalid IPv4 address !"));
            return;
        }

        // Handling port number Exception
        int port;
        try {
            port = Integer.parseInt(editText_Port.getText().toString());
        } catch (NumberFormatException e) {
            requireActivity().runOnUiThread(() -> TextView_status.setText("Status: Invalid Port number !"));
            return;
        }

        new Thread(() -> {
            String connectionKey = ipAddress + ":" + port;
            TCPClient tcpClient = activeConnections.get(connectionKey);
            if (tcpClient == null) {
                String status = "Status: No active connection to host " + connectionKey;
                requireActivity().runOnUiThread(() -> TextView_status.setText(status));
                return;
            }

            // isAlive() only take into consideration when heartbeat mechanism is enabled
            if (!tcpClient.isAlive()) {
                stopGetRegularData();
                stopFrameLogging();
                tcpClient.stopConnection();
                activeConnections.remove(connectionKey);
//                activeConnectionKey = "";
                String status = "Status: Disconnected to host " + connectionKey;
                requireActivity().runOnUiThread(() -> TextView_status.setText(status));
            }
        }).start();

        HistoryFragment.recordOperation("TCP: Manually disconnected from ROBOT CONTROLLER");
    }

    private void monitorConnection() {
        // Continuously checking for connected TCP devices
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                connectedTCPDevicesList.clear();
                connectedTCPDevicesList = activeConnections.keySet().stream()
                        .map(Object::toString)
                        .collect(Collectors.toList());
                tcpConnectedDeviceAdapter.updateDevices(connectedTCPDevicesList);

                if (activeConnections.isEmpty()) {
                    stopGetRegularData();
                }

                // Schedule the next execution
                handler.postDelayed(this, 1000);
            }
        }, 1000);
    }

    private void createTCPGetRegularDataTask() {
        ConcurrentHashMap<String, TCPClient> activeConnections = GlobalData.getInstance().getActiveConnections();
        TCPClient tcpClient = activeConnections.get(activeConnectionKey);


        if (tcpClient == null) {
            // Toast.makeText(this.requireContext(), "Server not found", Toast.LENGTH_SHORT).show();
            return;
        }

        if (tcpGetRegularDataTask != null) {
            return;
        }

        tcpGetRegularDataTask = new Runnable() {
            @Override
            public void run() {
                // Your repetitive code here
                tcpClient.getRegularData();

                // Schedule the next execution
                handler.postDelayed(this, 200);
            }
        };
    }

    private void startGetRegularData() {
        handler.postDelayed(tcpGetRegularDataTask, 200);
    }

    private void stopGetRegularData() {
        handler.removeCallbacks(tcpGetRegularDataTask);
        tcpGetRegularDataTask = null;
    }

    private void createFrameLoggingTask() {
        frameLoggingTask = new Runnable() {
            @Override
            public void run() {
                Log.i(TAG, "Cyclic logging task begin...");

                // Log TCP if over 500 lines
                // if (tcpFrameTracker != null) {
                if (tcpFrameTracker.getEntries().size() >= 50) {
                    Log.i(TAG, "Logging TCP frames...");
                    tcpFrameTracker.exportToCsv(requireContext(), FrameTracker.ChannelType.TCP);
                } else {
                    Log.i(TAG, "TCP entries log not big enough");
                }
                // }

                // Log Bluetooth if over 500 lines
                // if (btFrameTracker != null) {
                if (btFrameTracker.getEntries().size() >= 50) {
                    Log.i(TAG, "Logging Bluetooth frames...");
                    btFrameTracker.exportToCsv(requireContext(), FrameTracker.ChannelType.Bluetooth);
                }
                // }

                // Schedule the next execution
                handler.postDelayed(this, 3000);
            }
        };
    }

    private void startFrameLogging() {
        handler.postDelayed(frameLoggingTask, 3000);
    }

    private void stopFrameLogging() {
        handler.removeCallbacks(frameLoggingTask);

        int tcpPacketLoss = tcpSentCounter.get() - tcpFrameIdCounter.get();
        float tcpPacketLossRate = (float) tcpPacketLoss / tcpSentCounter.get();
        Log.i(TAG, "TCP Packet loss: " + tcpPacketLoss + " , Packet loss rate is: " + tcpPacketLossRate);

        int btPacketLoss = btSentCounter.get() - btFrameIdCounter.get();
        float btPacketLossRate = (float) btPacketLoss / btSentCounter.get();
        Log.i(TAG, "BT Packet loss: " + tcpPacketLoss + " , Packet loss rate is: " + btPacketLossRate);
    }

    public void bluetoothInit() {
        bluetoothPowerSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // Checked
            if (isChecked) {
                // bluetoothHelper = new BluetoothHelper(this.requireContext().getApplicationContext());
                bluetoothHelper = new BluetoothHelper(requireActivity());

                // Bluetooth supported
                if (bluetoothHelper.isBluetoothSupported()) {
                    bluetoothHelper.enableBluetooth();
                    bluetoothHelper.registerReceiver(bluetoothReceiver);
                    localDeviceNameEditText.setText(bluetoothHelper.getLocalDeviceName());

                    // Register for auto-reconnection
                    bluetoothHelper.registerConnectionListener(this);

                    // Continuously searching for paired devices
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            pairedDevicesSet = bluetoothHelper.getPairedDevices();
                            pairedDevicesList.clear();
                            pairedDevicesList.addAll(pairedDevicesSet);
                            bluetoothPairedDeviceAdapter.updateDevices(pairedDevicesList);

                            // Schedule the next execution
                            handler.postDelayed(this, delay);
                        }
                    }, 4000);

                    // Continuously searching for connected BT devices
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            connectedDevicesSet = bluetoothHelper.getConnectedDevices();
                            connectedDevicesList.clear();
                            connectedDevicesList.addAll(connectedDevicesSet);
                            bluetoothConnectedDeviceAdapter.updateDevices(connectedDevicesList);

                            // Schedule the next execution
                            handler.postDelayed(this, delay);
                        }
                    }, 1000);
                }
                // Bluetooth not supported
                else {
                    Toast.makeText(this.requireContext(), "Device does not support Bluetooth", Toast.LENGTH_SHORT).show();
                }

            }
            // Unchecked
            else {
                if (bluetoothHelper != null && bluetoothHelper.isBluetoothSupported()) {
                    bluetoothHelper.disableBluetooth();
                    bluetoothHelper.unregisterReceiver(bluetoothReceiver);
                }

            }
        });

        bluetoothCommunicationSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked && bluetoothHelper != null && bluetoothHelper.isBluetoothEnabled() && bluetoothHelper.isBluetoothSupported()) {
                Toast.makeText(this.requireContext(), "Successfully enabled Bluetooth communication", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this.requireContext(), "Please enable Bluetooth on this device", Toast.LENGTH_SHORT).show();
            }
        });

        bluetoothConnectButton.setOnClickListener(v -> {
            bluetoothConnect();
        });

        bluetoothDisconnectButton.setOnClickListener(v -> {
            bluetoothDisconnect();
        });

        bluetoothScanButton.setOnClickListener(v -> {
            bluetoothScan();
        });
    }

    public void bluetoothConnect() {
        if (globalSelectedBTDevice != null && bluetoothHelper != null && bluetoothHelper.isBluetoothEnabled()) {
            if (bluetoothScanButton.getText().equals("STOP"))
                bluetoothScan();  // Stop discovery to connect (if discovering)

            // May add runtime permission request for 24 <= API < 31 here

            bluetoothHelper.connect(globalSelectedBTDevice);
            // startFrameLogging();

            // Wait for fully established connection before checking
            handler.postDelayed(() -> {
                if (PriorityCommunication.checkAvailableChannel() == PriorityCommunication.ChannelType.Bluetooth) {
                    HistoryFragment.recordOperation("Bluetooth: Manually connected to Robot Controller");
                }
            }, 2000);

        } else {
            Toast.makeText(this.requireContext(), "No device selected or bluetooth not enabled", Toast.LENGTH_SHORT).show();
        }
    }

    public void bluetoothDisconnect() {
        if (globalSelectedBTDevice != null && bluetoothHelper != null && bluetoothHelper.isBluetoothEnabled()) {
            bluetoothHelper.disconnect(globalSelectedBTDevice);
            stopFrameLogging();

            if (PriorityCommunication.checkAvailableChannel() != PriorityCommunication.ChannelType.Bluetooth) {
                HistoryFragment.recordOperation("Bluetooth: Manually disconnected to Robot Controller");
            }
        } else {
            Toast.makeText(this.requireContext(), "No device selected or bluetooth not enabled", Toast.LENGTH_SHORT).show();
        }
    }

    public void bluetoothScan() {
        if (bluetoothHelper == null) {
            return;
        }

        if (!bluetoothHelper.isBluetoothSupported()) {
            return;
        }

        if (!scanPressed) {
            scanPressed = true;
            bluetoothScanButton.setText("STOP");
            // Toast.makeText(this.requireContext(), "Scanning for Bluetooth devices", Toast.LENGTH_SHORT).show();

            // Clear all nearby devices
            discoveredBTDevices.clear();
            bluetoothDeviceAdapter.notifyDataSetChanged();

            // Runtime permission check for 24 <= API < 31
//            if (Build.VERSION.SDK_INT >= 24 && Build.VERSION.SDK_INT < 31) {
//                if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)
//                        != PackageManager.PERMISSION_GRANTED) {
//
//                    if (!ActivityCompat.shouldShowRequestPermissionRationale(requireActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
//                        Log.i(TAG, "Permission previously denied with 'Don't ask again'");
//
//                        InfoDialog infoDialog = new InfoDialog(requireActivity(), "OPEN_APP_INFO");
//                        infoDialog.setInfoHint("Permissions missing\nOpen App Info to set Bluetooth permissions");
//                        infoDialog.showNumpadPopup(requireActivity());
//
//                        return;
//                    }
//
//                    ActivityCompat.requestPermissions(
//                            requireActivity(),
//                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
//                            REQUEST_PERMISSIONS
//                    );
//                }
//            }

            // Start scanning
            bluetoothHelper.startDiscovery();

            // Start swing animation
            if (!rotateAnimator.isRunning()) {
                rotateAnimator.start();
            }
        } else {
            scanPressed = false;
            bluetoothScanButton.setText("SCAN");
            // Toast.makeText(this.requireContext(), "Stopped scanning for Bluetooth devices", Toast.LENGTH_SHORT).show();

            // Stop scanning
            bluetoothHelper.stopDiscovery();

            // Stop swing animation
            if (rotateAnimator.isRunning()) {
                rotateAnimator.cancel();
                rotateIcon.setRotation(0); // Reset to default position
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // handler.removeCallbacks(tcpGetRegularDataTask);
        if (tcpGetRegularDataTask != null) stopGetRegularData();
        if (frameLoggingTask != null) stopFrameLogging();

        if (bluetoothHelper != null) {
            bluetoothHelper.unregisterReceiver(bluetoothReceiver);
        } else {
            System.out.println("Cannot unregister bluetooth: NULL receiver");
        }
    }

//    @Override
//    public void onAttach(@NonNull Context context) {
//        super.onAttach(this.requireContext());
//        if (bluetoothHelper != null) {
//            bluetoothHelper.registerConnectionListener(this);
//        }
//    }

    @Override
    public void onResume() {
        super.onResume();
        if (bluetoothHelper != null) {
            bluetoothHelper.registerConnectionListener(this);
        }
        bluetoothConnectedDeviceAdapter.notifyDataSetChanged();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (bluetoothHelper != null) {
            bluetoothHelper.removeConnectionListener(this);
        }
    }

    @Override
    public void onConnectedDevicesChanged
            (Set<BluetoothDevice> connectedDevices, BluetoothHelper.ConnectionState
                    connectionState, BluetoothHelper.Disconnector disconnector) {
        requireActivity().runOnUiThread(() -> {
            connectedDevicesSet = connectedDevices; // Get connected devices
            connectedDevicesList.clear();
            connectedDevicesList.addAll(connectedDevicesSet);
            bluetoothConnectedDeviceAdapter.updateDevices(connectedDevicesList);
        });

        // Only reconnect if not "manual" disconnection (a.k.a Controller disconnect)
        if (connectionState == BluetoothHelper.ConnectionState.DISCONNECTED &&
                disconnector == BluetoothHelper.Disconnector.PARTNER) {
            // Log
            handler.postDelayed(() -> {
                HistoryFragment.recordOperation("Bluetooth: Unexpected disconnection");
            }, 500);


            if (bluetoothHelper != null && globalSelectedBTDevice != null) {
                showToast("Reconnecting (BT) to Robot Controller", false);
                Log.i(TAG, "Check point for reconnecting ongoing...");
                handler.postDelayed(() -> {
                    for (int i = 0; i < 1; i++) {   // !CAUTION: Only loop more than once for testing
                        bluetoothHelper.connect(globalSelectedBTDevice);
                        /* Avoid recall socket since it is already removed once connection closes.
                        connect() already check if connected. Feel free to use it/ */
                    }
                }, 3000);
            } else {
                showToast("Bluetooth unavailable for now", false);
            }
        }

        if (connectionState == BluetoothHelper.ConnectionState.CONNECTED &&
                disconnector == BluetoothHelper.Disconnector.NONE) {
            // Log
            handler.postDelayed(() -> {
                HistoryFragment.recordOperation("Bluetooth: Reconnection succeed");
            }, 500);
        }
    }

    @Override
    public void onTCPConnectedDevicesChanged(TCPClient.ConnectionState
                                                     connectionState, TCPClient.Disconnector disconnector) {
        // Only reconnect if not "manual" disconnection (a.k.a Controller disconnect)
        if (connectionState == TCPClient.ConnectionState.DISCONNECTED &&
                disconnector == TCPClient.Disconnector.PARTNER) {
            // Log
            handler.postDelayed(() -> {
                HistoryFragment.recordOperation("TCP: Unexpected disconnection");
            }, 500);

            showToast("Reconnecting (TCP) to Robot Controller", false);
            for (int i = 0; i < MAX_TCP_RETRIES; i++) { // Reconnection timeout = single connect TIMEOUT * RETRIES = 12s
                handler.postDelayed(this::connectToServers, i == 0 ? 1000 : 3000);
            }
        }
    }

    private void showToast(String message, boolean longToast) {
        new Handler(Looper.getMainLooper()).post(() ->
                Toast.makeText(this.requireContext(), message, longToast ? Toast.LENGTH_LONG : Toast.LENGTH_SHORT).show()
        );
    }
}