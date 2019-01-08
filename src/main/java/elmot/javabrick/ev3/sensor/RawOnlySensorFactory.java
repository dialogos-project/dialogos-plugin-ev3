/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elmot.javabrick.ev3.sensor;

import elmot.javabrick.ev3.EV3;
import java.util.Arrays;
import java.util.Collection;

/**
 * A sensor that only has a RAW mode.
 * 
 * @author koller
 */
public class RawOnlySensorFactory extends SensorFactory {
     protected RawOnlySensorFactory(EV3 ev3) {
        super(ev3);
    }
     
    
    public enum MODE implements Mode {
        RAW(0);

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
            return toString();
        }
    }

    @Override
    public Collection<? extends Mode> getModes() {
        return Arrays.asList(MODE.values());
    }

    @Override
    public Mode decodeMode(String modename) {
        return MODE.valueOf(modename);
    }

}
