package elmot.javabrick.ev3;


import java.io.IOException;

/**
 * @author elmot
 */
public class SystemFactory extends FactoryBase {

    public static final int CMD_SOUND = 0x94;
    public static final int CMD_UI_READ = 0x81;
    public static final int CMD_GET = 0xD3;
    public static final byte SYSTEM_COMMAND_REPLY = (byte) 0x01;

    private static final int SUBCMD_TONE = 1;
    private static final int SUBCMD_GET_VBATT = 1;
    private static final int SUBCMD_GET_IBATT = 2;
    private static final int SUBCMD_GET_BRICKNAME = 0x0D;

    SystemFactory(EV3 ev3) {
        super(ev3);
    }

    public void playTone(int volume, int frequency, int durationMs) throws IOException {
        Command command = new Command(CMD_SOUND);
        command.addByte(SUBCMD_TONE);
        command.addLCX(volume);
        command.addLCX(frequency);
        command.addLCX(durationMs);
        run(command);
    }

    public float getVBatt() throws IOException {
        Command command = new Command(CMD_UI_READ, 4);
        command.addByte(SUBCMD_GET_VBATT);
        command.addShortGlobalVariable(0);
        Response response = run(command, float.class);
        return response.getFloat(0);
    }

    public float getIBatt() throws IOException {
        Command command = new Command(CMD_UI_READ, 4);
        command.addByte(SUBCMD_GET_IBATT);
        command.addShortGlobalVariable(0);
        Response response = run(command, float.class);
        return response.getFloat(0);
    }
    
    public String getBrickName() throws IOException {
        Command command = new Command(CMD_GET, 100);
        command.addByte(SUBCMD_GET_BRICKNAME);
        command.addLCX(64);
        command.addShortGlobalVariable(0);
        Response response = run(command, String.class);
        return response.getString(0);
        
    }
}
