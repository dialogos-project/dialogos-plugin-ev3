/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elmot.javabrick.ev3;

import elmot.javabrick.ev3.bluetooth.Ev3FactoryBluecove;
import elmot.javabrick.ev3.usb.EV3FactoryUsb;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 *
 * @author koller
 */
public class Ev3Descriptor implements Comparable<Ev3Descriptor> {

    public static enum ConnectionTypes {
        USB("USB"),
        BLUECOVE("Bluetooth"),
        WIFI("Wi-Fi"),
        DUMMY("Dummy");

        private String typestr;

        private ConnectionTypes(String typestr) {
            this.typestr = typestr;
        }
    }

    private ConnectionTypes connectionType;
    private String port;
    private String brickname;

    private static List<Ev3Descriptor> allDescriptors = new ArrayList<>();

    public Ev3Descriptor(ConnectionTypes connectionType, String port, String brickname) {
        this.connectionType = connectionType;
        this.port = port;
        this.brickname = brickname;
    }

    public ConnectionTypes getConnectionType() {
        return connectionType;
    }

    public String getPort() {
        return port;
    }

    public EV3 instantiate() throws IOException {
        if( connectionType == null ) {
            throw new UnsupportedOperationException("Unknown connection type.");
        }
        
        switch (connectionType) {
            case USB:
                return EV3FactoryUsb.instantiate(this);
            case BLUECOVE:
                return Ev3FactoryBluecove.instantiate(this);
            default:
                throw new UnsupportedOperationException("Unsupported connection type: " + connectionType.typestr);
        }
    }

    public EV3 instantiateWithRetries(int numConnectionAttempts) throws IOException {
        Exception lastException = null;

        for (int i = 0; i < numConnectionAttempts; i++) {
            System.err.printf("connection attempt %d ...\n", i + 1);

            try {
                EV3 brick = instantiate();

                if (brick != null) {
                    brick.SYSTEM.getBrickName();
                    return brick;
                }
            } catch (Exception e) {
//                System.err.printf("exception on connection attempt %d: %s\n", i + 1, e.getMessage());
                lastException = e;

                try {
                    Thread.sleep(500);
                } catch (InterruptedException ex) {
                }
            }
        }

        if (lastException != null) {
            throw new IOException(lastException);
        } else {
            return null;
        }
    }

    public static void discoverAll() {
        allDescriptors.clear();

        try {
            EV3FactoryUsb.discoverDevices(allDescriptors);
        } catch (Exception e) {
            System.err.println("Exception during USB discovery: " + e.getMessage());
        }

        try {
            Ev3FactoryBluecove.discoverDevices(allDescriptors);
        } catch (Exception e) {
            System.err.println("Exception during Bluetooth discovery: " + e.getMessage());
        }

    }

    public static List<Ev3Descriptor> getAllDescriptors() {
        return allDescriptors;
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", brickname, connectionType.typestr);
    }

    @Override
    public int compareTo(Ev3Descriptor t) {
        return Comparator.comparing(Ev3Descriptor::getConnectionType).thenComparing(Ev3Descriptor::getPort).compare(this, t);
    }
}
