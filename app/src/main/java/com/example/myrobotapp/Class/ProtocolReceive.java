package com.example.myrobotapp.Class;

import static com.example.myrobotapp.Class.Bluetooth.BluetoothHelper.btSendTimes;
import static com.example.myrobotapp.Class.TCP.TCPClient.tcpSendTimes;
import static com.example.myrobotapp.Fragments.Features.Settings.AxisSettingFragment.numAxis;
import static com.example.myrobotapp.Fragments.Features.Settings.AxisSettingFragment.numCounter;
import static com.example.myrobotapp.Fragments.Features.Settings.AxisSettingFragment.numInput;
import static com.example.myrobotapp.Fragments.Features.Settings.AxisSettingFragment.numOutput;
import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.btFrameTracker;
import static com.example.myrobotapp.Fragments.Features.Settings.ConnectionFragment.tcpFrameTracker;

import android.bluetooth.BluetoothDevice;
import android.util.Log;

import com.example.myrobotapp.Class.Bluetooth.BluetoothHelper;
import com.example.myrobotapp.Class.TCP.TCPClient;

import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class ProtocolReceive implements BluetoothHelper.BluetoothFrameListener, TCPClient.TCPFrameListener{

    private static final String TAG = "ProtocolReceive";

    public static ErrorListener errorListener;

    public interface ErrorListener {
        void onErrorReceived(String errorCodes, int axisPosition);
    }

    public static void notifyErrorReceived(String errorCode, int axisPosition) {   // axisPosition = order of axis on the configuration
        if (errorListener != null) {
            errorListener.onErrorReceived(errorCode, axisPosition);
        }
    }

    private static final byte STX = 0x02;  // Start of Text
    private static final byte ETX = 0x03;  // End of Text
    private static final byte DLE = 0x10;  // Data Link Escape
    private static ByteArrayOutputStream baos;

    public static final int[] ACK_SEND_PROG_PART = {0x00, 0x96};
    public static final int[] ACK_SENT_PROG = {0x00, 0x97};
    public static final int[] COMPILE_RESULT = {0x00, 0x98};
    public static final int[] REBOOT_CONFIRM = {0x00, 0x99};
    public static final int[] READ_PROG_PART = {0x00, 0x9A};
    public static final int[] READ_PROG_ENDED = {0x00, 0x9C};
    public static final int[] UPDATE_REG_DATA = {0x00, 0x9B};
    public static final int[] UPDATE_PROG_STATE = {0x00, 0x9D};
    public static final int[] IS_ALIVE = {0x00, 0x13};          // TEST PING

    // COMPILE RESULT (OK: 0x59, ERROR: 0x4E), PROGRAM SENDING, REBOOT
    public static String compileResult = "None";
    public static String programRunningStatus = "Stopped";
    public static int sendProgramPartACK = 0;
    public static int sendProgramEndPartACK = 0;
    public static String rebootQuestion = "";

    public static String regularDataStatus = "None";

    // NUM AXIS (1 BYTE)
    public static int numAxisReal;

    // CONTROLLER'S ERROR CODE (1 BYTE UINT8_T)
    public static String ctrlErrorCode = "Not updated";  // 1 byte uint8_t

    // AXIS ERROR CODE (NUM AXIS * 2 BYTE UINT16_T)
    public static List<String> axesErrCodeList = new ArrayList<>();
    public static String axis1ErrCode = "Not updated";  // 2 byte uint16_ts
    public static String axis2ErrCode = "Not updated";  // 2 byte uint16_t
    public static String axis3ErrCode = "Not updated";  // 2 byte uint16_t
    public static String axis4ErrCode = "Not updated";  // 2 byte uint16_t
    public static String axis5ErrCode = "Not updated";  // 2 byte uint16_t
    public static String axis6ErrCode = "Not updated";  // 2 byte uint16_t

    // AXIS STATUS WORD (NUM AXIS * 4 BYTE UINT32_T)
    public static List<String> axisStatusWordList = new ArrayList<>();

    // AXIS CONTROL WORD (NUM AXIS * 4 BYTE UINT32_T)
    public static List<String> axisControlWordList = new ArrayList<>();

    // AXIS POSITION (NUM AXIS * 4 BYTE INT32_T)
    public static List<String> actualAxesPosList = new ArrayList<>();
    public static String axis1Pos = "";   // 4 byte int32_t
    public static String axis2Pos = "";   // 4 byte int32_t
    public static String axis3Pos = "";   // 4 byte int32_t
    public static String axis4Pos = "";   // 4 byte int32_t
    public static String axis5Pos = "";   // 4 byte int32_t
    public static String axis6Pos = "";   // 4 byte int32_t

    // TCP POSITION + JOINT POSITION
    public static List<String> jointPosList = new ArrayList<>();
    public static List<String> tcpPosList = new ArrayList<>();
    public static String xPos = "", yPos = "", zPos = "", rX = "", rY = "", rZ = "";       // 6*8 byte double
    public static String joint1Pos = "";     // numAxis*4 byte int32_t
    public static String joint2Pos = "";     // numAxis*4 byte int32_t
    public static String joint3Pos = "";     // numAxis*4 byte int32_t
    public static String joint4Pos = "";     // numAxis*4 byte int32_t
    public static String joint5Pos = "";     // numAxis*4 byte int32_t
    public static String joint6Pos = "";     // numAxis*4 byte int32_t

    // SYSTEM INPUT VALUE (2 * 1 BYTE UINT8_T)
    public static List<String> systemInputValueList = new ArrayList<>();

    // SYSTEM OUTPUT VALUE (2 * 1 BYTE UINT8_T)
    public static List<String> systemOutputValueList = new ArrayList<>();


    // INPUT / OUTPUT / COUNTER (NUM_INPUT/NUM_OUTPUT/NUM_COUNTER  * 4 BYTE UINT32_T)
    public static boolean[] inputState = new boolean[32];
    public static boolean[] outputState = new boolean[32];

    // FRAME TRACKING
    public static AtomicInteger tcpFrameIdCounter = new AtomicInteger(1);
    public static AtomicInteger btFrameIdCounter = new AtomicInteger(1);

    // REGISTER FRAME RECEIVED
    // (Already done in ConnectionFragment)

    // ******************************* CONVERTER FOR JAVA TO INTERPRET **************************** //
    public static int readUINT16_T(byte lsb, byte msb) {
        return ((msb & 0xFF) << 8 | lsb & 0xFF);
    }

    public static long readUINT32_T(byte firstByte, byte secondByte, byte thirdByte, byte fourthByte) {
        return ((long) (fourthByte & 0xFF) << 24) |
                ((long) (thirdByte & 0xFF) << 16) |
                ((long) (secondByte & 0xFF) << 8) |
                ((long) (firstByte & 0xFF));
    }

    public static double readDOUBLE(byte[] value) {
        ByteBuffer buffer = ByteBuffer.wrap(value);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getDouble();
    }

    public static float readFLOAT(byte[] value) {
        ByteBuffer buffer = ByteBuffer.wrap(value);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getFloat();
    }

    public static int readINT32_T(byte[] value) {
        ByteBuffer buffer = ByteBuffer.wrap(value);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        return buffer.getInt();
    }

    // Convert byte array to Hex
    public static String byteToHex(byte[] byteArray, int index) {
        if (byteArray == null || index < 0 || index >= byteArray.length) {
            throw new IllegalArgumentException("Index is out of bounds or byte array is null.");
        }

        // Get the specific byte
        byte selectedByte = byteArray[index];

        // Convert to different formats
        String hexValue = String.format("0x%02X", selectedByte & 0xFF); // Convert uint8_t to signed int8_t
        return hexValue;
    }

    // ******************************* FRAME PROCESSING ******************************************* //
    public static void process(byte[] frame) {
        baos = new ByteArrayOutputStream();

        // Extract CMD bytes
        int cmd1 = frame[2] & 0xFF;
        int cmd2 = ((int) frame[3] & 0xFF) & 0xFF;

        // Extract DATA and save to a new array
        byte[] pureData = new byte[frame.length - 6];
        System.arraycopy(frame, 4, pureData, 0, pureData.length);

        // Filter out escaped DLE, STX, ETX in pureData array
        baos.reset();
        for (int i = 0; i < pureData.length; i++) {
            // If current byte is DLE
            if (pureData[i] == DLE) {
                // Check for bounds before accessing i+1
                if (i + 1 < pureData.length) {
                    // If DLE is escaping DLE, STX, or ETX, write the next byte
                    if (pureData[i + 1] == DLE || pureData[i + 1] == STX || pureData[i + 1] == ETX) {
                        baos.write(pureData[i + 1]);
                        i++; // Skip the next byte as it has been escaped
                        continue;
                    }
                }
            }
            // Write current byte if not part of escaping logic
            baos.write(pureData[i]);
        }

        // This byte array should contained DATA section with DLE filtered
        byte[] unConvertedDataArray = baos.toByteArray();

        // Debugging: Print results
        System.out.println("\nWhole frame from controller is: " + bytesToHex(frame));
        System.out.println("Whole frame from controller size: " + frame.length);
        System.out.println("CMD1: " + cmd1 + ", CMD2: " + cmd2);
        System.out.println("Pure Data Length: " + pureData.length);
        System.out.println("Unconverted Data Array Length: " + unConvertedDataArray.length);
        System.out.println("Unconverted Data (Hex): " + bytesToHex(unConvertedDataArray));

        // Processing data...
        if (cmd1 == ACK_SEND_PROG_PART[0] && cmd2 == ACK_SEND_PROG_PART[1]) {
            byte firstByte = (byte) unConvertedDataArray[1];
            byte secondByte = (byte) unConvertedDataArray[0];

            sendProgramPartACK = ((firstByte & 0xFF) << 8) | (secondByte & 0xFF);
            Log.i(TAG, "Updated sendProgramPartACK to: " + sendProgramPartACK);
        }
        else if (cmd1 == ACK_SENT_PROG[0] && cmd2 == ACK_SENT_PROG[1]) {
            byte firstByte = (byte) unConvertedDataArray[1];
            byte secondByte = (byte) unConvertedDataArray[0];

            sendProgramEndPartACK = ((firstByte & 0xFF) << 8) | (secondByte & 0xFF);
            Log.i(TAG, "Updated sendProgramEndPartACK to: " + sendProgramEndPartACK);
        }
        else if (cmd1 == COMPILE_RESULT[0] && cmd2 == COMPILE_RESULT[1]) {
            String convertedData = byteToHex(unConvertedDataArray, 0);
            System.out.println("Compile result in hex: " + convertedData);
            if (convertedData.equals("0x59")) {
                compileResult = "OK";
            } else if (convertedData.equals("0x4E")) {
                compileResult = "Error";
            } else {
                compileResult = "Invalid Compile result !";
            }
        }
        else if (cmd1 == UPDATE_PROG_STATE[0] && cmd2 == UPDATE_PROG_STATE[1]) {
            String convertedData = byteToHex(unConvertedDataArray, 0);
            System.out.println("Program state in hex: " + convertedData);
            if (convertedData.equals("0x03")) {
                programRunningStatus = "Running";
            } else if (convertedData.equals("0x02")) {
                programRunningStatus = "Stopped";
            } else {
                programRunningStatus = "Invalid Program state !";
            }
        }
        else if (cmd1 == UPDATE_REG_DATA[0] && cmd2 == UPDATE_REG_DATA[1]) {
            try {
                // Validate frame size
                if (calculateExpectedSize() != unConvertedDataArray.length) {
                    regularDataStatus = "Wrong configuration! Data size not correct";
                    System.out.println("Wrong configuration! Data size not correct. Expected frame size is: " + calculateExpectedSize());
                    return;
                }

                regularDataStatus = "Good";

                // Define current array element position to follow for extracting
                int indexPointer = 0;

                // Num Axis (1 byte)
                numAxisReal = unConvertedDataArray[0];
                indexPointer++;

                // Controller Error Code (1 byte)
                ctrlErrorCode = String.format("%02X", unConvertedDataArray[indexPointer] & 0xFF);
                indexPointer++;

                // Axis Error Codes (numAxis * 2 byte)
                axesErrCodeList.clear();
                for (int i = indexPointer; i < indexPointer + numAxisReal * 2; i += 2) {
                    axesErrCodeList.add(Integer.toHexString(readUINT16_T(unConvertedDataArray[i], unConvertedDataArray[i + 1])));
                }
                indexPointer += numAxisReal * 2;

                // Axis Status Words (numAxis * 4 byte)
                axisStatusWordList.clear();
                for (int i = indexPointer; i < indexPointer + numAxisReal * 4; i += 4) {
                    byte[] statusWordBytes = Arrays.copyOfRange(unConvertedDataArray, i, i + 4);
                    axisStatusWordList.add(String.valueOf(readUINT32_T(statusWordBytes[0], statusWordBytes[1], statusWordBytes[2], statusWordBytes[3])));
                }
                indexPointer += numAxisReal * 4;

                // Axis Control Words (numAxis * 4 byte)
                axisControlWordList.clear(); // New list for Axis Control Word
                for (int i = indexPointer; i < indexPointer + numAxisReal * 4; i += 4) {
                    byte[] controlWordBytes = Arrays.copyOfRange(unConvertedDataArray, i, i + 4);
                    axisControlWordList.add((String.valueOf(readUINT32_T(controlWordBytes[0], controlWordBytes[1], controlWordBytes[2], controlWordBytes[3]))));
                }
                indexPointer += numAxisReal * 4;

                // Actual Axis Positions (numAxis * 4 byte)
                actualAxesPosList.clear();
                for (int i = indexPointer; i < indexPointer + numAxisReal * 4; i += 4) {
                    byte[] actualPosBytes = Arrays.copyOfRange(unConvertedDataArray, i, i + 4);
                    actualAxesPosList.add(Integer.toString(readINT32_T(actualPosBytes)));
                }
                indexPointer += numAxisReal * 4;

                // TCP Positions (6 * 4 byte)
                tcpPosList.clear();
                for (int i = indexPointer; i < indexPointer + 6 * 4; i += 4) {
                    byte[] tcpPosBytes = Arrays.copyOfRange(unConvertedDataArray, i, i + 4);

                    float tcpPosValue = readFLOAT(tcpPosBytes);
                    tcpPosList.add(String.format(Locale.US, "%.2f", tcpPosValue));

//                    tcpPosList.add(String.valueOf(readFLOAT(tcpPosBytes)));
                }
                indexPointer += 6 * 4;

                // Joint Positions (numAxis * 4 byte)
                jointPosList.clear();
                for (int i = indexPointer; i < indexPointer + numAxisReal * 4; i += 4) {
                    byte[] jointPosBytes = Arrays.copyOfRange(unConvertedDataArray, i, i + 4);

                    float jointPosValue = readFLOAT(jointPosBytes);
                    jointPosList.add(String.format(Locale.US, "%.2f", jointPosValue));

//                    jointPosList.add(String.valueOf(readFLOAT(jointPosBytes)));
                }
                indexPointer += numAxisReal * 4;

                // System Input States (2 * 1 byte)
                systemInputValueList.clear(); // New list for Input States
                for (int i = indexPointer; i < indexPointer + 2 * 1; i++) {
                    systemInputValueList.add(String.valueOf(unConvertedDataArray[i] & 0xFF));
                }
                indexPointer += 2;

                // System Output States (2 * 1 byte)
                systemOutputValueList.clear(); // New list for Output States
                for (int i = indexPointer; i < indexPointer + 2 * 1; i++) {
                    systemOutputValueList.add(String.valueOf(unConvertedDataArray[i] & 0xFF));
                }
                indexPointer += 2;

                // Input State (module) (numInput * 4 byte)
                byte[] inputStateInt = Arrays.copyOfRange(unConvertedDataArray, indexPointer, indexPointer + numInput * 4);
                StringBuilder binaryInputStringBuilder = new StringBuilder();
                for (byte b : inputStateInt) {
                    binaryInputStringBuilder.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
                }
                String binaryInputString = binaryInputStringBuilder.toString();
                for (int i = 0; i < 32; i++) {
                    inputState[i] = binaryInputString.charAt(i) == '1';
                }
                // Inverse each input group
                invertGroups(inputState);
                indexPointer += numInput * 4;

                System.out.println("Returned input value: " + Arrays.toString(inputState));

                // Output State (module) (numOutput * 4 byte)
                byte[] outputStateInt = Arrays.copyOfRange(unConvertedDataArray, indexPointer, indexPointer + numOutput * 4);
                StringBuilder binaryOutputStringBuilder = new StringBuilder();
                for (byte b : outputStateInt) {
                    binaryOutputStringBuilder.append(String.format("%8s", Integer.toBinaryString(b & 0xFF)).replace(' ', '0'));
                }
                String binaryOutputString = binaryOutputStringBuilder.toString();
                for (int i = 0; i < 32; i++) {
                    outputState[i] = binaryOutputString.charAt(i) == '1';
                }
                // Inverse each output group
                invertGroups(outputState);
                indexPointer += numOutput * 4;

                System.out.println("Returned output value: " + Arrays.toString(outputState));

                // Counter State (module) (numCounter * 4 byte)
                //

                // UID (1 byte)

                // Update
                setUpdateRegData();
                updateErrorCode();

                // You would now assert or print results based on your lists
                System.out.println("Num Axis Real: " + numAxisReal);
                Log.i(TAG, "Ctrl Error code: " + ctrlErrorCode);
                Log.i(TAG, "Axis Error Codes: " + axesErrCodeList);
                System.out.println("Axis Status: " + axisStatusWordList);
                System.out.println("Axis Ctrl: " + axisControlWordList);
                System.out.println("Actual Axis Pos: " + actualAxesPosList);
                System.out.println("TCP Pos: " + tcpPosList);
                System.out.println("Joint Pos: " + jointPosList);
                System.out.println("System In: " + systemInputValueList);
                System.out.println("System Out: " + systemOutputValueList);
                System.out.println("Input States: " + Arrays.toString(inputState));
                System.out.println("Output States: " + Arrays.toString(outputState));
            }
            catch (IndexOutOfBoundsException e1) {
                Log.i(TAG, "Index out of bound: " + e1.getMessage());
            }
            catch (Exception e2) {
                System.out.println("General error processing frame: " + e2.getMessage());
            }
        }
        else if (cmd1 == REBOOT_CONFIRM[0] && cmd2 == REBOOT_CONFIRM[1]) {
            String convertedData = byteToHex(unConvertedDataArray, 0);
            System.out.println("Compile result in hex: " + convertedData);
            if (convertedData.equals("?")) {
                rebootQuestion = "OK";
            } else {
                compileResult = "Invalid reboot question !";
            }
        }
        else if (cmd1 == IS_ALIVE[0] && cmd2 == IS_ALIVE[1]) {
            Log.i(TAG, "Received: PING message ALIVE");
        }
    }

    // Add STX at beginning, ETX at end and escape DLE in data section
    public static byte[] refineSendFrame(byte[] frame) {
        List<Byte> refinedFrameList = new ArrayList<>();
        // Add STX to the beginning of frame
        refinedFrameList.add(STX);

        // Start from second byte and end at (last - 1) byte
        for (int i = 1; i < frame.length - 1; i++) {
            if (frame[i] == STX || frame[i] == ETX) {
                refinedFrameList.add(DLE);
            }
            refinedFrameList.add(frame[i]);
        }

        // Add ETX to the end of frame
        refinedFrameList.add(ETX);

        byte[] refinedFrame = new byte[refinedFrameList.size()];

        for (int i = 0; i < refinedFrameList.size(); i++) {
            refinedFrame[i] = refinedFrameList.get(i);
        }

        return refinedFrame;
    }

    // Invert Input/Output value
    public static void invertGroups(boolean[] array) {
        if (array.length != 32) {
            throw new IllegalArgumentException("Array must contain exactly 32 elements.");
        }

        int groupSize = 8; // Each group has 8 elements

        for (int group = 0; group < 4; group++) { // Iterate through 4 groups
            int start = group * groupSize; // Starting index of the group
            int end = start + groupSize - 1; // Ending index of the group

            while (start < end) {
                // Swap elements at start and end
                boolean temp = array[start];
                array[start] = array[end];
                array[end] = temp;

                // Move start forward and end backward
                start++;
                end--;
            }
        }
    }

    // DEBUG
    public static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02X ", b));
        }
        return sb.toString().trim();
    }

    public static void setUpdateRegData() {
        // Axis Error Code
        if (!axesErrCodeList.isEmpty()) axis1ErrCode = axesErrCodeList.get(0);
        if (axesErrCodeList.size() > 1) axis2ErrCode = axesErrCodeList.get(1);
        if (axesErrCodeList.size() > 2) axis3ErrCode = axesErrCodeList.get(2);
        if (axesErrCodeList.size() > 3) axis4ErrCode = axesErrCodeList.get(3);
        if (axesErrCodeList.size() > 4) axis5ErrCode = axesErrCodeList.get(4);
        if (axesErrCodeList.size() > 5) axis6ErrCode = axesErrCodeList.get(5);

        // Actual Axis Position
        if (!actualAxesPosList.isEmpty()) axis1Pos = actualAxesPosList.get(0);
        if (actualAxesPosList.size() > 1) axis2Pos = actualAxesPosList.get(1);
        if (actualAxesPosList.size() > 2) axis3Pos = actualAxesPosList.get(2);
        if (actualAxesPosList.size() > 3) axis4Pos = actualAxesPosList.get(3);
        if (actualAxesPosList.size() > 4) axis5Pos = actualAxesPosList.get(4);
        if (actualAxesPosList.size() > 5) axis6Pos = actualAxesPosList.get(5);

        // TCP Position
        xPos = tcpPosList.get(0);
        yPos = tcpPosList.get(1);
        zPos = tcpPosList.get(2);
        rX = tcpPosList.get(3);
        rY = tcpPosList.get(4);
        rZ = tcpPosList.get(5);

        // Joint Position
        if (!jointPosList.isEmpty()) joint1Pos = jointPosList.get(0);
        if (jointPosList.size() > 1) joint2Pos = jointPosList.get(1);
        if (jointPosList.size() > 2) joint3Pos = jointPosList.get(2);
        if (jointPosList.size() > 3) joint4Pos = jointPosList.get(3);
        if (jointPosList.size() > 4) joint5Pos = jointPosList.get(4);
        if (jointPosList.size() > 5) joint6Pos = jointPosList.get(5);
    }

    public static void updateErrorCode() {
        if (errorListener == null) {
            Log.i(TAG, "errorListener is null");
            return;
        }

        // Notify controller error code
        if (!("00").equals(ctrlErrorCode)) {
            notifyErrorReceived(ctrlErrorCode, 0);
        }

        // Notify servo axes code
        if (!containErrorCode(axesErrCodeList)) {
            for (int i = 0; i < axesErrCodeList.size(); i++) {
                if (!("00").equals(axesErrCodeList.get(i))) {
                    notifyErrorReceived(axesErrCodeList.get(i), i + 1);
                }
            }
        }

    }

    public static int calculateExpectedSize() {
        int size = 0;

        // numAxis (1 byte)
        size += 1;

        // Controller Error Code (1 byte)
        size += 1;

        // Axis Error Code (numAxis, 2 bytes each)
        size += numAxis * 2;

        // Axis Status Word
        size += numAxis * 4;

        // Axis Control Word
        size += numAxis * 4;

        // Actual Axis Position (numAxis, 4 bytes each)
        size += numAxis * 4;

        // Robot TCP Position (6 elements, 4 bytes each)
        size += 6 * 4;

        // Joint Position (numAxis, 4 bytes each)
        size += numAxis * 4;

        // System input value
        size += 2 * 1;

        // System output value
        size += 2 * 1;

        // Input Values (2 inputs, 4 bytes each)
        size += numInput * 4;

        // Output Values (2 outputs, 4 bytes each)
        size += numOutput * 4;

        // Counter Value (0 counter, 4 bytes)
        size += numCounter * 4;

        // UID
        size += 1;

        return size;
    }

    public static boolean containErrorCode(List<String> strings) {
        return strings.stream().anyMatch(s -> s.contains("00"));
    }

    @Override
    public void onBTFrameReceived(BluetoothDevice device, byte[] frame) {
        if (!btSendTimes.isEmpty()) {
            try {
                long sendTime = btSendTimes.poll();
                long receiveTime = System.currentTimeMillis();
                btFrameTracker.log(btFrameIdCounter.getAndIncrement(), sendTime, receiveTime, frame.length);
            } catch (NullPointerException e) {
                Log.e(TAG, "EXCEPTION: with btSendTimes queue: " + e);
            }

        }
        process(frame);
    }

    @Override
    public void onTCPFrameReceived(byte[] frame) {
        if (!tcpSendTimes.isEmpty()) {
            try {
                long sendTime = tcpSendTimes.poll();
                long receiveTime = System.currentTimeMillis();
                tcpFrameTracker.log(tcpFrameIdCounter.getAndIncrement(), sendTime, receiveTime, frame.length);
            } catch (NullPointerException e) {
                Log.e(TAG, "EXCEPTION: with tcpSendTimes queue: " + e);
            }

        }
        process(frame);
    }
}
