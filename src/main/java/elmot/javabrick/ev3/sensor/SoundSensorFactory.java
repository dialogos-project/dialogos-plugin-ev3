package elmot.javabrick.ev3.sensor;

import elmot.javabrick.ev3.EV3;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author elmot
 */
public class SoundSensorFactory extends SensorFactory {

    public float read(int daisyChainLevel, Port port, int modeId) throws IOException {
        return getRead(daisyChainLevel, port, (byte) modeId);
    }
    
    /*
    public float read(int daisyChainLevel, Port port, SOUND_MODE mode) throws IOException {
        return read(daisyChainLevel, port, mode.val);
    }
*/

    public enum SOUND_MODE implements Mode {
        SOUND_DB(0),
        SOUND_DBA(1);

        private final int val;

        private SOUND_MODE(int val) {
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

    public SoundSensorFactory(EV3 brick) {
        super(brick);
    }
    
    
    @Override
    public List<? extends Mode> getModes() {
        return Arrays.asList(SOUND_MODE.values());
    }

    @Override
    public Mode decodeMode(String modename) {
        return SOUND_MODE.valueOf(modename);
    }

    @Override
    public Object readValue(Port port) throws IOException {
        return read(0, port, getModeId());
    }

}
