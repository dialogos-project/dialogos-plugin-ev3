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
public interface Ev3Connector {
    public void ensureOpen() throws IOException;
    public void close() throws IOException;
    public ByteBuffer dataExchange(ByteBuffer bytes, int expectedSeqNo) throws IOException;

}
