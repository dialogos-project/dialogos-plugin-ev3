package elmot.javabrick.ev3.sensor;

import elmot.javabrick.ev3.EV3;

import java.io.IOException;

/**
 * @author elmot
 */
public class TemperatureSensorFactory extends RawOnlySensorFactory {

    public float read(int daisyChainLevel, Port port) throws IOException {
        return readRaw(daisyChainLevel, port) / 256.0f;
    }

    public TemperatureSensorFactory(EV3 brick) {
        super(brick);
    }
}
