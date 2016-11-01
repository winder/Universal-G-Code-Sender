/*
 * GRBL Control layer, coordinates all aspects of control.
 */
/*
    Copywrite 2013-2016 Will Winder

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

import com.willwinder.universalgcodesender.gcode.GcodeCommandCreator;
import com.willwinder.universalgcodesender.gcode.GcodeUtils;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.GrblSettingsListener;
import com.willwinder.universalgcodesender.model.Overrides;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent.ControlState;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.types.GrblFeedbackMessage;
import com.willwinder.universalgcodesender.types.GrblSettingMessage;
import com.willwinder.universalgcodesender.utils.GrblLookups;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author wwinder
 */
public class GrblController extends AbstractController {
    private static final GrblLookups ALARMS = new GrblLookups("alarm_codes");
    private static final GrblLookups ERRORS = new GrblLookups("error_codes");

    // Grbl state
    private double grblVersion = 0.0;           // The 0.8 in 'Grbl 0.8c'
    private Character grblVersionLetter = null; // The c in 'Grbl 0.8c'
    protected Boolean isReady = false;          // Not ready until version is received.
    private GrblSettingsListener settings;

    // Grbl status members.
    private GrblUtils.Capabilities capabilities = null;
    private String grblState = "";
    private Position machineLocation;
    private Position workLocation;
    private double maxZLocationMM;

    // Polling state
    private int outstandingPolls = 0;
    private Timer positionPollTimer = null;  
    private ControllerStatus controllerStatus = null;

    // Canceling state
    private Boolean isCanceling = false;     // Set for the position polling thread.
    private int attemptsRemaining;
    private Position lastLocation;
    
    public GrblController(AbstractCommunicator comm) {
        super(comm);
        
        this.commandCreator = new GcodeCommandCreator();
        this.positionPollTimer = createPositionPollTimer();
        this.maxZLocationMM = -1;

        // Listen for any setting changes.
        this.settings = new GrblSettingsListener();
        this.comm.setListenAll(settings);
        this.addListener(settings);
    }
    
    public GrblController() {
        this(new GrblCommunicator());
    }

    @Override
    public long getJobLengthEstimate(File gcodeFile) {
        // Pending update to support cross-platform and multiple GRBL versions.
        return 0;
        //GrblSimulator simulator = new GrblSimulator(settings.getSettings());
        //return simulator.estimateRunLength(jobLines);
    }

    /***********************
     * API Implementation. *
     ***********************/
    
    @Override
    protected void rawResponseHandler(String response) {
        if (GcodeCommand.isOkErrorResponse(response)) {
            String processed = response;
            if (response.startsWith("error:")) {
                String parts[] = response.split(":");
                if (parts.length == 2) {
                    String code = parts[1].trim();
                    if (StringUtils.isNumeric(code)) {
                        String[] errorParts = ERRORS.lookup(code);
                        if (errorParts != null && errorParts.length >= 3) {
                            processed = "error: " + errorParts[1] + ": " +
                                    errorParts[2];
                        }
                    }
                }
            }
            try {
                this.commandComplete(processed);
            } catch (Exception e) {
                this.errorMessageForConsole(Localization.getString("controller.error.response")
                        + " <" + processed + ">: " + e.getMessage());
            }
            
            this.messageForConsole(processed + "\n");
        }
        
        else if (GrblUtils.isGrblVersionString(response)) {
            this.stopPollingPosition();
            positionPollTimer = createPositionPollTimer();
            this.beginPollingPosition();

            this.isReady = true;
            resetBuffers();

            // In case a reset occurred while streaming.
            if (this.isStreaming()) {
                checkStreamFinished();
            }

            // Version string goes to console
            this.messageForConsole(response + "\n");
            
            this.grblVersion = GrblUtils.getVersionDouble(response);
            this.grblVersionLetter = GrblUtils.getVersionLetter(response);
            
            this.capabilities = GrblUtils.getGrblStatusCapabilities(this.grblVersion, this.grblVersionLetter);
            try {
                this.sendCommandImmediately(createCommand(GrblUtils.GRBL_VIEW_SETTINGS_COMMAND));
                this.sendCommandImmediately(createCommand(GrblUtils.GRBL_VIEW_PARSER_STATE_COMMAND));
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            this.beginPollingPosition();
            
            Logger.getLogger(GrblController.class.getName()).log(Level.CONFIG, 
                    "{0} = {1}{2}", new Object[]{Localization.getString("controller.log.version"), this.grblVersion, this.grblVersionLetter});
            Logger.getLogger(GrblController.class.getName()).log(Level.CONFIG, 
                    "{0} = {1}", new Object[]{Localization.getString("controller.log.realtime"), this.capabilities.REAL_TIME});
        }
        
        else if (GrblUtils.isGrblStatusString(response)) {
            // Only 1 poll is sent at a time so don't decrement, reset to zero.
            this.outstandingPolls = 0;

            // Status string goes to verbose console
            verboseMessageForConsole(response + "\n");
            
            this.handleStatusString(response);
        }

        else if (GrblUtils.isGrblFeedbackMessage(response, capabilities)) {
            GrblFeedbackMessage grblFeedbackMessage = new GrblFeedbackMessage(response);
            this.verboseMessageForConsole(grblFeedbackMessage.toString() + "\n");
            setDistanceModeCode(grblFeedbackMessage.getDistanceMode());
            setUnitsCode(grblFeedbackMessage.getUnits());
        }

        else if (GrblUtils.isGrblSettingMessage(response)) {
            GrblSettingMessage message = new GrblSettingMessage(response);
            this.messageForConsole(message + "\n");
            if (message.isReportingUnits()) {
                setReportingUnits(message.getReportingUnits());
            }
        }

        else {
            // Display any unhandled messages
            this.messageForConsole(response + "\n");
        }
    }

    @Override
    protected void pauseStreamingEvent() throws Exception {
        if (this.capabilities.REAL_TIME) {
            this.comm.sendByteImmediately(GrblUtils.GRBL_PAUSE_COMMAND);
        }
    }
    
    @Override
    protected void resumeStreamingEvent() throws Exception {
        if (this.capabilities.REAL_TIME) {
            this.comm.sendByteImmediately(GrblUtils.GRBL_RESUME_COMMAND);
        }
    }
    
    @Override
    protected void closeCommBeforeEvent() {
        this.stopPollingPosition();
    }
    
    @Override
    protected void closeCommAfterEvent() {
        this.grblVersion = 0.0;
        this.grblVersionLetter = null;
    }
    
    @Override
    protected void openCommAfterEvent() throws Exception {
        this.comm.sendByteImmediately(GrblUtils.GRBL_RESET_COMMAND);
    }

    @Override
    protected void isReadyToStreamCommandsEvent() throws Exception {
        isReadyToSendCommandsEvent();
        if (grblState != null && grblState.equals("Alarm")) {
            throw new Exception(Localization.getString("grbl.exception.Alarm"));
        }
    }

    @Override
    protected void isReadyToSendCommandsEvent() throws Exception {
        if (this.isReady == false) {
            throw new Exception(Localization.getString("controller.exception.booting"));
        }
    }
    
    @Override
    protected void cancelSendBeforeEvent() throws Exception {
        boolean paused = isPaused();
        // The cancel button is left enabled at all times now, but can only be
        // used for some versions of GRBL.
        if (paused && !this.capabilities.REAL_TIME) {
            throw new Exception("Cannot cancel while paused with this version of GRBL. Reconnect to reset GRBL.");
        }

        // Check if we can get fancy with a soft reset.
        if (!paused && this.capabilities.REAL_TIME) {
            try {
                this.pauseStreaming();
                this.dispatchStateChange(ControlState.COMM_SENDING_PAUSED);
            } catch (Exception e) {
                // Oh well, was worth a shot.
                System.out.println("Exception while trying to issue a soft reset: " + e.getMessage());
            }
        }
    }
    
    @Override
    protected void cancelSendAfterEvent() throws Exception {
        if (this.capabilities.REAL_TIME && this.getStatusUpdatesEnabled()) {
            // Trigger the position listener to watch for the machine to stop.
            this.attemptsRemaining = 50;
            this.isCanceling = true;
            this.lastLocation = null;
        }
    }
    
    /**
     * Sends the version specific homing cycle to the machine.
     */
    @Override
    public void performHomingCycle() throws Exception {
        if (this.isCommOpen()) {
            String gcode = GrblUtils.getHomingCommand(this.grblVersion, this.grblVersionLetter);
            if (!"".equals(gcode)) {
                GcodeCommand command = createCommand(gcode);
                this.sendCommandImmediately(command);
                return;
            }
        }
        // Throw exception
        super.performHomingCycle();
    }
    
    @Override
    public void resetCoordinatesToZero() throws Exception {
        if (this.isCommOpen()) {
            String gcode = GrblUtils.getResetCoordsToZeroCommand(this.grblVersion, this.grblVersionLetter);
            if (!"".equals(gcode)) {
                GcodeCommand command = createCommand(gcode);
                this.sendCommandImmediately(command);
                return;
            }
        }
        // Throw exception
        super.resetCoordinatesToZero();
    }
    
    @Override
    public void resetCoordinateToZero(final char coord) throws Exception {
        if (this.isCommOpen()) {
            String gcode = GrblUtils.getResetCoordToZeroCommand(coord, this.grblVersion, this.grblVersionLetter);
            if (!"".equals(gcode)) {
                GcodeCommand command = createCommand(gcode);
                this.sendCommandImmediately(command);
                return;
            }
        }
        // Throw exception
        super.resetCoordinatesToZero();
    }
    
    @Override
    public void returnToHome() throws Exception {
        if (this.isCommOpen()) {
            // Not using max for now, it was causing issue for many people.
            double max = 0;
            if (this.maxZLocationMM != -1) {
                max = this.maxZLocationMM;
            }
            ArrayList<String> commands = GrblUtils.getReturnToHomeCommands(this.grblVersion, this.grblVersionLetter);
            if (!commands.isEmpty()) {
                Iterator<String> iter = commands.iterator();
                // Perform the homing commands
                while(iter.hasNext()){
                    String gcode = iter.next();
                    GcodeCommand command = createCommand(gcode);
                    this.sendCommandImmediately(command);
                }
                return;
            }

            restoreParserModalState();
        }
        // Throw exception
        super.returnToHome();
    }
    
    @Override
    public void killAlarmLock() throws Exception {
        if (this.isCommOpen()) {
            String gcode = GrblUtils.getKillAlarmLockCommand(this.grblVersion, this.grblVersionLetter);
            if (!"".equals(gcode)) {
                GcodeCommand command = createCommand(gcode);
                this.sendCommandImmediately(command);
                return;
            }
        }
        // Throw exception
        super.killAlarmLock();
    }

    @Override
    public void toggleCheckMode() throws Exception {
        if (this.isCommOpen()) {
            String gcode = GrblUtils.getToggleCheckModeCommand(this.grblVersion, this.grblVersionLetter);
            if (!"".equals(gcode)) {
                GcodeCommand command = createCommand(gcode);
                this.sendCommandImmediately(command);
                return;
            }
        }
        // Throw exception
        super.toggleCheckMode();
    }

    @Override
    public void viewParserState() throws Exception {
        if (this.isCommOpen()) {
            String gcode = GrblUtils.getViewParserStateCommand(this.grblVersion, this.grblVersionLetter);
            if (!"".equals(gcode)) {
                GcodeCommand command = createCommand(gcode);
                this.sendCommandImmediately(command);
                return;
            }
        }
        // Throw exception
        super.viewParserState();
    }
    
    /**
     * If it is supported, a soft reset real-time command will be issued.
     */
    @Override
    public void softReset() throws Exception {
        if (this.isCommOpen() && this.capabilities.REAL_TIME) {
            this.comm.sendByteImmediately(GrblUtils.GRBL_RESET_COMMAND);
            //Does GRBL need more time to handle the reset?
            this.comm.softReset();
        }
    }

    @Override
    public void jogMachine(int dirX, int dirY, int dirZ, double stepSize, 
            double feedRate, Units units) throws Exception {
        if (capabilities.JOG_MODE) {
            // Format step size from spinner.
            String formattedStepSize = Utils.formatter.format(stepSize);
            String formattedFeedRate = Utils.formatter.format(feedRate);

            String commandString = GcodeUtils.generateXYZ("G91", units,
                    formattedStepSize, formattedFeedRate, dirX, dirY, dirZ);
            GcodeCommand command = createCommand("$J=" + commandString);
            sendCommandImmediately(command);
        } else {
            super.jogMachine(dirX, dirY, dirZ, stepSize, feedRate, units);
        }
    }

    /************
     * Helpers.
     ************/

    public String getGrblVersion() {
        if (this.isCommOpen()) {
            StringBuilder str = new StringBuilder();
            str.append("Grbl ");
            if (this.grblVersion > 0.0) {
                str.append(this.grblVersion);
            }
            if (this.grblVersionLetter != null) {
                str.append(this.grblVersionLetter);
            }
            
            if (this.grblVersion <= 0.0 && this.grblVersionLetter == null) {
                str.append("<").append(Localization.getString("unknown")).append(">");
            }
            
            return str.toString();
        }
        return "<" + Localization.getString("controller.log.notconnected") + ">";
    }

    /**
     * Create a timer which will execute GRBL's position polling mechanism.
     */
    private Timer createPositionPollTimer() {
        // Action Listener for GRBL's polling mechanism.
        ActionListener actionListener = new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent actionEvent) {
                java.awt.EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            if (outstandingPolls == 0) {
                                outstandingPolls++;
                                comm.sendByteImmediately(GrblUtils.GRBL_STATUS_COMMAND);
                            } else {
                                // If a poll is somehow lost after 20 intervals,
                                // reset for sending another.
                                outstandingPolls++;
                                if (outstandingPolls >= 20) {
                                    outstandingPolls = 0;
                                }
                            }
                        } catch (Exception ex) {
                            messageForConsole(Localization.getString("controller.exception.sendingstatus")
                                    + ": " + ex.getMessage() + "\n");
                            ex.printStackTrace();
                        }
                    }
                });
                
            }
        };
        
        return new Timer(this.getStatusUpdateRate(), actionListener);
    }
    /**
     * Begin issuing GRBL status request commands.
     */
    private void beginPollingPosition() {
        // Start sending '?' commands if supported and enabled.
        if (this.capabilities != null && this.getStatusUpdatesEnabled()) {
            if (this.positionPollTimer.isRunning() == false) {
                this.outstandingPolls = 0;
                this.positionPollTimer.start();
            }
        }
    }

    /**
     * Stop issuing GRBL status request commands.
     */
    private void stopPollingPosition() {
        if (this.positionPollTimer.isRunning()) {
            this.positionPollTimer.stop();
        }
    }
    
    // No longer a listener event
    private void handleStatusString(final String string) {
        if (this.capabilities == null) {
            return;
        }

        String beforeState =  (controllerStatus != null) ?
                controllerStatus.getState() : "";

        controllerStatus = GrblUtils.getStatusFromStatusString(
                controllerStatus, string, capabilities, getReportingUnits());

        // Make UGS more responsive to the state being reported by GRBL.
        if (!beforeState.equals(controllerStatus.getState())) {
            switch (controllerStatus.getState().toLowerCase()) {
                case "jog":
                case "run":
                    this.dispatchStateChange(ControlState.COMM_SENDING);
                    break;
                case "hold":
                case "door":
                    this.dispatchStateChange(ControlState.COMM_SENDING_PAUSED);
                    break;
                case "check":
                case "alarm":
                case "idle":
                    if (isStreaming()){
                        this.dispatchStateChange(ControlState.COMM_SENDING_PAUSED);
                    } else {
                        // GRBL 1.1: cancel the send when from jog -> idle.
                        if (beforeState.toLowerCase().equals("jog")) {
                            this.comm.cancelSend();
                        }
                        this.dispatchStateChange(ControlState.COMM_IDLE);
                    }
                    break;
            }
        }

        grblState = controllerStatus.getState();
        machineLocation = controllerStatus.getMachineCoord();
        workLocation = controllerStatus.getWorkCoord();

        // Prior to GRBL v1.1 the GUI is required to keep checking locations
        // to verify that the machine has come to a complete stop after
        // pausing.
        if (isCanceling) {
            if (attemptsRemaining > 0 && lastLocation != null) {
                attemptsRemaining--;
                // If the machine goes into idle, we no longer need to cancel.
                if (grblState.equals("Idle")) {
                    isCanceling = false;
                }
                // Otherwise check if the machine is Hold and stopped.
                else if (grblState.equals("Hold")
                        && lastLocation.equals(machineLocation)) {
                    try {
                        this.issueSoftReset();
                    } catch(Exception e) {
                        this.errorMessageForConsole(e.getMessage());
                    }
                    isCanceling = false;
                }
                if (isCanceling && attemptsRemaining == 0) {
                    this.errorMessageForConsole(Localization.getString("grbl.exception.cancelReset"));
                }
            }
            lastLocation = new Position(machineLocation);
        }
        
        // Save max Z location
        if (machineLocation != null && this.getUnitsCode() != null) {
            Units u = this.getUnitsCode().toUpperCase().equals("G21") ?
                    Units.MM : Units.INCH;
            double zLocationMM = machineLocation.z;
            if (u == Units.INCH)
                zLocationMM *= 26.4;
            
            if (zLocationMM > this.maxZLocationMM) {
                maxZLocationMM = zLocationMM;
            }
        }

        dispatchStatusString(controllerStatus);
    }
    
    @Override
    protected void statusUpdatesEnabledValueChanged(boolean enabled) {
        if (enabled) {
            beginPollingPosition();
        } else {
            stopPollingPosition();
        }
    }
    
    @Override
    protected void statusUpdatesRateValueChanged(int rate) {
        this.stopPollingPosition();
        this.positionPollTimer = this.createPositionPollTimer();
        
        // This will start the timer up again if it is supported and enabled.
        this.beginPollingPosition();
    }

    @Override
    public void sendOverrideCommand(Overrides command) throws Exception {
        Byte realTimeCommand = GrblUtils.getOverrideForEnum(command, capabilities);
        if (realTimeCommand != null) {
            this.messageForConsole(String.format(">>> 0x%02x\n", realTimeCommand));
            this.comm.sendByteImmediately(realTimeCommand);
        }
    }
}
