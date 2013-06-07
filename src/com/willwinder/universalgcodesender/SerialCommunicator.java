/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender;

import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPort;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wwinder
 */
public abstract class SerialCommunicator extends AbstractCommunicator implements SerialPortEventListener{
    /***************************
     * Additional API Commands. These ones internal to the class.
     ***************************
     **/
    abstract protected void commPortOpenedEvent();
    abstract protected void commPortClosedEvent();
    abstract protected void responseMessage(String response);
    // General variables
    private CommPort commPort;
    protected InputStream in;   // protected for unit testing.
    protected OutputStream out; // protected for unit testing.
    private StringBuilder inputBuffer = null;

    // Must create /var/lock on OSX, fixed in more current RXTX (supposidly):
    // $ sudo mkdir /var/lock
    // $ sudo chmod 777 /var/lock
    @Override
    synchronized public boolean openCommPort(String name, int baud) 
            throws NoSuchPortException, PortInUseException, 
            UnsupportedCommOperationException, IOException, 
            TooManyListenersException, Exception {
        
        this.inputBuffer = new StringBuilder();
        
        boolean returnCode;

        CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(name);
           
        if (portIdentifier.isCurrentlyOwned()) {
            throw new Exception("This port is already owned by another process.");
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

        // Hook for implementing class.
        commPortOpenedEvent();
        
        return returnCode;
    }
        
    @Override
    public void closeCommPort() {
        // Stop listening before anything, we're done here.
        SerialPort serialPort = (SerialPort) this.commPort;
        serialPort.removeEventListener();

        this.cancelSend();
        
        try {
            in.close();
            out.close();
            in = null;
            out = null;
        } catch (IOException ex) {
            Logger.getLogger(GrblCommunicator.class.getName()).log(Level.SEVERE, null, ex);
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
    protected void sendStringToComm(String command) {
        // Command already has a newline attached.
        this.sendMessageToConsoleListener(">>> " + command);
        
        // Send command to the serial port.
        PrintStream printStream = new PrintStream(this.out);
        printStream.print(command);
        printStream.close(); 
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
                    if (inputBuffer.toString().contains(this.getLineTerminator())) {
                        // Split with the -1 option will give an empty string at
                        // the end if there is a terminator there as well.
                        String []commands = inputBuffer.toString().split(getLineTerminator(), -1);

                        for (int i=0; i < commands.length; i++) {
                            if ((i+1) < commands.length) {
                                responseMessage(commands[i]);
                            } else {
                                inputBuffer = new StringBuilder().append(commands[i]);
                            }
                        }
                    }
                }                
            }
            catch ( IOException e )
            {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }
}
