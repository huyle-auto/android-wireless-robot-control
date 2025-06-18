package com.example.myrobotapp;

import static com.example.myrobotapp.Class.ProtocolReceive.actualAxesPosList;
import static com.example.myrobotapp.Class.ProtocolReceive.axesErrCodeList;
import static com.example.myrobotapp.Class.ProtocolReceive.axisControlWordList;
import static com.example.myrobotapp.Class.ProtocolReceive.axisStatusWordList;
import static com.example.myrobotapp.Class.ProtocolReceive.inputState;
import static com.example.myrobotapp.Class.ProtocolReceive.jointPosList;
import static com.example.myrobotapp.Class.ProtocolReceive.outputState;
import static com.example.myrobotapp.Class.ProtocolReceive.process;
import static com.example.myrobotapp.Class.ProtocolReceive.systemInputValueList;
import static com.example.myrobotapp.Class.ProtocolReceive.systemOutputValueList;
import static com.example.myrobotapp.Class.ProtocolReceive.tcpPosList;

import org.junit.Test;

import static org.junit.Assert.*;

import com.example.myrobotapp.Class.ProtocolReceive;

import java.util.Arrays;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void addition_isCorrect() {
        assertEquals(4, 2 + 2);
    }

    @Test
    public void testDecodeStatusFrame() {
        // Sample values (assuming numAxis = 2, numInput = 1, numOutput = 1, numCounter = 1)
        // Total frame layout based on your structure:
        // 1 + 1 + (2*2) + (2*4) + (2*4) + (2*4) + (6*4) + (2*4) + 2 + 2 + (1*4) + (1*4) + (1*4)
        // = 1 + 1 + 4 + 8 + 8 + 8 + 24 + 8 + 2 + 2 + 4 + 4 + 4 = 78 bytes

        byte[] sampleFrame = new byte[] {
                // NumAxis (1 byte)
                0x02,                           // numAxis = 2

                // Controller Error Code (1 byte)
                0x05,                           // Controller Error Code = 0x05

                // Axis Error Codes (2 axes * 2 bytes)
                0x01, 0x02,                     // Axis 0 Error Code = 0x0201 = 513
                0x03, 0x04,                     // Axis 1 Error Code = 0x0403 = 1027

                // Axis Status Words (2 * 4 bytes)
                0x11, 0x00, 0x00, 0x00,         // Axis 0 Status = 0x00000011 = 17
                0x22, 0x00, 0x00, 0x00,         // Axis 1 Status = 0x00000022 = 34

                // Axis Control Words (2 * 4 bytes)
                0x33, 0x00, 0x00, 0x00,         // Axis 0 Ctrl = 51
                0x44, 0x00, 0x00, 0x00,         // Axis 1 Ctrl = 68

                // Actual Axis Positions (2 * 4 bytes, int32)
                0x10, 0x00, 0x00, 0x00,         // Axis 0 Pos = 16
                0x20, 0x00, 0x00, 0x00,         // Axis 1 Pos = 32

                // TCP Position (6 * 4 bytes, float)
                0x00, 0x00, (byte)0x80, 0x3F,   // 1.0
                0x00, 0x00, 0x00, 0x40,         // 2.0
                0x00, 0x00, 0x40, 0x40,         // 3.0
                0x00, 0x00, (byte) 0x80, 0x40,         // 4.0
                0x00, 0x00, (byte)0xA0, 0x40,   // 5.0
                0x00, 0x00, (byte)0xC0, 0x40,   // 6.0

                // Joint Position (2 * 4 bytes, float)
                0x00, 0x00, (byte)0xA0, 0x40,   // 5.0
                0x00, 0x00, (byte)0xC0, 0x40,   // 6.0

                // System Input State (2 * 1 byte)
                0x00, 0x01,                     // 0x00 and 0x01

                // System Output State (2 * 1 byte)
                0x01, 0x00,                     // 0x01 and 0x00

                // Input Value (1 * 4 bytes)
                (byte)0xF0, 0x0F, 0x00, 0x00,   // binary: 11110000 00001111...

                // Output Value (1 * 4 bytes)
                0x55, (byte)0xAA, 0x00, 0x00,   // binary: 01010101 10101010...

                // Counter Value (1 * 4 bytes)
                0x10, 0x00, 0x00, 0x00          // 16
        };

        // Call your parsing function
        ProtocolReceive.process(sampleFrame);

        // You would now assert or print results based on your lists
        System.out.println("Axis Error Codes: " + axesErrCodeList);     // [513, 1027]
        System.out.println("Axis Status: " + axisStatusWordList);       // [17, 34]
        System.out.println("Axis Ctrl: " + axisControlWordList);        // [51, 68]
        System.out.println("Actual Axis Pos: " + actualAxesPosList);    // [16, 32]
        System.out.println("TCP Pos: " + tcpPosList);                   // [1.0, 2.0, 3.0, 4.0, 5.0, 6.0]
        System.out.println("Joint Pos: " + jointPosList);               // [5.0, 6.0]
        System.out.println("System In: " + systemInputValueList);       // [0, 1]
        System.out.println("System Out: " + systemOutputValueList);     // [1, 0]
        System.out.println("Input States: " + Arrays.toString(inputState));
        System.out.println("Output States: " + Arrays.toString(outputState));
        // ...and so on
    }
}