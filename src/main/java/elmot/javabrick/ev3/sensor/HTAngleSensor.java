package elmot.javabrick.ev3.sensor;

import elmot.javabrick.ev3.EV3;

import java.io.IOException;

/**
 * @author elmot
 */
public class HTAngleSensor extends RawOnlySensorFactory {
    public HTAngleSensor(EV3 brick) {
        super(brick);
    }

    public int readAngle(int daisyChainLevel, Port port) throws IOException {
        int b = readRawByte(daisyChainLevel, port);
        if(b < 0) b = 256 + b;
        return b * 2;
    }
}
