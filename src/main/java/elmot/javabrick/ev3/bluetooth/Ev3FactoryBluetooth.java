/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elmot.javabrick.ev3.bluetooth;

import com.clt.util.Platform;
import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.Ev3Descriptor;
import java.io.IOException;
import java.util.List;

/**
 *
 * @author koller
 */
public class Ev3FactoryBluetooth {

    private static final String EV3_TTY_MAC = "tty.EV3-SerialPort";

    public static void discoverDevices(List<Ev3Descriptor> descriptors) {
        for (String port : SerialPort.getAvailablePorts()) {

            if (isPortCandidate(port)) {
                try {
                    Ev3Bluetooth inst = new Ev3Bluetooth(port);
                    String brickname = inst.SYSTEM.getBrickName();
                    inst.close();

                    if (brickname != null) {
                        descriptors.add(new Ev3Descriptor(Ev3Descriptor.ConnectionTypes.BLUETOOTH, port, brickname));
                    }
                } catch (Exception ex) {
                    // If an Exception occurred while trying to instantiate the EV3, this
                    // is evidence that this serial port doesn't actually have an EV3 connected
                    // to it. So we just skip it.
                    //
                    // NB: The Exception is not necessarily an IOException; it may be an
                    // IndexOutOfBoundsException thrown by CommandBlock#run. Thus we need
                    // to resist the IDE's suggestion to replace "Exception" in the catch clause
                    // by just IOException.
                }
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
