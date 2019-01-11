package elmot.javabrick.ev3.sensor;

import elmot.javabrick.ev3.EV3;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author elmot
 */
public class ColorSensorFactory extends SensorFactory {

    public enum COLOR {
        NONE(0), BLACK(1), BLUE(2), GREEN(3),
        YELLOW(4), RED(5), WHITE(6), BROWN(7);
        private final int val;

        private COLOR(int val) {
            this.val = val;
        }
        private static Map<Integer, COLOR> MAP;

        static {
            MAP = new TreeMap<Integer, COLOR>();
            for (COLOR color : COLOR.values()) {
                MAP.put(color.val, color);
            }
        }

    }

    public enum COLOR_MODE implements Mode {
        /// Use the color sensor to read reflected light
        REFLECTION(0),
        /// Use the color sensor to detect the light intensity
        AMBIENT(1),
        /// Use the color sensor to distinguish between eight different colors
        COLOR(2),
        /// Read the raw value of the reflected light
        RAW(3),
        /// Activate the green light on the color sensor. Only works with the NXT Color sensor
        NXT_GREEN(3),
        /// Activate the green blue on the color sensor. Only works with the NXT Color sensor
        NXT_BLUE(4);

        //Raw(5)
        //RGBRaw (4),
        //ColorCalculated (5,
        private final int val;

        private COLOR_MODE(int val) {
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

    public ColorSensorFactory(EV3 brick) {
        super(brick);
    }

    /*
    public void setMode(int daisyChainLevel, Port port, COLOR_MODE mode) throws IOException {
        setMode(daisyChainLevel, port, mode.val);
    }
*/

    public COLOR getColor(Port port) throws IOException {
        Integer colorIndex = Integer.valueOf(readRaw(0, port));
        COLOR color = COLOR.MAP.get(colorIndex);
        if (color == null) {
            System.err.println("Unexpected color:" + colorIndex);
        }
        return color;
    }

    public String getColorAsString(Port port) throws IOException {
        return getColor(port).name();
    }

    @Override
    public List<? extends Mode> getModes() {
        return Arrays.asList(COLOR_MODE.values());
    }

    @Override
    public Mode decodeMode(String modename) {
        return COLOR_MODE.valueOf(modename);
    }

    @Override
    public Object readValue(Port port) throws IOException {
        return getColor(port);
    }
}
