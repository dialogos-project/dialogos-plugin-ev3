package elmot.javabrick.ev3;

import com.clt.lego.ev3.Ev3Descriptor;
import elmot.javabrick.ev3.impl.Command;
import elmot.javabrick.ev3.impl.FactoryBase;
import elmot.javabrick.ev3.impl.Response;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * @author elmot
 */
public class SystemFactory extends FactoryBase {

    public static final int CMD_SOUND = 0x94;
    public static final int CMD_UI_READ = 0x81;
    public static final byte SYSTEM_COMMAND_REPLY = (byte) 0x01;

    private static final int SUBCMD_TONE = 1;
    private static final int SUBCMD_GET_VBATT = 1;
    private static final int SUBCMD_GET_IBATT = 2;

    private static final byte SYSCMD_LIST_FILES = (byte) 0x99;

    SystemFactory(EV3 ev3) {
        super(ev3);
    }

    public void playTone(int volume, int frequency, int durationMs) throws IOException {
        Command command = new Command(CMD_SOUND);
        command.addByte(SUBCMD_TONE);
        command.addIntConstantParam(volume);
        command.addIntConstantParam(frequency);
        command.addIntConstantParam(durationMs);
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

    private static void putString(String s, ByteBuffer buf) {
        buf.put(s.getBytes(StandardCharsets.US_ASCII)); // path
        buf.put((byte) 0); // zero-terminated string
    }

    private static String readString(ByteBuffer buf, char delimiter) {
        ByteBuffer ret = ByteBuffer.allocate(buf.capacity());

        while (true) {
            byte b = buf.get();

            if (b == delimiter) {
                break;
            } else {
                ret.put(b);
            }
        }

        return new String(ret.array());
    }

    public List<String> listFiles(String path) throws IOException {
        ByteBuffer cmd = ByteBuffer.allocate(50);
        cmd.order(ByteOrder.LITTLE_ENDIAN);

        cmd.putShort((short) 10); // len
        cmd.putShort((short) 1);  // ID
        cmd.put(SYSTEM_COMMAND_REPLY); // cmd type
        cmd.put(SYSCMD_LIST_FILES); // command
        cmd.putShort((short) 1012); // max response length
        putString(path, cmd); // path

        ByteBuffer response = brick.dataExchange(cmd, 1);

        response.rewind();
        int messageLength = response.getShort(); // # bytes in message
        
        response.position(6);
        int status = response.get(); // return status
        int length = response.getInt(); // total length of filenames
        int handle = response.get(); // skip handle
        
        List<String> ret = new ArrayList<>();
        while( response.position() < messageLength ) { // TODO finetuning
            ret.add(readString(response, '\n'));
        }
        
        // TODO also do CONTINUE_LIST_FILES
        // TODO deal with empty directories
        
        return ret;
    }

    public static void main(String[] args) throws IOException {
        Ev3Descriptor.discoverAll();
        List<Ev3Descriptor> availableBricks = Ev3Descriptor.getAllDescriptors();
        EV3 brick = availableBricks.get(0).instantiate();
        
        SystemFactory sf = new SystemFactory(brick);
        List<String> files = sf.listFiles("/");
        System.out.println(files);

    }
}
