/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package universal.g.code.sender;

import gnu.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

/**
 *
 * @author wwinder
 */
public class CommPortHelper implements SerialPortEventListener{
        public static final int GRBL_RX_BUFFER_SIZE= 128;
        
    private CommPort commPort;
    private InputStream in;
    private OutputStream out;
    StringBuffer commandStream;
    private JTextArea outputConsole;
    private SerialWriter serialWriter;
    private Thread serialWriterThread;
    private boolean fileMode = false;
    
    // On OSX must run create /var/lock for some reason:
    // $ sudo mkdir /var/lock
    // $ sudo chmod 777 /var/lock
    boolean openCommPort(String name, int baud, JTextArea serialInputArea) {
        this.outputConsole = serialInputArea;
        this.commandStream = new StringBuffer();
        boolean returnCode;
        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(name);
            
            if (portIdentifier.isCurrentlyOwned()) {
                returnCode = false;
            } else {
                    this.commPort = portIdentifier.open(this.getClass().getName(), 2000);
                    
                    SerialPort serialPort = (SerialPort) this.commPort;
                    serialPort.setSerialPortParams(baud,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);
                                                            
                    this.in = serialPort.getInputStream();
                    this.out = serialPort.getOutputStream();
                    serialPort.addEventListener(this);
                    serialPort.notifyOnDataAvailable(true);  
                    serialPort.notifyOnBreakInterrupt(true);
                    
                    // Launch the writer thread.
                    this.serialWriter= new SerialWriter(out, this.commandStream);
                    this.serialWriterThread= new Thread(this.serialWriter);
                    this.serialWriterThread.start();

            }
            
            returnCode = true;
            
        // Is this nonsense really how Java developers deal with excpetions???
        } catch (NoSuchPortException ex) {
            returnCode = false;
            System.out.println("No such port exception.");
            ex.printStackTrace();
            //Logger.getLogger(CommPortHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PortInUseException ex) {
            returnCode = false;
            System.out.println("Port in use exception.");
            ex.printStackTrace();
            //Logger.getLogger(CommPortHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedCommOperationException ex) {
            returnCode = false;
            System.out.println("Unsupported Comm Operation Exception exception.");
            ex.printStackTrace();
        } catch (IOException ex) {
            returnCode = false;
            System.out.println("Unsupported Comm Operation Exception exception.");
            ex.printStackTrace();
        } catch (TooManyListenersException ex) {
            returnCode = false;
            System.out.println("Too Many Listeners exception.");
            ex.printStackTrace();
        } 

        return returnCode;
    }
        
    void closeCommPort() {

        SerialPort serialPort = (SerialPort) this.commPort;
        serialPort.removeEventListener();
        this.commPort.close();

    }
    
    // Puts a command in the command buffer, the SerialWriter class should pick
    // it up and send it to the serial device.
    void sendCommandToComm(String command) {
        
        String str = command;
        this.commandStream.append(command);
        System.out.println("Command sent to comm: '"+str.replaceAll("\\r\\n|\\r|\\n", "")+"'");
    
    }
    
    // TODO: Figure out how this thing can detect it is disconnected...
    boolean isCommPortOpen() {
        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(this.commPort.getName());
            String owner = portIdentifier.getCurrentOwner();
            String thisClass = this.getClass().getName();

            //SerialPort serialPort = (SerialPort) this.commPort;
            //serialPort.isRTS();
            
            return portIdentifier.isCurrentlyOwned() && owner.equals(thisClass);                    
        } catch (NoSuchPortException ex) {
        }
        
        return false;
    }
    
                
    static java.util.List<CommPortIdentifier> getSerialPortList() {
        int type = CommPortIdentifier.PORT_SERIAL;
        
        java.util.Enumeration<CommPortIdentifier> portEnum = 
                CommPortIdentifier.getPortIdentifiers();
        java.util.List<CommPortIdentifier> returnList =
                new java.util.ArrayList<CommPortIdentifier>();
        
        while ( portEnum.hasMoreElements() ) 
        {
            CommPortIdentifier portIdentifier = portEnum.nextElement();
            if (portIdentifier.getPortType() == type) {
                returnList.add(portIdentifier);
            }
        }
        return returnList;
    }

    @Override
    // Reads data as it is returned by the serial port.
    public void serialEvent(SerialPortEvent arg0) {
        int data;
        byte[] buffer = new byte[1024];

        //if (arg0.getEventType() == SerialPortEvent.BI) {
        //    String str = "**** Connection closed, serial reader exiting ****";
        //    this.outputConsole.append(str + '\n');
        //    System.exit(-1);
        //} else if (arg0.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
        if (arg0.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try
            {
                int len = 0;
                while ( ( data = in.read()) > -1 )
                {
                    if ( data == '\n' ) {
                        break;
                    }
                    buffer[len++] = (byte) data;
                }

                if (fileMode) {
                    // Call fileMode data handler.
                } else {
                    // Print it right to the GUI.
                    String output = new String(buffer,0,len) + '\n';
                    this.outputConsole.append(output);
                }
            }
            catch ( IOException e )
            {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    
    public class SerialWriter implements Runnable {
        private StringBuffer lineBuffer;
        private OutputStream out;
        public boolean exit = false;

        public SerialWriter(OutputStream os, StringBuffer lineBuffer) {
            this.out = os;
            this.lineBuffer = lineBuffer;
        }

        synchronized public void run() {
            try {
                String s;
                while (!exit) {
                    if (lineBuffer.length() < GRBL_RX_BUFFER_SIZE) {
                        s = lineBuffer.toString();
                        lineBuffer.setLength(0);
                    } else {
                        s = lineBuffer.substring(0, GRBL_RX_BUFFER_SIZE-1);
                        lineBuffer.delete(0, GRBL_RX_BUFFER_SIZE-1);
                    }

                    if (s.length() > 0) {
                        PrintStream printStream = new PrintStream(this.out);
                        printStream.print(s);
                        printStream.close();
                        //s= lineBuffer.getNextToSend().line;
                        //if(!resetting)
                        //send((s + "\n").getBytes());
                    }
                    this.wait(1000);
                }
            } catch (InterruptedException ex) {
                System.out.println("SerialWriter thread died.");
                ex.printStackTrace();
                System.exit(-1);
            }
        }
    }
    /**
     * Handles the input coming from the serial port. All output is written
     * directly to the JTextArea. It would be nice if JTextArea had nothing to
     * do with this class... but that would require learning java.
     */
    @Deprecated
    class SerialReader implements SerialPortEventListener 
    {
        private InputStream in;
        private byte[] buffer = new byte[1024];
        private StringBuffer outputStream;
        private JTextArea outputConsole;
        
        public SerialReader ( InputStream in , StringBuffer stringOutput, JTextArea consoleOutput)
        {
            this.in = in;
            this.outputConsole = consoleOutput;
            this.outputStream = stringOutput;
        }
        
        @Override
        public void serialEvent(SerialPortEvent arg0) {
            int data;

            //if (arg0.getEventType() == SerialPortEvent.BI) {
            //    String str = "**** Connection closed, serial reader exiting ****";
            //    this.outputConsole.append(str + '\n');
            //    System.exit(-1);
            //} else if (arg0.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            if (arg0.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try
            {
                int len = 0;
                while ( ( data = in.read()) > -1 )
                {
                    if ( data == '\n' ) {
                        break;
                    }
                    buffer[len++] = (byte) data;
                }

                    String output = new String(buffer,0,len) + '\n';
                    this.outputConsole.append(output);
                    this.outputStream.append(output);
            }
            catch ( IOException e )
            {
                e.printStackTrace();
                System.exit(-1);
            }
            }
        }
        
        
    }

}