package elmot.javabrick.ev3.sensor;

import elmot.javabrick.ev3.EV3;

import java.io.IOException;

/**
 * @author elmot
 */
public class SoundSensorFactory extends SensorFactory {

    public float read(int daisyChainLevel, Port port, SOUND_MODE mode) throws IOException {
        return getRead(daisyChainLevel, port, (byte) mode.val);
    }

    public enum SOUND_MODE {
        SOUND_DB(0),
        SOUND_DBA(1);

        private final int val;

        private SOUND_MODE(int val) {
            this.val = val;
        }
    }

    public SoundSensorFactory(EV3 brick) {
        super(brick);
    }

}
