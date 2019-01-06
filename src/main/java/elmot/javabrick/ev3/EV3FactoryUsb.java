package elmot.javabrick.ev3;

import com.clt.lego.ev3.Ev3Constants;
import com.clt.lego.ev3.Ev3Descriptor;
import elmot.javabrick.ev3.impl.EV3Usb;
import java.io.IOException;

import javax.usb.*;
import java.net.SocketException;
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

    public static void discoverDevices(List<Ev3Descriptor> descriptors) {
        UsbHub hub;
        portsToInterfaces.clear();
        
        try {
            hub = UsbHostManager.getUsbServices().getRootUsbHub();

            for (UsbDevice device : (List<UsbDevice>) hub.getAttachedUsbDevices()) {
                UsbDeviceDescriptor desc = device.getUsbDeviceDescriptor();
                if (desc.idVendor() == Ev3Constants.EV3_USB_VENDOR_ID && desc.idProduct() == Ev3Constants.EV3_USB_PRODUCT_ID) {
                    UsbInterface usbInterface = device.getActiveUsbConfiguration().getUsbInterface((byte) 0);
                    String port = makeUsbLocation(device);
                    portsToInterfaces.put(port, usbInterface);
                    
                    Ev3Descriptor descriptor = new Ev3Descriptor(Ev3Descriptor.ConnectionTypes.USB, port);
                    descriptors.add(descriptor);
                }
            }
        } catch (UsbException | SecurityException ex) {
            Logger.getLogger(EV3FactoryUsb.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static EV3 instantiate(Ev3Descriptor descriptor) {
        UsbInterface intf = portsToInterfaces.get(descriptor.getPort());
        EV3 ret = new EV3Usb(intf);
        return ret;
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

    private static void findDevices(UsbHub hub, short vendorId, short productId, List<EV3> targetList) {
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

    static public synchronized List<EV3> listDiscovered() {
        ArrayList<EV3> ev3s = new ArrayList<EV3>();
        try {
            findDevices(UsbHostManager.getUsbServices().getRootUsbHub(), (short) 0x0694, (short) 0x0005, ev3s);
        } catch (UsbException e) {
            throw new RuntimeException(e);
        }
        return ev3s;
    }
}
