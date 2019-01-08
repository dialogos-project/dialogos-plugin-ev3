package elmot.javabrick.ev3.sensor;

import elmot.javabrick.ev3.EV3;

import java.io.IOException;

/**
 * @author elmot
 */
public class IRSensorFactory extends SensorFactory {

    public enum IR_MODE {
        /// Use the IR sensor as a distance sensor
        PROXIMITY(0),
        /// Use the IR sensor to detect the location of the IR Remote
        SEEK(1),
        /// Use the IR sensor to detect wich Buttons where pressed on the IR Remote
        REMOTE(2);

        private final int val;

        private IR_MODE(int val) {
            this.val = val;
        }
    }

    public void setMode(int daisyChainLevel, Port port, IR_MODE mode) throws IOException {
        setMode(daisyChainLevel, port, mode.val);
    }

    public IRSensorFactory(EV3 ev3) {
        super(ev3);
    }

    public int readProximity(int daisyChainLevel, Port port) throws IOException {
        return getRead(daisyChainLevel, port, (byte) IR_MODE.PROXIMITY.val);
    }

    public int readSeek(int daisyChainLevel, Port port) throws IOException {
        return readRaw(daisyChainLevel, port);
    }
    public int readRemote(int daisyChainLevel, Port port) throws IOException {
        return readRaw(daisyChainLevel, port);
    }
}
