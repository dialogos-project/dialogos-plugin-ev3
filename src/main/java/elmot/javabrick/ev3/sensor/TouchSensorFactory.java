package elmot.javabrick.ev3.sensor;

import elmot.javabrick.ev3.EV3;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author elmot
 */
public class TouchSensorFactory extends SensorFactory {

    public enum TOUCH_MODE implements Mode {
        BOOL(0), COUNT(1);

        private final int val;

        private TOUCH_MODE(int val) {
            this.val = val;
        }

        @Override
        public int getId() {
            return val;
        }

        @Override
        public String getName() {
            return toString();
        }
    }

    public TouchSensorFactory(EV3 brick) {
        super(brick);
    }

    public void setMode(int daisyChainLevel, Port port, TOUCH_MODE mode) throws IOException {
        setMode(daisyChainLevel, port, mode.val);
    }

    public int getBumps(int daisyChainLevel, Port port) throws IOException {
        return (int) readSI(daisyChainLevel, port, TOUCH_MODE.COUNT.val);
    }

    public boolean getTouch(int daisyChainLevel, Port port) throws IOException {
        return readSI(daisyChainLevel, port, TOUCH_MODE.BOOL.val) != 0;
    }

    @Override
    public List<? extends Mode> getModes() {
        return Arrays.asList(TOUCH_MODE.values());
    }

    @Override
    public Mode decodeMode(String modename) {
        return TOUCH_MODE.valueOf(modename);
    }

    @Override
    public Object readValue(Port port) throws IOException {
        TOUCH_MODE m = TOUCH_MODE.values()[getModeId()];
        
        switch(m) {
            case BOOL:
                return getTouch(0, port);
            case COUNT:
                return getBumps(0, port);
        }
        
        return null;
    }

}
