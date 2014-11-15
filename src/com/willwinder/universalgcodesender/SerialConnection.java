/*
 * A serial connection object implementing the connection API.
 */

/*
    Copywrite 2013 Will Winder

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
package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.i18n.Localization;
import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wwinder
 */
public class SerialConnection extends Connection implements SerialPortEventListener {
    @Deprecated private String lineTerminator;

    // General variables
    private CommPort commPort;
    protected InputStream in;   // protected for unit testing.
    protected OutputStream out; // protected for unit testing.
    private StringBuilder inputBuffer = null;

    public SerialConnection() {
        this("\r\n");
    }
    
    public SerialConnection(String terminator) {
        lineTerminator = terminator;
    }
    
    @Deprecated public void setLineTerminator(String lt) {
        this.lineTerminator = lt;
    }
    
    @Deprecated public String getLineTerminator() {
        return this.lineTerminator;
    }
    // Must create /var/lock on OSX, fixed in more current RXTX (supposedly):
    // $ sudo mkdir /var/lock
    // $ sudo chmod 777 /var/lock
    @Override
    synchronized public boolean openPort(String name, int baud) throws Exception {
        
        this.inputBuffer = new StringBuilder();
        
        boolean returnCode;

        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(name);
           
        if (portIdentifier.isCurrentlyOwned()) {
            throw new Exception(Localization.getString("connection.exception.inuse"));
        } else {
            this.commPort = portIdentifier.open(this.getClass().getName(), 2000);

            SerialPort serialPort = (SerialPort) this.commPort;
            serialPort.setSerialPortParams(baud,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);

            this.in = serialPort.getInputStream();
            this.out = serialPort.getOutputStream();

            serialPort.addEventListener(this);
            serialPort.notifyOnDataAvailable(true);  
            serialPort.notifyOnBreakInterrupt(true);

            returnCode = true;
        }
        
        return returnCode;
    }
        
    @Override
    public void closePort() {
        // Stop listening before anything, we're done here.
        SerialPort serialPort = (SerialPort) this.commPort;
        serialPort.removeEventListener();
        
        try {
            in.close();
            out.close();
            in = null;
            out = null;
        } catch (IOException ex) {
            Logger.getLogger(SerialConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        this.inputBuffer = null;
        
        this.commPort.close();

        this.commPort = null;

    }
    
    /**
     * Sends a command to the serial device. This actually streams the bits to
     * the comm port.
     * @param command   Command to be sent to serial device.
     */
    @Override
    public void sendStringToComm(String command) {
        // Send command to the serial port.
        try (PrintStream printStream = new PrintStream(this.out)) {
            printStream.print(command);
        } 
    }
        
    /**
     * Immediately sends a byte, used for real-time commands.
     */
    @Override
    public void sendByteImmediately(byte b) throws IOException {
        out.write(b);
    }
    
    /**
     * Reads data from the serial port. RXTX SerialPortEventListener method.
     */
    @Override
    public void serialEvent(SerialPortEvent evt) {
        if (inputBuffer == null) {
            inputBuffer = new StringBuilder();
        }
        
        // Check for evt == null to allow faking a call to this event.
        if (evt == null || evt.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try
            {
                int availableBytes = in.available();
                if (availableBytes > 0) {
                    byte[] readBuffer = new byte[availableBytes];

                    // Read from serial port
                    in.read(readBuffer, 0, availableBytes);
                    inputBuffer.append(new String(readBuffer, 0, availableBytes));

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
            }
            catch ( IOException e ) {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    @Override
    public boolean supports(String portname) {
        List<CommPortIdentifier> ports = CommUtils.getSerialPortList();
        for (CommPortIdentifier cpi: ports) {
            if (cpi.getName().equals(portname)) {
                return true;
            }
        }

        return false;
    }
}
