/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package universal.g.code.sender;

import gnu.io.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.TooManyListenersException;
import javax.swing.JLabel;
import javax.swing.JTextArea;

/**
 *
 * @author wwinder
 */
public class SerialCommunicator implements SerialPortEventListener{
    
    // General variables
    private CommPort commPort;
    private InputStream in;
    private OutputStream out;
    StringBuffer commandStream;
    private SerialWriter serialWriter;
    private Thread serialWriterThread;
    private boolean fileMode = false;
    private String lineTerminator = "\r\n";
    
    // File transfer variables.
    private File gcodeFile;
    private FileInputStream fstream;
    private DataInputStream dis;
    private BufferedReader fileStream;
    private Integer numRows;
    private Integer numResponses;
    private Integer numTotal;
    private String nextCommand;
    private List<String> sentBuffer;
    
    // Callback interfaces
    SerialCommunicatorListener fileStreamCompleteListener;
    SerialCommunicatorListener commandCompleteListener;
    SerialCommunicatorListener commandPreprocessorListener;
    SerialCommunicatorListener commConsoleListener;

    /** Getters & Setters. */
    void setLineTerminator(String terminator) {
        if (terminator.length() < 1) {
            this.lineTerminator = "\r\n";
        } else {
            this.lineTerminator = terminator;
        }
    }

    // Register for callbacks
    void setFileStreamCompleteListener(SerialCommunicatorListener fscl) {
        this.fileStreamCompleteListener = fscl;
    }
    
    void setCommandCompleteListener(SerialCommunicatorListener fscl) {
        this.commandCompleteListener = fscl;
    }
    
    void setCommandPreprocessorListener(SerialCommunicatorListener fscl) {
        this.commandPreprocessorListener = fscl;
    }
    
    void setCommConsoleListener(SerialCommunicatorListener fscl) {
        this.commConsoleListener = fscl;
    }

    
    // On OSX must run create /var/lock for some reason:
    // $ sudo mkdir /var/lock
    // $ sudo chmod 777 /var/lock
    synchronized boolean openCommPort(String name, int baud) throws Exception {
        this.commandStream = new StringBuffer();
        this.sentBuffer = new ArrayList<String>();

        boolean returnCode = false;

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
                
                returnCode = true;
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
        
        this.sentBuffer.add(this.nextCommand);
        this.commandStream.append(command);
        synchronized (this.serialWriterThread) {
            this.serialWriterThread.notifyAll();
        }
    }
       
    // TODO: Figure out why this isn't working ...
    boolean isCommPortOpen() throws NoSuchPortException {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(this.commPort.getName());
            String owner = portIdentifier.getCurrentOwner();
            String thisClass = this.getClass().getName();
            
            return portIdentifier.isCurrentlyOwned() && owner.equals(thisClass);                    
    }
      
    
    /** File Stream Methods. **/
    
    // Setup for streaming to serial port then launch the first command.
    void streamFileToComm(File file, int totalLines) throws Exception {
        fileMode = true;
        
        this.gcodeFile = file;
        this.numRows = 0;
        this.numResponses = 0;
        this.numTotal = totalLines;
        this.sentBuffer = new ArrayList<String>();

        // Setup the file stream.
        this.fstream = new FileInputStream(this.gcodeFile);
        this.dis = new DataInputStream(fstream);
        this.fileStream = new BufferedReader(new InputStreamReader(dis));

        this.streamFileCommands();     
    }
    
    // 
    void streamFileCommands() {
        try {
            // Keep sending commands until there are no more, or the character
            // buffer is full.
            while ((numberOfCharacters(this.sentBuffer) < CommPortUtils.GRBL_RX_BUFFER_SIZE) &&
                    ((this.nextCommand = fileStream.readLine()) != null)) {
                        
                // Allow a command preprocessor listener to preprocess the command.
                if (this.commandPreprocessorListener != null) {
                    this.nextCommand = this.commandPreprocessorListener.preprocessCommand(this.nextCommand);
                }

                this.numRows++;
                this.sendCommandToComm(this.nextCommand + '\n');
                this.sendMessageToConsoleListener(
                        "\nSND: "+this.numRows+
                        " : " + this.nextCommand + 
                        " BUF: " + numberOfCharacters(this.sentBuffer) + 
                        " REC: ");
            }

            // If we've received as many responses as we expect... wrap up.
            if (this.numTotal == this.numResponses) {
                this.finishStreamFileToComm();
                //System.out.println("FINISH UP DAMMIT");
                //th.is.isnt.called.why.finishStreamFileToComm();
            }
            
        } catch (IOException ex) {
            System.err.println("Error: " + ex.getMessage());
            ex.printStackTrace();
        }
    }
    
    void finishStreamFileToComm() {
        try {
            this.fileStream.close();
            this.dis.close();
            this.fstream.close();
            fileMode = false;
            
            this.sendMessageToConsoleListener("\n**** Finished sending file. ****\n\n");
            // Trigger callback
            if (this.fileStreamCompleteListener != null) {
                boolean success = (this.numTotal == this.numResponses);
                this.fileStreamCompleteListener.fileStreamComplete(this.gcodeFile.getName(), success);
            }
        } catch (IOException ex) {
            System.out.println("Error while closing streams: "+ex.getMessage());
        }
    }
    
    void cancelSend() {
        if (fileMode) {
            finishStreamFileToComm();
        }
    }

    // Processes a serial response
    void fileResponseMessage( String response ) {
        if (fileMode) {
            // Command complete, can be 'ok' or 'error'.
            String okError = "";
            if (response.toLowerCase().equals("ok")) {
                okError = "ok";
            } else if (response.toLowerCase().startsWith("error")) {
                okError = "error["+response.substring("error: ".length()) +"]";
            }
            
            // If the response was a command terminator, send more commands.
            if (!okError.isEmpty()) {
                this.numResponses++;
                this.sendMessageToConsoleListener(" " + okError +this.numResponses);
                if (this.commandCompleteListener != null) {
                    this.commandCompleteListener.commandComplete(this.sentBuffer.get(0), response);
                }
                // Remove completed command from buffer tracker.
                this.sentBuffer.remove(0);

                this.streamFileCommands();
            }
        }
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
                //output = output.replaceAll(lineTerminator, "");
                output = output.replace("\n", "").replace("\r","");
                // File mode has a stricter handling on data to GUI.
                if (fileMode) {
                    this.fileResponseMessage(output);
                // Else command mode.
                } else {
                    // Print it right to the GUI.
                    this.sendMessageToConsoleListener(output + "\n");
                }
            }
            catch ( IOException e )
            {
                e.printStackTrace();
                System.exit(-1);
            }
        }
    }

    // Helper for the console listener.              
    void sendMessageToConsoleListener(String msg) {
        if (this.commConsoleListener != null) {
            this.commConsoleListener.messageForConsole(msg);
        }
    }
    
    // Helper for buffer counting.
    private static int numberOfCharacters(List<String> arr) {
        Iterator<String> iter = arr.iterator();
        int characters = 0;
        while (iter.hasNext()) {
            String next = iter.next();
            characters += next.length();
        }
        return characters;
    }
}