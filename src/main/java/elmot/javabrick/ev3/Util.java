/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elmot.javabrick.ev3;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 *
 * @author Alexander
 */
public class Util {
    

    public static void putString(String s, ByteBuffer buf) {
        buf.put(s.getBytes(StandardCharsets.US_ASCII)); // path
        buf.put((byte) 0); // zero-terminated string
    }

    /**
     * Decode string from current position of ByteBuffer.
     * 
     * @param buf
     * @param delimiter
     * @return 
     */
    public static String readString(ByteBuffer buf, char delimiter) {
        ByteBuffer ret = ByteBuffer.allocate(buf.capacity());
        int strlen = 0;

        while (true) {
            byte b = buf.get();

            if (b == delimiter) {
                break;
            } else {
                ret.put(b);
                strlen++;
            }
        }

        byte[] strbuf = new byte[strlen];
        ret.rewind();
        ret.get(strbuf, 0, strlen);
        return new String(strbuf);
    }
    
    /**
     * Decode string from given offset position of ByteBuffer.
     * 
     * @param buf
     * @param offset
     * @param delimiter
     * @return 
     */
    public static String readString(ByteBuffer buf, int offset, char delimiter) {
        ByteBuffer ret = ByteBuffer.allocate(buf.capacity());
        int strlen = 0;

        while (true) {
            byte b = buf.get(offset++);

            if (b == delimiter) {
                break;
            } else {
                ret.put(b);
                strlen++;
            }
        }
        
        byte[] strbuf = new byte[strlen];
        ret.rewind();
        ret.get(strbuf, 0, strlen);
        
        return new String(strbuf);
    }

    public static void hexdump(byte[] data) {
        hexdump(data, Integer.MAX_VALUE);
    }

    public static void hexdump(byte[] data, int limit) {
        int len = Math.min(data.length, limit);
        int numRows = (len + 7) / 8;
        for (int row = 0; row < numRows; row++) {
            int numCols = Math.min(8, len - 8 * row);
            StringBuilder hexPart = new StringBuilder();
            StringBuilder chrPart = new StringBuilder();
            for (int col = 0; col < numCols; col++) {
                hexPart.append(String.format("%02x ", data[8 * row + col]));
                chrPart.append((char) data[8 * row + col]);
            }
            System.err.printf("%-24s %s\n", hexPart.toString(), chrPart.toString());
        }
    }
}
