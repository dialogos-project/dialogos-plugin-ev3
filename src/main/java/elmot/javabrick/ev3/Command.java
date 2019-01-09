package elmot.javabrick.ev3;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

/**
 * @author elmot
 */
public class Command {

    private static final byte PARAMETER_TYPE_VARIABLE = (byte) 0x40;
    private static final byte VARIABLE_SCOPE_GLOBAL = (byte) 0x20;
    
    private final byte byteCode;
    private final int replyByteCount;
    private int numLocalVariables = 0;
    
    private byte commandType = Ev3Constants.DIRECT_COMMAND_REPLY;

    private final List<byte[]> params = new ArrayList<byte[]>();

    public Command(int byteCode, int replyByteCount) {
        this.byteCode = (byte) byteCode;
        this.replyByteCount = replyByteCount;
    }

    public Command(int byteCode) {
        this(byteCode, 0);
    }

    public int getReplyByteCount() {
        return replyByteCount;
    }

    public byte getType() {
        return commandType;
    }

    public void setType(byte commandType) {
        this.commandType = commandType;
    }

    public void writeTo(ByteBuffer buffer) throws IOException {
        buffer.put((byte) byteCode);
        for (byte[] param : params) {
            buffer.put(param);
        }
    }

    public Command addByte(int val) {
        params.add(new byte[]{(byte) val});
        return this;
    }

    public void addLongOneByte(int val) {
        params.add(new byte[]{(byte) 0x81, (byte) val});
    }

    public void addLongTwoBytes(int val) {
        params.add(new byte[]{(byte) 0x82, (byte) val, (byte) (val >> 8)});
    }
    
    public void addLC2(short val) {
        addLongTwoBytes(byteCode);
    }

    public void addIntFourBytes(int val, byte modifiers) {
        params.add(new byte[]{(byte) 0x83, (byte) val, (byte) (val >> 8), (byte) (val >> 16), (byte) (val >> 24),});
    }

    public void addShortGlobalVariable(int val) {
        byte b = (byte) (PARAMETER_TYPE_VARIABLE | VARIABLE_SCOPE_GLOBAL | (val & 0x1f));
        addByte(b);
    }

    public void addIntConstantParam(int val) {
        addIntFourBytes(val, (byte) 0);
    }
    
    public void addLCX(int value) {
        if( value < 0 ) {
            throw new UnsupportedOperationException("Currently only non-negative constants are supported.");
        }
        
        if( value < 32 ) {
            addByte(value); // LC0
        } else if( value < 128 ) {
            addLongOneByte(value); // LC1            
        } else if( value < 32768 ) {
            addLongTwoBytes(value); // LC2
        } else {
            addIntFourBytes(value, (byte) 0); // LC4
        }
    }
    
    public void addLCS(String s) {
        byte[] data = new byte[s.length()+2];
        data[0] = (byte) 0x84;
        System.arraycopy(s.getBytes(), 0, data, 1, s.length());
        data[data.length-1] = 0;
        
        params.add(data);
    }
    
    public void addLV0(byte value) {
        addByte(PARAMETER_TYPE_VARIABLE | value);
        numLocalVariables++;
    }
    
    public void addLV1(byte value) {
        params.add(new byte[] { (byte) 0xC1, value });
        numLocalVariables++;
    }
    
    public void addLVX(int value) {
        if( value < 32 ) {
            addLV0((byte) value);
        } else if( value < 256 ) {
            addLV1((byte) value);
        } else {
            throw new UnsupportedOperationException("Local variables that require more than one byte of memory are not supported.");
        }
    }

    public int getNumLocalVariables() {
        return numLocalVariables;
    }
}
