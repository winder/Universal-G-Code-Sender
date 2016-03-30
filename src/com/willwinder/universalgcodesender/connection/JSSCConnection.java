/*
 * A serial connection object implementing the connection API.
 */

/*
    Copywrite 2015 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
*/
package com.willwinder.universalgcodesender.connection;

import jssc.SerialPort;
import jssc.SerialPortEventListener;
import jssc.SerialPortEvent;
import jssc.SerialPortException;

/**
 *
 * @author wwinder
 */
public class JSSCConnection extends Connection implements SerialPortEventListener {
    @Deprecated private String lineTerminator;

    // General variables
    private SerialPort serialPort;
    private StringBuilder inputBuffer = null;

    public JSSCConnection() {
        this("\r\n");
    }
    
    public JSSCConnection(String terminator) {
        lineTerminator = terminator;
    }
    
    @Deprecated public void setLineTerminator(String lt) {
        this.lineTerminator = lt;
    }
    
    @Deprecated public String getLineTerminator() {
        return this.lineTerminator;
    }
    @Override
    synchronized public boolean openPort(String name, int baud) throws Exception {
        this.inputBuffer = new StringBuilder();
        
        this.serialPort = new SerialPort(name);
        this.serialPort.openPort();
        this.serialPort.setParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE, true, true);
        this.serialPort.addEventListener(this);

        if (this.serialPort == null) {
            throw new Exception("Serial port not found.");
        }

        return serialPort.isOpened();
    }
        
    @Override
    public void closePort() throws Exception {
        if (this.serialPort != null) {
            try {
                this.serialPort.removeEventListener();

                if (this.serialPort.isOpened()) {
                    this.serialPort.closePort();
                }
            } finally {
                this.inputBuffer = null;
                this.serialPort = null;
            }
        }
    }

    @Override
    public boolean isOpen() {
        return serialPort != null && serialPort.isOpened();
    }

    /**
     * Sends a command to the serial device. This actually streams the bits to
     * the comm port.
     * @param command   Command to be sent to serial device.
     */
    @Override
    public void sendStringToComm(String command) throws Exception {
        this.serialPort.writeString(command);
    }
        
    /**
     * Immediately sends a byte, used for real-time commands.
     */
    @Override
    public void sendByteImmediately(byte b) throws Exception {
        this.serialPort.writeByte(b);
    }
    
    /**
     * Reads data from the serial port. RXTX SerialPortEventListener method.
     */
    @Override
    public void serialEvent(SerialPortEvent evt) {
        if (inputBuffer == null) {
            inputBuffer = new StringBuilder();
        }

        try {
            byte[] buf = this.serialPort.readBytes();
            if (buf != null && buf.length > 0) {
                String s = new String(buf, 0, buf.length);
                inputBuffer.append(s);
                // Check for line terminator and split out command(s).
                if (inputBuffer.toString().contains(comm.getLineTerminator())) {
                    // Split with the -1 option will give an empty string at
                    // the end if there is a terminator there as well.
                    String []commands = inputBuffer.toString().split(comm.getLineTerminator(), -1);
                    for (int i=0; i < commands.length; i++) {
                        // Make sure this isn't the last command.
                        if ((i+1) < commands.length) {
                            comm.responseMessage(commands[i]);
                        // Append last command to input buffer because it didn't have a terminator.
                        } else {
                            inputBuffer = new StringBuilder().append(commands[i]);
                        }
                    }
                }
            }
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(-1);
        }
    }

    public static boolean supports(String portname, int baud) {
        SerialPort serialPort = new SerialPort(portname);
        try {
            serialPort.openPort();
            serialPort.setParams(baud, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE, true, true);
            serialPort.closePort();
            return true;
        } catch (SerialPortException e) {
            return false;
        } finally {
            if (serialPort.isOpened()) {
                try {
                    serialPort.closePort();
                } catch (SerialPortException e) {
                    // noop
                }
            }
        }
    }        
}

