/*
    Copywrite 2015-2016 Will Winder

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
package com.willwinder.universalgcodesender.model;

import com.willwinder.universalgcodesender.AbstractController;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.utils.*;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.gcode.processors.ArcExpander;
import com.willwinder.universalgcodesender.gcode.processors.CommandLengthProcessor;
import com.willwinder.universalgcodesender.gcode.processors.CommandSplitter;
import com.willwinder.universalgcodesender.gcode.processors.CommentProcessor;
import com.willwinder.universalgcodesender.gcode.processors.DecimalProcessor;
import com.willwinder.universalgcodesender.gcode.processors.FeedOverrideProcessor;
import com.willwinder.universalgcodesender.gcode.processors.M30Processor;
import com.willwinder.universalgcodesender.gcode.processors.WhitespaceProcessor;
import com.willwinder.universalgcodesender.model.Utils.Units;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.UGSEvent.ControlState;
import com.willwinder.universalgcodesender.model.UGSEvent.FileState;
import com.willwinder.universalgcodesender.pendantui.SystemStateBean;
import com.willwinder.universalgcodesender.types.GcodeCommand;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;

/**
 *
 * @author wwinder
 */
public class GUIBackend implements BackendAPI, ControllerListener {
    private static final Logger logger = Logger.getLogger(GUIBackend.class.getName());
    private static final String NEW_LINE = "\n    ";
    private static final int AUTO_DISCONNECT_THRESHOLD = 5000;

    private AbstractController controller = null;
    private Settings settings = null;
    private Position machineCoord = null;
    private Position workCoord = null;
    private Units reportUnits = Units.UNKNOWN;

    private String state;
    private final Collection<ControllerListener> controllerListeners = new ArrayList<>();
    private final Collection<UGSEventListener> controlStateListeners = new ArrayList<>();

    // GUI State
    private File gcodeFile = null;
    private File processedGcodeFile = null;
    private File tempDir = null;
    private String lastComment;
    private String activeState;
    private ControlState controlState = ControlState.COMM_DISCONNECTED;
    private long estimatedSendDuration = -1L;
    //private long estimatedSendTimeRemaining = 0;
    //private long rowsInFile = 0;
    private String openCloseButtonText;
    private boolean openCloseButtonEnabled;
    private String pauseButtonText;
    private String cancelButtonText;

    private long lastResponse = Long.MIN_VALUE;
    private long lastConnectAttempt = Long.MIN_VALUE;
    private boolean streamFailed = false;
    private boolean autoconnect = false;
    private final java.util.Timer autoConnectTimer = new Timer("AutoConnectTimer", true);
    
    public GcodeParser gcp = new GcodeParser();

    public GUIBackend() {
        scheduleTimers();
    }

    protected final void scheduleTimers() {
        autoConnectTimer.scheduleAtFixedRate(new TimerTask() {
            private int count = 0;
            @Override
            public void run() {
                autoconnect();

                // Move the mouse every 30 seconds to prevent sleeping.
                if (isPaused() || isSending()) {
                    count++;
                    if (count % 10 == 0) {
                        keepAwake();
                        count = 0;
                    }
                }
            }
        }, 1000, 1000);
    }

    @Override
    public void addUGSEventListener(UGSEventListener listener) {
        logger.log(Level.INFO, "Adding control state listener.");
        controlStateListeners.add(listener);
    }
    
    @Override
    public void addControllerListener(ControllerListener listener) {
        logger.log(Level.INFO, "Adding controller listener.");
        controllerListeners.add(listener);
        if (this.controller != null) {
            this.controller.addListener(listener);
        }
    }
    
    //////////////////
    // GUI API
    //////////////////
    
    @Override
    public void preprocessAndExportToFile(File f) throws Exception {
        gcp.reset();
        try(BufferedReader br = new BufferedReader(new FileReader(this.getGcodeFile()))) {
            try (GcodeStreamWriter gsw = new GcodeStreamWriter(f)) {
                int i = 0;
                for(String line; (line = br.readLine()) != null; ) {
                    i++;
                    if (i % 1000000 == 0) {
                        logger.log(Level.FINE, "i: " + i);
                    }
                    String comment = GcodePreprocessorUtils.parseComment(line);
                    // Parse the gcode for the buffer.
                    Collection<String> lines = gcp.preprocessCommand(line);

                    // If it is a comment-only line, add the comment,
                    if (!comment.isEmpty() && lines.isEmpty()) {
                        gsw.addLine(line, "", comment, i);
                    }
                    // Otherwise add each processed line (often just one line).
                    else {
                        for(String processedLine : lines) {
                            gsw.addLine(line, processedLine, comment, i);
                            gcp.addCommand(processedLine);
                        }
                    }
                }
            }
        }
    }

    @Override
    public void connect(String firmware, String port, int baudRate) throws Exception {
        logger.log(Level.INFO, "Connecting to {0} on port {1}", new Object[]{firmware, port});
        lastConnectAttempt = System.currentTimeMillis();

        this.controller = FirmwareUtils.getControllerFor(firmware);
        applySettingsToController(settings, this.controller);

        this.controller.addListener(this);
        for (ControllerListener l : controllerListeners) {
            this.controller.addListener(l);
        }
        
        if (openCommConnection(port, baudRate)) {
            this.sendControlStateEvent(new UGSEvent(ControlState.COMM_IDLE));
            streamFailed = false;   //reset
        }
    }

    @Override
    public boolean isConnected() {
        boolean isConnected = this.controlState != ControlState.COMM_DISCONNECTED;
        logger.log(Level.INFO, "Is connected: {0}", isConnected);
        return isConnected;
    }
    
    @Override
    public void disconnect() throws Exception {
        autoconnect = false;
        disconnectInternal();
    }

    private void disconnectInternal() throws Exception {
        logger.log(Level.INFO, "Disconnecting.");
        if (this.controller != null) {
            this.controller.closeCommPort();
            this.controller = null;
            this.sendControlStateEvent(new UGSEvent(ControlState.COMM_DISCONNECTED));
        }
    }

    public void autoconnect() {
        if (!autoconnect) {
            return;
        }

        // This breaks when a machine is homing. GRBL at least will stop sending
        // status during a homing operation.
        /*
        // Check if a timeout has occurred.
        if (controller.getStatusUpdatesEnabled() && settings.isAutoReconnect()) {
            long now = System.currentTimeMillis();
            if (now - lastResponse > AUTO_DISCONNECT_THRESHOLD &&
                    now - lastConnectAttempt > AUTO_DISCONNECT_THRESHOLD ) {
                logger.log(Level.INFO, "No Response in " + (now - lastResponse)+"ms.");
                if (controller != null && controller.isStreamingFile()) {
                    streamFailed = true;
                }

                try {
                    disconnectInternal();
                } catch (Exception e) {
                    logger.log(Level.INFO, "Disconnect failed ", e);
                }
            }
        }
        */

        if (!isConnected()) {
            if (settings == null || streamFailed) {
                return;
            }
            if (lastResponse == Long.MIN_VALUE && autoconnect) {
                logger.log(Level.INFO, "Attempting auto connect.");
            } else if (lastResponse > Long.MIN_VALUE && settings.isAutoReconnect()) {
                logger.log(Level.INFO, "Attempting auto reconnect.");
            } else {
                return;
            }

            try {
                String[] portList = CommUtils.getSerialPortList();
                boolean portMatch = false;
                for (String port : portList) {
                    if (port.equals(settings.getPort())) {
                        portMatch = true;
                        break;
                    }
                }

                if (portMatch) {
                    connect(settings.getFirmwareVersion(), settings.getPort(), Integer.parseInt(settings.getPortRate()));
                }
            } catch (Exception e) {
                logger.log(Level.INFO, "Auto connect failed",e);
            }
        }
    }

    public void keepAwake() {
        logger.log(Level.INFO, "Moving the mouse location slightly to keep the computer awake.");
        try {
            Robot hal = new Robot();
            Point pObj = MouseInfo.getPointerInfo().getLocation();
            hal.mouseMove(pObj.x + 1, pObj.y + 1);
            hal.mouseMove(pObj.x - 1, pObj.y - 1);
            pObj = MouseInfo.getPointerInfo().getLocation();
            System.out.println(pObj.toString() + "x>>" + pObj.x + "  y>>" + pObj.y);
        } catch (AWTException ex) {
            Logger.getLogger(GUIBackend.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void applySettings(Settings settings) throws Exception {
        logger.log(Level.INFO, "Applying settings.");
        this.settings = settings;
        if (this.controller != null) {
            applySettingsToController(this.settings, this.controller);
        }

        // Configure gcode parser.
        gcp.resetCommandProcessors();

        //gcp.setRemoveAllWhitespace(settings.isRemoveAllWhitespace());
        if (settings.isRemoveAllWhitespace()) {
            gcp.addCommandProcessor(new WhitespaceProcessor());
        }

        //gcp.setSpeedOverride(value);
        if (settings.isOverrideSpeedSelected()) {
            double value = settings.getOverrideSpeedValue();
            gcp.addCommandProcessor(new FeedOverrideProcessor(value));
        }

        gcp.addCommandProcessor(new CommentProcessor());

        gcp.addCommandProcessor(new DecimalProcessor(settings.getTruncateDecimalLength()));

        gcp.addCommandProcessor(new M30Processor());

        gcp.addCommandProcessor(new CommandLengthProcessor(50));

        if (settings.isConvertArcsToLines()) {
            gcp.addCommandProcessor(new CommandSplitter());
            gcp.addCommandProcessor(new ArcExpander(true, settings.getSmallArcSegmentLength(), settings.getTruncateDecimalLength()));
        }
    }

    @Override
    public void updateSystemState(SystemStateBean systemStateBean) {
        logger.log(Level.INFO, "Getting system state 'updateSystemState'");
        if (gcodeFile != null)
            systemStateBean.setFileName(gcodeFile.getAbsolutePath());
        systemStateBean.setLatestComment(lastComment);
        systemStateBean.setActiveState(activeState);
        systemStateBean.setControlState(controlState);
        if (this.machineCoord != null) {
            systemStateBean.setMachineX(Utils.formatter.format(this.machineCoord.getX()));
            systemStateBean.setMachineY(Utils.formatter.format(this.machineCoord.getY()));
            systemStateBean.setMachineZ(Utils.formatter.format(this.machineCoord.getZ()));
        }
        if (this.controller != null) {
            systemStateBean.setRemainingRows(String.valueOf(this.getNumRemainingRows()));
            systemStateBean.setRowsInFile(String.valueOf(this.getNumRows()));
            systemStateBean.setSentRows(String.valueOf(this.getNumSentRows()));
            systemStateBean.setDuration(String.valueOf(this.getSendDuration()));
            systemStateBean.setEstimatedTimeRemaining(String.valueOf(this.getSendRemainingDuration()));
        }
        if (this.workCoord != null) {
            systemStateBean.setWorkX(Utils.formatter.format(this.workCoord.getX()));
            systemStateBean.setWorkY(Utils.formatter.format(this.workCoord.getY()));
            systemStateBean.setWorkZ(Utils.formatter.format(this.workCoord.getZ()));
        }
        systemStateBean.setSendButtonText(openCloseButtonText);
        systemStateBean.setSendButtonEnabled(openCloseButtonEnabled);
        systemStateBean.setPauseResumeButtonText(pauseButtonText);
        systemStateBean.setPauseResumeButtonEnabled(this.canPause());
        systemStateBean.setCancelButtonText(cancelButtonText);
        systemStateBean.setCancelButtonEnabled(this.canCancel());
    }

    @Override
    public void sendGcodeCommand(String commandText) throws Exception {
        GcodeCommand command = controller.createCommand(commandText);
        sendGcodeCommand(command);
    }

    @Override
    public void sendGcodeCommand(GcodeCommand command) throws Exception {
        logger.log(Level.INFO, "Sending gcode command: {0}", command.getCommandString());
        this.sendControlStateEvent(new UGSEvent(ControlState.COMM_SENDING));
        controller.sendCommandImmediately(command);
    }

    /**
     * Sends a G91 command in some combination of x, y, and z directions with a
     * step size of stepDirection.
     * 
     * Direction is specified by the direction param being positive or negative.
     */
    @Override
    public void adjustManualLocation(int dirX, int dirY, int dirZ, double stepSize, Units units) throws Exception {
        logger.log(Level.INFO, "Adjusting manual location.");
        // Don't send empty commands.
        if ((dirX == 0) && (dirY == 0) && (dirZ == 0)) {
            return;
        }

        // Format step size from spinner.
        String formattedStepSize = Utils.formatter.format(stepSize);

        // Build G91 command.
        StringBuilder builder = new StringBuilder();
        
        // Set jog command to the preferred units.
        if (units == Units.INCH) {
            builder.append("G20 ");
        } else if (units == Units.MM) {
            builder.append("G21 ");
        }

        builder.append("G91 G0 ");
        
        if (dirX != 0) {
            builder.append(" X");
            if (dirX < 0) {
                builder.append('-');
            }
            builder.append(formattedStepSize);
        } if (dirY != 0) {
            builder.append(" Y");
            if (dirY < 0) {
                builder.append('-');
            }
            builder.append(formattedStepSize);
        } if (dirZ != 0) {
            builder.append(" Z");
            if (dirZ < 0) {
                builder.append('-');
            }
            builder.append(formattedStepSize);
        }

        GcodeCommand command = controller.createCommand(builder.toString());
        command.setTemporaryParserModalChange(true);
        controller.sendCommandImmediately(command);
        controller.restoreParserModalState();
    }

    @Override
    public Settings getSettings() {
        logger.log(Level.INFO, "Getting settings.");
        return this.settings;
    }

    @Override
    public ControlState getControlState() {
        logger.log(Level.INFO, "Getting control state.");
        return this.controlState;
    }
    
    @Override
    public IController getController() {
        logger.log(Level.INFO, "Getting controller");
        return this.controller;
    }
    
    @Override
    public void setTempDir(File file) throws IOException {
        if (file.isDirectory())
            this.tempDir = file;
        else
            throw new IOException("Temp dir " + file.toString() + " is not a directory.");
    }

    private File getTempDir() {
        if (tempDir == null) {
            tempDir = new File(System.getProperty("java.io.tmpdir"));
        }
        return tempDir;
    }

    @Override
    public void setGcodeFile(File file) throws Exception {
        logger.log(Level.INFO, "Setting gcode file.");
        this.gcodeFile = file;
        this.processedGcodeFile = null;

        this.sendControlStateEvent(new UGSEvent(FileState.FILE_LOADING,
                file.getAbsolutePath()));

        initializeProcessedLines(true);

        this.sendControlStateEvent(new UGSEvent(FileState.FILE_LOADED,
                processedGcodeFile.getAbsolutePath()));
    }
    
    @Override
    public File getGcodeFile() {
        logger.log(Level.INFO, "Getting gcode file.");
        return this.gcodeFile;
    }

    @Override
    public File getProcessedGcodeFile() {
        logger.log(Level.INFO, "Getting processed gcode file.");
        return this.processedGcodeFile;
    }
    
    @Override
    public void send() throws Exception {
        logger.log(Level.INFO, "Sending gcode file.");
        // Note: there is a divide by zero error in the timer because it uses
        //       the rowsValueLabel that was just reset.

        try {
            // This will throw an exception and prevent that other stuff from
            // happening (clearing the table before its ready for clearing.
            this.controller.isReadyToStreamFile();

            this.sendControlStateEvent(new UGSEvent(ControlState.COMM_SENDING));

            //this.controller.queueCommands(processedCommandLines);
            //this.controller.queueStream(new BufferedReader(new FileReader(this.processedGcodeFile)));
            this.controller.queueStream(new GcodeStreamReader(this.processedGcodeFile));

            this.controller.beginStreaming();
        } catch (Exception e) {
            this.sendControlStateEvent(new UGSEvent(ControlState.COMM_IDLE));
            e.printStackTrace();
            throw new Exception(Localization.getString("mainWindow.error.startingStream") + ": "+e.getMessage());
        }
    }
    
    @Override
    public long getNumRows() {
        //logger.log(Level.INFO, "Getting number of rows.");
        return this.controller.rowsInSend();
    }
    
    @Override
    public long getNumSentRows() {
        //logger.log(Level.INFO, "Getting number of sent rows.");
        return controller == null ? 0 : controller.rowsSent();
    }

    @Override
    public long getNumRemainingRows() {
        return controller == null ? 0 : controller.rowsRemaining();
    }

    @Override
    public long getSendDuration() {
        return controller == null ? 0 : controller.getSendDuration();
    }
    
    @Override
    public long getSendRemainingDuration() {
        long sent = this.getNumSentRows();

        // Early exit condition. Can't make an estimate if we haven't started.
        if (sent == 0) { return -1L; }

        long estimate = this.estimatedSendDuration;
        
        long elapsedTime = this.getSendDuration();
        // If we don't have an actual duration estimate, make a crude estimate.
        if (estimate <= 0) {
            long timePerCode = elapsedTime / sent;
            estimate = timePerCode * this.getNumRows();
        }
        
        return estimate - elapsedTime;
    }

    @Override
    public void pauseResume() throws Exception {
        logger.log(Level.INFO, "Pause/Resume");
        try {
            switch(controlState) {
                case COMM_SENDING:
                    this.controller.pauseStreaming();
                    this.sendControlStateEvent(new UGSEvent(ControlState.COMM_SENDING_PAUSED));
                    return;
                case COMM_SENDING_PAUSED:
                    this.controller.resumeStreaming();
                    this.sendControlStateEvent(new UGSEvent(ControlState.COMM_SENDING));
                    return;
                default:
                    throw new Exception();
            }
        } catch (Exception e) {
            logger.log(Level.INFO, "Exception in pauseResume", e);
            throw new Exception(Localization.getString("mainWindow.error.pauseResume"));
        }
    }
    
    @Override
    public String getPauseResumeText() {
        if (isPaused())
            return Localization.getString("mainWindow.ui.resumeButton");
        else
            return Localization.getString("mainWindow.ui.pauseButton");
    }
    
    @Override
    public boolean isSending() {
        return this.controlState == ControlState.COMM_SENDING;
    }

    @Override
    public boolean isIdle() {
        try {
            return this.controller.isReadyToStreamFile();
        } catch(Exception e) {
            return false;
        }
    }
    
    @Override
    public boolean isPaused() {
        return this.controlState == ControlState.COMM_SENDING_PAUSED;
    }
    
    @Override
    public boolean canPause() {
        return this.controlState == ControlState.COMM_SENDING;
    }

    @Override
    public boolean canCancel() {
        return canPause() || isPaused();
    }
    
    @Override
    public boolean canSend() {
        return (this.controlState == ControlState.COMM_IDLE) && (this.gcodeFile != null);
    }
    
    @Override
    public void cancel() throws Exception {
        if (this.canCancel()) {
            this.controller.cancelSend();
            this.sendControlStateEvent(new UGSEvent(ControlState.COMM_IDLE));
        }
    }

    @Override
    public void returnToZero() throws Exception {
        this.controller.returnToHome();
    }

    @Override
    public void resetCoordinatesToZero() throws Exception {
        this.controller.resetCoordinatesToZero();
    }

    @Override
    public void resetCoordinateToZero(char coordinate) throws Exception {
        this.controller.resetCoordinateToZero(coordinate);
    }
    
    @Override
    public void killAlarmLock() throws Exception {
        this.controller.killAlarmLock();
    }
    
    @Override
    public void performHomingCycle() throws Exception {
        this.controller.performHomingCycle();
    }
    
    @Override
    public void toggleCheckMode() throws Exception {
        this.controller.toggleCheckMode();
    }
    
    @Override
    public void issueSoftReset() throws Exception {
        this.controller.issueSoftReset();
    }
    
    @Override
    public void requestParserState() throws Exception {
        this.controller.viewParserState();
    }

    @Override
    public void performAction(ACTIONS action) throws Exception {
        switch (action) {
            case RETURN_TO_ZERO:
                returnToZero();
                break;
            case RESET_COORDINATES_TO_ZERO:
                resetCoordinatesToZero();
                break;
            case KILL_ALARM_LOCK:
                killAlarmLock();
                break;
            case HOMING_CYCLE:
                performHomingCycle();
                break;
            case TOGGLE_CHECK_MODE:
                toggleCheckMode();
                break;
            case ISSUE_SOFT_RESET:
                issueSoftReset();
                break;
            case REQUEST_PARSER_STATE:
                requestParserState();
                break;
            default:
                break;
        }
    }

    //////////////////
    // Controller Listener
    //////////////////
    @Override
    public void controlStateChange(ControlState state) {
        this.sendControlStateEvent(new UGSEvent(state));
    }

    @Override
    public void fileStreamComplete(String filename, boolean success) {
        this.sendControlStateEvent(new UGSEvent(ControlState.COMM_IDLE));
    }

    @Override
    public void commandSkipped(GcodeCommand command) {
    }

    @Override
    public void commandSent(GcodeCommand command) {
    }

    @Override
    public void commandComplete(GcodeCommand command) {
        controller.updateParserModalState(command);
        if (isIdle()) {
            this.sendControlStateEvent(new UGSEvent(ControlState.COMM_IDLE));
        }
    }

    @Override
    public void commandComment(String comment) {
        this.lastComment = comment;
    }

    @Override
    public void messageForConsole(MessageType type, String msg) {
        if (type == MessageType.ERROR) {
            GUIHelpers.displayErrorDialog(msg);
        }
    }

    @Override
    public void statusStringListener(String state, Position machineCoord, Position workCoord) {
        this.activeState = state;
        this.machineCoord = machineCoord;
        this.workCoord = workCoord;
        this.reportUnits = machineCoord.getUnits();
        this.lastResponse = System.currentTimeMillis();
    }

    @Override
    public void postProcessData(int numRows) {
    }
    
    ////////////////////
    // Utility functions
    ////////////////////
    
    /**
     * This would be static but I want to define it in the interface.
     * @param settings Settings to apply to the controller.
     * @param controller Controller to receive settings.
     * @throws java.lang.Exception Exception thrown if controller doesn't support some settings.
     */
    @Override
    public void applySettingsToController(Settings settings, IController controller) throws Exception {
        if (settings == null) {
            throw new Exception("Programmer error.");
        }
        autoconnect = settings.isAutoConnectEnabled();
        // Apply settings settings to controller.

        try {
            controller.getCommandCreator().setMaxCommandLength(settings.getMaxCommandLength());
            
            controller.setSingleStepMode(settings.isSingleStepMode());
            controller.setStatusUpdatesEnabled(settings.isStatusUpdatesEnabled());
            controller.setStatusUpdateRate(settings.getStatusUpdateRate());
        } catch (Exception ex) {

            StringBuilder message = new StringBuilder()
                    .append(Localization.getString("mainWindow.error.firmwareSetting"))
                    .append(": \n    ")
                    .append(Localization.getString("firmware.feature.maxCommandLength")).append(NEW_LINE)
                    .append(Localization.getString("firmware.feature.truncateDecimal")).append(NEW_LINE)
                    .append(Localization.getString("firmware.feature.singleStep")).append(NEW_LINE)
                    .append(Localization.getString("firmware.feature.removeWhitespace")).append(NEW_LINE)
                    .append(Localization.getString("firmware.feature.linesToArc")).append(NEW_LINE)
                    .append(Localization.getString("firmware.feature.statusUpdates")).append(NEW_LINE)
                    .append(Localization.getString("firmware.feature.statusUpdateRate"));
            
            throw new Exception(message.toString());
        }
    }

    @Override
    public void sendMessageForConsole(String msg) {
        if (controller != null) {
            controller.messageForConsole(msg);
        } else {
            //should still send!  Controller probably shouldn't ever be null.
        }
    }
    
    /////////////////////
    // Private functions.
    /////////////////////
    
    private boolean openCommConnection(String port, int baudRate) throws Exception {
        boolean connected = false;
        try {
            connected = controller.openCommPort(port, baudRate);
            
            this.initializeProcessedLines(false);
        } catch (Exception e) {
            logger.log(Level.INFO, "Exception in openCommConnection.", e);
            throw new Exception(Localization.getString("mainWindow.error.connection")
                    + " ("+ e.getClass().getName() + "): "+e.getMessage());
        }
        return connected;
    }

    private void initializeProcessedLines(boolean forceReprocess) throws FileNotFoundException, Exception {
        if (this.gcodeFile != null) {
            Charset cs;
            try (FileReader fr = new FileReader(this.gcodeFile)) {
                cs = Charset.forName(fr.getEncoding());
            }
            logger.info("Start preprocessing");
            long start = System.currentTimeMillis();
            if (this.processedGcodeFile == null || forceReprocess) {
                this.processedGcodeFile = new File(this.getTempDir(), this.gcodeFile.getName());
                this.preprocessAndExportToFile(processedGcodeFile);
            }
            long end = System.currentTimeMillis();
            logger.info("Took " + (end - start) + "ms to preprocess");

            if (this.isConnected()) {
                this.estimatedSendDuration = -1L;

                Thread estimateThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        estimatedSendDuration = controller.getJobLengthEstimate(processedGcodeFile);
                    }
                });
                estimateThread.start();
            }
        }
    }
    
    private void sendControlStateEvent(UGSEvent event) {
        if (event.isStateChangeEvent()) {
            this.controlState = event.getControlState();
        }
        
        for (UGSEventListener l : controlStateListeners) {
            logger.info("Sending control state change.");
            l.UGSEvent(event);
        }
    }

    @Override
    public void sendOverrideCommand(Overrides override) throws Exception {
        this.controller.sendOverrideCommand(override);
    }
}
