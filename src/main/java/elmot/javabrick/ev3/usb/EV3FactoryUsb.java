package elmot.javabrick.ev3.usb;

import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.Ev3Connector;
import elmot.javabrick.ev3.Ev3Constants;
import elmot.javabrick.ev3.Ev3Descriptor;
import java.io.IOException;

import javax.usb.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author elmot
 */
public class EV3FactoryUsb {

    private static final Pattern USB_LOCATION_PATTERN = Pattern.compile(".*?(\\d+).*?(\\d+).*");

    private static Map<String, UsbInterface> portsToInterfaces = new HashMap<>();

    private static void discoverDevices(UsbHub hub, List<Ev3Descriptor> descriptors) {
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
            try {
                UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
//                System.err.printf("vendor %04x, product %04x (hub=%b)\n", desc.idVendor(), desc.idProduct(), device.isUsbHub()); // AKAKAK

                if (device.isUsbHub()) {
                    discoverDevices((UsbHub) device, descriptors);
                } else if (desc.idVendor() == Ev3Constants.EV3_USB_VENDOR_ID && desc.idProduct() == Ev3Constants.EV3_USB_PRODUCT_ID) {
                    UsbInterface usbInterface = device.getActiveUsbConfiguration().getUsbInterface((byte) 0);

                    EV3Usb inst = new EV3Usb(usbInterface);
                    EV3 brick = new EV3(inst);
                    String brickname = brick.SYSTEM.getBrickName();
                    inst.close();

                    String port = makeUsbLocation(device);
                    portsToInterfaces.put(port, usbInterface);

                    Ev3Descriptor descriptor = new Ev3Descriptor(Ev3Descriptor.ConnectionTypes.USB, port, brickname);
                    descriptors.add(descriptor);
                }
            } catch (SecurityException | IOException ex) {
                Logger.getLogger(EV3FactoryUsb.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    public static void discoverDevices(List<Ev3Descriptor> descriptors) {
        portsToInterfaces.clear();

        try {
            discoverDevices(UsbHostManager.getUsbServices().getRootUsbHub(), descriptors);
        } catch (UsbException | SecurityException ex) {
            Logger.getLogger(EV3FactoryUsb.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Ev3Connector instantiate(Ev3Descriptor descriptor) {
        UsbInterface intf = portsToInterfaces.get(descriptor.getPort());
        Ev3Connector conn = new EV3Usb(intf);
        return conn;
    }

    private static String makeUsbLocation(UsbDevice dev) {
        String baseString = dev.toString();
        Matcher m = USB_LOCATION_PATTERN.matcher(baseString);

        if (m.matches()) {
            return String.format("%s/%s", m.group(1), m.group(2));
        } else {
            return baseString;
        }
    }

    /*
    private static void findDevices(UsbHub hub, short vendorId, short productId, List<Ev3Connector> targetList) {
        //noinspection unchecked
        for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
            UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
            if (desc.idVendor() == vendorId && desc.idProduct() == productId) {
                UsbInterface usbInterface = device.getActiveUsbConfiguration().getUsbInterface((byte) 0);
                targetList.add(new EV3Usb(usbInterface));
            }
            if (device.isUsbHub()) {
                findDevices((UsbHub) device, vendorId, productId, targetList);
            }
        }
    }

    public EV3FactoryUsb() throws SocketException {
    }

    static public synchronized List<Ev3Connector> listDiscovered() {
        ArrayList<Ev3Connector> ev3s = new ArrayList<Ev3Connector>();
        try {
            findDevices(UsbHostManager.getUsbServices().getRootUsbHub(), (short) 0x0694, (short) 0x0005, ev3s);
        } catch (UsbException e) {
            throw new RuntimeException(e);
        }
        return ev3s;
    }
*/

    public static void main(String[] args) throws IOException {
        List<Ev3Descriptor> descriptors = new ArrayList<>();
        discoverDevices(descriptors);
        
        String x=descriptors.get(0).instantiate().SYSTEM.getBrickName();
        System.out.println(x);
    }
}
