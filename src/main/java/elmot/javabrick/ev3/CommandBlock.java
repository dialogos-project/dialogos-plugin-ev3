package elmot.javabrick.ev3;

import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.Util;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author elmot
 */
public class CommandBlock {

    private static AtomicInteger seqCounter = new AtomicInteger(0);
    private List<Command> commands = new ArrayList<>();

    private byte commandType;

    @SuppressWarnings("UnusedDeclaration")
    public CommandBlock() {
    }

    public CommandBlock(Command... commands) {
        for (Command command : commands) {
            addCommand(command);
        }
    }

    public void addCommand(Command command) {
        if (commands.isEmpty()) {
            commandType = command.getType();
        } else if (commandType != command.getType()) {
            throw new RuntimeException("Commands of different types are not supported");
        }
        commands.add(command);
    }
    
    public static int getNextSeqCounter() {
        return seqCounter.incrementAndGet() & 0x7fff;
    }

    public Response run(Ev3Connector conn, Class<?>... outParametersTypes) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocate(1024).order(ByteOrder.LITTLE_ENDIAN);
        int seqNumber = getNextSeqCounter();
        buffer.putShort(2, (short) seqNumber);
        buffer.put(4, commandType);
        
        // write commands with their parameters at position 7
        buffer.position(7);
        int globalVarCount = 0;
        int localVarCount = 0;
        
        for (Command command : commands) {
            globalVarCount += command.getReplyByteCount();
            localVarCount += command.getNumLocalVariables();
            command.writeTo(buffer);
        }

        int len = buffer.position() - 2;
        buffer.flip();
        buffer.putShort(0, (short) len);
        int memoryDeclarationHeader = globalVarCount + (localVarCount << 10);
        buffer.putShort(5, (short) memoryDeclarationHeader);
        buffer.rewind();

        ByteBuffer responseBytes = conn.dataExchange(buffer, seqNumber);

        int readSeqNo = responseBytes.getShort(2);
        if (readSeqNo != seqNumber) {
            throw new IOException(String.format("Reponse had unexpected message ID: expected %d, got %d", seqNumber, readSeqNo));
        }
        
        
        int status = responseBytes.get(4);
        if (outParametersTypes != null) {
            Response response = new Response(outParametersTypes.length, status);
            for (int i = 0, index = 5; i < outParametersTypes.length; i++) {
                Class<?> outParameterType = outParametersTypes[i];
                
                if (outParameterType == Integer.class || outParameterType == int.class) {
                    response.setOutParameter(i, responseBytes.getInt(index));
                    index += 4;
                } else if (outParameterType == Float.class || outParameterType == float.class) {
                    response.setOutParameter(i, responseBytes.getFloat(index));
                    index += 4;
                } else if (outParameterType == Byte.class || outParameterType == byte.class) {
                    response.setOutParameter(i, responseBytes.get(index));
                    index += 1;
                } else if( outParameterType == String.class ) {
                    String s = Util.readString(responseBytes, index, (char) 0);
                    response.setOutParameter(i, s);
                    index += s.length() + 1;
                } else {
                    throw new IllegalArgumentException("OUT parameter of type " + outParameterType + " is not supported");
                }
            }
            return response;
        } else {
            return new Response(0, status);
        }
    }
}
