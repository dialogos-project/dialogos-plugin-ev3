package elmot.javabrick.ev3;

import elmot.javabrick.ev3.EV3;

import java.io.IOException;

/**
 * @author elmot
 */
public class FactoryBase {
    public static final int DIRECT_REPLY = 2;
    private static final int SYSTEM_REPLY = 3;
    private static final int DIRECT_ERROR = 4;
    
    protected final EV3 brick;

    protected FactoryBase(EV3 brick) {
        this.brick = brick;
    }

    protected Response run(CommandBlock commandBlock, Class<?>... responseClasses) throws IOException {
        return brick.run(commandBlock, responseClasses);
    }

    protected Response run(Command command, Class<?>... responseClasses) throws IOException, DirectCommandException {
        CommandBlock commandBlock = new CommandBlock(command);
        Response run = brick.run(commandBlock, responseClasses);
        int statusCode = run.getStatusCode();
        
        if( statusCode == DIRECT_ERROR ) {
            throw new DirectCommandException();
        }
        
        if (statusCode != DIRECT_REPLY && statusCode != SYSTEM_REPLY) {
            System.err.println("Unexpected reply status:" + statusCode);
        }

        return run;
    }


    public static class DirectCommandException extends RuntimeException {
        
    }
}
