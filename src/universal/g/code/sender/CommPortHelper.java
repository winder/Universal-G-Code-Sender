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
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
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
    private SerialWriter serialWriter;
    private Thread serialWriterThread;
    private boolean fileMode = false;
    private String lineTerminator;

    // These should be set with some sort of callback instead of in here.
    private JTextArea outputConsole;
    private JLabel numRowsLabel;
    
    CommPortHelper() {
        this.lineTerminator = "\r\n";
    }
    
    void setLineTerminator(String terminator) {
        if (terminator.length() < 1) {
            this.lineTerminator = "\r\n";
        } else {
            this.lineTerminator = terminator;
        }
    }
    
    void setTextArea(JTextArea jta) {
        this.outputConsole = jta;
    }
    
    void setRowsLabel(JLabel jl) {
        this.numRowsLabel = jl;
    }
    
    // On OSX must run create /var/lock for some reason:
    // $ sudo mkdir /var/lock
    // $ sudo chmod 777 /var/lock
    boolean openCommPort(String name, int baud) {
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
    
    private File gcodeFile;
    private FileInputStream fstream;
    private DataInputStream dis;
    private BufferedReader fileStream;
    private Integer numRows;
    private String nextCommand;
    // Setup for streaming to serial port.
    // 1. Initialize objects.
    // 2. Open file.
    // 3. Call first send.
    void streamFileToComm(File file) {
        fileMode = true;
        
        this.gcodeFile = file;
        this.numRows = 0;

        try{
  
            // Setup the file stream.
            this.fstream = new FileInputStream(this.gcodeFile);
            this.dis = new DataInputStream(fstream);
            this.fileStream = new BufferedReader(new InputStreamReader(dis));

            sendNextFileCommand();
            //Close the input stream
        }catch (Exception e){//Catch exception if any
            System.err.println("Error: " + e.getMessage());
        }
        
    }
    
    void finishStreamFileToComm() {
        try {
            this.fileStream.close();
            this.dis.close();
            this.fstream.close();

            fileMode = false;
        } catch (IOException ex) {
            System.out.println("Error while closing streams: "+ex.getMessage());
        }
    }
    
    void sendNextFileCommand() {
        try {
            // Get the next command.
            if ((this.nextCommand = fileStream.readLine()) != null) {
                this.numRows++;
                this.numRowsLabel.setText(this.numRows.toString());
                
                sendCommandToComm(this.nextCommand + '\n');
            // If the command was null, we're done, wrap up the transfer.
            } else {
                finishStreamFileToComm();
            }
            
        } catch (IOException ex) {
            System.err.println("Error: " + ex.getMessage());
        }
    }
    
    void validateCommandResponse( String response ) {
        this.outputConsole.append("SENT: "+this.nextCommand+" RCV: "+response + '\n');
        if (response.equalsIgnoreCase("ok")) {
            System.out.println("Good response!");
        }
        sendNextFileCommand();
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
        StringBuilder buffer = new StringBuilder();
        
        if (arg0.getEventType() == SerialPortEvent.DATA_AVAILABLE) {
            try
            {
                int len = 0;
                int terminatorPosition = 0;
                while ( ( data = in.read()) > -1 )
                {
                    // My funky way of checking for the terminating characters..
                    if ( data == lineTerminator.charAt(terminatorPosition) ) {
                        terminatorPosition++;
                    } else {
                        terminatorPosition = 0;
                    }
                    
                    if (terminatorPosition == lineTerminator.length()) {
                        break;
                    }
                    
                    buffer.append((char)data);
                    //buffer[len++] = (byte) data;
                }

                // Strip off that terminator.
                String output = buffer.toString();
                buffer.setLength(0);
                output = output.replaceAll(lineTerminator, "");
                
                // File mode has a stricter handling on data to GUI.
                if (fileMode) {
                    this.validateCommandResponse(output);
                // Else command mode.
                } else {
                    // Print it right to the GUI.
                    this.outputConsole.append(output + "\n");
                }
            }
            catch ( IOException e )
            {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    
    // This thread continuously polls a string buffer for data then writes it
    // to an output stream.
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
                    // Need to do 2 operations with lineBuffer in a row in here.
                    synchronized(lineBuffer) {
                        if (lineBuffer.length() < GRBL_RX_BUFFER_SIZE) {
                            s = lineBuffer.toString();
                            lineBuffer.setLength(0);
                        } else {
                                s = lineBuffer.substring(0, GRBL_RX_BUFFER_SIZE-1);
                                lineBuffer.delete(0, GRBL_RX_BUFFER_SIZE-1);
                        }
                    }

                    if (s.length() > 0) {
                        PrintStream printStream = new PrintStream(this.out);
                        printStream.print(s);
                        printStream.close();
                        
                        System.out.println("Just sent command: "+s);
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
}