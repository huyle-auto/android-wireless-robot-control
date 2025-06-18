package com.example.myrobotapp.Class;

import static com.example.myrobotapp.Class.FileManager.readFile;

import android.content.Context;
import android.graphics.Path;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.Console;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProtocolSend {
    private static final byte STX = 0x02;  // Start of Text
    private static final byte ETX = 0x03;  // End of Text
    private static final byte DLE = 0x10;  // Data Link Escape

    public enum Operation {
        GET_REG_DATA((byte) 0x00, (byte) 0x19),
        SET_SERVO((byte) 0x00, (byte) 0x03),
        RESET_ERROR((byte) 0x00, (byte) 0x04),
        SET_CONTROL_MODE((byte) 0x00, (byte) 0x0A),
        JOG_MANUAL((byte) 0x00, (byte) 0x0B),
        SET_OUTPUT_VALUE((byte) 0x00, (byte) 0x0E),
        SEND_PROG_PART((byte) 0x00, (byte) 0x12),
        SEND_PROG_END_PART((byte) 0x00, (byte) 0x13),
        COMPILE((byte) 0x00, (byte) 0x14),
        CHECK_COMPILE_RESULT((byte) 0x00, (byte) 0x15),
        RUN_PROG((byte) 0x00, (byte) 0x16),
        STOP_PROG((byte) 0x00, (byte) 0x18),
        REBOOT_1ST_SIG((byte) 0x00, (byte) 0x1A),
        REBOOT_2ND_SIG((byte) 0x00, (byte) 0x1B),
        SAVE_AXES_ZERO_VALUE((byte) 0x00, (byte) 0x1F),
        // PLC_COMM ((byte) 0x00, (byte), 0x20),
        MOVE_TO_POSITION((byte) 0x00, (byte) 0x21),
        SAVE_HOME((byte) 0x00, (byte) 0x22),
        MOVE_HOME((byte) 0x00, (byte) 0x23);

        private final byte cmd1;
        private final byte cmd2;

        Operation(byte cmd1, byte cmd2) {
            this.cmd1 = cmd1;
            this.cmd2 = cmd2;
        }

        public byte[] getCommandBytes() {
            return new byte[]{cmd1, cmd2};
        }
    }

    // CONVERTER FOR C/C++ TO INTERPRET (still signed type after conversion)
    // 32-bit (4 bytes) signed float (Java) to LITTLE ENDIAN 4 bytes float (signed)
    public static byte[] floatToBytes(float value){
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putFloat(value);
        return buffer.array();
    }


    // 64-bit (8 bytes) signed double (Java) to LITTLE ENDIAN 8 bytes integer (signed)
    public static byte[] doubleToBytes (double value){
        ByteBuffer buffer = ByteBuffer.allocate(8);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putDouble(value);
        return buffer.array();
    }

    // 32-bit (4 bytes) signed integer (Java) to LITTLE ENDIAN 4 bytes integer (signed)
    public static byte[] intToBytes (int value){
        ByteBuffer buffer = ByteBuffer.allocate(4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.putInt(value);
        return buffer.array();
    }

    // GET REGULAR DATA + RESET ERROR
    // + COMPILE + CHECK COMPILE RESULT + RUN ROBOT PROG + STOP ROBOT PROG + REBOOT CTRL. 1ST SIGNAL
    public static byte[] operate(Operation operation, ByteArrayOutputStream baos){
        baos.reset();
        // START OF TEXT
        baos.write(STX);
        // COMMAND
        try {
            baos.write(operation.getCommandBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // DATA (NULL)

        // END OF TEXT
        baos.write(ETX);

        return baos.toByteArray();
    }

    // RUN PROGRAM (WITH SPEED)
    public static byte[] operate(Operation operation, ByteArrayOutputStream baos, int programSpeed, String miscel1, String miscel2, String miscel3){
        baos.reset();
        // START OF TEXT
        baos.write(STX);
        // COMMAND
        try {
            baos.write(operation.getCommandBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // DATA
        baos.write((byte) programSpeed);

        // END OF TEXT
        baos.write(ETX);

        return baos.toByteArray();
    }

    // REBOOT CTRL. 2ND SIGNAL
    public static byte[] operate(Operation operation, ByteArrayOutputStream baos, char confirmLetter){
        baos.reset();
        // START OF TEXT
        baos.write(STX);
        // COMMAND
        try {
            baos.write(operation.getCommandBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // DATA
        baos.write((int) confirmLetter & 0xFF); // Confirm Letter: 'Y' (dec. 89) or 'N' (dec. 78)

        // END OF TEXT
        baos.write(ETX);

        return baos.toByteArray();
    }

    // SERVO ON/OFF
    public static byte[] operate(Operation operation, ByteArrayOutputStream baos, int axes, int state){
        baos.reset();
        // START OF TEXT
        baos.write(STX);
        // COMMAND
        try {
            baos.write(operation.getCommandBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // DATA
        baos.write((byte) axes);

        baos.write((byte) (state)); ///dfadsfasdf

        // END OF TEXT
        baos.write(ETX);

        return baos.toByteArray();
    }

    // AUTO/MANUAL (1/2)
    public static byte[] operate(Operation operation, ByteArrayOutputStream baos, int mode){
        baos.reset();
        // START OF TEXT
        baos.write(STX);
        // COMMAND
        try {
            baos.write(operation.getCommandBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // DATA
        // redundant STX/ETX check and DLE addition
//        if ((byte) mode == STX || (byte) mode == ETX){
//            baos.write(DLE);
//            baos.write((byte) (mode & 0xFF));
//        }
//        else{
//
//        }
        baos.write((byte) (mode & 0xFF));
        // END OF TEXT
        baos.write(ETX);

        return baos.toByteArray();
    }

    // MANUAL JOG
    // (1 byte UINT8_T): Coordinate (0x01 = Joint, 0x02 = World, 0x03 = Tool, 0x04 = User)
    // (1 byte UINT8_T): Type (0x01 --> 0x06)
    // (1 byte UINT8_T): Button State (0x01 = Press, 0x02 = Release)
    // (1 byte UINT8_T): Direction (0x01 = Decrement, 0x02 = Increment)
    // (8 byte DOUBLE): Linear (mm/s) | rotation (deg/s)
    public static byte[] operate(Operation operation, ByteArrayOutputStream baos, int coordinate, int axis, int buttonState, int direction, float velocity){
        baos.reset();
        // START OF TEXT
        baos.write(STX);
        // COMMAND
        try {
            baos.write(operation.getCommandBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        //// DATA
        // 1. Coordinate
        baos.write((byte) (coordinate & 0xFF));

        // 2. Axis
        baos.write((byte) (axis & 0xFF));

        // 3. Button State
        baos.write((byte) (buttonState & 0xFF));

        // 4. Direction
        baos.write((byte) (direction & 0xFF));

        // 5. Velocity
        ByteBuffer byteBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putFloat(velocity);
        byte[] velocityArray = byteBuffer.array();

        for (byte b: velocityArray){
            baos.write(b);
        }

        // END OF TEXT
        baos.write(ETX);

        return baos.toByteArray();
    }

    // SAVE AXIS ZERO VALUE
    public static byte[] operate(Operation operation, ByteArrayOutputStream baos, int axisQuantity, int[] zeroValues){
        baos.reset();
        // START OF TEXT
        baos.write(STX);
        // COMMAND
        try {
            baos.write(operation.getCommandBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // DATA
        baos.write((byte) axisQuantity & 0xFF);
        for (int zeroValue : zeroValues) {
            baos.write(zeroValue);
        }

        // END OF TEXT
        baos.write(ETX);

        // Add DLE for byte array and return
        return baos.toByteArray();
    }
    // SAVE ROBOT HOME VALUE

    // MOVE TO POSTION/POINT
    // (1 byte UINT8_T): Coordinate (0x01 = Joint, 0x02 = World, 0x03 = Tool, 0x04 = User)
    // (1 byte UINT8_T): nAxis
    // (4 byte FLOAT): Speed Level (0.01 -> 1.00)
    // (max. 6*8 byte DOUBLE): Target position (Joint/World)

    // MOVE TO HOME
    public static byte[] operate(Operation operation, ByteArrayOutputStream baos, int homePosIndex, float speedLevel){
        baos.reset();
        // START OF TEXT
        baos.write(STX);
        // COMMAND
        try {
            baos.write(operation.getCommandBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // DATA
        baos.write((byte) homePosIndex & 0xFF);
        try {
            ByteBuffer byteBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
            byteBuffer.putFloat(speedLevel);
            byte[] speedLevelArray = byteBuffer.array();    // speed level: 4 byte float

            baos.write(speedLevelArray);
        }
        catch (IOException e){
            System.out.println("Error frame MOVE_HOME: " + e.getMessage());
        }

        // END OF TEXT
        baos.write(ETX);

        // Add DLE for byte array and return
        return baos.toByteArray();
    }

    // MOVE TO POSITION
    public static byte[] operate(Operation operation, ByteArrayOutputStream baos, int coordinateState, float speedLevel, String[] position ){
        baos.reset();
        // START OF TEXT
        baos.write(STX);
        // COMMAND
        try {
            baos.write(operation.getCommandBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // DATA
        baos.write((byte) coordinateState & 0xFF);  // Coordinate
        baos.write((byte) 0x00);                       // nAxis (constant 0x00 as stated in protocol sheet)
        try{
            ByteBuffer byteBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
            byteBuffer.putFloat(speedLevel);
            baos.write(byteBuffer.array());      // Speed Level
            byteBuffer.clear();

            // Position (max 6*4 byte float)
            for (String pos : position){
                byteBuffer.putFloat(Float.parseFloat(pos));
                baos.write(byteBuffer.array());
                byteBuffer.clear();
            }
        }
        catch (IOException e){
            System.out.println("Error frame MOVE_TO_POSITION: " + e.getMessage());
        }

        // END OF TEXT
        baos.write(ETX);

        return baos.toByteArray();
    }

    // SEND PROGRAM PART
    public static byte[] operate (Operation operation, ByteArrayOutputStream baos, int partIndex, String programName, Context context) {
        baos.reset();
        // START OF TEXT
        baos.write(STX);
        // COMMAND
        try {
            baos.write(operation.getCommandBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // DATA
        baos.write((byte) partIndex & 0xFF);    // Program part index (2 byte uint8_t)
        baos.write((partIndex >> 8) & 0xFF);

        // file type
        byte fileType = 0x01;
        baos.write(fileType);

        //      File sending
        try {
            //      1. Find the exact ctrl's file and read it, then break it into parts
            String ctrlFilePath = "Program/" + programName + "/" + programName + "_Ctrl.txt";
            File ctrlFile = new File(context.getFilesDir(), ctrlFilePath);

            // byte[] ctrlFileBytes = Files.readAllBytes(ctrlFile.toPath());
            byte[] ctrlFileBytes = readInternalFile(context, ctrlFilePath);

            //      2. Write bytes to baos, max 200 bytes for each data frame
            int startByteIndex = (partIndex) * 200;
            int endByteIndex = startByteIndex + 199;

            for (int i = startByteIndex; i <= endByteIndex; i++) {
                baos.write(ctrlFileBytes[i]);
            }

        } catch (Exception e) {
            System.out.println("Error finding Ctrl's file: " + e.getMessage());
        }

        // END OF TEXT
        baos.write(ETX);

        return baos.toByteArray();
    }

    // SEND PROGRAM END PART
    public static byte[] operate (Operation operation, ByteArrayOutputStream baos, int endPartIndex, String programName, Context context, String miscellaneous) {
        baos.reset();
        // START OF TEXT
        baos.write(STX);
        // COMMAND
        try {
            baos.write(operation.getCommandBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // DATA
        baos.write((byte) endPartIndex & 0xFF);    // Program part index (2 byte uint8_t)
        baos.write((endPartIndex >> 8) & 0xFF);

        // file type
        byte fileType = 0x01;
        baos.write(fileType);

        //      File sending
        try {
            //      1. Find the exact ctrl's file and read it, then break it into parts
            String ctrlFilePath = "Program/" + programName + "/" + programName + "_Ctrl.txt";
            System.out.println("Currently sending program: " + ctrlFilePath);
            File ctrlFile = new File(context.getFilesDir(), ctrlFilePath);

            byte[] ctrlFileBytes = readInternalFile(context, ctrlFilePath);
            // byte[] ctrlFileBytes = Files.readAllBytes(ctrlFile.toPath());

            //      2. Write bytes to baos, max 200 bytes for each data frame
            int startByteIndex = (endPartIndex) * 200;
            int endByteIndex = ctrlFileBytes.length;

            for (int i = startByteIndex; i < endByteIndex; i++) {
                baos.write(ctrlFileBytes[i]);
            }

            System.out.println("Bytes for the current program part are: " + Arrays.toString(baos.toByteArray()));

        } catch (IOException e) {
            System.out.println("Protocol Send Error finding Ctrl's file: " + e.getMessage());
        }

        // END OF TEXT
        baos.write(ETX);

        return baos.toByteArray();
    }

    // SEND CONFIG PART
    public static byte[] operate (Operation operation, ByteArrayOutputStream baos, int partIndex, String configFileName, Context context, boolean isCustom) {
        baos.reset();
        // START OF TEXT
        baos.write(STX);
        // COMMAND
        try {
            baos.write(operation.getCommandBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // DATA
        baos.write((byte) partIndex & 0xFF);    // Program part index (2 byte uint8_t)
        baos.write((partIndex >> 8) & 0xFF);

        // file type
        byte fileType = 0x02;
        baos.write(fileType);

        //      File sending
        try {
            //      1. Find the exact config file and read it, then break it into parts
            String configFilePath = "ControllerSettings/" + "custom/" + "ControllerConfig.txt";
            byte[] configFileBytes = readInternalFile(context, configFilePath);

            //      2. Write bytes to baos, max 200 bytes for each data frame
            int startByteIndex = (partIndex) * 200;
            int endByteIndex = startByteIndex + 199;

            for (int i = startByteIndex; i <= endByteIndex; i++) {
                baos.write(configFileBytes[i]);
            }

        } catch (Exception e) {
            System.out.println("Error finding configurations file: " + e.getMessage());
        }

        // END OF TEXT
        baos.write(ETX);

        return baos.toByteArray();
    }

    // SEND CONFIG END PART
    public static byte[] operate (Operation operation, ByteArrayOutputStream baos, int endPartIndex, String configFileName, Context context, boolean isCustom, String miscellaneous) {
        baos.reset();
        // START OF TEXT
        baos.write(STX);
        // COMMAND
        try {
            baos.write(operation.getCommandBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // DATA
        baos.write((byte) endPartIndex & 0xFF);    // Program part index (2 byte uint8_t)
        baos.write((endPartIndex >> 8) & 0xFF);

        // file type
        byte fileType = 0x02;
        baos.write(fileType);

        //      File sending
        try {
            //      1. Find the exact ctrl's file and read it, then break it into parts
            String configFilePath = "ControllerSettings/" + "custom/" + "ControllerConfig.txt";
            byte[] configFileBytes = readInternalFile(context, configFilePath);

            //      2. Write bytes to baos, max 200 bytes for each data frame
            int startByteIndex = (endPartIndex) * 200;
            int endByteIndex = configFileBytes.length;

            for (int i = startByteIndex; i < endByteIndex; i++) {
                baos.write(configFileBytes[i]);
            }

            System.out.println("Bytes for the current program part are: " + Arrays.toString(baos.toByteArray()));

        } catch (IOException e) {
            System.out.println("Error finding configurations file: " + e.getMessage());
        }

        // END OF TEXT
        baos.write(ETX);

        return baos.toByteArray();
    }

    // SET OUTPUT VALUE
    public static byte[] operate (Operation operation, ByteArrayOutputStream baos, int group, int value, String anything) {
        baos.reset();
        // START OF TEXT
        baos.write(STX);
        // COMMAND
        try {
            baos.write(operation.getCommandBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // DATA
        baos.write((byte) group & 0xFF);    // group

        ByteBuffer byteBuffer = ByteBuffer.allocate(4);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        byteBuffer.putInt(value);

        try {
            baos.write(byteBuffer.array());
            byteBuffer.clear();
        }
        catch (IOException e) {
            System.out.println("Error setting output value frame");
        }

        // END OF TEXT
        baos.write(ETX);

        return baos.toByteArray();
    }

    // SAVE ROBOT HOME VALUE
    public static byte[] operate(Operation operation, ByteArrayOutputStream baos, int numAxis, String[] home1Position, String[] home2Position ){
        baos.reset();
        // START OF TEXT
        baos.write(STX);
        // COMMAND
        try {
            baos.write(operation.getCommandBytes());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        // DATA
        baos.write((byte) numAxis & 0xFF);  // numAxis (1 byte uint8_t)

        try{
            ByteBuffer byteBuffer = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);

            // HOME 1 POSITION (max 6*4 byte float)
            for (String pos1 : home1Position){
                byteBuffer.putFloat(Float.parseFloat(pos1));
                baos.write(byteBuffer.array());
                byteBuffer.clear();
            }

            // HOME 2 POSITION (max 6*4 byte float)
            for (String pos2 : home2Position){
                byteBuffer.putFloat(Float.parseFloat(pos2));
                baos.write(byteBuffer.array());
                byteBuffer.clear();
            }
        }
        catch (IOException e){
            System.out.println("Error frame MOVE_TO_POSITION: " + e.getMessage());
        }

        // END OF TEXT
        baos.write(ETX);

        return baos.toByteArray();
    }

    public static byte[] readInternalFile(Context context, String fileName) throws IOException {
        File file = new File(context.getFilesDir(), fileName); // Locate the file in internal storage
        FileInputStream fis = new FileInputStream(file);       // Open a stream to the file
        byte[] data = new byte[(int) file.length()];           // Allocate a byte array for the file's size
        fis.read(data);                                        // Read the file into the array
        fis.close();                                           // Close the stream
        return data;                                           // Return the file's content
    }

}
