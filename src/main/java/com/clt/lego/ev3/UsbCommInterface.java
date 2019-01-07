/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.clt.lego.ev3;

import elmot.javabrick.ev3.Ev3Constants;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.usb.UsbConfiguration;
import javax.usb.UsbConst;
import javax.usb.UsbDevice;
import javax.usb.UsbDeviceDescriptor;
import javax.usb.UsbDisconnectedException;
import javax.usb.UsbEndpoint;
import javax.usb.UsbException;
import javax.usb.UsbHostManager;
import javax.usb.UsbHub;
import javax.usb.UsbInterface;
import javax.usb.UsbNotActiveException;
import javax.usb.UsbNotClaimedException;
import javax.usb.UsbNotOpenException;
import javax.usb.UsbPipe;
import javax.usb.UsbServices;

/**
 *
 * @author Alexander
 */
public class UsbCommInterface implements CommInterface {
    private static final Pattern USB_LOCATION_PATTERN = Pattern.compile(".*?(\\d+).*?(\\d+).*");
    private static final boolean DEBUG = true;
    private static final int BUFFERSIZE = 1024;
    
    private String port;
    private final UsbDevice device;
    private final UsbInterface iface;
    private static Map<String, UsbDevice> nxtDevices;
    
    
    static {
        // At program startup time, restore the USB devices. This makes it possible
        // to save a dialogue to a file (where the old portname is stored) and then
        // load it again, and run it without having to go to "Find NXT bricks".
        // This will work as long as there is always at most one NXT brick attached
        // over USB, which is then called "USB1". If there are multiple bricks,
        // the user will have to do "Find NXT bricks" manually each time.
        discoverNxtDevices();
    }
    
    public UsbCommInterface(String port) throws IOException {
        this.port = port;
        device = nxtDevices.get(port);

        if (device == null) {
            throw new IOException(Resources.getString("UsbNxtDisconnected"));
        }

        UsbConfiguration configuration = device.getUsbConfiguration((byte) 1);
        if (configuration == null) {
            throw new IOException(Resources.getString("UsbNxtDisconnected"));
        }

        iface = configuration.getUsbInterface((byte) 0);

        try {
            iface.claim();
        } catch (UsbException | UsbNotActiveException | UsbDisconnectedException ex) {
            throw new IOException(Resources.getString("UsbNxtDisconnected"));
        }
    }
    
    private static int roundup(int number, int divisor) {
        if( number % divisor > 0 ) {
            int rest = number/divisor;
            return (rest+1)*divisor;
        } else {
            return number;
        }
    }

    @Override
    public void send(byte[] data) throws IOException {
        UsbEndpoint endpoint = findEndpoint(UsbConst.ENDPOINT_DIRECTION_OUT);
        int fullBufferSize = roundup(data.length, BUFFERSIZE); // cf https://groups.google.com/forum/#!topic/usb4java/Feb2WuIzonA
        byte[] buf = new byte[fullBufferSize];
        System.arraycopy(data, 0, buf, 0, data.length);

        if (endpoint == null) {
            throw new IOException("Could not find endpoint for sending");
        }

        if (DEBUG) {
            System.err.println("send:");
            Ev3.hexdump(buf);
        }

        UsbPipe pipe = endpoint.getUsbPipe();
        try {
            pipe.open();
            int sent = pipe.syncSubmit(buf);
            pipe.close();
            
            
            UsbEndpoint rcvEndpoint =  iface.getUsbEndpoint((byte) 0x81);
            UsbPipe rcvPipe = endpoint.getUsbPipe();
            rcvPipe.open();
            int received = rcvPipe.syncSubmit(buf);
            rcvPipe.close();
            
            System.err.println("rcv");
            Ev3.hexdump(buf);
        } catch (UsbException | UsbNotActiveException | UsbNotClaimedException | UsbDisconnectedException ex) {
            throw new IOException(ex);
        } finally {
            /*
            try {
                pipe.close();
            } catch (UsbException | UsbNotActiveException | UsbNotOpenException | UsbDisconnectedException ex) {
                throw new IOException(ex);
            }
            */
        }
    }

    private UsbEndpoint findEndpoint(byte direction) {
        UsbEndpoint ret = null;

        for (UsbEndpoint ep : (List<UsbEndpoint>) iface.getUsbEndpoints()) {
            if ((ep.getDirection() & UsbConst.ENDPOINT_DIRECTION_MASK) == direction) {
                ret = ep;
            }
        }

        return ret;
    }

    @Override
    public byte[] read(int numBytes) throws IOException {
        UsbEndpoint endpoint = findEndpoint(UsbConst.ENDPOINT_DIRECTION_IN);
        int bufferSize = roundup(numBytes, BUFFERSIZE);

        endpoint = iface.getUsbEndpoint((byte) 0x81);
        System.err.println(endpoint);
        
        if (endpoint == null) {
            throw new IOException("Could not find endpoint for receiving");
        }

        UsbPipe pipe = endpoint.getUsbPipe();
        System.err.println(pipe);
        
        try {
            pipe.open();
            byte[] data = new byte[BUFFERSIZE];
            System.err.println("*****");
            int received = pipe.syncSubmit(data);
            System.err.println("XXXXX");
            
            if( DEBUG ) {
                System.err.println("\n\nreceive:");
                Ev3.hexdump(data);
            }
            
            return data;
        } catch (UsbException | UsbNotActiveException | UsbNotClaimedException | UsbDisconnectedException ex) {
            throw new IOException(ex);
        } finally {
            try {
                pipe.close();
            } catch (UsbException | UsbNotActiveException | UsbNotOpenException | UsbDisconnectedException ex) {
                throw new IOException(ex);
            }
        }
    }
    
    private static void traverseUsbTreeForNxt(UsbHub hub, List<UsbDevice> nxts) {
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
            if (device.isUsbHub()) {
                traverseUsbTreeForNxt((UsbHub) device, nxts);
            } else {
                UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
                if (desc.idVendor() == Ev3Constants.EV3_USB_VENDOR_ID 
                        && desc.idProduct() == Ev3Constants.EV3_USB_PRODUCT_ID) {
                    nxts.add(device);
                }
            }
        }
    }
    
    private static List<UsbDevice> getUsbNxtDevices() throws UsbException {
        List<UsbDevice> ret = new ArrayList<>();
        UsbServices services = UsbHostManager.getUsbServices();
        UsbHub hub = services.getRootUsbHub();

        traverseUsbTreeForNxt(hub, ret);
        return ret;
    }
    
    /**
     * Refresh the list of known attached NXT devices. This removes previously
     * discovered devices if they are no longer attached. Each device is
     * assigned a unique "port" name.
     *
     */
    public static void discoverNxtDevices() {
        nxtDevices = new HashMap<>();

        try {
            List<UsbDevice> nxtDev = getUsbNxtDevices();
            int id = 1;

            for (UsbDevice dev : nxtDev) {
                String port = makeUsbLocation(dev);
                nxtDevices.put(port, dev);
                id++;
            }
        } catch (UsbException ex) {
            Logger.getLogger(UsbCommInterface.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    
    private static String makeUsbLocation(UsbDevice dev) {
        String baseString = dev.toString();
        Matcher m = USB_LOCATION_PATTERN.matcher(baseString);

        if (m.matches()) {
            return String.format("Bus %s, Device %s", m.group(1), m.group(2));
        } else {
            return baseString;
        }
    }
    
    public static List<String> getAvailablePorts() {
        List<String> ret = new ArrayList<>(nxtDevices.keySet());
        return ret;
    }

    
    public static void main(String[] args) throws UsbException, IOException {
        UsbCommInterface.discoverNxtDevices();
        String port = UsbCommInterface.nxtDevices.keySet().iterator().next();
        UsbCommInterface intf = new UsbCommInterface(port);
        System.err.println(intf);
        
        
        
        System.exit(0);
        
        
        UsbServices services = UsbHostManager.getUsbServices();
        UsbHub hub = services.getRootUsbHub();
        
        List<UsbDevice> nxts = new ArrayList<>();
        traverseUsbTreeForNxt(hub, nxts);
        System.err.println(nxts);
        
    }
    
}
