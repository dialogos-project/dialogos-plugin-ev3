package elmot.javabrick.ev3.usb;

import elmot.javabrick.ev3.EV3;

import javax.usb.*;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.logging.Logger;

public class EV3Usb extends EV3 implements UsbInterfacePolicy {

    public static final boolean SUPPRESS_WARNINGS = true;
    public static final int EV3_USB_BLOCK_SIZE = 1024;
    public static final Logger LOGGER = Logger.getLogger(EV3Usb.class.getName());
    private final UsbInterface brick;
    private byte[] dataBlock = new byte[EV3_USB_BLOCK_SIZE];

    public EV3Usb(UsbInterface brick) {
        this.brick = brick;
    }

    @Override
    public void ensureOpen() throws IOException {
//        if (brick.isActive()) throw new IOException("Brick is not active. Disconnected?");
    }

    @Override
    public void close() throws IOException {
    }

    @Override
    public boolean forceClaim(UsbInterface usbInterface) {
        return true;
    }

    @Override
    public ByteBuffer dataExchange(ByteBuffer command, int expectedSeqNo) throws IOException {
        try {
            brick.claim(this);
            try {
                UsbEndpoint endpointIn = brick.getUsbEndpoint((byte) 0x81);
                UsbEndpoint endpointOut = brick.getUsbEndpoint((byte) 0x1);
                UsbPipe pipeIn = endpointIn.getUsbPipe();
                UsbPipe pipeOut = endpointOut.getUsbPipe();

                pipeOut.open();
                command.rewind();
                command.get(dataBlock, 0, command.limit());

//                System.err.println("\nsend:");  // AKAKAK
//                Util.hexdump(dataBlock, 100);
                try {
                    pipeOut.syncSubmit(dataBlock);
                } finally {
                    pipeOut.close();
                }
                pipeIn.open();
                try {
                    while (true) {
                        pipeIn.syncSubmit(dataBlock);

                        int length = 2 + (0xff & (int) dataBlock[0]) + (dataBlock[1] << 8);

                        // Do we need this? Let's comment out for now. - AK
                        if (length < 3 || length > 1022) {
                            if (!SUPPRESS_WARNINGS) {
                                LOGGER.warning("Garbage in USB queue - skipping");
                            }
                            continue;
                        }

                        ByteBuffer response = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN);
                        response.put(dataBlock, 0, length);

//                        System.err.println("\nreceived:");  // AKAKAK
//                        Ev3.hexdump(dataBlock);
                        int readSeqNo = response.getShort(2);
                        if (readSeqNo != expectedSeqNo) {
                            if (!SUPPRESS_WARNINGS) {
                                LOGGER.warning("Resynch EV3 seq no");
                            }

//                            Ev3.hexdump(dataBlock, 100);
//                            System.err.println();
                            continue;
                        }

                        return response;
                    }
                } finally {
                    pipeIn.close();
                }
            } finally {
                brick.release();
            }
        } catch (UsbException e) {
            throw new IOException(e);
        }
    }
}
