/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elmot.javabrick.ev3;

import com.clt.lego.SerialPort;
import com.clt.lego.ev3.Ev3Descriptor;
import com.clt.util.Platform;
import elmot.javabrick.ev3.impl.Ev3Bluetooth;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author koller
 */
public class Ev3FactoryBluetooth {
//    private static List<String> ports = new ArrayList<>();

    private static final String EV3_TTY_MAC = "tty.EV3-SerialPort";

    public static void discoverDevices(List<Ev3Descriptor> descriptors) {
        // TODO - move SerialPort to same package

        for (String port : SerialPort.getAvailablePorts()) {
            if (isPortCandidate(port)) {
                // TODO - check that this SerialPort actually has an EV3 connected
                
                descriptors.add(new Ev3Descriptor(Ev3Descriptor.ConnectionTypes.BLUETOOTH, port));
            }
        }
    }

    private static boolean isPortCandidate(String port) {
        if (Platform.isMac()) {
            return EV3_TTY_MAC.equals(port);
        } else {
            return true;
        }
    }
    
    public static EV3 instantiate(Ev3Descriptor descriptor) throws IOException {
        return new Ev3Bluetooth(descriptor.getPort());
    }

}
