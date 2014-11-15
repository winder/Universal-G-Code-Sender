/*
 * GRBL Control layer, coordinates all aspects of control.
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

import com.willwinder.universalgcodesender.gcode.GcodeCommandCreator;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.GrblSettingsListener;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Timer;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class GrblController extends AbstractController {
    // Grbl state
    private double grblVersion = 0.0;        // The 0.8 in 'Grbl 0.8c'
    private String grblVersionLetter = null; // The c in 'Grbl 0.8c'
    private Boolean isReady = false;         // Not ready until version is received.
    private GrblSettingsListener settings;

    // Grbl status members.
    private GrblUtils.Capabilities positionMode = null;
    private Boolean realTimeCapable = false;
    private String grblState;
    private Point3d machineLocation;
    private Point3d workLocation;
    private double maxZLocation;
    
    // Polling state
    private int outstandingPolls = 0;
    private Timer positionPollTimer = null;  
    
    protected GrblController(AbstractCommunicator comm) {
        super(comm);
        
        this.commandCreator = new GcodeCommandCreator();
        this.positionPollTimer = createPositionPollTimer();
        this.maxZLocation = -1;
        this.settings = new GrblSettingsListener(this);
    }
    
    public GrblController() {
        this(new GrblCommunicator()); //f4grx: connection created at opencomm() time
    }

    @Override
    public long getJobLengthEstimate(Collection<String> jobLines) {
        // Pending update to support cross-platform and multiple GRBL versions.
        return 0;
        //GrblSimulator simulator = new GrblSimulator(settings.getSettings());
        //return simulator.estimateRunLength(jobLines);
    }

    /***********************
     * API Implementation.
     ***********************
     */
    
    @Override
    protected void rawResponseHandler(String response) {
        /*
        // Check if was 'ok' or 'error'.
        if (GcodeCommand.isOkErrorResponse(response)) {
            
            // All Ok/Error messages go to console
            this.sendMessageToConsoleListener(response + "\n");

            // Pop the front of the active list.
            GcodeCommand command = this.activeCommandList.pop();

            command.setResponse(response);

            ListenerUtils.dispatchListenerEvents(ListenerUtils.COMMAND_COMPLETE, 
                    this.commandCompleteListeners, command);

            if (this.sendPaused == false) {
                this.streamCommands();
            }
        }
        */
        
        if (GcodeCommand.isOkErrorResponse(response)) {            
            try {
                this.commandComplete(response);
            } catch (Exception e) {
                this.errorMessageForConsole(Localization.getString("controller.error.response")
                        + " <" + response + ">: " + e.getMessage());
            }
            
            this.messageForConsole(response + "\n");
        }
        
        else if (GrblUtils.isGrblVersionString(response)) {
            // Version string goes to console
            this.messageForConsole(response + "\n");
            
            this.grblVersion = GrblUtils.getVersionDouble(response);
            this.grblVersionLetter = GrblUtils.getVersionLetter(response);
            this.isReady = true;
            
            this.realTimeCapable = GrblUtils.isRealTimeCapable(this.grblVersion);
            
            this.positionMode = GrblUtils.getGrblStatusCapabilities(this.grblVersion, this.grblVersionLetter);
            this.beginPollingPosition();
            
            Logger.getLogger(GrblController.class.getName()).log(Level.CONFIG, 
                    "{0} = {1}{2}", new Object[]{Localization.getString("controller.log.version"), this.grblVersion, this.grblVersionLetter});
            Logger.getLogger(GrblController.class.getName()).log(Level.CONFIG, 
                    "{0} = {1}", new Object[]{Localization.getString("controller.log.realtime"), this.realTimeCapable});
        }
        
        else if (GrblUtils.isGrblStatusString(response)) {
            // Only 1 poll is sent at a time so don't decrement, reset to zero.
            this.outstandingPolls = 0;

            // Status string goes to verbose console
            verboseMessageForConsole(response + "\n");
            
            this.handlePositionString(response);
        }
        
        else {
            // Display any unhandled messages
            this.messageForConsole(response + "\n");
        }
    }
    @Override
    protected void pauseStreamingEvent() throws IOException {
        if (this.realTimeCapable) {
            this.comm.sendByteImmediately(GrblUtils.GRBL_PAUSE_COMMAND);
        }
    }
    
    @Override
    protected void resumeStreamingEvent() throws IOException {
        if (this.realTimeCapable) {
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
    protected void openCommAfterEvent() throws IOException {
        this.comm.sendByteImmediately(GrblUtils.GRBL_RESET_COMMAND);
    }

    @Override
    protected void isReadyToStreamFileEvent() throws Exception {
        if (this.isReady == false) {
            throw new Exception(Localization.getString("controller.exception.booting"));
        }
    }
    
    @Override
    protected void cancelSendBeforeEvent() {
        // Check if we can get fancy with a soft reset.
        if (this.realTimeCapable == true) {
            // This doesn't seem to work.
            /*
            try {
                if (!this.paused) {
                    this.pauseStreaming();
                }
                this.issueSoftReset();
                this.awaitingResponseQueue.clear();
            } catch (IOException e) {
                // Oh well, was worth a shot.
                System.out.println("Exception while trying to issue a soft reset: " + e.getMessage());
            }
            */
        }
    }
    
    @Override
    protected void cancelSendAfterEvent() {
    }
    
    /**
     * Sends the version specific homing cycle to the machine.
     */
    @Override
    public void performHomingCycle() throws Exception {
        if (this.isCommOpen()) {
            String command = GrblUtils.getHomingCommand(this.grblVersion, this.grblVersionLetter);
            if (!"".equals(command)) {
                this.queueStringForComm(command);
                return;
            }
        }
        // Throw exception
        super.performHomingCycle();
    }
    
    @Override
    public void resetCoordinatesToZero() throws Exception {
        if (this.isCommOpen()) {
            String command = GrblUtils.getResetCoordsToZeroCommand(this.grblVersion, this.grblVersionLetter);
            if (!"".equals(command)) {
                this.queueStringForComm(command);
                return;
            }
        }
        // Throw exception
        super.resetCoordinatesToZero();
    }
    
    @Override
    public void resetCoordinateToZero(final char coord) throws Exception {
        if (this.isCommOpen()) {
            String command = GrblUtils.getResetCoordToZeroCommand(coord, this.grblVersion, this.grblVersionLetter);
            if (!"".equals(command)) {
                this.queueStringForComm(command);
                return;
            }
        }
        // Throw exception
        super.resetCoordinatesToZero();
    }
    
    @Override
    public void returnToHome() throws Exception {
        if (this.isCommOpen()) {
            double max = 4;
            if (this.maxZLocation != -1) {
                max = this.maxZLocation;
            }
            ArrayList<String> commands = GrblUtils.getReturnToHomeCommands(this.grblVersion, this.grblVersionLetter, max);
            if (!commands.isEmpty()) {
                Iterator<String> iter = commands.iterator();
                // Perform the homing commands
                while(iter.hasNext()){
                    String command = iter.next();
                    this.queueStringForComm(command);
                }
                return;
            }
        }
        // Throw exception
        super.returnToHome();
    }
    
    @Override
    public void killAlarmLock() throws Exception {
        if (this.isCommOpen()) {
            String command = GrblUtils.getKillAlarmLockCommand(this.grblVersion, this.grblVersionLetter);
            if (!"".equals(command)) {
                this.queueStringForComm(command);
                return;
            }
        }
        // Throw exception
        super.killAlarmLock();
    }

    @Override
    public void toggleCheckMode() throws Exception {
        if (this.isCommOpen()) {
            String command = GrblUtils.getToggleCheckModeCommand(this.grblVersion, this.grblVersionLetter);
            if (!"".equals(command)) {
                this.queueStringForComm(command);
                return;
            }
        }
        // Throw exception
        super.toggleCheckMode();
    }

    @Override
    public void viewParserState() throws Exception {
        if (this.isCommOpen()) {
            String command = GrblUtils.getViewParserStateCommand(this.grblVersion, this.grblVersionLetter);
            if (!"".equals(command)) {
                this.queueStringForComm(command);
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
    public void softReset() throws IOException {
        if (this.isCommOpen() && this.realTimeCapable) {
            this.comm.sendByteImmediately(GrblUtils.GRBL_RESET_COMMAND);
            //Does GRBL need more time to handle the reset?
            this.comm.softReset();
        }
    }
        
    /************
     * Helpers.
     ************
     */
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
                        } catch (IOException ex) {
                            messageForConsole(Localization.getString("controller.exception.sendingstatus")
                                    + ": " + ex.getMessage() + "\n");
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
        if (this.positionMode != null && this.getStatusUpdatesEnabled()) {
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
    private void handlePositionString(String string) {
        if (this.positionMode != null) {
            this.grblState = GrblUtils.getStateFromStatusString(string, this.positionMode);
            this.machineLocation = GrblUtils.getMachinePositionFromStatusString(string, this.positionMode);
            this.workLocation = GrblUtils.getWorkPositionFromStatusString(string, this.positionMode);
            if ( (this.machineLocation != null) && 
                    (this.machineLocation.z > this.maxZLocation)) {
                this.maxZLocation = this.machineLocation.z;
            }
         
            this.dispatchStatusString(this.grblState, this.machineLocation, this.workLocation);
        }
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
}
