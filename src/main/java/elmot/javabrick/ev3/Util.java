/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elmot.javabrick.ev3;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

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
}
