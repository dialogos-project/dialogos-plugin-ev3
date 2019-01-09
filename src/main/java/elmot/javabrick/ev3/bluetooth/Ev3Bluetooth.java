/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elmot.javabrick.ev3.bluetooth;

import elmot.javabrick.ev3.usb.EV3Usb;
import elmot.javabrick.ev3.EV3;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author koller
 */
public class Ev3Bluetooth extends EV3 {

    private final SerialPort serialPort;

    public Ev3Bluetooth(String port) throws IOException {
        serialPort = new SerialPort(port);
        serialPort.open();
    }

    @Override
    public void ensureOpen() throws IOException {
        // nop for now - let's see if this is needed
    }

    @Override
    public void close() throws IOException {
        serialPort.close();
    }

    @Override
    public ByteBuffer dataExchange(ByteBuffer bytes, int expectedSeqNo) throws IOException {
        bytes.rewind();
        int inputLength = bytes.limit();
        byte[] x = new byte[inputLength];
        bytes.get(x, 0, inputLength);

//        System.err.println("send:");
//        Ev3.hexdump(x);
        // send to brick
        serialPort.getOutputStream().write(x);

        // read response
        byte[] response = new byte[EV3Usb.EV3_USB_BLOCK_SIZE];
        int length = serialPort.getInputStream().read(response);

//        System.err.println("receive:");
//        Ev3.hexdump(response, length);
        ByteBuffer ret = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN);
        ret.put(response, 0, length);

        return ret;
    }
}
