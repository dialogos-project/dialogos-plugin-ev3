/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clt.lego.ev3;

import com.clt.lego.SerialPort;
import static com.clt.lego.ev3.Ev3.hexdump;
import com.clt.util.Platform;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author koller
 */
public class BluetoothCommInterface implements CommInterface{
    private static final String EV3_TTY_MAC = "tty.EV3-SerialPort";
    private static final boolean DEBUG = true;
    
    private SerialPort serialPort;

    public BluetoothCommInterface() throws IOException {
        portEnumerationLoop:
        for (String port : SerialPort.getAvailablePorts()) {
            if( isPortCandidate(port)) {
                try {
                    SerialPort p = new SerialPort(port);
                    p.openForEv3();
                    
                    // found EV3
                    serialPort = p;
                    break portEnumerationLoop;
                } catch (IOException ex) {
                    Logger.getLogger(BluetoothCommInterface.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        if( serialPort == null ) {
            throw new IOException("Couldn't connect to an EV3 brick over Blueooth.");
        }
    }
    
    private static boolean isPortCandidate(String port) {
        if( Platform.isMac() ) {
            return EV3_TTY_MAC.equals(port);
        } else {
            return true;
        }
    }
    
    @Override
    public void send(byte[] data) throws IOException {
        if(DEBUG) {
            System.err.println("send:");
            hexdump(data);
        }
        
        serialPort.getOutputStream().write(data);
        
        if(DEBUG) {
            System.err.println(" - sent.");
        }
    }

    @Override
    public byte[] read(int expectedLength) throws IOException {
//        int responseLength = (int) BrickUtils.readNum(serialPort.getInputStream(), 2, false);
        byte[] response = new byte[expectedLength];
        
        if(DEBUG) {
            System.err.printf("read %d bytes ...\n", expectedLength);
        }
        
        int length = serialPort.getInputStream().read(response);
        
        if(DEBUG) {
            System.err.printf("read %d bytes:\n", length);
            hexdump(response);
        }
        
        if( length != expectedLength ) {
            throw new IOException(String.format("Expected to receive %d bytes, but got %d.", expectedLength, length));
        }

        return response;
    }
}
