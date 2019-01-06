package com.clt.lego.ev3;

import elmot.javabrick.ev3.EV3;
import elmot.javabrick.ev3.EV3FactoryUsb;
import elmot.javabrick.ev3.MotorFactory;
import elmot.javabrick.ev3.PORT;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.List;

/**
 * @author koller
 *
 */
public class Ev3 {

    private short nextMessageId = 1;
    private CommInterface comm;

    public Ev3(CommInterface comm) {
        this.comm = comm;
    }

    /**
     * Sends a SYSTEM_COMMAND_REPLY to the Ev3 brick. The "command" parameter
     * is the system command to be sent to the Ev3 brick, including only the
     * system command (byte 5) and its parameters (bytes 6 ff.). The method
     * reads the reply from the Ev3: a message header of 6 bytes plus the
     * expectedResponseLength, which is only the length of the response without
     * the header (bytes 6 ff.). The method wraps the resulting byte array
     * into a little-endian ByteBuffer for convenient reading.
     * 
     * @param command
     * @param expectedResponseLength
     * @return
     * @throws IOException 
     */
    private synchronized ByteBuffer sendSystemCommand(byte... command) throws IOException {
        // add header and send command
        short messageId = nextMessageId++;
        ByteBuffer buf = ByteBuffer.allocate(command.length + 5);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        buf.putShort((short) (command.length + 3));         // bytes 0-1: command length
        buf.putShort(messageId);                          // bytes 2-3: message counter
        buf.put(Ev3Constants.SYSTEM_COMMAND_REPLY);       // byte 4: command type
        buf.put(command);                                 // bytes 5+: the command itself

        comm.send(buf.array());

        // receive response
        byte[] msgLenResponse = comm.read(2); // read message length of reply
        System.err.println("\nmsglen response:");
        hexdump(msgLenResponse);
        
        ByteBuffer bbMsgLen = ByteBuffer.wrap(msgLenResponse);
        bbMsgLen.order(ByteOrder.LITTLE_ENDIAN);
        int responseLength = bbMsgLen.getShort();
        
        byte[] response = comm.read(responseLength);
        System.err.println("response:");
        hexdump(response);
        
        ByteBuffer responseBuf = ByteBuffer.wrap(response);
        responseBuf.order(ByteOrder.LITTLE_ENDIAN);

        short replyMessageId = responseBuf.getShort();    // bytes 2-3: message counter
        byte replyType = responseBuf.get();               // byte 4: reply type
        byte replySysCommand = responseBuf.get();         // byte 5: system command which this is reply to

        System.err.printf("Reply type=%d, syscmd=%d\n", replyType, replySysCommand);

        if (replyMessageId != messageId) {
            throw new IOException(String.format("In reply to message with ID %d, received incorrect message ID %d", messageId, replyMessageId));
        }

        if (replyType != Ev3Constants.SYSTEM_REPLY) {
            throw new IOException("The system command returned an error code.");
        }
        
        return responseBuf;
    }
    
    private List<String> listFiles() throws IOException {
        ByteBuffer response = sendSystemCommand(Ev3Constants.LIST_FILES);
        hexdump(response.array());
        return null;
    }
    
    private short encodeNumVariables(int numGlobal, int numLocal) {
        int ret = (numLocal) << 10 | numGlobal;
        System.err.printf("encode %d and %d to %04x\n", numGlobal, numLocal, (short) ret);
        return (short) ret;
    }
    
    private ByteBuffer sendDirectCommand(int numGlobal, int numLocal, byte... command) throws IOException {
        // add header and send command
        short messageId = nextMessageId++;
        ByteBuffer buf = ByteBuffer.allocate(command.length + 7);
        buf.order(ByteOrder.LITTLE_ENDIAN);

        buf.putShort((short) (command.length + 5));       // bytes 0-1: command length
        buf.putShort(messageId);                          // bytes 2-3: message counter
        buf.put(Ev3Constants.DIRECT_COMMAND_REPLY);       // byte 4: command type
        buf.putShort(encodeNumVariables(numGlobal, numLocal)); // bytes 5-6: allocate variables for response
        buf.put(command);                                 // bytes 5+: the command itself

        comm.send(buf.array());
        
        
        // receive response
        
        
        
        
        byte[] msgLenResponse = comm.read(4); // read message length of reply
        System.exit(0);
        
        System.err.println("\nmsglen response:");
        hexdump(msgLenResponse);
        
        ByteBuffer bbMsgLen = ByteBuffer.wrap(msgLenResponse);
        bbMsgLen.order(ByteOrder.LITTLE_ENDIAN);
        int ff55 = bbMsgLen.getShort(); // skip these (Bluetooth only?) - TODO check this
        int responseLength = bbMsgLen.getShort();
        
        byte[] response = comm.read(responseLength);
        System.err.println("response:");
        hexdump(response);
        
        return ByteBuffer.wrap(response);
    }
    
    public String getInfo() throws IOException {
        // 000660
        ByteBuffer response = sendDirectCommand(6, 0, Ev3Constants.opINFO, (byte) 00, (byte) 6, (byte) 0x60);
        return null;
    }
    
    public static void hexdump(byte[] data) {
        int numRows = (data.length + 7) / 8;
                
        for( int row = 0; row < numRows; row ++ ) {
            int numCols = Math.min(8, data.length - 8*row);
            StringBuilder hexPart = new StringBuilder();
            StringBuilder chrPart = new StringBuilder();
            
            for( int col = 0; col < numCols; col++ ) {
                hexPart.append(String.format("%02x ", data[8*row+col]));
                chrPart.append((char) data[8*row+col]);
            }
            
            System.err.printf("%-24s %s\n", hexPart.toString(), chrPart.toString());
        }
    }
    
    private byte[] split(String x) {
        x = x.trim();
        String[] parts = x.split(" ");
        byte[] ret = new byte[parts.length];
        for( int i = 0; i < parts.length; i++ ) {
            ret[i] = (byte) Integer.decode(parts[i]).byteValue();
        }
        
        return ret;
    }
    
    public void test() throws IOException {
        byte[] cmd  = split("0x0F 0x00 0x00 0x00 0x80 0x00 0x00 0x94 0x01 0x81 0x32 0x82 0x0B 0x02 0x82 0xF4 0x01");
        comm.send(cmd);
        
        byte[] resp = comm.read(8);
        
    }
    

    public long keepAlive() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void close() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String[] getPrograms() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void startProgram(String program) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public Object getCurrentProgram() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public void sendMessage(int i, String message) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String getResourceString() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public String getPort() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    public boolean stopProgram() {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void setOutputState(Motor.Port port, int power, int i, Motor.Regulation regulation, int i0, Motor.State state, int i1) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    MotorState getOutputState(Motor.Port port) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void resetMotorPosition(Motor.Port port, boolean relative) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    void setSensorType(int id, Sensor.Type type, Sensor.Mode mode, int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    int getSensorValue(int id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    int getSensorRawValue(int id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    byte[] lsRead(int id, byte[] b, int i) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    Sensor.Type getSensorType(int id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    Sensor.Mode getSensorMode(int id) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    
    
    
    public static void main(String[] args) throws IOException, InterruptedException {
        List<EV3> ev3s = EV3FactoryUsb.listDiscovered();
        EV3 ev3 = ev3s.get(0);
        
//        System.out.println(ev3.getPort());
        
//        ev3.SYSTEM.playTone(50, 400, 1000);
        
        System.out.println(ev3.TOUCH.getTouch(0, PORT.P1));

        ev3.MOTOR.direction(MotorFactory.MOTORSET.A, MotorFactory.DIR.FORWARD);
        ev3.MOTOR.speed(MotorFactory.MOTORSET.A, 50);
        
        ev3.MOTOR.start(MotorFactory.MOTORSET.A);
        
        Thread.sleep(1000);
        
        ev3.MOTOR.stop(MotorFactory.MOTORSET.A, MotorFactory.BRAKE.BRAKE);
        
        
//        
//        //CommInterface comm = new BluetoothCommInterface();
//        List<String> availablePorts = UsbCommInterface.getAvailablePorts();
//        String port = availablePorts.get(0);
//        CommInterface comm = new UsbCommInterface(port);
//        
//        Ev3 ev3 = new Ev3(comm);
//        
//        //ev3.test();  // works on Win/USB
//        ev3.getInfo();
//        
        //ev3.listFiles();
    }
}
