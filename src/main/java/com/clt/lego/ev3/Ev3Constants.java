package com.clt.lego.ev3;

/**
 * Constants for EV3 commands and responses.
 *
 * @author koller
 */
public class Ev3Constants {
    public static final int EV3_USB_VENDOR_ID = 0x0694;
    public static final int EV3_USB_PRODUCT_ID = 0x0005;

    public static final byte SYSTEM_COMMAND_REPLY = (byte) 0x01;
    public static final byte SYSTEM_COMMAND_NO_REPLY = (byte) 0x81;
    public static final byte DIRECT_COMMAND_REPLY = (byte) 0x00; // Direct command, reply required
    public static final byte DIRECT_COMMAND_NO_REPLY = (byte) 0x80; // Direct command, reply not required

    public static final byte SYSTEM_REPLY = (byte) 0x03; // System command reply OK
    public static final byte SYSTEM_REPLY_ERROR = (byte) 0x05; // System command reply ERROR

    // system commands
    public static final byte BEGIN_DOWNLOAD = (byte) 0x92;  // Begin file download
    public static final byte CONTINUE_DOWNLOAD = (byte) 0x93;  // Continue file download
    public static final byte BEGIN_UPLOAD = (byte) 0x94;  // Begin file upload
    public static final byte CONTINUE_UPLOAD = (byte) 0x95;  // Continue file upload
    public static final byte BEGIN_GETFILE = (byte) 0x96;  // Begin get bytes from a file (while writing to the file)
    public static final byte CONTINUE_GETFILE = (byte) 0x97;  // Continue get byte from a file (while writing to the file)
    public static final byte CLOSE_FILEHANDLE = (byte) 0x98;  // Close file handle
    public static final byte LIST_FILES = (byte) 0x99;  // List files
    public static final byte CONTINUE_LIST_FILES = (byte) 0x9A;  // Continue list files
    public static final byte CREATE_DIR = (byte) 0x9B;  // Create directory
    public static final byte DELETE_FILE = (byte) 0x9C;  // Delete
    public static final byte LIST_OPEN_HANDLES = (byte) 0x9D;  // List handles
    public static final byte WRITEMAILBOX = (byte) 0x9E;  // Write to mailbox
    public static final byte BLUETOOTHPIN = (byte) 0x9F;  // Transfer trusted pin code to brick
    public static final byte ENTERFWUPDATE = (byte) 0xA0;  // Restart the brick in Firmware update mode
    
    // system command reply status codes
    public static final byte SUCCESS = (byte) 0x00;
    public static final byte UNKNOWN_HANDLE = (byte) 0x01;
    public static final byte HANDLE_NOT_READY = (byte) 0x02;
    public static final byte CORRUPT_FILE = (byte) 0x03;
    public static final byte NO_HANDLES_AVAILABLE = (byte) 0x04;
    public static final byte NO_PERMISSION = (byte) 0x05;
    public static final byte ILLEGAL_PATH = (byte) 0x06;
    public static final byte FILE_EXITS = (byte) 0x07;
    public static final byte END_OF_FILE = (byte) 0x08;
    public static final byte SIZE_ERROR = (byte) 0x09;
    public static final byte UNKNOWN_ERROR = (byte) 0x0A;
    public static final byte ILLEGAL_FILENAME = (byte) 0x0B;
    public static final byte ILLEGAL_CONNECTION = (byte) 0x0C;
    
    // direct commands
    public static final byte opINFO = (byte) 0x7C; // GET_ID
}
