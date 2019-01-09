package elmot.javabrick.ev3.bluetooth;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;

import purejavacomm.CommPort;
import purejavacomm.CommPortIdentifier;
import purejavacomm.NoSuchPortException;
import purejavacomm.PortInUseException;
import purejavacomm.UnsupportedCommOperationException;

/**
 * Communication with an external device over a serial connection, such as
 * Bluetooth.
 *
 * @author dabo, koller
 */
public class SerialPort {
    // TODO see if that works reliably, or if it needs to be increased back to 3000
    private static final int CONNECTION_TIMEOUT = 1000;

    // private static final int BAUDRATE_BLUETOOTH = 460800;
//    private static final int BAUDRATE_RCX = 4800;
//    private static final int BAUDRATE_BLUETOOTH = 115200;

    private CommPortIdentifier portIdentifier;
    private purejavacomm.SerialPort serialPort;
    private InputStream in;
    private OutputStream out;

    public SerialPort(String portName) throws IOException {

        try {
            this.portIdentifier = CommPortIdentifier.getPortIdentifier(portName);
        } catch (NoSuchPortException exn) {
            throw new IOException("Unknown port " + portName);
        }

        if (this.portIdentifier.getPortType() != CommPortIdentifier.PORT_SERIAL) {
            throw new IOException(portName + " is not a serial port");
        }
    }

    public String getPortname() {
        return this.portIdentifier.getName();
    }
    
    public void open() throws IOException {
        CommPort port;

        try {
            System.err.println("open");
            port = this.portIdentifier.open(portIdentifier.getName(), CONNECTION_TIMEOUT);
            System.err.println("opened");
        } catch (PortInUseException exn) {
            throw new IOException("Port is already in use by " + portIdentifier.getCurrentOwner());
        }

        if (port != null) {
            if (port instanceof purejavacomm.SerialPort) {
                try {
                    this.serialPort = (purejavacomm.SerialPort) port;
                    serialPort.enableReceiveTimeout(CONNECTION_TIMEOUT);
                    this.in = serialPort.getInputStream();
                    this.out = serialPort.getOutputStream();
                } catch (UnsupportedCommOperationException ex) {
                    throw new IOException(ex);
                }
            } else {
                port.close();
                throw new IOException(portIdentifier.getName() + " is not a serial port.");
            }
        }
    }

    /*
      ** Parameters that were used for NXT:
    
//                    serialPort.setSerialPortParams(baudRate, dataBits, stopBits, parity);
//                    serialPort.enableReceiveTimeout(SerialPort.CONNECTION_TIMEOUT);

    // This allows NXT programs to be run without resetting NXT each time.
//                serialPort.setRTS(true);
//                serialPort.setDTR(true);
     */
    public InputStream getInputStream() {
        return this.in;
    }

    public OutputStream getOutputStream() {
        return this.out;
    }

    public void close() {
        this.serialPort.close();

        try {
            this.in.close();
        } catch (IOException exn) {
            // ignore
        }

        try {
            this.out.close();
        } catch (IOException exn) {
            // ignore
        }
    }

    public static String[] getAvailablePorts() {
        Collection<String> ports = new ArrayList<String>();
        for (Enumeration<?> e = CommPortIdentifier.getPortIdentifiers(); e.hasMoreElements();) {
            CommPortIdentifier info = (CommPortIdentifier) e.nextElement();
            if (info.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                ports.add(info.getName());
            }
        }

        return ports.toArray(new String[ports.size()]);
    }
}
