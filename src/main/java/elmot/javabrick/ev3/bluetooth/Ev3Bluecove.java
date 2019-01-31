/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elmot.javabrick.ev3.bluetooth;

import elmot.javabrick.ev3.EV3;
import static elmot.javabrick.ev3.usb.EV3Usb.LOGGER;
import static elmot.javabrick.ev3.usb.EV3Usb.SUPPRESS_WARNINGS;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import javax.microedition.io.Connector;
import javax.microedition.io.StreamConnection;

/**
 * A bluetooth connection to an EV3 brick using the Bluecove library.
 * <p>
 * 
 * Note: Bluecove connections can be a bit brittle. If you try to open
 * a second Bluecove connection to the brick without closing the first one
 * first, you may experience errors ranging from Java exceptions to
 * hard JVM crashes with segmentation faults. Thus, be sure to close
 * the old connection first before opening another one.
 * 
 * @author koller
 */
public class Ev3Bluecove extends EV3 {

    private StreamConnection c;
    private InputStream in;
    private OutputStream out;

    public Ev3Bluecove(String bluetoothAddress) throws IOException {
        try {
            c = (StreamConnection) Connector.open("btspp://" + bluetoothAddress + ":1");
            in = c.openInputStream();
            out = c.openOutputStream();
        } catch (Exception e) {
            throw new IOException(e);
        }
    }

    @Override
    public void ensureOpen() throws IOException {
    }

    @Override
    public void close() throws IOException {
        in.close();
        out.close();
        c.close();
    }

    @Override
    public ByteBuffer dataExchange(ByteBuffer bytes, int expectedSeqNo) throws IOException {
        bytes.rewind();
        int inputLength = bytes.limit();
        byte[] x = new byte[inputLength];
        bytes.get(x, 0, inputLength);

//        System.err.println("send:");
//        Util.hexdump(x);

        // send to brick
        out.write(x);
        out.flush();

        // read response
        while (true) {
            byte[] response = new byte[256];
            int length = in.read(response);

//            System.err.println("\nreceive:");
//            Util.hexdump(response, length);

            ByteBuffer ret = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN);
            ret.put(response, 0, length);

            int readSeqNo = ret.getShort(2);
            if (readSeqNo != expectedSeqNo) {
                if (!SUPPRESS_WARNINGS) {
                    LOGGER.warning("Resynch EV3 seq no");
                }

                continue;
            }

            return ret;
        }
    }

}
