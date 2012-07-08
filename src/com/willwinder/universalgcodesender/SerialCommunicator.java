/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender;

import gnu.io.*;
import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

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
    private List<GcodeCommand> commandList;
    
    // File transfer variables.
    private Boolean sendPaused = false;
    private File gcodeFile;
    private Integer numRows;
    private Integer numResponses;
    private List<String> sentBuffer;
    
    // Callback interfaces
    SerialCommunicatorListener fileStreamCompleteListener;
    SerialCommunicatorListener commandQueuedListener;
    SerialCommunicatorListener commandSentListener;
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
    
    void setCommandQueuedListener(SerialCommunicatorListener fscl) {
        this.commandQueuedListener = fscl;
    }
    
    void setCommandSentListener(SerialCommunicatorListener fscl) {
        this.commandSentListener = fscl;
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
        this.cancelSend();
        this.commPort.close();
    }
    
    // Puts a command in the command buffer, the SerialWriter class should pick
    // it up and send it to the serial device.
    void sendStringToComm(String command) {
        String str = command;
        
        // Command has a newline attached.
        this.sendMessageToConsoleListener(">>> " + command);
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
    void streamFileToComm(File file) throws Exception {
        fileMode = true;
        this.commandList = new ArrayList<GcodeCommand>();
        
        this.gcodeFile = file;
        this.numRows = 0;
        this.numResponses = 0;
        this.sentBuffer = new ArrayList<String>();

        // Get command list.
        try {
            this.parseFileIntoCommandList(file);
        } catch (Exception e) {
            // Wrap up then re-throw exception for GUI to display.
            finishStreamFileToComm();
            throw e;
        }
        this.streamFileCommands();     
    }
    
    // TODO: This could probably be a static helper that returns the commandList.
    Boolean parseFileIntoCommandList(File file) throws FileNotFoundException, IOException {

        FileInputStream fstream = new FileInputStream(this.gcodeFile);
        DataInputStream dis = new DataInputStream(fstream);
        BufferedReader fileStream = new BufferedReader(new InputStreamReader(dis));

        String line;
        GcodeCommand command;
        int commandNum = 0;
        while ((line = fileStream.readLine()) != null) {
            command = new GcodeCommand(line, commandNum++);
            this.commandList.add(command);
            if (this.commandQueuedListener != null) {
                this.commandQueuedListener.commandQueued(command);
            }
        }
        
        return true;
    }
    
    // TODO: This could be a static helper.
    private Boolean checkRoomInBuffer(String nextCommand) {
        int charInBuffer = numberOfCharacters(this.sentBuffer);
        charInBuffer += nextCommand.length();
        return charInBuffer < CommPortUtils.GRBL_RX_BUFFER_SIZE;
    }
    
    void streamFileCommands() {

        // Keep sending commands until there are no more, or the character
        // buffer is full.
        while (this.numRows < this.commandList.size() &&
                checkRoomInBuffer(this.commandList.get(this.numRows).getCommand())) {

            // TODO: Use an iterator.
            GcodeCommand command = this.commandList.get(numRows);

            // Allow a command preprocessor listener to preprocess the command.
            if (this.commandPreprocessorListener != null) {
                String processed = this.commandPreprocessorListener.preprocessCommand(command.getCommand());
                command.setCommand(processed);
            }

            this.numRows++;
            this.sentBuffer.add(command.getCommand());
            this.sendStringToComm(command.getCommand() + '\n');
            
            command.setSent(true);
            
            if (this.commandSentListener != null) {
                this.commandCompleteListener.commandSent(command);
            }
            
            //this.sendMessageToConsoleListener(
            //        "\nSND: "+this.numRows+
            //        " : " + command.getCommand() + 
            //        " BUF: " + numberOfCharacters(this.sentBuffer));
        }
        
        // If we've received as many responses as we expect... wrap up.
        if (this.commandList.size() == this.numResponses) {
            this.finishStreamFileToComm();
        }            
    }
    
    void finishStreamFileToComm() {
        fileMode = false;

        this.sendMessageToConsoleListener("\n**** Finished sending file. ****\n\n");
        // Trigger callback
        if (this.fileStreamCompleteListener != null) {
            boolean success = (this.commandList.size() == this.numResponses);
            this.fileStreamCompleteListener.fileStreamComplete(this.gcodeFile.getName(), success);
        }
    }
    
    void pauseSend() {
        this.sendMessageToConsoleListener("\n**** Pausing file transfer. ****\n");
        this.sendPaused = true;
    }
    
    void resumeSend() {
        this.sendMessageToConsoleListener("\n**** Resuming file transfer. ****\n");
                
        this.sendPaused = false;
        this.streamFileCommands();
    }
    
    void cancelSend() {
        if (fileMode) {
            finishStreamFileToComm();
        }
    }

    // Processes a serial response
    void responseMessage( String response ) {
        // If not file mode, send it to the console without processing.
        this.sendMessageToConsoleListener(response + "\n");
        
        // If file mode, parse the output
        if (fileMode) {            
            // Check if was 'ok' or 'error'.
            if (GcodeCommand.isOkErrorResponse(response)) {

                // TODO: Another iterator for this one would be good.
                GcodeCommand command = this.commandList.get(this.numResponses);
                command.setResponse(response);
                this.numResponses = this.numResponses + 1;
                
                // No longer need ok/error in the console...
                //this.sendMessageToConsoleListener(" " + command.responseString());

                if (this.commandCompleteListener != null) {
                    this.commandCompleteListener.commandComplete(command);
                }
                // Remove completed command from buffer tracker.
                this.sentBuffer.remove(0);

                if (this.sendPaused == false) {
                    this.streamFileCommands();
                }
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
                }

                // Strip off that terminator.
                String output = buffer.toString();
                buffer.setLength(0);

                output = output.replace("\n", "").replace("\r","");
                
                // File mode has a stricter handling on data to GUI.
                this.responseMessage(output);
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