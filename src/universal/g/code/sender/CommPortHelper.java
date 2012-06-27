/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package universal.g.code.sender;

import gnu.io.*;
import java.io.*;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.JTextArea;

/**
 *
 * @author wwinder
 */
public class CommPortHelper {
    
    private CommPort commPort;
    private InputStream in;
    private OutputStream out;
    JTextArea outputConsole;
    
    // On OSX must run create /var/lock for some reason:
    // $ sudo mkdir /var/lock
    // $ sudo chmod 777 /var/lock
    boolean openCommPort(String name, int baud, JTextArea serialInputArea) {
        outputConsole = serialInputArea;
        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(name);
            
            if (portIdentifier.isCurrentlyOwned()) {
                return false;
            } else {
                    commPort = portIdentifier.open(this.getClass().getName(), 2000);
                    
                    SerialPort serialPort = (SerialPort) commPort;
                    serialPort.setSerialPortParams(baud,SerialPort.DATABITS_8,SerialPort.STOPBITS_1,SerialPort.PARITY_NONE);

                    in = serialPort.getInputStream();
                    out = serialPort.getOutputStream();

                    serialPort.addEventListener(new SerialReader(in, serialInputArea));
                    serialPort.notifyOnDataAvailable(true);                    
            }
            
            return true;
        } catch (NoSuchPortException ex) {
            System.out.println("No such port exception.");
            ex.printStackTrace();
            //Logger.getLogger(CommPortHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (PortInUseException ex) {
            System.out.println("Port in use exception.");
            ex.printStackTrace();
            //Logger.getLogger(CommPortHelper.class.getName()).log(Level.SEVERE, null, ex);
        } catch (UnsupportedCommOperationException ex) {
            System.out.println("Unsupported Comm Operation Exception exception.");
            ex.printStackTrace();
        } catch (IOException ex) {
            System.out.println("Unsupported Comm Operation Exception exception.");
            ex.printStackTrace();
        } catch (TooManyListenersException ex) {
            System.out.println("Too Many Listeners exception.");
            ex.printStackTrace();
        } 

        return false;
    }
    
    void sendCommandToComm(String command) {
        
        String str = command;
        
        System.out.println("Command sent to comm: '"+str+"'");

        // Write command to output stream.
        PrintStream printStream = new PrintStream(out);
        printStream.print(command);
        printStream.close();
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


    /**
     * Handles the input coming from the serial port. All output is written
     * directly to the JTextArea. It would be nice if JTextArea had nothing to
     * do with this class... but that would require learning java.
     */
    class SerialReader implements SerialPortEventListener 
    {
        private InputStream in;
        private byte[] buffer = new byte[1024];
        private JTextArea outputConsole;
        
        public SerialReader ( InputStream in , JTextArea output)
        {
            this.in = in;
            this.outputConsole = output;
        }
        
        public void serialEvent(SerialPortEvent arg0) {
            int data;
          
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

                outputConsole.append(new String(buffer,0,len) + '\n');
            }
            catch ( IOException e )
            {
                e.printStackTrace();
                System.exit(-1);
            }             
        }
    }

}