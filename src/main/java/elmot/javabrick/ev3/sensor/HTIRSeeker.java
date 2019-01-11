package elmot.javabrick.ev3.sensor;

import elmot.javabrick.ev3.EV3;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 * @author elmot
 */
public class HTIRSeeker extends SensorFactory {
    public HTIRSeeker(EV3 brick) {
        super(brick);
    }

    /*
    public void setMode(int daisyChainLevel, Port port, MODE mode) throws IOException {
        super.setMode(daisyChainLevel, port, mode.val);
    }
*/

    public int read(int daisyChainLevel, Port port) throws IOException {
        return readRawByte(daisyChainLevel, port);
    }

    public enum MODE implements Mode {
        DIR_DC(0), DIR_AC(1);

        private final int val;

        private MODE(int val) {
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
        return read(0, port);
    }
  }
