/*
    Copywrite 2015 Will Winder

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

import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControlStateListener;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.utils.FirmwareUtils;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.model.Utils.ControlState;
import com.willwinder.universalgcodesender.model.Utils.Units;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.pendantui.SystemStateBean;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class GUIBackend implements BackendAPI, ControllerListener {
    private static final Logger logger = Logger.getLogger(GUIBackend.class.getName());
    
    private IController controller = null;
    private Settings settings = null;
    Point3d machineCoord = null;
    Point3d workCoord = null;
    String state;
    Collection<ControllerListener> controllerListeners = new ArrayList<>();
    Collection<ControlStateListener> controlStateListeners = new ArrayList<>();

    // Machine state
    Units units = Units.UNKNOWN;
    
    // GUI State
    File gcodeFile;
    String lastComment;
    String activeState;
    ControlState controlState = ControlState.COMM_DISCONNECTED;
    long sendStartTime = 0;
    long estimatedSendDuration = -1L;
    long estimatedSendTimeRemaining = 0;
    long rowsInFile = 0;
    String openCloseButtonText;
    boolean openCloseButtonEnabled;
    String pauseButtonText;
    boolean pauseButtonEnabled;
    String cancelButtonText;
    boolean cancelButtonEnabled;

    boolean G91Mode = false;
    
    public GcodeParser gcp = new GcodeParser();
    
    @Override
    public void addControlStateListener(ControlStateListener listener) {
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
        try(BufferedReader br = new BufferedReader(new FileReader(this.getFile()))) {
            try (PrintWriter pw = new PrintWriter(f, "UTF-8")) {
                for(String line; (line = br.readLine()) != null; ) {
                    Collection<String> lines = gcp.preprocessCommand(line);
                    for(String processedLine : lines) {
                        pw.println(processedLine);
                    }
                }
            }
        }
    }

    @Override
    public void connect(String firmware, String port, int baudRate) throws Exception {
        logger.log(Level.INFO, "Connecting to {0} on port {1}", new Object[]{firmware, port});
        this.controller = FirmwareUtils.getControllerFor(firmware);
        applySettingsToController(settings, this.controller);

        this.controller.addListener(this);
        for (ControllerListener l : controllerListeners) {
            this.controller.addListener(l);
        }
        
        if (openCommConnection(port, baudRate)) {
            this.sendControlStateEvent(new ControlStateEvent(ControlState.COMM_IDLE));
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
        logger.log(Level.INFO, "Disconnecting.");
        this.controller.closeCommPort();
        this.controller = null;
        this.sendControlStateEvent(new ControlStateEvent(ControlState.COMM_DISCONNECTED));
    }

    @Override
    public void applySettings(Settings settings) throws Exception {
        logger.log(Level.INFO, "Applying settings.");
        this.settings = settings;
        if (this.controller != null) {
            applySettingsToController(this.settings, this.controller);
        }
    }
    
    @Override
    public void updateSystemState(SystemStateBean systemStateBean) {
        logger.log(Level.INFO, "Getting system state 'updateSystemState'");
        systemStateBean.setFileName(gcodeFile.getAbsolutePath());
        systemStateBean.setLatestComment(lastComment);
        systemStateBean.setActiveState(activeState);
        systemStateBean.setControlState(controlState);
        systemStateBean.setDuration(String.valueOf(this.getSendDuration()));
        systemStateBean.setEstimatedTimeRemaining(String.valueOf(this.getSendRemainingDuration()));
        systemStateBean.setMachineX(Utils.formatter.format(this.machineCoord.getX()));
        systemStateBean.setMachineY(Utils.formatter.format(this.machineCoord.getY()));
        systemStateBean.setMachineZ(Utils.formatter.format(this.machineCoord.getZ()));
        systemStateBean.setRemainingRows(String.valueOf(this.getNumRemainingRows()));
        systemStateBean.setRowsInFile(String.valueOf(this.getNumRows()));
        systemStateBean.setSentRows(String.valueOf(this.getNumSentRows()));
        systemStateBean.setWorkX(Utils.formatter.format(this.workCoord.getX()));
        systemStateBean.setWorkY(Utils.formatter.format(this.workCoord.getY()));
        systemStateBean.setWorkZ(Utils.formatter.format(this.workCoord.getZ()));
        systemStateBean.setSendButtonText(openCloseButtonText);
        systemStateBean.setSendButtonEnabled(openCloseButtonEnabled);
        systemStateBean.setPauseResumeButtonText(pauseButtonText);
        systemStateBean.setPauseResumeButtonEnabled(this.canPause());
        systemStateBean.setCancelButtonText(cancelButtonText);
        systemStateBean.setCancelButtonEnabled(this.canCancel());
    }

    @Override
    public void sendGcodeCommand(String commandText) throws Exception {
        logger.log(Level.INFO, "Sending gcode command: {0}", commandText);
        controller.sendCommandImmediately(commandText);
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
        StringBuilder command = new StringBuilder();
        
        // Set jog command to the preferred units.
        if (this.units != units) {
            if (units == Units.INCH) {
                command.append("G20 ");
            } else if (units == Units.MM) {
                command.append("G21 ");
            }

            this.units = units;
        }
        
        command.append("G91 G0 ");
        
        if (dirX != 0) {
            command.append(" X");
            if (dirX < 0) {
                command.append('-');
            }
            command.append(formattedStepSize);
        } if (dirY != 0) {
            command.append(" Y");
            if (dirY < 0) {
                command.append('-');
            }
            command.append(formattedStepSize);
        } if (dirZ != 0) {
            command.append(" Z");
            if (dirZ < 0) {
                command.append('-');
            }
            command.append(formattedStepSize);
        }

        this.sendGcodeCommand(command.toString());
        G91Mode = true;
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
    public void setFile(File file) throws Exception {
        logger.log(Level.INFO, "Setting gcode file.");
        this.gcodeFile = file;
        initializeProcessedLines(true);
        this.sendControlStateEvent(new ControlStateEvent(file.getAbsolutePath()));
    }
    
    @Override
    public File getFile() {
        logger.log(Level.INFO, "Getting gcode file.");
        return this.gcodeFile;
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

            this.sendControlStateEvent(new ControlStateEvent(ControlState.COMM_SENDING));

            if (this.G91Mode) {
                List<String> processed = gcp.preprocessCommand("G90");
                controller.queueCommands(processed);
                this.G91Mode = false;
            }

            this.controller.queueCommands(processedCommandLines);

            this.sendStartTime = System.currentTimeMillis();
            this.controller.beginStreaming();
        } catch (Exception e) {
            this.sendControlStateEvent(new ControlStateEvent(ControlState.COMM_IDLE));
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
        return this.controller.rowsSent();
    }

    @Override
    public long getNumRemainingRows() {
        return getNumRows() - getNumSentRows();
    }
    @Override
    public long getSendDuration() {
        return this.controller.getSendDuration();
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
                    this.sendControlStateEvent(new ControlStateEvent(ControlState.COMM_SENDING_PAUSED));
                    return;
                case COMM_SENDING_PAUSED:
                    this.controller.resumeStreaming();
                    this.sendControlStateEvent(new ControlStateEvent(ControlState.COMM_SENDING));
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
    public boolean isPaused() {
        return this.controlState == ControlState.COMM_SENDING_PAUSED;
    }
    
    @Override
    public boolean canPause() {
        return this.controlState == ControlState.COMM_SENDING;
    }

    @Override
    public boolean canCancel() {
        // Note: Cannot cancel a send while paused because there are commands
        //       in the GRBL buffer which can't be un-sent.
        return this.controlState == ControlState.COMM_SENDING;
    }
    
    @Override
    public boolean canSend() {
        return (this.controlState == ControlState.COMM_IDLE) && (this.gcodeFile != null);
    }
    
    @Override
    public void cancel() throws Exception {
        if (this.canCancel()) {
            this.controller.cancelSend();
            this.sendControlStateEvent(new ControlStateEvent(ControlState.COMM_IDLE));
        }
    }

    @Override
    public void returnToZero() throws Exception {
        this.controller.returnToHome();
        
        // TODO: These should get pushed into the controller?
        
        // The return to home command uses G91 to lift the tool.
        this.G91Mode = true;
        // Also sets the units to mm.
        this.units = Units.MM;
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

    //////////////////
    // Controller Listener
    //////////////////

    @Override
    public void fileStreamComplete(String filename, boolean success) {
        this.sendControlStateEvent(new ControlStateEvent(ControlState.COMM_IDLE));
    }

    @Override
    public void commandQueued(GcodeCommand command) {
    }

    @Override
    public void commandSent(GcodeCommand command) {
    }

    @Override
    public void commandComplete(GcodeCommand command) {
        String gcodeString = command.getCommandString().toLowerCase();

        // Check for unit changes.
        Units before = this.units;
        if (gcodeString.contains("g21")) {
            this.units = Units.MM;
        } else if (gcodeString.contains("g20")) {
            this.units = Units.INCH;
        }
            
        controller.currentUnits(this.units);
    }

    @Override
    public void commandComment(String comment) {
        this.lastComment = comment;
    }

    @Override
    public void messageForConsole(String msg, Boolean verbose) {
    }

    @Override
    public void statusStringListener(String state, Point3d machineCoord, Point3d workCoord) {
        this.activeState = state;
        this.machineCoord = machineCoord;
        this.workCoord = workCoord;
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
        // Apply settings settings to controller.
        if (settings.isOverrideSpeedSelected()) {
            double value = settings.getOverrideSpeedValue();
            gcp.setSpeedOverride(value);
        } else {
            gcp.setSpeedOverride(-1);
        }

        try {
            gcp.setTruncateDecimalLength(settings.getTruncateDecimalLength());
            gcp.setRemoveAllWhitespace(settings.isRemoveAllWhitespace());
            gcp.setConvertArcsToLines(settings.isConvertArcsToLines());
            gcp.setSmallArcThreshold(settings.getSmallArcThreshold());
            gcp.setSmallArcSegmentLength(settings.getSmallArcSegmentLength());
            
            controller.getCommandCreator().setMaxCommandLength(settings.getMaxCommandLength());
            
            controller.setSingleStepMode(settings.isSingleStepMode());
            controller.setStatusUpdatesEnabled(settings.isStatusUpdatesEnabled());
            controller.setStatusUpdateRate(settings.getStatusUpdateRate());

        } catch (Exception ex) {

            StringBuilder message = new StringBuilder()
                    .append(Localization.getString("mainWindow.error.firmwareSetting"))
                    .append(": \n    ")
                    .append(Localization.getString("firmware.feature.maxCommandLength")).append("\n    ")
                    .append(Localization.getString("firmware.feature.truncateDecimal")).append("\n    ")
                    .append(Localization.getString("firmware.feature.singleStep")).append("\n    ")
                    .append(Localization.getString("firmware.feature.removeWhitespace")).append("\n    ")
                    .append(Localization.getString("firmware.feature.linesToArc")).append("\n    ")
                    .append(Localization.getString("firmware.feature.statusUpdates")).append("\n    ")
                    .append(Localization.getString("firmware.feature.statusUpdateRate"));
            
            throw new Exception(message.toString());
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

    Collection<String> processedCommandLines = null;
    private void initializeProcessedLines(boolean forceReprocess) throws FileNotFoundException, IOException {
        if (this.gcodeFile != null) {
            Charset cs;
            try (FileReader fr = new FileReader(this.gcodeFile)) {
                cs = Charset.forName(fr.getEncoding());
            }
            List<String> lines = Files.readAllLines(this.gcodeFile.toPath(), cs);
            System.out.println("Finished loading");
            long start = System.currentTimeMillis();
            if (this.processedCommandLines == null || forceReprocess) {
                this.processedCommandLines = gcp.preprocessCommands(lines);
            }
            long end = System.currentTimeMillis();
            System.out.println("Took " + (end - start) + "ms to preprocess");

            if (this.isConnected()) {
                this.estimatedSendDuration = -1L;

                Thread estimateThread = new Thread(new Runnable() {
                    @Override
                    public void run() {
                        estimatedSendDuration = controller.getJobLengthEstimate(processedCommandLines);
                    }
                });
                estimateThread.start();
            }
        }
    }
    
    private void sendControlStateEvent(ControlStateEvent event) {
        if (event.getEventType() == ControlStateEvent.event.STATE_CHANGED) {
            this.controlState = event.getState();
        }
        
        for (ControlStateListener l : controlStateListeners) {
            logger.info("Sending control state change.");
            l.ControlStateEvent(event);
        }
    }
}
