package elmot.javabrick.ev3.sensor;

import elmot.javabrick.ev3.EV3;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

/**
 * @author elmot
 */
public class UltrasonicSensorFactory extends SensorFactory {

    public void setMode(int daisyChainLevel, Port port, ULTRASONIC_MODE mode) throws IOException {
        setMode(daisyChainLevel, port, mode.val);
    }

    public float read(int daisyChainLevel, Port port) throws IOException {
        return readRaw(daisyChainLevel, port);
    }

    public enum ULTRASONIC_MODE implements Mode {
        CM(0),
        INCH(1),
        LISTEN(2);

        private final int val;

        private ULTRASONIC_MODE(int val) {
            this.val = val;
        }

        @Override
        public int getId() {
            return val;
        }

        @Override
        public String getName() {
            return name();
        }
    }

    public UltrasonicSensorFactory(EV3 brick) {
        super(brick);
    }

    public float readSi(int daisyChainLevel, Port port, ULTRASONIC_MODE mode) throws IOException {
        return readSI(daisyChainLevel, port, mode.val);
    }
    
    
    @Override
    public Collection<? extends Mode> getModes() {
        return Arrays.asList(ULTRASONIC_MODE.values());
    }

    @Override
    public Mode decodeMode(String modename) {
        return ULTRASONIC_MODE.valueOf(modename);
    }
}
