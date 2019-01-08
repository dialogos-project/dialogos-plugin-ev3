package elmot.javabrick.ev3.sensor;

import elmot.javabrick.ev3.EV3;

import java.io.IOException;

/**
 * @author elmot
 */
public class TouchSensorFactory extends SensorFactory {

    public enum TOUCH_MODE {
        BOOL(0), COUNT(1);

        private final int val;

        private TOUCH_MODE(int val) {
            this.val = val;
        }
    }

    public TouchSensorFactory(EV3 brick) {
        super(brick);
    }

    public void setMode(int  daisyChainLevel, Port port, TOUCH_MODE mode) throws IOException {
        setMode(daisyChainLevel, port, mode.val);
    }

    public int getBumps(int daisyChainLevel, Port port) throws IOException
    {
        return (int) readSI(daisyChainLevel,port, TOUCH_MODE.COUNT.val);
    }

    public boolean getTouch(int daisyChainLevel, Port port) throws IOException
    {
        return  readSI(daisyChainLevel,port, TOUCH_MODE.BOOL.val) != 0;
    }

}
