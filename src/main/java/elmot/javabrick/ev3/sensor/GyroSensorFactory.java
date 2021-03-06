package elmot.javabrick.ev3.sensor;

import elmot.javabrick.ev3.EV3;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import static org.apache.commons.lang3.ObjectUtils.mode;

/**
 * @author elmot
 */
public class GyroSensorFactory extends SensorFactory {
    public GyroSensorFactory(EV3 brick) {
        super(brick);
    }

    /*
    public void setMode(int daisyChainLevel, Port port, MODE mode) throws IOException {
        setMode(daisyChainLevel, port, mode.val);
        this.mode = mode;
    }
*/


    public float read(int daisyChainLevel, Port port) throws IOException {
        return readRaw(daisyChainLevel, port);
    }

    public float readSI(int daisyChainLevel, Port port, MODE mode) throws IOException    {
        return readSI(daisyChainLevel, port, mode.val);
    }

    public enum MODE implements Mode {
        ANGLE(0), // in degrees
        RATE(1),  // in degrees per second
        FAS(2);

        private final int val;

        private MODE(int val)
        {
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
    
    
    @Override
    public List<? extends Mode> getModes() {
        return Arrays.asList(MODE.values());
    }

    @Override
    public Mode decodeMode(String modename) {
        return MODE.valueOf(modename);
    }

    @Override
    public Object readValue(Port port) throws IOException {
        return readSI(0, port, getModeId());
    }
}
