package com.example.myrobotapp.Class.TCP;

import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.activeConnectionKey;
import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.activeConnections;
import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.tcpFrameTracker;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.myrobotapp.Class.ProtocolSend;
import com.example.myrobotapp.Fragments.Features.HistoryFragment;
import com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment;

import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class TCPClient {
    public static final String TAG = "TCPClient";

    // Boolean for checking if previous frame send is done
    public static boolean uiFrameSendDone = true;
    public static final int TIMEOUT_MS = 3000;

    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private boolean isAlive;
    private ProtocolSend protocolSend;
    private ByteArrayOutputStream baos, baosR;
    private BufferedOutputStream outputStream;
    private BufferedInputStream inputStream;

    private ExecutorService receiveExecutor;
    private ExecutorService frameProcessorPool;
    private static final int MAX_FRAME_SIZE = 512;

    // Executor service for managing heartbeats
    private ScheduledExecutorService heartbeatExecutor;
    private static final int HEARTBEAT_DELAY = 5; // seconds
    private ExecutorService sendExecutor;

    private final List<TCPConnectionListener> tcpConnectionListeners = new ArrayList<>();
    private static final List<TCPFrameListener> tcpFrameListeners = new ArrayList<>();

    public static Queue<Long> tcpSendTimes = new ConcurrentLinkedQueue<>();
    public static AtomicInteger tcpSentCounter = new AtomicInteger(0);

    public enum ConnectionState {
        CONNECTED,
        DISCONNECTED
    }

    public enum Disconnector {
        SELF,
        PARTNER,
        NONE
    }

    // ***************************** ON CONNECTED DEVICES CHANGE ****************************
    public interface TCPConnectionListener {
        void onTCPConnectedDevicesChanged(ConnectionState connectionState, Disconnector disconnector);
    }

    // Register listener
    public void registerTCPConnectionListener(TCPConnectionListener listener) {
        if (!tcpConnectionListeners.contains(listener)) {
            tcpConnectionListeners.add(listener);
        }
    }

    // Remove listener
    public void removeTCPConnectionListener(TCPConnectionListener listener) {
        tcpConnectionListeners.remove(listener);
    }

    // Notify listener
    public void notifyTCPConnectionChanged(ConnectionState connectionState, Disconnector disconnector) {
        for (TCPConnectionListener listener : tcpConnectionListeners) {
            listener.onTCPConnectedDevicesChanged(connectionState, disconnector);  // Notify listeners and send them new Set
        }
    }

    // ***************************** ON TCP FRAME RECEIVED ***************************************
    public interface TCPFrameListener {
        void onTCPFrameReceived(byte[] frame);
    }

    public static void registerTCPFrameListener(TCPFrameListener listener) {
        if (!tcpFrameListeners.contains(listener)) {
            tcpFrameListeners.add(listener);
        }
    }

    public static void unregisterTCPFrameListener(TCPFrameListener listener) {
        tcpFrameListeners.remove(listener);
    }

    public void notifyTCPFrameReceived(byte[] frame) {
        for (TCPFrameListener listener : tcpFrameListeners) {
            listener.onTCPFrameReceived(frame);
        }
    }

    // ************************************* TCP CLIENT CONNECTION **********************************//
    public void startConnection(String ip, int port, int timeoutMillis) throws IOException {
        clientSocket = new Socket();
        clientSocket.connect(new InetSocketAddress(ip, port), timeoutMillis);
        //out = new PrintWriter(clientSocket.getOutputStream(), true);
        //in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        outputStream = new BufferedOutputStream(clientSocket.getOutputStream());
        inputStream = new BufferedInputStream(clientSocket.getInputStream());

        // Protocol for Sending
        protocolSend = new ProtocolSend();
        baos = new ByteArrayOutputStream();
        baosR = new ByteArrayOutputStream();

        // Start listening for frames
        startListening();

        // Log connection succeed
        new Handler(Looper.getMainLooper()).post(() -> {
            HistoryFragment.recordOperation("TCP: Reconnection succeed");
        });

        System.out.println("Connection established and listener started.");
    }

    // Overloaded "startConnection" method with 3s timeout by default
    public void startConnection(String ip, int port) throws IOException {
        startConnection(ip, port, TIMEOUT_MS);
    }

    public void stopConnection() {
        try {
            // Stop frame listener and heartbeat
            shutdownExecutor(sendExecutor, "Send");
            shutdownExecutor(receiveExecutor, "Receive");
            shutdownExecutor(frameProcessorPool, "Frame Processor");
            shutdownExecutor(heartbeatExecutor, "Heartbeat");

            stopListening();    // Stop frame listener

            if (clientSocket != null && !clientSocket.isClosed()) clientSocket.close();

            System.out.println("Closed client Socket");

            if (inputStream != null) inputStream.close();

            if (outputStream != null) outputStream.close();

            baos = null;        // Let's change to baos.close(); tomorrow

            baosR = null;       // Let's change to baosR.close();

            System.out.println("Connection stopped and listener terminated.");
        } catch (IOException e) {
            System.out.println("Error stopping connection: " + e.getMessage());
        }
    }

    private void shutdownExecutor(ExecutorService executor, String name) {
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    executor.shutdownNow(); // Force shutdown if timeout occurs
                    System.out.println(name + " executor forcibly terminated.");
                } else {
                    System.out.println(name + " executor terminated gracefully.");
                }
            } catch (InterruptedException e) {
                executor.shutdownNow(); // Interrupt if necessary
                Thread.currentThread().interrupt();
                System.out.println(name + " executor interrupted during shutdown.");
            }
        }
    }


    // ************************************* FRAME LISTENING **********************************//
    public void startListening() {
        if (isConnected() && (receiveExecutor == null || receiveExecutor.isShutdown())) {
            receiveExecutor = Executors.newFixedThreadPool(2);
            frameProcessorPool = Executors.newFixedThreadPool(4); // Adjust pool size as needed
            receiveExecutor.submit(this::receiveFrame);
            System.out.println("Started listening for frames.");
        } else {
            System.out.println("Cannot start listening; not connected or already listening.");
        }
    }

    public void stopListening() {
        try {
            if (receiveExecutor != null && !receiveExecutor.isShutdown()) {
                receiveExecutor.shutdownNow(); // Stop the main listener thread
                System.out.println("Stopped main frame listener.");
            }

            if (frameProcessorPool != null && !frameProcessorPool.isShutdown()) {
                frameProcessorPool.shutdownNow(); // Stop all frame processing threads
                System.out.println("Stopped frame processing threads.");
            }
        } catch (Exception e) {
            System.out.println("Error stopping listeners: " + e.getMessage());
        }
    }

    public boolean isConnected() {
        // Can use heartbeat mechanism for more robust connection validation
        return clientSocket != null && clientSocket.isConnected() && !clientSocket.isClosed() && !clientSocket.isInputShutdown();
    }
    // *********************************************************************************************

    // Heartbeat mechanism
    private void startHeartbeat() {
        heartbeatExecutor = Executors.newSingleThreadScheduledExecutor();
        heartbeatExecutor.scheduleWithFixedDelay(() -> {
            try {
                if (isConnected()) {
                    sendHeartbeat();
                }
            } catch (IOException e) {
                System.out.println("Failed to send heartbeat: " + e.getMessage());
            }
        }, 0, HEARTBEAT_DELAY, TimeUnit.SECONDS);  // Initial delay is set to 0, followed by fixed delay of 5 second
    }

    // Heartbeat sending method
    private void sendHeartbeat() throws IOException {
        sendMessage("ping");
    }

    // Send FRAME DATA (in use)
    public void sendFrame(byte[] frame) {
        if (sendExecutor == null || sendExecutor.isShutdown()) {
            sendExecutor = Executors.newSingleThreadExecutor();
        }
        sendExecutor.submit(() -> {
            try {
                if (clientSocket == null || clientSocket.isClosed()) {
                    throw new IOException("Connection is not open");
                }
                // Start sending
//                if (uiFrameSendDone) {
//                    uiFrameSendDone = false;
                Log.i(TAG, "Frame sent is: " + Arrays.toString(frame));
                outputStream.write(frame);
                outputStream.flush();
//                    uiFrameSendDone = true;
//                }

            } catch (IOException e) {
                System.out.println("Error sending data: " + e.getMessage());
            }
        });

        // Record sendTime to static variable
        // CAUTION: Must add frameId to match with receive frameId for accurate latency calculation
        // OR: Only track GET_REG_DATA frame and ignore all others
        tcpSendTimes.add(System.currentTimeMillis());
        tcpSentCounter.getAndIncrement();
    }

    // Receive FRAME DATA (in use)
    private void receiveFrame() {
        System.out.println("receiveFrame() method is running...");
        boolean isPartnerDisconnect = false;

        final byte DLE = 0x10;
        final byte STX = 0x02;
        final byte ETX = 0x03;

        final int MAX_DATA_SIZE = 506;
        final int FRAME_OVERHEAD = 2 + 2 + 2; // DLE+STX + CMD (2 bytes) + DLE+ETX
        final int MAX_FRAME_SIZE = MAX_DATA_SIZE + FRAME_OVERHEAD;

        ByteArrayOutputStream frameBuffer = new ByteArrayOutputStream();
        boolean inFrame = false;
        boolean dleDetected = false; // Tracks a DLE byte for start/end detection

        try {
            byte[] buffer = new byte[1024];
            int bytesRead;

            System.out.println("1st checkpoint...");
            while (!Thread.currentThread().isInterrupted()) {
                if ((bytesRead = inputStream.read(buffer)) == -1) {
                    System.out.println("Connection closed by server.");
                    isPartnerDisconnect = true;
                    break;
                }

                for (int i = 0; i < bytesRead; i++) {
                    byte b = buffer[i];

                    // Check for DLE byte
                    if (b == DLE) {
                        dleDetected = true; // DLE detected, mark it
                        frameBuffer.write(b); // Write DLE to buffer
                        continue;
                    }

                    if (dleDetected) {
                        // If the previous byte was DLE, check for STX or ETX
                        if (!inFrame && b == STX) {
                            // Start of frame detected
                            inFrame = true;
                            frameBuffer.write(b);   // Write STX after DLE
                        } else if (inFrame && b == ETX) {
                            // End of frame detected
                            frameBuffer.write(b); // Write ETX
                            final byte[] frame = frameBuffer.toByteArray();
                            frameBuffer.reset();

                            System.out.println("Frame received: [");
                            String receiveDebug = bytesToHex(frame);
                            System.out.println(receiveDebug);
//                            ProtocolReceive.process(frame);
                            notifyTCPFrameReceived(frame);
                            Log.i(TAG, "Notified TCP frame received");

                            inFrame = false; // Reset frame state
                        } else {
                            // Write the current byte after DLE detection
                            frameBuffer.write(b);
                        }
                        dleDetected = false; // Reset DLE detection
                        continue;
                    }

                    // Write to buffer if inFrame
                    if (inFrame) {
                        frameBuffer.write(b);
                        // Check for buffer overflow
                        if (frameBuffer.size() > MAX_FRAME_SIZE) {
                            System.out.println("Frame exceeded maximum size, discarding.");
                            frameBuffer.reset();
                            inFrame = false;
                        }
                    }
                }
            }

            if (receiveExecutor != null && !receiveExecutor.isShutdown()) {
                System.out.println("Receive Executor is still running...");
            } else {
                System.out.println("Receive Executor is interrupted/killed !");
            }
        } catch (IOException e) {
            if (!Thread.currentThread().isInterrupted()) {  // Listening thread stops
                System.out.println("Error receiving data: " + e.getMessage());
            }
        } finally { // Both manual and unexpected disconnect go through this block
            System.out.println("Receiver thread stopping !");
            ConnectionFragment.activeConnections.remove(activeConnectionKey);
            Log.i(TAG, "activeConnections after removal: " + activeConnections);

            stopConnection();

            Log.i(TAG, "TCP Server side closed");
            if (isPartnerDisconnect) {
                notifyTCPConnectionChanged(ConnectionState.DISCONNECTED, Disconnector.PARTNER);
                isPartnerDisconnect = false;
            }
            Log.i(TAG, "Notified TCP Connection changed");
        }
    }

    private String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    // Send DATA/message to server (only used by heartbeat mechanism)
    private void sendMessage(String msg) throws IOException {
        if (clientSocket == null || clientSocket.isClosed()) {
            throw new IOException("Connection is not open");
        }
        out.println(msg);
    }

    // Start receiving messages (only for heartbeat responses) on a separate thread
    private void startReceivingMessages() {
        if (receiveExecutor == null || receiveExecutor.isShutdown()) {
            receiveExecutor = Executors.newSingleThreadExecutor();
        }
        receiveExecutor.submit(() -> {
            try {
                while (isConnected()) {
                    String message = receiveMessage();
                    // to process data further (which we are going to do in main file)
                    if (message != null) {
                        handleReceivedMessage(message);
                    }
                }
            } catch (IOException e) {
                System.out.println("Error receiving message: " + e.getMessage());
            }
        });
    }

    // Receive a message from server (blocking call) (only for heartbeat responses)
    private String receiveMessage() throws IOException {
        if (clientSocket == null || clientSocket.isClosed()) {
            throw new IOException("Connection is not open");
        }
        return in.readLine(); // Blocking call, waits for message
    }

    // Handle received messages
    private void handleReceivedMessage(String message) {
        if ("".equals(message)) {
            isAlive = false;
            return;
        }
        if ("pong".equals(message)) {
            // Handle heartbeat response
            isAlive = true;
            System.out.println("Received heartbeat response from server.");
        } else {
            // Handle main data
            System.out.println("Received data: " + message);
        }
    }

    // Quick check for alive connection (only for heartbeat responses)
    public boolean isAlive() {
        return isAlive;
    }

    // ************************************* ROBOT OPERATION PROCESSING ********************************** //
    // GET REGULAR DATA + RESET ERROR
    // + COMPILE + CHECK COMPILE RESULT + RUN ROBOT PROG + STOP ROBOT PROG + REBOOT CONTR. 1ST SIGNAL
    // Các chức năng khác code y hệt (DATA = null), thay mỗi Operation nhưng viết sau vì quá dài
    public void getRegularData() {
        baos.reset();
        byte[] regDataFrame = protocolSend.operate(ProtocolSend.Operation.GET_REG_DATA, baos);
        sendFrame(regDataFrame);
    }

    public void rebootFirst() {
        baos.reset();
        byte[] rebootFirstFrame = protocolSend.operate(ProtocolSend.Operation.REBOOT_1ST_SIG, baos);
        sendFrame(rebootFirstFrame);
    }

    public void resetErrors() {
        baos.reset();
        byte[] resetErrorFrame = protocolSend.operate(ProtocolSend.Operation.RESET_ERROR, baos);
        sendFrame(resetErrorFrame);
    }

    public void compileProgram() {
        baos.reset();
        byte[] regDataFrame = protocolSend.operate(ProtocolSend.Operation.COMPILE, baos);
        sendFrame(regDataFrame);
    }

    public void checkCompileResult() {
        baos.reset();
        byte[] regDataFrame = protocolSend.operate(ProtocolSend.Operation.CHECK_COMPILE_RESULT, baos);
        sendFrame(regDataFrame);
    }

    public void runRobotProgram() {
        baos.reset();
        byte[] regDataFrame = protocolSend.operate(ProtocolSend.Operation.RUN_PROG, baos, 10, "", "", "");
        sendFrame(regDataFrame);
    }

    public void stopRobotProgram() {
        baos.reset();
        byte[] stopRobotProgramFrame = protocolSend.operate(ProtocolSend.Operation.STOP_PROG, baos);
        sendFrame(stopRobotProgramFrame);
    }

    // SET SERVO ON/OFF
    public void setServo(int axes, int state) {
        baos.reset();
        byte[] setServoFrame = protocolSend.operate(ProtocolSend.Operation.SET_SERVO, baos, axes, state);
        sendFrame(setServoFrame);
    }

    // SET AUTO/MANUAL MODE: 0x01 = MAN, 0x02 = AUTO
    public void setMode(int mode) {
        baos.reset();
        byte[] setModeFrame = protocolSend.operate(ProtocolSend.Operation.SET_CONTROL_MODE, baos, mode);
        sendFrame(setModeFrame);
    }

    // MANUAL JOG
    public void jog(int coordinate, int axis, int buttonState, int direction, float velocity) {
        baos.reset();
        byte[] jogFrame = protocolSend.operate(ProtocolSend.Operation.JOG_MANUAL, baos, coordinate, axis, buttonState, direction, velocity);
        sendFrame(jogFrame);
    }

    // REBOOT CTRL. 2ND SIGNAL (CONFIRMATION)
    public void rebootSecond(char confirm) {
        baos.reset();
        byte[] rebootSecondFrame = protocolSend.operate(ProtocolSend.Operation.REBOOT_2ND_SIG, baos, confirm);
        sendFrame(rebootSecondFrame);
    }

    // MOVE TO HOME
    public void moveToHome(int homePosIndex, float speedLevel) {
        baos.reset();
        byte[] moveToHomeFrame = protocolSend.operate(ProtocolSend.Operation.MOVE_HOME, baos, homePosIndex, speedLevel);
        sendFrame(moveToHomeFrame);
    }

    // MOVE TO POSITION
    public void moveToPosition(int coordinateState, float speedLevel, String[] position) {
        baos.reset();
        byte[] moveToPositionFrame = protocolSend.operate(ProtocolSend.Operation.MOVE_TO_POSITION, baos, coordinateState, speedLevel, position);
        sendFrame(moveToPositionFrame);
    }

    // SEND PROGRAM FILE PART
    public void sendProgramPart(int partIndex, String programName, Context context) {
        baos.reset();
        byte[] sendProgramPartFrame = protocolSend.operate(ProtocolSend.Operation.SEND_PROG_PART, baos, partIndex, programName, context);
        sendFrame(sendProgramPartFrame);
    }

    // SEND PROGRAM END PART
    public void sendProgramEndPart(int endPartIndex, String programName, Context context, String miscellaneous) {
        baos.reset();
        byte[] sendProgramPartFrame = protocolSend.operate(ProtocolSend.Operation.SEND_PROG_END_PART, baos, endPartIndex, programName, context, miscellaneous);
        sendFrame(sendProgramPartFrame);
    }

    // SEND CONFIG FILE PART
    public void sendConfigPart(int partIndex, String programName, Context context, boolean isCustom) {
        baos.reset();
        byte[] sendConfigPartFrame = protocolSend.operate(ProtocolSend.Operation.SEND_PROG_PART, baos, partIndex, programName, context, isCustom);
        sendFrame(sendConfigPartFrame);
    }

    // SEND CONFIG END PART
    public void sendConfigEndPart(int endPartIndex, String programName, Context context, boolean isCustom, String miscellaneous) {
        baos.reset();
        byte[] sendConfigEndPartFrame = protocolSend.operate(ProtocolSend.Operation.SEND_PROG_END_PART, baos, endPartIndex, programName, context, isCustom, miscellaneous);
        sendFrame(sendConfigEndPartFrame);
    }

    // SAVE AXES ZERO VALUE
    public void saveAxesZeroValue(int axisQuantity, int[] zeroValues) {
        baos.reset();
        byte[] saveAxesZeroValueFrame = protocolSend.operate(ProtocolSend.Operation.SAVE_AXES_ZERO_VALUE, baos, axisQuantity, zeroValues);
    }

    // SAVE ROBOT HOME VALUE
    public void saveRobotHomeValue(int numAxis, String[] home1Position, String[] home2Position) {
        baos.reset();
        byte[] saveRobotHomeValueFrame = protocolSend.operate(ProtocolSend.Operation.SAVE_HOME, baos, numAxis, home1Position, home2Position);
        sendFrame(saveRobotHomeValueFrame);
    }

    // SET OUTPUT VALUE
    public void setOutputValue(int group, int value) {
        baos.reset();
        byte[] sendProgramPartFrame = protocolSend.operate(ProtocolSend.Operation.SET_OUTPUT_VALUE, baos, group, value, "");
        sendFrame(sendProgramPartFrame);
    }
}
