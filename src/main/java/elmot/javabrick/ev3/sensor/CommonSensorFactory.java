package elmot.javabrick.ev3.sensor;

import elmot.javabrick.ev3.EV3;

import java.io.IOException;

/**
 * @author elmot
 */
public class CommonSensorFactory extends SensorFactory {

    public int read(int daisyChainLevel, Port port) throws IOException {
        return readRaw(daisyChainLevel, port);
    }

    public CommonSensorFactory(EV3 brick) {
        super(brick);
    }

}
