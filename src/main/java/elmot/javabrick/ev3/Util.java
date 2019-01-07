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

    public static String readString(ByteBuffer buf, char delimiter) {
        ByteBuffer ret = ByteBuffer.allocate(buf.capacity());

        while (true) {
            byte b = buf.get();

            if (b == delimiter) {
                break;
            } else {
                ret.put(b);
            }
        }

        return new String(ret.array());
    }
}
