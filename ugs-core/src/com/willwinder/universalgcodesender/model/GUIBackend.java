/*
    Copyright 2015-2017 Will Winder

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

import com.google.common.io.Files;
import com.willwinder.universalgcodesender.AbstractController;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.utils.*;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.GcodeStats;
import com.willwinder.universalgcodesender.gcode.processors.CommandLengthProcessor;
import com.willwinder.universalgcodesender.gcode.processors.CommentProcessor;
import com.willwinder.universalgcodesender.gcode.processors.DecimalProcessor;
import com.willwinder.universalgcodesender.gcode.processors.M30Processor;
import com.willwinder.universalgcodesender.gcode.processors.WhitespaceProcessor;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserUtils;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerStateListener;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.model.UGSEvent.ControlState;
import com.willwinder.universalgcodesender.model.UGSEvent.FileState;
import com.willwinder.universalgcodesender.pendantui.SystemStateBean;
import com.willwinder.universalgcodesender.types.GcodeCommand;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.UGSEvent.EventType;
import com.willwinder.universalgcodesender.utils.Settings.FileStats;
import java.awt.AWTException;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.Robot;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import com.willwinder.universalgcodesender.gcode.processors.CommandProcessor;

/**
 *
 * @author wwinder
 */
public class GUIBackend implements BackendAPI, ControllerListener, SettingChangeListener {
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
    private final Collection<UGSEventListener> ugsEventListener = new ArrayList<>();
    private final Collection<ControllerStateListener> controllerStateListener = new ArrayList<>();

    // GUI State
    private File gcodeFile = null;
    private File processedGcodeFile = null;
    private File tempDir = null;
    private String lastComment;
    private String activeState;
    private long estimatedSendDuration = -1L;
    private boolean sendingFile = false;
    //private long estimatedSendTimeRemaining = 0;
    //private long rowsInFile = 0;
    private String openCloseButtonText;
    private boolean openCloseButtonEnabled;
    private String pauseButtonText;
    private String cancelButtonText;
    private String firmware = null;
    private boolean reprocessFileAfterStreamComplete = false;

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
                //autoconnect();

                // Move the mouse every 30 seconds to prevent sleeping.
                if (isPaused() || isActive()) {
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
        ugsEventListener.add(listener);
    }

    @Override
    public void addControllerStateListener(ControllerStateListener listener) {
        logger.log(Level.INFO, "Adding control state listener.");
        controllerStateListener.add(listener);
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
        preprocessAndExportToFile(this.gcp, this.getGcodeFile(), f);
    }
    
    /**
     * Special utility to loop over a gcode file and apply any modifications made by a gcode parser. The results are
     * stored in a GcodeStream formatted file.
     * Additional rules:
     * * Comment lines are left
     */
    protected void preprocessAndExportToFile(GcodeParser gcp, File input, File output) throws Exception {
        logger.log(Level.INFO, "Preprocessing {0} to {1}", new Object[]{input.getCanonicalPath(), output.getCanonicalPath()});
        GcodeParserUtils.processAndExport(gcp, input, output);
    }

    private void initGcodeParser() {
        // Configure gcode parser.
        gcp.resetCommandProcessors();

        try {
            List<CommandProcessor> processors = FirmwareUtils.getParserFor(firmware, settings).orElse(null);
            for (CommandProcessor p : processors) {
                gcp.addCommandProcessor(p);
            }
        }
        catch (Exception e) {
            initializeWithFallbackProcessors(gcp);
        }
    }

    private void updateWithFirmware(String firmware) throws Exception {
        this.firmware = firmware;

        // Load command processors for this firmware.
        try {
            Optional<List<CommandProcessor>> processor_ret = FirmwareUtils.getParserFor(firmware, settings);
        }
        catch (Exception e) {
            disconnect();
            throw new Exception("Bad configuration file for: " + firmware + " (" + e.getMessage() + ")");
        }

        // Reload gcode file to use the controllers processors.
        if (this.gcodeFile != null) {
            setGcodeFile(this.gcodeFile);
        }
    }

    @Override
    public void connect(String firmware, String port, int baudRate) throws Exception {
        logger.log(Level.INFO, "Connecting to {0} on port {1}", new Object[]{firmware, port});
        lastConnectAttempt = System.currentTimeMillis();

        updateWithFirmware(firmware);

        Optional<AbstractController> c = FirmwareUtils.getControllerFor(firmware);
        if (!c.isPresent()) {
            throw new Exception("Unable to create handler for: " + firmware);
        }
        this.controller = c.get();
        applySettings(settings);

        this.controller.addListener(this);
        for (ControllerListener l : controllerListeners) {
            this.controller.addListener(l);
        }
        
        if (openCommConnection(port, baudRate)) {
            streamFailed = false;   //reset
        }
    }

    @Override
    public boolean isConnected() {
        boolean isConnected = this.controller != null && this.controller.isCommOpen();
        logger.log(Level.FINEST, "Is connected: {0}", isConnected);
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
            this.sendUGSEvent(new UGSEvent(ControlState.COMM_DISCONNECTED), false);
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
                logger.log(Level.WARNING, "Auto connect failed",e);
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
            logger.log(Level.INFO, pObj.toString() + "x>>" + pObj.x + "  y>>" + pObj.y);
        } catch (AWTException | NullPointerException ex) {
            Logger.getLogger(GUIBackend.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void applySettings(Settings settings) throws Exception {
        logger.log(Level.INFO, "Applying settings.");
        this.settings = settings;
        this.settings.setSettingChangeListener(this);
        if (this.controller != null) {
            applySettingsToController(this.settings, this.controller);
        }
        // Reload gcode file to use the controllers processors.
        if (this.gcodeFile != null) {
            setGcodeFile(this.gcodeFile);
        }
    }

    /**
     * This allows us to visualize a file without loading a controller profile.
     */
    private static void initializeWithFallbackProcessors(GcodeParser parser) {
        // Comment processor must come first otherwise we try to parse codes
        // out of the comments, like an f-code when we see "(feed rate is 100)"
        parser.addCommandProcessor(new CommentProcessor());
        parser.addCommandProcessor(new WhitespaceProcessor());
        parser.addCommandProcessor(new M30Processor());
        parser.addCommandProcessor(new DecimalProcessor(4));
        parser.addCommandProcessor(new CommandLengthProcessor(50));
    }

    @Override
    public void updateSystemState(SystemStateBean systemStateBean) {
        logger.log(Level.FINE, "Getting system state 'updateSystemState'");
        if (gcodeFile != null)
            systemStateBean.setFileName(gcodeFile.getAbsolutePath());
        systemStateBean.setLatestComment(lastComment);
        systemStateBean.setActiveState(activeState);

        systemStateBean.setControlState(getControlState());
        if (this.machineCoord != null) {
            systemStateBean.setMachineX(Utils.formatter.format(this.machineCoord.x));
            systemStateBean.setMachineY(Utils.formatter.format(this.machineCoord.y));
            systemStateBean.setMachineZ(Utils.formatter.format(this.machineCoord.z));
        }
        if (this.controller != null) {
            systemStateBean.setRemainingRows(String.valueOf(this.getNumRemainingRows()));
            systemStateBean.setRowsInFile(String.valueOf(this.getNumRows()));
            systemStateBean.setSentRows(String.valueOf(this.getNumSentRows()));
            systemStateBean.setDuration(String.valueOf(this.getSendDuration()));
            systemStateBean.setEstimatedTimeRemaining(String.valueOf(this.getSendRemainingDuration()));
        }
        if (this.workCoord != null) {
            systemStateBean.setWorkX(Utils.formatter.format(this.workCoord.x));
            systemStateBean.setWorkY(Utils.formatter.format(this.workCoord.y));
            systemStateBean.setWorkZ(Utils.formatter.format(this.workCoord.z));
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
        sendGcodeCommand(false, commandText);
    }

    @Override
    public void sendGcodeCommand(boolean restoreParserState, String commandText) throws Exception {
        if (this.isConnected()) {
            GcodeCommand command = controller.createCommand(commandText);
            command.setTemporaryParserModalChange(restoreParserState);
            sendGcodeCommand(command);

            if (restoreParserState && this.isConnected()) {
                controller.restoreParserModalState();
            }
        } else {
            throw new Exception(Localization.getString("controller.log.notconnected"));
        }
    }

    @Override
    public void sendGcodeCommand(GcodeCommand command) throws Exception {
        if (this.isConnected()) {
            logger.log(Level.INFO, "Sending gcode command: {0}", command.getCommandString());
            controller.sendCommandImmediately(command);
        }
    }

    /**
     * Sends a G91 command in some combination of x, y, and z directions with a
     * step size of stepDirection.
     * 
     * Direction is specified by the direction param being positive or negative.
     */
    @Override
    public void adjustManualLocation(int dirX, int dirY, int dirZ,
            double stepSize, double feedRate, Units units) throws Exception {
        // Don't send empty commands.
        if ((dirX == 0) && (dirY == 0) && (dirZ == 0)) {
            return;
        }

        controller.jogMachine(dirX, dirY, dirZ, stepSize, feedRate, units);
    }

    @Override
    public void probe(String axis, double feedRate, double distance, UnitUtils.Units units) throws Exception {
        controller.probe(axis, feedRate, distance, units);
    }

    @Override
    public void offsetTool(String axis, double offset, UnitUtils.Units units) throws Exception {
        controller.offsetTool(axis, offset, units);
    }

    @Override
    public Settings getSettings() {
        logger.log(Level.FINEST, "Getting settings.");
        return this.settings;
    }

    @Override
    public ControlState getControlState() {
        logger.log(Level.FINEST, "Getting control state.");
        return this.controller == null ?
                ControlState.COMM_DISCONNECTED : this.controller.getControlState();
    }

    @Override
    public Position getWorkPosition() {
        return this.workCoord;
    }

    @Override
    public Position getMachinePosition() {
        return this.machineCoord;
    }
    
    @Override
    public IController getController() {
        logger.log(Level.FINEST, "Getting controller");
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
            tempDir = Files.createTempDir();
        }
        return tempDir;
    }

    @Override
    public void setGcodeFile(File file) throws Exception {
        logger.log(Level.INFO, "Setting gcode file.");
        initGcodeParser();
        this.gcodeFile = file;
        this.processedGcodeFile = null;

        this.sendUGSEvent(new UGSEvent(FileState.FILE_LOADING,
                file.getAbsolutePath()), false);

        initializeProcessedLines(true, this.gcodeFile, this.gcp);

        this.sendUGSEvent(new UGSEvent(FileState.FILE_LOADED,
                processedGcodeFile.getAbsolutePath()), false);
    }

    @Override
    public void applyGcodeParser(GcodeParser parser) throws Exception {
        logger.log(Level.INFO, "Applying new parser filters.");

        if (this.processedGcodeFile == null) {
            return;
        }

        // re-initialize starting with the already processed file.
        initializeProcessedLines(true, this.processedGcodeFile, parser);

        this.sendUGSEvent(new UGSEvent(FileState.FILE_LOADED,
                processedGcodeFile.getAbsolutePath()), false);
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
            this.sendingFile = true;
            // This will throw an exception and prevent that other stuff from
            // happening (clearing the table before its ready for clearing.
            this.controller.isReadyToStreamFile();

            //this.controller.queueCommands(processedCommandLines);
            //this.controller.queueStream(new BufferedReader(new FileReader(this.processedGcodeFile)));
            this.controller.queueStream(new GcodeStreamReader(this.processedGcodeFile));

            this.controller.beginStreaming();
        } catch (Exception e) {
            this.sendUGSEvent(new UGSEvent(ControlState.COMM_IDLE), false);
            e.printStackTrace();
            throw new Exception(Localization.getString("mainWindow.error.startingStream") + ": "+e.getMessage());
        }
    }
    
    @Override
    public long getNumRows() {
        logger.log(Level.FINEST, "Getting number of rows.");
        return this.controller.rowsInSend();
    }
    
    @Override
    public long getNumSentRows() {
        logger.log(Level.FINEST, "Getting number of sent rows.");
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
            switch(getControlState()) {
                case COMM_IDLE:
                default:
                    if (!sendingFile) {
                        throw new Exception("Cannot pause while '" + getControlState() + "'.");
                    }
                    // Fall through if we're really sending a file.
                    // This can happen at the beginning of a stream when GRBL
                    // reports an error before we send it a status request.
                case COMM_SENDING:
                    this.controller.pauseStreaming();
                    return;
                case COMM_SENDING_PAUSED:
                    this.controller.resumeStreaming();
                    return;
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Exception in pauseResume", e);
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
    public boolean isActive() {
        return this.controller != null && !isIdle();
    }

    @Override
    public boolean isSendingFile() {
        return sendingFile;
    }

    @Override
    public boolean isIdle() {
        return this.controller != null && controller.isIdle();
    }
    
    @Override
    public boolean isPaused() {
        return this.controller != null && this.controller.isPaused();
    }
    
    @Override
    public boolean canPause() {
        return this.controller != null && !isIdle() && !isPaused();
    }

    @Override
    public boolean canCancel() {
        return canPause() || isPaused();
    }
    
    @Override
    public boolean canSend() {
        return isIdle() && (this.gcodeFile != null);
    }
    
    @Override
    public void cancel() throws Exception {
        if (this.canCancel()) {
            this.controller.cancelSend();
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
    public void restoreParserState() throws Exception {
        this.controller.restoreParserModalState();
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
        // This comes from the boss, force the event change.
        this.sendUGSEvent(new UGSEvent(state), true);
    }

    @Override
    public void fileStreamComplete(String filename, boolean success) {
        // If we were sending a file, we aren't anymore.
        this.sendingFile = false;

        // Reprocess file if a custom pattern remover was added while streaming.
        if (this.reprocessFileAfterStreamComplete) {
            try {
                updateWithFirmware(firmware);
            } catch (Exception ex) {
                Logger.getLogger(GUIBackend.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    @Override
    public void commandSkipped(GcodeCommand command) {
    }

    @Override
    public void commandSent(GcodeCommand command) {
    }

    @Override
    public void commandComplete(GcodeCommand command) {
        if (command.isError()) {
            if (this.sendingFile && !this.isPaused()) {
                try {
                    this.pauseResume();
                } catch (Exception e) {
                    GUIHelpers.displayErrorDialog(e.getLocalizedMessage());
                }

                /*
                String error =
                        String.format(Localization.getString("controller.exception.sendError"),
                                command.getCommandString(),
                                command.getResponse()).replaceAll("\\.\\.", "\\.");
                messageForConsole(MessageType.INFO, error);

                // The logic below used to automatically add "pattern processor remover" entries to the gcode processor.
                // It was causing a lot of issues where users were adding commands which shouldn't be added.
                String checkboxQuestion = Localization.getString("controller.exception.ignoreFutureErrors");
                Object[] params = {String.format(NarrowOptionPane.pattern, 300, error), checkboxQuestion};
                int n = JOptionPane.showConfirmDialog(new JFrame(),
                        params,
                        Localization.getString("error"),
                        JOptionPane.YES_NO_OPTION);
                if (n == JOptionPane.YES_OPTION) {
                    try {
                        FirmwareUtils.addPatternRemoverForFirmware(firmware,
                                Matcher.quoteReplacement(command.getCommandString()));
                        this.reprocessFileAfterStreamComplete = true;
                    } catch (IOException ex) {
                        GUIHelpers.displayErrorDialog(ex.getLocalizedMessage());
                    }
                }
                */
            }
        }
    }

    @Override
    public void commandComment(String comment) {
        this.lastComment = comment;
    }

    @Override
    public void probeCoordinates(Position p) {
        this.sendUGSEvent(new UGSEvent(p), false);
    }

    @Override
    public void messageForConsole(MessageType type, String msg) {
        if (type == MessageType.ERROR) {
            GUIHelpers.displayErrorDialog(msg);
        }
    }

    @Override
    public void statusStringListener(ControllerStatus status) {
        this.activeState = status.getState();
        this.machineCoord = status.getMachineCoord();
        this.workCoord = status.getWorkCoord();
        if (machineCoord != null) {
            this.reportUnits = machineCoord.getUnits();
        }
        this.lastResponse = System.currentTimeMillis();
        this.sendControllerStateEvent(new UGSEvent(status));
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
            controller.getCommandCreator();
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
            
            this.initializeProcessedLines(false, this.gcodeFile, this.gcp);
        } catch (Exception e) {
            logger.log(Level.INFO, "Exception in openCommConnection.", e);
            throw new Exception(Localization.getString("mainWindow.error.connection")
                    + " ("+ e.getClass().getName() + "): "+e.getMessage());
        }
        return connected;
    }

    private void initializeProcessedLines(boolean forceReprocess, File startFile, GcodeParser gcodeParser)
            throws FileNotFoundException, Exception {
        if (startFile != null) {
            Charset cs;
            try (FileReader fr = new FileReader(startFile)) {
                cs = Charset.forName(fr.getEncoding());
            }
            logger.info("Start preprocessing");
            long start = System.currentTimeMillis();
            if (this.processedGcodeFile == null || forceReprocess) {
                gcp.reset();

                String name = startFile.getName();

                // If this is being re-processed, strip the ugs postfix and try again.
                Pattern word = Pattern.compile("(.*)_ugs_[\\d]+$");
                Matcher match = word.matcher(name);
                if (match.matches()) {
                    name = match.group(1);
                }
                this.processedGcodeFile =
                        new File(this.getTempDir(), name + "_ugs_" + System.currentTimeMillis());
                this.preprocessAndExportToFile(gcodeParser, startFile, this.processedGcodeFile);

                // Store gcode file stats.
                GcodeStats gs = gcp.getCurrentStats();
                this.settings.setFileStats(new FileStats(
                    gs.getMin(), gs.getMax(), gs.getCommandCount()));
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
    
    private void sendUGSEvent(UGSEvent event, boolean force) {
        if (event.isControllerStatusEvent()) return;

        logger.log(Level.FINE, "Sending control state event {0}.", event.getEventType());
        if (event.isStateChangeEvent()) {
            if (this.controller != null && this.controller.handlesAllStateChangeEvents() && !force){
                return;
            }
        }
        
        for (UGSEventListener l : ugsEventListener) {
            l.UGSEvent(event);
        }
    }

    private void sendControllerStateEvent(UGSEvent event) {
        if (!event.isControllerStatusEvent()) return;

        for (ControllerStateListener l : controllerStateListener) {
            l.UGSEvent(event);
        }
    }

    @Override
    public void sendOverrideCommand(Overrides override) throws Exception {
        this.controller.sendOverrideCommand(override);
    }

    @Override
    public void settingChanged() {
        this.sendUGSEvent(new UGSEvent(EventType.SETTING_EVENT), false);
    }
}
