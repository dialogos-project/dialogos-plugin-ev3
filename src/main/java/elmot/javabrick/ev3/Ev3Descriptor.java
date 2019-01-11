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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
        
        Ev3Connector conn = null;
        
        switch (connectionType) {
            case USB:
                conn = EV3FactoryUsb.instantiate(this);
                break;
            case BLUECOVE:
                conn = Ev3FactoryBluecove.instantiate(this);
                break;
            case DUMMY:
                conn = new Ev3Dummy();
                break;
            default:
                throw new UnsupportedOperationException("Unsupported connection type: " + connectionType.typestr);
        }
        
        return new EV3(conn);
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
    
    public String serialize() {
        return String.format("%s:%s:%s", connectionType.name(), brickname, port);
    }
    
    private static final Pattern DESERIALIZATION_PATTERN = Pattern.compile("([^:]*):([^:]*):(.*)");
    
    public static Ev3Descriptor deserialize(String serializedRepresentation) {
        Matcher m = DESERIALIZATION_PATTERN.matcher(serializedRepresentation);
        
        if( m.matches() ) {
            return new Ev3Descriptor(ConnectionTypes.valueOf(m.group(1)), m.group(3), m.group(2));
        } else {
            return null;
        }
    }
}
