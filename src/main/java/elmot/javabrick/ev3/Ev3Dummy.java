/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elmot.javabrick.ev3;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 *
 * @author Alexander
 */
public class Ev3Dummy extends EV3 {
    @Override
    public void ensureOpen() throws IOException {
        
    }

    @Override
    public void close() throws IOException {
        
    }

    @Override
    public ByteBuffer dataExchange(ByteBuffer bytes, int expectedSeqNo) throws IOException {
        ByteBuffer response = ByteBuffer.allocate(0);
        return response;
    }
    
}
