/*
    Copyright 2013-2018 Will Winder

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
import com.willwinder.universalgcodesender.gcode.util.GcodeUtils;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.model.Alarm;
import com.willwinder.universalgcodesender.model.Overrides;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent.ControlState;
import static com.willwinder.universalgcodesender.model.UGSEvent.ControlState.COMM_CHECK;
import static com.willwinder.universalgcodesender.model.UGSEvent.ControlState.COMM_IDLE;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.firmware.grbl.GrblFirmwareSettings;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.model.Axis;
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
 * GRBL Control layer, coordinates all aspects of control.
 *
 * @author wwinder
 */
public class GrblController extends AbstractController {
    private static final Logger logger = Logger.getLogger(GrblController.class.getName());

    private static final GrblLookups ALARMS = new GrblLookups("alarm_codes");
    private static final GrblLookups ERRORS = new GrblLookups("error_codes");

    // Grbl state
    private double grblVersion = 0.0;           // The 0.8 in 'Grbl 0.8c'
    private Character grblVersionLetter = null; // The c in 'Grbl 0.8c'
    protected Boolean isReady = false;          // Not ready until version is received.
    private Capabilities capabilities = new Capabilities();
    private final GrblFirmwareSettings firmwareSettings;

    // Grbl status members.
    private double maxZLocationMM;

    // Polling state
    private int outstandingPolls = 0;
    private Timer positionPollTimer = null;  
    private ControllerStatus controllerStatus = new ControllerStatus("Idle", ControllerState.IDLE, new Position(0,0,0,Units.MM), new Position(0,0,0,Units.MM));

    // Canceling state
    private Boolean isCanceling = false;     // Set for the position polling thread.
    private int attemptsRemaining;
    private Position lastLocation;

    /**
     * For storing a temporary state if using single step mode when entering the state
     * check mode. When leaving check mode the temporary single step mode will be reverted.
     */
    private boolean temporaryCheckSingleStepMode = false;

    public GrblController(AbstractCommunicator comm) {
        super(comm);
        
        this.commandCreator = new GcodeCommandCreator();
        this.positionPollTimer = createPositionPollTimer();
        this.maxZLocationMM = -1;

        // Add our controller settings manager
        this.firmwareSettings = new GrblFirmwareSettings(this);
        this.comm.setListenAll(firmwareSettings);
    }
    
    public GrblController() {
        this(new GrblCommunicator());
    }

    @Override
    public Boolean handlesAllStateChangeEvents() {
        return capabilities.hasCapability(GrblCapabilitiesConstants.REAL_TIME);
    }

    @Override
    public Capabilities getCapabilities() {
        return capabilities;
    }

    @Override
    public IFirmwareSettings getFirmwareSettings() {
        return firmwareSettings;
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

    private static String lookupCode(String input, boolean shortString) {
        if (input.contains(":")) {
            String inputParts[] = input.split(":");
            if (inputParts.length == 2) {
                String code = inputParts[1].trim();
                if (StringUtils.isNumeric(code)) {
                    String[] lookupParts = null;
                    switch(inputParts[0].toLowerCase()) {
                        case "error":
                            lookupParts = ERRORS.lookup(code);
                            break;
                        case "alarm":
                            lookupParts = ALARMS.lookup(code);
                            break;
                        default:
                            return input;
                    }

                    if (lookupParts == null) {
                        return "(" + input + ") An unknown error has occurred";
                    } else if (shortString ) {
                        return input + " (" + lookupParts[1] + ")";
                    } else {
                        return "(" + input + ") " + lookupParts[2];
                    }
                }
            }
        }

        return input;
    }
    
    @Override
    protected void rawResponseHandler(String response) {
        String processed = response;
        try {
            boolean verbose = false;

            if (GrblUtils.isOkResponse(response)) {
                this.commandComplete(processed);
            }

            // Error case.
            else if (GrblUtils.isOkErrorAlarmResponse(response)) {
                if (GrblUtils.isAlarmResponse(response)) {
                    //this is not updating the state to Alarm in the GUI, and the alarm is no longer being processed
                    // TODO: Find a builder library.
                    String stateString = lookupCode(response, true);
                    this.controllerStatus = new ControllerStatus(
                            stateString,
                            ControllerState.ALARM,
                            this.controllerStatus.getMachineCoord(),
                            this.controllerStatus.getWorkCoord(),
                            this.controllerStatus.getFeedSpeed(),
                            this.controllerStatus.getSpindleSpeed(),
                            this.controllerStatus.getOverrides(),
                            this.controllerStatus.getWorkCoordinateOffset(),
                            this.controllerStatus.getEnabledPins(),
                            this.controllerStatus.getAccessoryStates());

                    Alarm alarm = GrblUtils.parseAlarmResponse(response);
                    dispatchAlarm(alarm);
                    dispatchStatusString(this.controllerStatus);
                    dispatchStateChange(COMM_IDLE);
                }

                // If there is an active command, mark it as completed with error
                Optional<GcodeCommand> activeCommand = this.getActiveCommand();
                if( activeCommand.isPresent() ) {
                    processed =
                            String.format(Localization.getString("controller.exception.sendError"),
                                    activeCommand.get().getCommandString(),
                                    lookupCode(response, false)).replaceAll("\\.\\.", "\\.");
                    this.dispatchConsoleMessage(MessageType.ERROR, processed + "\n");
                    this.commandComplete(processed);
                } else {
                    processed =
                            String.format(Localization.getString("controller.exception.unexpectedError"),
                                    lookupCode(response, false)).replaceAll("\\.\\.", "\\.");
                    dispatchConsoleMessage(MessageType.INFO,processed + "\n");
                }
                checkStreamFinished();
                processed = "";
            }

            else if (GrblUtils.isGrblVersionString(response)) {
                this.isReady = true;
                resetBuffers();

                // When exiting COMM_CHECK mode a soft reset is done, do not clear the
                // controller status because we need to know the previous state for resetting
                // single step mode
                if (getControlState() != COMM_CHECK) {
                    this.controllerStatus = null;
                }

                this.stopPollingPosition();
                positionPollTimer = createPositionPollTimer();
                this.beginPollingPosition();

                // In case a reset occurred while streaming.
                if (this.isStreaming()) {
                    checkStreamFinished();
                }
                
                this.grblVersion = GrblUtils.getVersionDouble(response);
                this.grblVersionLetter = GrblUtils.getVersionLetter(response);
                
                this.capabilities = GrblUtils.getGrblStatusCapabilities(this.grblVersion, this.grblVersionLetter);
                try {
                    this.sendCommandImmediately(createCommand(GrblUtils.GRBL_VIEW_SETTINGS_COMMAND));
                    this.sendCommandImmediately(createCommand(GrblUtils.GRBL_VIEW_PARSER_STATE_COMMAND));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
                
                Logger.getLogger(GrblController.class.getName()).log(Level.CONFIG, 
                        "{0} = {1}{2}", new Object[]{Localization.getString("controller.log.version"), this.grblVersion, this.grblVersionLetter});
                Logger.getLogger(GrblController.class.getName()).log(Level.CONFIG, 
                        "{0} = {1}", new Object[]{Localization.getString("controller.log.realtime"), this.capabilities.hasCapability(GrblCapabilitiesConstants.REAL_TIME)});
            }
            
            else if (GrblUtils.isGrblProbeMessage(response)) {
                Position p = GrblUtils.parseProbePosition(response, getFirmwareSettings().getReportingUnits());
                if (p != null) {
                    dispatchProbeCoordinates(p);
                }
            }

            else if (GrblUtils.isGrblStatusString(response)) {
                // Only 1 poll is sent at a time so don't decrement, reset to zero.
                this.outstandingPolls = 0;

                // Status string goes to verbose console
                verbose = true;
                
                this.handleStatusString(response);
                this.checkStreamFinished();
            }

            else if (GrblUtils.isGrblFeedbackMessage(response, capabilities)) {
                GrblFeedbackMessage grblFeedbackMessage = new GrblFeedbackMessage(response);
                // Convert feedback message to raw commands to update modal state.
                this.updateParserModalState(new GcodeCommand(GrblUtils.parseFeedbackMessage(response, capabilities)));
                this.dispatchConsoleMessage(MessageType.VERBOSE, grblFeedbackMessage.toString() + "\n");
                setDistanceModeCode(grblFeedbackMessage.getDistanceMode());
                setUnitsCode(grblFeedbackMessage.getUnits());
                dispatchStateChange(COMM_IDLE);
            }

            else if (GrblUtils.isGrblSettingMessage(response)) {
                GrblSettingMessage message = new GrblSettingMessage(response);
                processed = message.toString();
            }

            if (StringUtils.isNotBlank(processed)) {
                if (verbose) {
                    this.dispatchConsoleMessage(MessageType.VERBOSE,processed + "\n");
                } else {
                    this.dispatchConsoleMessage(MessageType.INFO, processed + "\n");
                }
            }
        } catch (Exception e) {
            String message = "";
            if (e.getMessage() != null) {
                message = ": " + e.getMessage();
            }
            message = Localization.getString("controller.error.response")
                    + " <" + processed + ">" + message;

            logger.log(Level.SEVERE, message, e);
            this.dispatchConsoleMessage(MessageType.ERROR,message + "\n");
        }
    }

    @Override
    protected void pauseStreamingEvent() throws Exception {
        if (this.capabilities.hasCapability(GrblCapabilitiesConstants.REAL_TIME)) {
            this.comm.sendByteImmediately(GrblUtils.GRBL_PAUSE_COMMAND);
        }
    }
    
    @Override
    protected void resumeStreamingEvent() throws Exception {
        if (this.capabilities.hasCapability(GrblCapabilitiesConstants.REAL_TIME)) {
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
        if (this.controllerStatus != null && this.controllerStatus.getState() == ControllerState.ALARM) {
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
        if (paused && !this.capabilities.hasCapability(GrblCapabilitiesConstants.REAL_TIME)) {
            throw new Exception("Cannot cancel while paused with this version of GRBL. Reconnect to reset GRBL.");
        }

        // If we're canceling a "jog" just send the door hold command.
        if (capabilities.hasJogging() && controllerStatus != null &&
                controllerStatus.getState() == ControllerState.JOG) {
            this.comm.sendByteImmediately(GrblUtils.GRBL_JOG_CANCEL_COMMAND);
        }
        // Otherwise, check if we can get fancy with a soft reset.
        else if (!paused && this.capabilities.hasCapability(GrblCapabilitiesConstants.REAL_TIME)) {
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
        if (this.capabilities.hasCapability(GrblCapabilitiesConstants.REAL_TIME) && this.getStatusUpdatesEnabled()) {
            // Trigger the position listener to watch for the machine to stop.
            this.attemptsRemaining = 50;
            this.isCanceling = true;
            this.lastLocation = null;
        } else {

        }
    }

    @Override
    protected Boolean isIdleEvent() {
        if (this.capabilities.hasCapability(GrblCapabilitiesConstants.REAL_TIME)) {
            return getControlState() == COMM_IDLE || getControlState() == COMM_CHECK;
        }
        // Otherwise let the abstract controller decide.
        return true;
    }

    @Override
    public ControlState getControlState() {
        if (!this.capabilities.hasCapability(GrblCapabilitiesConstants.REAL_TIME)) {
            return super.getControlState();
        }

        String state = this.controllerStatus == null ? "" : StringUtils.defaultString(this.controllerStatus.getStateString());
        switch(state.toLowerCase()) {
            case "jog":
            case "run":
                return ControlState.COMM_SENDING;
            case "hold":
            case "door":
            case "queue":
                return ControlState.COMM_SENDING_PAUSED;
            case "idle":
                if (isStreaming()){
                    return ControlState.COMM_SENDING_PAUSED;
                } else {
                    return ControlState.COMM_IDLE;
                }
            case "alarm":
                return ControlState.COMM_IDLE;
            case "check":
                if (isStreaming() && comm.isPaused()) {
                    return ControlState.COMM_SENDING_PAUSED;
                } else if (isStreaming() && !comm.isPaused()) {
                    return ControlState.COMM_SENDING;
                } else {
                    return COMM_CHECK;
                }
            default:
                return ControlState.COMM_IDLE;
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
    public void resetCoordinateToZero(final Axis axis) throws Exception {
        if (this.isCommOpen()) {
            String gcode = GrblUtils.getResetCoordToZeroCommand(axis, this.grblVersion, this.grblVersionLetter);
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
    public void setWorkPosition(Axis axis, double position) throws Exception {
        if (!this.isCommOpen()) {
            throw new Exception("Must be connected to set work position");
        }

        String gcode = GrblUtils.getSetCoordCommand(axis, position, this.grblVersion, this.grblVersionLetter);
        if (StringUtils.isNotEmpty(gcode)) {
            GcodeCommand command = createCommand(gcode);
            this.sendCommandImmediately(command);
        }
    }

    @Override
    public void returnToHome() throws Exception {
        if (this.isCommOpen()) {
            ArrayList<String> commands = GrblUtils.getReturnToHomeCommands(this.grblVersion, this.grblVersionLetter, this.controllerStatus.getWorkCoord().z);
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
        if (this.isCommOpen() && this.capabilities.hasCapability(GrblCapabilitiesConstants.REAL_TIME)) {
            this.comm.sendByteImmediately(GrblUtils.GRBL_RESET_COMMAND);
            //Does GRBL need more time to handle the reset?
            this.comm.softReset();
        }
    }

    @Override
    public void jogMachine(int dirX, int dirY, int dirZ, double stepSize, 
            double feedRate, Units units) throws Exception {
        if (capabilities.hasCapability(GrblCapabilitiesConstants.HARDWARE_JOGGING)) {
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

    @Override
    public String getFirmwareVersion() {
        return getGrblVersion();
    }

    @Override
    public ControllerStatus getControllerStatus() {
        return controllerStatus;
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
                            dispatchConsoleMessage(MessageType.INFO,Localization.getString("controller.exception.sendingstatus")
                                    + " (" + ex.getMessage() + ")\n");
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
        if (this.isReady && this.capabilities != null && this.getStatusUpdatesEnabled()) {
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

        ControlState before = getControlState();
        String beforeState = controllerStatus == null ? "" : controllerStatus.getStateString();

        controllerStatus = GrblUtils.getStatusFromStatusString(
                controllerStatus, string, capabilities, getFirmwareSettings().getReportingUnits());

        // Make UGS more responsive to the state being reported by GRBL.
        if (before != getControlState()) {
            this.dispatchStateChange(getControlState());
        }

        // GRBL 1.1 jog complete transition
        if (StringUtils.equals(beforeState, "Jog") && controllerStatus.getStateString().equals("Idle")) {
            this.comm.cancelSend();
        }

        // Set and restore the step mode when transitioning from CHECK mode to IDLE.
        if (before == COMM_CHECK && getControlState() != COMM_CHECK) {
            setSingleStepMode(temporaryCheckSingleStepMode);
        } else if (before != COMM_CHECK && getControlState() == COMM_CHECK) {
            temporaryCheckSingleStepMode = getSingleStepMode();
            setSingleStepMode(true);
        }

        // Prior to GRBL v1.1 the GUI is required to keep checking locations
        // to verify that the machine has come to a complete stop after
        // pausing.
        if (isCanceling) {
            if (attemptsRemaining > 0 && lastLocation != null) {
                attemptsRemaining--;
                // If the machine goes into idle, we no longer need to cancel.
                if (this.controllerStatus.getStateString().equals("Idle") || this.controllerStatus.getStateString().equalsIgnoreCase("Check")) {
                    isCanceling = false;

                    // Make sure the GUI gets updated
                    this.dispatchStateChange(getControlState());
                }
                // Otherwise check if the machine is Hold/Queue and stopped.
                else if ((this.controllerStatus.getStateString().equals("Hold")
                        || this.controllerStatus.getStateString().equals("Queue"))
                        && lastLocation.equals(this.controllerStatus.getMachineCoord())) {
                    try {
                        this.issueSoftReset();
                    } catch(Exception e) {
                        this.dispatchConsoleMessage(MessageType.ERROR, e.getMessage() + "\n");
                    }
                    isCanceling = false;
                }
                if (isCanceling && attemptsRemaining == 0) {
                    this.dispatchConsoleMessage(MessageType.ERROR, Localization.getString("grbl.exception.cancelReset") + "\n");
                }
            }
            lastLocation = new Position(this.controllerStatus.getMachineCoord());
        }
        
        // Save max Z location
        if (this.controllerStatus != null && this.getUnitsCode() != null
                && this.controllerStatus.getMachineCoord() != null) {
            Units u = this.getUnitsCode().equalsIgnoreCase("G21") ?
                    Units.MM : Units.INCH;
            double zLocationMM = this.controllerStatus.getMachineCoord().z;
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
            this.dispatchConsoleMessage(MessageType.INFO, String.format(">>> 0x%02x\n", realTimeCommand));
            this.comm.sendByteImmediately(realTimeCommand);
        }
    }
}
