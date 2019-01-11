package elmot.javabrick.ev3.sensor;

import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.Command;
import elmot.javabrick.ev3.FactoryBase;
import elmot.javabrick.ev3.Response;

import java.io.IOException;
import java.util.List;

/**
 * @author elmot
 */
public abstract class SensorFactory extends FactoryBase {
    public static final int CMD_INPUT_DEVICE = 0x99;
    public static final int CMD_INPUT_READ = 0x9a;
    public static final int CMD_INPUT_READ_SI = 0x9d;
    public static final int CMD_INPUT_READ_EXT = 0x9d;
    public static final int SUBCMD_GET_RAW = 11;

    private static final int SUBCMD_STOP_ALL = 13;
    public static final int SUBCMD_CLR_CHANGES = 26;
    public static final int SUBCMD_READ_SI = 29;
    public static final int SUBCMD_GET_BUMPS = 31;
    
    private int modeId = 0;
    
    protected SensorFactory(EV3 ev3) {
        super(ev3);
    }

    protected float readSI(int daisyChainLevel, Port port, int mode) throws IOException {
        Command command = new Command(CMD_INPUT_READ_SI, 4);
        command.addByte(daisyChainLevel);
        command.addByte(port.portNum);
        command.addIntConstantParam(0);
        command.addByte(mode);
        command.addShortGlobalVariable(0);
        Response response = run(command, float.class);
        return response.getFloat(0);
    }

    public void setMode(int daisyChainLevel, Port port, int mode) throws IOException {
        this.modeId = mode;
        
        Command command = new Command(0x9d, 4);
        command.addByte(daisyChainLevel);
        command.addByte(port.portNum);
        command.addIntConstantParam(0);
        command.addByte(mode);
        command.addShortGlobalVariable(0);
        run(command);
    }
    
    protected int getModeId() {
        return modeId;
    }
    
    protected Mode getMode() {
        return getModes().get(getModeId());
    }

    public void stopAll(int daisyLevel) throws IOException {
        Command command = new Command(CMD_INPUT_DEVICE, 0);
        command.addByte(SUBCMD_STOP_ALL);
        command.addByte(daisyLevel);
        run(command);
    }

    protected int readRaw(int daisyLevel, Port port) throws IOException {
        Command command = new Command(CMD_INPUT_DEVICE, 4);
        command.addByte(SUBCMD_GET_RAW);
        command.addByte(daisyLevel);
        command.addByte(port.portNum);
        command.addShortGlobalVariable(0);
        Response response = run(command, int.class);
        return response.getInt(0);
    }

    protected int readRawByte(int daisyLevel, Port port) throws IOException {
        Command command = new Command(CMD_INPUT_DEVICE, 4);
        command.addByte(SUBCMD_GET_RAW);
        command.addByte(daisyLevel);
        command.addByte(port.portNum);
        command.addShortGlobalVariable(0);
        Response response = run(command, byte.class);
        return response.getByte(0);
    }

    public void clearChanges(int daisyLevel, Port port) throws IOException {
        Command command = new Command(CMD_INPUT_DEVICE, 4);
        command.addByte(SUBCMD_CLR_CHANGES);
        command.addByte(daisyLevel);
        command.addByte(port.portNum);
        run(command);
    }

    protected int getRead(int daisyChainLevel, Port port, byte mode) throws IOException {
        Command command = new Command(CMD_INPUT_READ, 4);
        command.addByte(daisyChainLevel);
        command.addByte(port.portNum);
        command.addIntConstantParam(0);
        command.addByte(mode);
        command.addShortGlobalVariable(0);
        Response run = run(command, byte.class);
        return run.getInt(0);
    }

    public Mode decodeMode(int modeId) {
        for (Mode mode : getModes()) {
            if (mode.getId() == modeId) {
                return mode;
            }
        }

        return null;
    }

    public abstract List<? extends Mode> getModes();
    public abstract Mode decodeMode(String modename);

    /**
     * Returns a value for a sensor of this type at the
     * given port. The type of value depends on the sensor
     * and on the mode to which it was set using prior calls
     * to {@link #setMode(int, elmot.javabrick.ev3.sensor.Port, int) }.
     * 
     * @param port
     * @return
     * @throws IOException 
     */
    public abstract Object readValue(Port port) throws IOException;
}
