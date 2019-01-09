/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elmot.javabrick.ev3.bluetooth;

import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.Ev3Descriptor;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.bluetooth.BluetoothStateException;
import javax.bluetooth.DeviceClass;
import javax.bluetooth.DiscoveryAgent;
import javax.bluetooth.DiscoveryListener;
import javax.bluetooth.LocalDevice;
import javax.bluetooth.RemoteDevice;
import javax.bluetooth.ServiceRecord;

/**
 *
 * @author koller
 */
public class Ev3FactoryBluecove {
    public static void discoverDevices(List<Ev3Descriptor> descriptors) {
        final Object inquiryCompletedEvent = new Object();
        
        DiscoveryListener listener = new DiscoveryListener() {
            @Override
            public void deviceDiscovered(RemoteDevice btDevice, DeviceClass cod) {
                try {
                    String port = btDevice.getBluetoothAddress();
                    Ev3Bluecove inst = new Ev3Bluecove(port);
                    String brickname = inst.SYSTEM.getBrickName();
                    inst.close();
                    
                    if (brickname != null) {
                        descriptors.add(new Ev3Descriptor(Ev3Descriptor.ConnectionTypes.BLUECOVE, port, brickname));
                    }
                } catch (IOException ex) {
                    Logger.getLogger(Ev3FactoryBluecove.class.getName()).log(Level.SEVERE, null, ex);
                }
            }

            @Override
            public void inquiryCompleted(int discType) {
                synchronized (inquiryCompletedEvent) {
                    inquiryCompletedEvent.notifyAll();
                }
            }

            @Override
            public void serviceSearchCompleted(int transID, int respCode) {
            }

            @Override
            public void servicesDiscovered(int transID, ServiceRecord[] servRecord) {
            }
        };
        
        synchronized (inquiryCompletedEvent) {
            try {
                boolean started = LocalDevice.getLocalDevice().getDiscoveryAgent().startInquiry(DiscoveryAgent.GIAC, listener);
                
                if (started) {
                    inquiryCompletedEvent.wait();
                }
            } catch (BluetoothStateException | InterruptedException ex) {
                Logger.getLogger(Ev3FactoryBluecove.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
    
    public static EV3 instantiate(Ev3Descriptor descriptor) throws IOException {
        return new Ev3Bluecove(descriptor.getPort());
    }
}

