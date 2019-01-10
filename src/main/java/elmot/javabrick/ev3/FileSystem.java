/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package elmot.javabrick.ev3;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Alexander
 */
public class FileSystem extends FactoryBase {

    public FileSystem(EV3 ev3) {
        super(ev3);
    }

    private static final int CHUNKSIZE = 1000;

    private static List<String> decodeResponses(ByteBuffer buf, int length, char separator) {
        byte[] arr = buf.array();

        List<String> ret = new ArrayList<>();
        int pos = 0;
        while (pos < length) {
            ByteBuffer strbuf = ByteBuffer.allocate(arr.length);
            int stringLength = 0;

            while (pos < length && arr[pos] != separator) {
                strbuf.put(arr[pos++]);
                stringLength++;
            }

            strbuf.flip();
            byte[] strarr = new byte[stringLength];
            strbuf.get(strarr);

            String s = new String(strarr);
            ret.add(s);

            pos++; // skip terminator
        }

        return ret;
    }

    /**
     * Returns the contents of the directory "path" on the EV3 brick.
     * Subdirectories are stored in the list "subdirectories", as relative names
     * below "path". Files are stored in the list "files".
     *
     * @param path
     * @param subdirectories
     * @param files
     * @throws IOException
     */
    public void listFiles(String path, List<String> subdirectories, List<Ev3File> files) throws IOException {
        List<String> ret = new ArrayList<>();
        int messageSeqNumber = CommandBlock.getNextSeqCounter();

        ByteBuffer collectedResponses = ByteBuffer.allocate(100000); // hopefully this is enough

        ByteBuffer cmd = ByteBuffer.allocate(1000);
        cmd.order(ByteOrder.LITTLE_ENDIAN);

        // LIST_FILES system command
        cmd.putShort((short) 0);                       // bytes 0-1: message length (placeholder)
        cmd.putShort((short) messageSeqNumber);        // bytes 2-3: message ID
        cmd.put(Ev3Constants.SYSTEM_COMMAND_REPLY);    // byte 4: cmd type
        cmd.put(Ev3Constants.LIST_FILES);              // byte 5: command
        cmd.putShort((short) CHUNKSIZE);               // byte 6-7: max response length
        Util.putString(path, cmd);                     // byte 8 ff.: zero-terminated path
        
        cmd.flip();
        cmd.putShort((short) (cmd.limit()-2));         // bytes 0-1: overwrite with actual message length (not counting bytes 0-1 themselves)
        cmd.rewind();

        ByteBuffer response = brick.getConnector().dataExchange(cmd, messageSeqNumber);

        // process response
        response.rewind();
        int messageLength = response.getShort();       // bytes 0-1: # bytes in message

        response.position(6);
        int status = response.get();                   // byte 6: return status  // TODO do something with it?
        int length = response.getInt();                // bytes 7-10: total length of filenames (in bytes)
        byte handle = response.get();                  // byte 11: handle

        int remainingBytes = length;
        int bytesInPart = Math.min(CHUNKSIZE, length);
        remainingBytes -= bytesInPart;

        // copy filenames to collectedResponses
        for (int i = 0; i < bytesInPart; i++) {
            collectedResponses.put(response.get());
        }

        while (remainingBytes > 0) {
            // CONTINUE_LIST_FILES system command
            cmd.clear();
            messageSeqNumber = CommandBlock.getNextSeqCounter();

            cmd.putShort((short) 0);                       // bytes 0-1: message length (placeholder)
            cmd.putShort((short) messageSeqNumber);        // bytes 2-3: message ID
            cmd.put(Ev3Constants.SYSTEM_COMMAND_REPLY);    // byte 4: cmd type
            cmd.put(Ev3Constants.CONTINUE_LIST_FILES);     // byte 5: command
            cmd.put(handle);                               // byte 6: handle
            cmd.putShort((short) CHUNKSIZE);               // byte 7-8: max response length

            cmd.flip();
            cmd.putShort((short) (cmd.limit()-2));         // bytes 0-1: overwrite with actual message length (not counting bytes 0-1 themselves)
            cmd.rewind();

            response = brick.getConnector().dataExchange(cmd, messageSeqNumber);
            response.rewind();
            response.position(8); // skip headers

            bytesInPart = Math.min(CHUNKSIZE, remainingBytes);
            remainingBytes -= bytesInPart;
            for (int i = 0; i < bytesInPart; i++) {
                collectedResponses.put(response.get());
            }
        }

        List<String> contents = decodeResponses(collectedResponses, length, '\n');
        for (String filename : contents) {
            if (filename.endsWith("/")) {
                subdirectories.add(filename);
            } else {
                files.add(Ev3File.fromString(path, filename));
            }
        }
    }

    public static final String PROJECT_ROOT = "/home/root/lms2012/prjs/";

    public static class Ev3File {

        private static final Pattern FILE_PATTERN = Pattern.compile("(\\S+)\\s+(\\S+)\\s+(.+)");

        private String md5;
        private int filesize;
        private String containingDirectory; // absolute name of directory that contains the file
        private String filename;  // filename (without directory)

        public static Ev3File fromString(String containingDirectory, String filename) {
            // filename is of shape: 85A66B3344C12CD3F94A17923DA9788C 00000327 IR Control.rbfw
            Ev3File ret = new Ev3File();
            Matcher m = FILE_PATTERN.matcher(filename);

            if (m.matches()) {
                ret.md5 = m.group(1);
                ret.filesize = Integer.parseInt(m.group(2), 16);
                ret.filename = m.group(3);
                ret.containingDirectory = containingDirectory;
                return ret;
            } else {
                return null;
            }
        }

        public static String makeFilenameRelativeToProjectRoot(String relativeFilename) {
            return PROJECT_ROOT + relativeFilename;
        }

        public String getMd5() {
            return md5;
        }

        public int getSize() {
            return filesize;
        }

        public String getContainingDirectory() {
            return containingDirectory;
        }

        public String getName() {
            return filename;
        }

        @Override
        public String toString() {
            return getContainingDirectory() + getName();
        }

        public String getRelativePathname() {
            String absoluteName = getContainingDirectory() + getName();

            if (absoluteName.startsWith(PROJECT_ROOT)) {
                return absoluteName.substring(PROJECT_ROOT.length());
            } else {
                return absoluteName;
            }
        }
    }

    /*
    public static void exploreFileTree(EV3 brick, String path, PrintWriter pw) throws IOException {
        List<String> subdirectories = new ArrayList<>();
        List<Ev3File> files = new ArrayList<>();

        pw.println(path);
        pw.flush();

        brick.FILE.listFiles(path, subdirectories, files);

        for (Ev3File file : files) {
            System.out.printf("%s --> %s (%s bytes)\n", path, file.getName(), file.getSize());
        }

        for (String f : subdirectories) {
            if (!f.endsWith("./") && !f.contains("subsystem")) { // avoid infinite recursions
                String sub = path + f;
                exploreFileTree(brick, sub, pw);
            }
        }
    }
    */

    private void forEachFileInSubtree(String directory, Consumer<Ev3File> fn) throws IOException {
        List<String> subdirectories = new ArrayList<>();
        List<Ev3File> files = new ArrayList<>();
        listFiles(directory, subdirectories, files);

        files.forEach(fn);

        for (String f : subdirectories) {
            if (!f.endsWith("./") && !f.contains("subsystem")) { // avoid infinite recursions
                String sub = directory + f;
                forEachFileInSubtree(sub, fn);
            }
        }
    }

    public List<Ev3File> findFiles(String directory, Predicate<Ev3File> filter) throws IOException {
        final List<Ev3File> files = new ArrayList<>();

        if (!directory.endsWith("/")) {
            directory = directory + "/";
        }

        forEachFileInSubtree(directory, file -> {
//            System.err.printf("%s -> %b\n", file.toString(), filter.test(file));

            if (filter.test(file)) {
                files.add(file);
            }
        });

        return files;
    }

    public void startProgram(String absoluteFilename) throws IOException {
        Command command = new Command(Ev3Constants.opFILE);
        command.addByte(Ev3Constants.LOAD_IMAGE);
        command.addLCX(1);                      // slot
        command.addLCS(absoluteFilename);       // name
        command.addLVX(0);                      // size
        command.addLVX(4);                      // IP*

        command.addByte(Ev3Constants.opPROGRAM_START);
        command.addLCX(1);                      // slot
        command.addLVX(0);                      // size
        command.addLVX(4);                      // IP*
        command.addLCX(0);                      // debug

        run(command);
    }

    /**
     * Stop the currently running program.
     *
     * @throws IOException
     */
    public void stopProgram() throws IOException {
        Command command = new Command(Ev3Constants.opPROGRAM_STOP);
        command.addLCX(1);                      // slot

        run(command);
    }

    /**
     * Checks if the program is currently running. Return values: 0 = OK
     * (terminated); 1 = BUSY (running).
     *
     * @return
     * @throws IOException
     */
    public byte checkStatus() throws IOException {
        Command command = new Command(Ev3Constants.opPROGRAM_INFO, 1);
        command.addByte(Ev3Constants.GET_PRGRESULT);
        command.addLCX(1);
        command.addShortGlobalVariable(0);

        Response resp = run(command, Byte.class);
        return resp.getByte(0);
    }

    public void waitUntilProgramTermination() throws IOException {
        byte status = 1;

        while (status == 1) {
            status = brick.FILE.checkStatus();
            try {
                Thread.sleep(200);
            } catch (InterruptedException ex) {
            }
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        Ev3Descriptor.discoverAll();
        List<Ev3Descriptor> availableBricks = Ev3Descriptor.getAllDescriptors();
        EV3 brick = availableBricks.get(0).instantiate();

//        List<Ev3File> files = brick.FILE.findFiles(PROJECT_ROOT, file -> true);
//        System.err.println(files);
//        System.exit(0);
        String absoluteFilename = Ev3File.makeFilenameRelativeToProjectRoot("Test/Program.rbf");

        brick.FILE.startProgram(absoluteFilename);

        byte status = 1;
        while (status > 0) {
            long startTime = System.nanoTime();
            status = brick.FILE.checkStatus();
            long endTime = System.nanoTime();
            System.err.println("checkStatus took " + (endTime-startTime)/1000000.0 + " ms");
            
            System.err.println("still running");
            Thread.sleep(200);
        }

        brick.FILE.stopProgram();
    }
}
