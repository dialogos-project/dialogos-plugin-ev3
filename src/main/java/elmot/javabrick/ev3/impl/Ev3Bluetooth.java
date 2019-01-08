/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elmot.javabrick.ev3.impl;

import com.clt.lego.SerialPort;
import com.clt.lego.ev3.Ev3;
import com.clt.lego.ev3.Ev3Descriptor;
import elmot.javabrick.ev3.EV3;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 *
 * @author koller
 */
public class Ev3Bluetooth extends EV3 {

    private final String port;
    private final SerialPort serialPort;

    public Ev3Bluetooth(String port) throws IOException {
        this.port = port;
        serialPort = new SerialPort(port);
        serialPort.openForEv3();
    }

    @Override
    public void ensureOpen() throws IOException {
        // nop for now - let's see if this is needed
    }

    @Override
    public void close() throws Exception {
        serialPort.close();
    }

    @Override
    public ByteBuffer dataExchange(ByteBuffer bytes, int expectedSeqNo) throws IOException {
        bytes.rewind();
        int inputLength = bytes.limit();

        // copy bytes to dataToSend at offset 2
        byte[] dataToSend = new byte[inputLength + 2];
        bytes.get(dataToSend, 2, inputLength);

        // write Bluetooth length header
        dataToSend[0] = (byte) (inputLength & 0xFF);
        dataToSend[1] = (byte) (inputLength >>> 8);
        
//        System.err.println("send:");
//        Ev3.hexdump(dataToSend);
        
        byte[] x = new byte[inputLength];
        bytes.rewind();
        bytes.get(x, 0, inputLength);
        
        
        // send to brick
        serialPort.getOutputStream().write(x);
        
        // read response
        byte[] response = new byte[EV3Usb.EV3_USB_BLOCK_SIZE];
        int length = serialPort.getInputStream().read(response);
        
//        System.err.printf("Received response of %d bytes:\n", length);
//        Ev3.hexdump(response, length);
        
        ByteBuffer ret = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN);
        ret.put(response, 0, length); //, 2, length-2);
        
        return ret;
    }
    
    public static void main(String[] args) throws IOException {
        Ev3Descriptor.discoverAll();
        Ev3Descriptor desc = Ev3Descriptor.getAllDescriptors().get(0);
        System.err.println(desc);
        
        EV3 brick = desc.instantiate();
        
        brick.SYSTEM.playTone(50, 440, 500);
    }

}
