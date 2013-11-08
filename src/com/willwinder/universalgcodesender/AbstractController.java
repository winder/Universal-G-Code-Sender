/*
 * Abstract Control layer, coordinates all aspects of control.
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

import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.SerialCommunicatorListener;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.visualizer.VisualizerUtils;
import java.io.File;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public abstract class AbstractController implements SerialCommunicatorListener {
    /** API Interface. */
    
    /**
     * Called before and after comm shutdown allowing device specific behavior.
     */
    abstract protected void closeCommBeforeEvent();
    abstract protected void closeCommAfterEvent();
    
    /**
     * Called after comm opening allowing device specific behavior.
     * @throws IOException 
     */
    protected void openCommAfterEvent() throws IOException {
    	// Empty default implementation. 
    }
    
    /**
     * Called before and after a send cancel allowing device specific behavior.
     */
    abstract protected void cancelSendBeforeEvent();
    abstract protected void cancelSendAfterEvent();
    
    /**
     * Called before the comm is paused and before it is resumed. 
     */
    abstract protected void pauseStreamingEvent() throws IOException;
    abstract protected void resumeStreamingEvent() throws IOException;
    
    /**
     * Called prior to streaming commands, throw an exception if not ready.
     */
    abstract protected void isReadyToStreamFileEvent() throws Exception;

    /**
     * Raw responses from the serial communicator.
     */
    abstract protected void rawResponseHandler(String response);
    
    /**
     * Performs homing cycle, throw an exception if not supported.
     */
    abstract public void performHomingCycle() throws Exception;
    
    /**
     * Returns machine to home location, throw an exception if not supported.
     */
    abstract public void returnToHome() throws Exception;
        
    /**
     * Reset machine coordinates to zero at the current location.
     */
    abstract public void resetCoordinatesToZero() throws Exception;
    
    /**
     * Disable alarm mode and put device into idle state, throw an exception 
     * if not supported.
     */
    abstract public void killAlarmLock() throws Exception;
    
    /**
     * Toggles check mode on or off, throw an exception if not supported.
     */
    abstract public void toggleCheckMode() throws Exception;
    
    /**
     * Request parser state, either print it here or expect it in the response
     * handler. Throw an exception if not supported.
     */
    abstract public void viewParserState() throws Exception;
    
    /**
     * Execute a soft reset, throw an exception if not supported.
     */
    abstract public void issueSoftReset() throws Exception;
    
    /**
     * Listener event for status update values;
     */
    abstract protected void statusUpdatesEnabledValueChanged(boolean enabled);
    abstract protected void statusUpdatesRateValueChanged(int rate);
    
    // These abstract objects are initialized in concrete class.
    protected AbstractCommunicator comm;
    protected GcodeCommandCreator commandCreator;
    
    // Outside influence
    private double speedOverride = -1;
    private int maxCommandLength = 50;
    private int truncateDecimalLength = 40;
    private boolean singleStepMode = false;
    private boolean removeAllWhitespace = true;
    private boolean statusUpdatesEnabled = true;
    private int statusUpdateRate = 200;
    private boolean convertArcsToLines = false;
    private double smallArcThreshold = 1.0;
    // Not configurable outside, but maybe it should be.
    private double arcLineLength = 0.3;
    
    // State
    private Boolean commOpen = false;
    
    // Parser state
    private Boolean absoluteMode = true;
    private Boolean metric = true;
    private int previousGCode = -1;
    private Point3d startPoint = null;
    
    // Added value
    private Boolean isStreaming = false;
    private Boolean paused = false;
    private long streamStart = 0;
    private long streamStop = 0;
    private File gcodeFile;
    
    // This metadata needs to be cached instead of inferred from queue's because
    // in case of a cancel the queues will be cleared.
    private int numCommands = 0;
    private int numCommandsSent = 0;
    private int numCommandsSkipped = 0;
    private int numCommandsCompleted = 0;
    
    // Structures for organizing all streaming commands.
    private LinkedList<GcodeCommand> prepQueue;             // preparing for send
    private LinkedList<GcodeCommand> outgoingQueue;         // waiting to be sent
    private LinkedList<GcodeCommand> awaitingResponseQueue; // waiting for response
    private LinkedList<GcodeCommand> completedCommandList;  // received response
    private LinkedList<GcodeCommand> errorCommandList;      // error in response
    
    // Listeners
    private ArrayList<ControllerListener> listeners;
    
    
    
    /**
     * Dependency injection constructor to allow a mock communicator.
     */
    protected AbstractController(AbstractCommunicator comm) {
        this.comm = comm;
        this.comm.setListenAll(this);
        
        this.prepQueue = new LinkedList<GcodeCommand>();
        this.outgoingQueue = new LinkedList<GcodeCommand>();
        this.awaitingResponseQueue = new LinkedList<GcodeCommand>();
        this.completedCommandList = new LinkedList<GcodeCommand>();
        this.errorCommandList = new LinkedList<GcodeCommand>();
        
        this.listeners = new ArrayList<ControllerListener>();
    }
    
    @Deprecated public AbstractController() {
        this(new GrblCommunicator()); //f4grx: connection created at opencomm() time
    }
    
    /**
     * Overrides the feed rate in gcode commands. Disable by setting to -1.
     */
    public void setSpeedOverride(double override) {
        this.speedOverride = override;
    }
    
    public double getSpeedOverride() {
        return this.speedOverride;
    }

    public void setMaxCommandLength(int length) {
        this.maxCommandLength = length;
    }
    
    public int getMaxCommandLength() {
        return this.maxCommandLength;
    }
    
    public void setTruncateDecimalLength(int length) {
        this.truncateDecimalLength = length;
    }
    
    public void setSingleStepMode(boolean enabled) {
        this.comm.setSingleStepMode(enabled);
    }

    public boolean getSingleStepMode() {
        return this.comm.getSingleStepMode();
    }
    
    public void setRemoveAllWhitespace(boolean enabled) {
        this.removeAllWhitespace = enabled;
    }

    public boolean getRemoveAllWhitespace() {
        return this.removeAllWhitespace;
    }

    public void setConvertArcsToLines(boolean enabled) {
        this.convertArcsToLines = enabled;
    }

    public boolean getConvertArcsToLines() {
        return this.convertArcsToLines;
    }
    
    public void setSmallArcThreshold(double length) {
        this.smallArcThreshold = length;
    }
    
    public double getSmallArcThreshold() {
        return this.smallArcThreshold;
    }
    
    public void setArcLineLength(double length) {
        this.arcLineLength = length;
    }
    
    public double getArcLineLength() {
        return this.arcLineLength;
    }
    
    public void setStatusUpdatesEnabled(boolean enabled) {
        if (this.statusUpdatesEnabled != enabled) {
            this.statusUpdatesEnabled = enabled;
            statusUpdatesEnabledValueChanged(enabled);
        }
    }
    
    public boolean getStatusUpdatesEnabled() {
        return this.statusUpdatesEnabled;
    }
    
    public void setStatusUpdateRate(int rate) {
        if (this.statusUpdateRate != rate) {
            this.statusUpdateRate = rate;
            statusUpdatesRateValueChanged(rate);
        }
    }
    
    public int getStatusUpdateRate() {
        return this.statusUpdateRate;
    }
    
    public Boolean openCommPort(String port, int portRate) throws Exception {
        if (this.commOpen) {
            throw new Exception("Comm port is already open.");
        }
        
        // No point in checking response, it throws an exception on errors.
        this.commOpen = this.comm.openCommPort(port, portRate);
        
        if (this.commOpen) {
            this.openCommAfterEvent();

            this.messageForConsole(
                   "**** Connected to " + port + " @ " + portRate + " baud ****\n");
        }
                
        return this.commOpen;
    }
    
    public Boolean closeCommPort() {
        // Already closed.
        if (this.commOpen == false) {
            return true;
        }
        
        this.closeCommBeforeEvent();
        
        this.messageForConsole("**** Connection closed ****\n");
        
        // I was noticing odd behavior, such as continuing to send 'ok's after
        // closing and reopening the comm port.
        // Note: The "Configuring-Grbl-v0.8" documentation recommends frequent
        //       soft resets, but also warns that the "startup" block will run
        //       on a reset and startup blocks may include motion commands.
        //this.issueSoftReset();
        this.flushSendQueues();
        this.commandCreator.resetNum();
        this.comm.closeCommPort();
        //this.comm = null;
        this.commOpen = false;
        
        this.closeCommAfterEvent();
        return true;
    }
    
    public Boolean isCommOpen() {
        // TODO: Query comm port for this information.
        return this.commOpen;
    }
    
    //// File send metadata ////
    
    public Boolean isStreamingFile() {
        return this.isStreaming;
    }
    
    /**
     * Send duration can be one of 3 things:
     * 1. the current running time of a send.
     * 2. the entire duration of the most recent send.
     * 3. 0 if there has never been a send.
     */
    public long getSendDuration() {
        // Last send duration.
        if (this.isStreaming == false) {
            return this.streamStop - this.streamStart;
        
        }
        // No send duration data available.
        else if (this.streamStart == 0L) {
            return 0L;
        }
        // Current send duration.
        else {
            return System.currentTimeMillis() - this.streamStart;
        }
    }
    
    public int rowsInSend() {
        return this.numCommands;
    }
    
    public int rowsSent() {
        return this.numCommandsSent;
    }
    
    public int rowsRemaining() {
        return this.numCommands - this.numCommandsCompleted - this.numCommandsSkipped;
    }
    
    /**
     * Creates a gcode command and queues it for send immediately.
     * Note: this is the only place where a string is sent to the comm.
     */
    public void queueStringForComm(String str) throws Exception {
        GcodeCommand command = this.commandCreator.createCommand(str);
        this.outgoingQueue.add(command);

        this.commandQueued(command);
        this.sendStringToComm(command.getCommandString());
    }
    
    /**
     * Accepts a command floating between the prepQueue and adds it to the next
     * stage.
     */
    private void queueCommandForComm(GcodeCommand command) throws Exception {
        // Don't send zero length commands.
        if (command.getCommandString().equals("")) {
            this.messageForConsole("Skipping command #" + command.getCommandNumber() + "\n");
            command.setResponse("<skipped by application>");
            command.setSkipped(true);
            // Need to queue the command first so that listeners don't
            // see a random command complete without notice.
            this.commandQueued(command);
            this.commandComplete(command);
            // For the listeners...
            dispatchCommandSent(command);
        } else if (command.getCommandString().length() > this.maxCommandLength) {
            throw new Exception("Command #" + command.getCommandNumber()
                    + " too long: (" + command.getCommandString().length()
                    + " > " + maxCommandLength + ") "
                    + command.getCommandString());
        } else {
            this.outgoingQueue.add(command);
            this.commandQueued(command);
            this.sendStringToComm(command.getCommandString());
        }
    }
    
    /**
     * This is the only place where commands with an expected 'ok'/'error'
     * response are sent to the comm.
     */
    private void sendStringToComm(String command) {
        this.comm.queueStringForComm(command+"\n");
        // Send command to the serial port.
        this.comm.streamCommands();
    }
    
    public Boolean isReadyToStreamFile() throws Exception {
        isReadyToStreamFileEvent();
        
        if (this.commOpen == false) {
            throw new Exception("Cannot begin streaming, comm port is not open.");
        }
        if (this.awaitingResponseQueue.size() != 0 || this.outgoingQueue.size() != 0) {
            throw new Exception("Cannot stream while there are active commands (controller).");
        }
        if (this.comm.areActiveCommands()) {
            throw new Exception("Cannot stream while there are active commands (communicator).");
        }

        return true;
    }
    
    /**
     * Appends command string to a queue awaiting to be sent.
     */
    public void appendGcodeCommand(String commandString) {
        GcodeCommand command = this.commandCreator.createCommand(commandString);
        this.prepQueue.add(command);
    }
    
    /**
     * Appends file of commands to a queue awaiting to be sent. Exception is
     * thrown on file IO errors.
     */
    public void appendGcodeFile(File file) throws IOException {
        this.gcodeFile = file;
        ArrayList<String> linesInFile = 
                VisualizerUtils.readFiletoArrayList(this.gcodeFile.getAbsolutePath());

        for (String line : linesInFile) {
            this.appendGcodeCommand(line);
        }
    }
    
    /**
     * Send all queued commands to comm port.
     */
    public void beginStreaming() throws Exception {
        this.isReadyToStreamFile();
        
        if (this.prepQueue.size() == 0) {
            throw new Exception("There are no commands queued for streaming.");
        }
        
        // Grbl's "Configuring-Grbl-v0.8" documentation recommends a soft reset
        // prior to starting a job. But will this cause GRBL to reset all the
        // way to reporting version info? Need to double check that before
        // enabling.
        //this.issueSoftReset();
        
        this.isStreaming = true;
        this.streamStop = 0;
        this.streamStart = System.currentTimeMillis();
        this.numCommands = 0;
        this.numCommandsSent = 0;
        this.numCommandsSkipped = 0;
        this.numCommandsCompleted = 0;
        this.startPoint = new Point3d(); // reset parser state.

        try {
            // Send all queued commands and wait for a response.
            GcodeCommand command;
            String[] processed;
            while (this.prepQueue.size() > 0) {
                command = this.prepQueue.remove();
                
                // TODO: Expand this to handle canned cycles (Issue#49)
                processed = this.preprocessCommand(command.getCommandString());
                
                for (String s : processed) {
                    command = new GcodeCommand(s.trim());
                    command.setCommandNumber(numCommands++);
                    queueCommandForComm(command);
                }
            }
            
            // Inform the GUI of the postprocessed number of commands.
            this.dispatchPostProcessData(numCommands);
        } catch(Exception e) {
            e.printStackTrace();
            this.isStreaming = false;
            this.streamStart = 0;
            throw e;
        }
    }
    
    public void pauseStreaming() throws IOException {
        this.messageForConsole("\n**** Pausing file transfer. ****\n\n");
        pauseStreamingEvent();
        this.paused = true;
        this.comm.pauseSend();
    }
    
    public void resumeStreaming() throws IOException {
        this.messageForConsole("\n**** Resuming file transfer. ****\n\n");
        resumeStreamingEvent();
        this.paused = false;
        this.comm.resumeSend();
    }
    
    public void cancelSend() {
        this.messageForConsole("\n**** Canceling file transfer. ****\n\n");

        cancelSendBeforeEvent();
        
        // Don't clear the command queue, there might be a situation where a
        // send is in progress while the next queue is being built. In which
        // case a cancel would only be expected to cancel the current action
        // to make way for the queued commands.
        //this.prepQueue.clear();
        
        this.outgoingQueue.clear();
        this.completedCommandList.clear();
        this.errorCommandList.clear();

        this.comm.cancelSend();
        
        cancelSendAfterEvent();
    }
    
    private void flushSendQueues() {
        this.prepQueue.clear();
        this.outgoingQueue.clear();
        this.awaitingResponseQueue.clear();
        this.completedCommandList.clear();
        this.errorCommandList.clear();
    }

    private void printStateOfQueues() {
        System.out.println("command queue size = " + this.prepQueue.size());
        System.out.println("outgoing queue size = " + this.outgoingQueue.size());
        System.out.println("awaiting response queue size = " + this.awaitingResponseQueue.size());
        System.out.println("completed command list size = " + this.completedCommandList.size());
        System.out.println("error command list size = " + this.errorCommandList.size());
        System.out.println("============");
        
    }
    
    // No longer a listener event
    private void commandQueued(GcodeCommand command) {
        dispatchCommandQueued(command);
    }

    // No longer a listener event
    private void fileStreamComplete(String filename, boolean success) {
        this.messageForConsole("\n**** Finished sending file. ****\n\n");
        this.streamStop = System.currentTimeMillis();
        this.isStreaming = false;
        dispatchStreamComplete(filename, success);        
    }

    private String[] preprocessGcodeCommand(String command) {
        

        return null;
    }
    
    // No longer a listener event
    private String[] preprocessCommand(String command) {
        String newCommand = command;

        // Remove comments from command.
        newCommand = GcodePreprocessorUtils.removeComment(newCommand);

        // Check for comment if length changed while preprocessing.
        if (command.length() != newCommand.length()) {
            dispatchCommandCommment(GcodePreprocessorUtils.parseComment(command));
        }
        
        // Override feed speed
        if (this.speedOverride > 0) {
            newCommand = GcodePreprocessorUtils.overrideSpeed(newCommand, this.speedOverride);
        }
        
        if (this.truncateDecimalLength > 0) {
            newCommand = GcodePreprocessorUtils.truncateDecimals(this.truncateDecimalLength, newCommand);
        }
        
        if (this.removeAllWhitespace) {
            newCommand = GcodePreprocessorUtils.removeAllWhitespace(newCommand);
        }
        
        String[] arr = {newCommand};
        
        // If this is enabled we need to keep track of some extra state data:
        // 1. Current units (inch/mm)
        // 2. Absolute/Referential mode
        // 3. Previous command, in case this one is implicit
        // 4. End point of the previous commands.
        if (this.convertArcsToLines) { // || this.expandCannedCycles) {
            List<Integer> l = GcodePreprocessorUtils.parseGCodes(newCommand);
            
            // Add here so that there isn't a special case for previous gcodes.
            if (l.isEmpty() && this.previousGCode != -1) {
                l.add(this.previousGCode);
            }
            
            for (Integer i : l) {
                switch (i) {
                    case 0:
                    case 1:
                        if (l.size() > 1) {
                            System.out.println("**** ERROR, too many GCodes for"
                                    + " this command type ***");
                        }
                        this.startPoint = 
                            GcodePreprocessorUtils.updatePointWithCommand(
                            newCommand, this.startPoint, this.absoluteMode);
                        break;
                        
                    // Arc command.
                    case 2:
                    case 3:
                        boolean clockwise = true;
                        if (i == 3) {
                            clockwise = false;
                        }
                        if (l.size() > 1) {
                            System.out.println("**** ERROR, too many GCodes for"
                                    + " this command type ***");
                        }
                        
                        List<String> args = 
                                GcodePreprocessorUtils.splitCommand(newCommand);
                        
                        Point3d nextPoint =
                            GcodePreprocessorUtils.updatePointWithCommand( args,
                            this.startPoint, this.absoluteMode);

                        Point3d center = 
                                GcodePreprocessorUtils.updateCenterWithCommand(
                                args, this.startPoint, false);

                        double radius = GcodePreprocessorUtils.parseCoord(args, 'R');
                        
                        // If radius was specified and IJK were not, convert R to IJK
                        if (!Double.isNaN(radius) && center.x == 0 && center.y == 0) {
                            center = GcodePreprocessorUtils.convertRToCenter(
                                    this.startPoint, nextPoint, radius, false,
                                    clockwise);
                        }
                        
                        // Calculate radius if necessary.
                        if (Double.isNaN(radius)) {
                            radius = Math.sqrt(Math.pow(this.startPoint.x - center.x, 2.0) + Math.pow(this.startPoint.y - center.y, 2.0));
                        }

                        /*
                        use GcodePreProcessorUtils.getAngle(point1, point2) multiplied by the radius to decide
                        whether or not its worth chopping the arc into segments.GcodePreProcessorUtils
                                
                        then refactor the arc function out of GcodeViewParse until there are pieces which can be
                        used for this AND the visualizer.
                         
                        Note: Be sure to convert from absolute to relative mode if needed!!
                        */
                        /*
                         * Regrettably lots of code below is copy/pasted from the
                         * first half of the arc calculation, because the sweep
                         * distance is an interim calculation.
                         */

                        // Calculate angles from center.
                        double startAngle = GcodePreprocessorUtils.getAngle(center, this.startPoint);
                        double endAngle = GcodePreprocessorUtils.getAngle(center, nextPoint);

                        // Fix semantics, if the angle ends at 0 it really should end at 360.
                        if (endAngle == 0) {
                                endAngle = Math.PI * 2;
                        }

                        // Calculate distance along arc.
                        double sweep;
                        if (!clockwise && endAngle < startAngle) {
                            sweep = ((Math.PI * 2 - startAngle) + endAngle);
                        } else if (clockwise && endAngle > startAngle) {
                            sweep = ((Math.PI * 2 - endAngle) + startAngle);
                        } else {
                            sweep = Math.abs(endAngle - startAngle);
                        }
                        
                        double distance = sweep * radius;
                        if (this.metric == false) {
                            distance *= 25.4;
                        }
                        
                        // If the arc is small enough, generate a few points and add them to an array...
                        if (distance < this.smallArcThreshold) {
                            int numLines = (new Double(distance / arcLineLength)).intValue();
                            List<Point3d> points =
                                    GcodePreprocessorUtils.generatePointsAlongArcBDring(
                                    this.startPoint, nextPoint, center, clockwise, radius,
                                    startAngle, endAngle, sweep, numLines);
                            
                            // Create the commands...
                            arr = new String[points.size()];
                            int index = 0;
                            StringBuilder sb;
                            Point3d start = new Point3d(this.startPoint);
                            
                            sb = new StringBuilder("#.");
                            for (index = 0; index < truncateDecimalLength; index++) {
                                sb.append("#");
                            }
                            DecimalFormat df = new DecimalFormat(sb.toString());
                            index = 0;
                            
                            for (Point3d p : points) {
                                sb = new StringBuilder();
                                sb.append("G1");
                                
                                if (this.absoluteMode) {
                                    if (!Double.isNaN(p.x)) {
                                        sb.append("X");
                                        sb.append(df.format(p.x));
                                    }
                                    if (!Double.isNaN(p.y)) {
                                        sb.append("Y");
                                        sb.append(df.format(p.y));
                                    }
                                    if (!Double.isNaN(p.z)) {
                                        sb.append("Z");
                                        sb.append(df.format(p.z));
                                    }
                                } else { // calculate offsets.
                                    if (!Double.isNaN(p.x)) {
                                        sb.append("X");
                                        sb.append(df.format(p.x-start.x));
                                    }
                                    if (!Double.isNaN(p.y)) {
                                        sb.append("Y");
                                        sb.append(df.format(p.y-start.x));
                                    }
                                    if (!Double.isNaN(p.z)) {
                                        sb.append("Z");
                                        sb.append(df.format(p.z-start.x));
                                    }
                                }
                                System.out.println("New G1 command: " + sb.toString());
                                arr[index++] = sb.toString();
                            }
                        }
                        
                        // Save off this point in case the next command is an
                        // arc too.
                        this.startPoint = nextPoint;
                        break;

                    case 20:
                        //inch
                        this.metric = false;
                        break;
                    case 21:
                        //mm
                        this.metric = true;
                        break;
                        
                    case 90:
                        this.absoluteMode = true;
                        break;
                    case 91:
                        this.absoluteMode = false;
                        break;
                }
                this.previousGCode = i;
            }
        }

        // Return the post processed command.
        return arr;
    }
    
    @Override
    public void commandSent(String command) {
        if (this.isStreamingFile()) {
            this.numCommandsSent++;
        }

        GcodeCommand c = this.outgoingQueue.remove();
        c.setSent(true);
        
        if (c.getCommandString() != command) {
            this.errorMessageForConsole("Command <"+c.getCommandString()+
                    "> does not equal expected command <"+command+">");
        }
        
        this.awaitingResponseQueue.add(c);
        
        dispatchCommandSent(c);
    }
    
    /**
     * Notify controller that the next command has completed with response.
     */
    public void commandComplete(String response) throws Exception {
        GcodeCommand command = this.awaitingResponseQueue.peek();
        if (command == null) {
            command = new GcodeCommand("");
        }
        command.setResponse(response);
        this.commandComplete(command);
    }
    
    /**
     * Internal command complete has extra handling for skipped command case.
     */
    private void commandComplete(GcodeCommand command) throws Exception {
        GcodeCommand c = command;

        // If the command wasn't sent, it was skipped and should be ignored
        // from the remaining queues.
        if (!command.isSkipped()) {
            if (this.awaitingResponseQueue.size() == 0) {
                throw new Exception("Attempting to completing a command that "
                        + "doesn't exist: <" + command.toString() + ">");
            }
            
            c = this.awaitingResponseQueue.remove();
            c.setResponse(command.getResponse());
            this.completedCommandList.add(c);
            
            if (this.isStreamingFile()) {
                this.numCommandsCompleted++;
            }
        } else {
            if (this.isStreamingFile()) {
                this.numCommandsSkipped++;
            }
        }
        
        dispatchCommandComplete(c);
        
        if (this.isStreamingFile() &&
                this.awaitingResponseQueue.size() == 0 &&
                this.outgoingQueue.size() == 0 &&
                this.prepQueue.size() == 0) {
            String streamName = "queued commands";
            if (this.gcodeFile != null) {
                streamName = this.gcodeFile.getName();
            } 
            
            boolean status = true;
            if (this.rowsRemaining() != 0) {
                status = false;
            }
            
            boolean isSuccess = (this.numCommands == (this.numCommandsSent + this.numCommandsSkipped));
            this.fileStreamComplete(streamName, isSuccess);
        }
    }

    @Override
    public void messageForConsole(String msg) {
        dispatchConsoleMessage(msg, Boolean.FALSE);
    }
    
    @Override
    public void verboseMessageForConsole(String msg) {
        dispatchConsoleMessage(msg, Boolean.TRUE);
    }
    
    @Override
    public void errorMessageForConsole(String msg) {
        dispatchConsoleMessage("[Error] " + msg, Boolean.TRUE);
    }


    @Override
    public void rawResponseListener(String response) {
        rawResponseHandler(response);
    }

    /**
     * Listener management.
     */
    public void addListener(ControllerListener cl) {
        this.listeners.add(cl);
    }

    protected void dispatchStatusString(String state, Point3d machine, Point3d work) {
        if (listeners != null) {
            for (ControllerListener c : listeners) {
                c.statusStringListener(state, machine, work);
            }
        }
    }
    
    protected void dispatchConsoleMessage(String message, Boolean verbose) {
        if (listeners != null) {
            for (ControllerListener c : listeners) {
                c.messageForConsole(message, verbose);
            }
        }
    }
    
    protected void dispatchStreamComplete(String filename, Boolean success) {
        if (listeners != null) {
            for (ControllerListener c : listeners) {
                c.fileStreamComplete(filename, success);
            }
        }
    }
    
    protected void dispatchCommandQueued(GcodeCommand command) {
        if (listeners != null) {
            for (ControllerListener c : listeners) {
                c.commandQueued(command);
            }
        }
    }
    
    protected void dispatchCommandSent(GcodeCommand command) {
        if (listeners != null) {
            for (ControllerListener c : listeners) {
                c.commandSent(command);
            }
        }
    }
    
    protected void dispatchCommandComplete(GcodeCommand command) {
        if (listeners != null) {
            for (ControllerListener c : listeners) {
                c.commandComplete(command);
            }
        }
    }
    
    protected void dispatchCommandCommment(String comment) {
        if (listeners != null) {
            for (ControllerListener c : listeners) {
                c.commandComment(comment);
            }
        }
    }
    
    protected void dispatchPostProcessData(int numRows) {
        if (listeners != null) {
            for (ControllerListener c : listeners) {
                c.postProcessData(numRows);
            }
        }
    }
}
