package elmot.javabrick.ev3.sensor;

import elmot.javabrick.ev3.EV3;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author elmot
 */
public class IRSensorFactory extends SensorFactory {
    public enum IR_MODE implements Mode {
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

        @Override
        public int getId() {
            return val;
        }

        @Override
        public String getName() {
            return name();
        }
    }

    /*
    public void setMode(int daisyChainLevel, Port port, IR_MODE mode) throws IOException {
        setMode(daisyChainLevel, port, mode.val);
        this.mode = mode;
    }
*/

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

    @Override
    public List<? extends Mode> getModes() {
        return Arrays.asList(IR_MODE.values());
    }

    @Override
    public Mode decodeMode(String modename) {
        return IR_MODE.valueOf(modename);
    }

    @Override
    public Object readValue(Port port) throws IOException {
        IR_MODE irm = (IR_MODE) getMode();
        
        switch(irm) {
            case PROXIMITY:
                return readProximity(0, port);
            case REMOTE:
                return readRemote(0, port);
            case SEEK:
                return readSeek(0, port);
        }
        
        return null;
    }

}
