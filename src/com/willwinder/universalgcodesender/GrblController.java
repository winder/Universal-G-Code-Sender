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

import com.willwinder.universalgcodesender.types.GcodeCommand;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
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
    
    // Grbl status members.
    private GrblUtils.Capabilities positionMode = null;
    private Boolean realTimeCapable = false;
    private String grblState;
    private Point3d machineLocation;
    private Point3d workLocation;
    
    // Polling state
    private int outstandingPolls = 0;
    private Timer positionPollTimer = null;  
    private int pollingRate = 200;
    
    protected GrblController(GrblCommunicator comm) {
        super(comm);
        
        this.commandCreator = new GcodeCommandCreator();
        this.positionPollTimer = createPositionPollTimer();
    }
    
    public GrblController() {
        this(new GrblCommunicator());
    }
    
    /***********************
     * API Implementation.
     ***********************
     */
    
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
            if (this.positionMode != null) {
                // Start sending '?' commands.
                this.beginPollingPosition();
            }
            
            Logger.getLogger(GrblController.class.getName()).log(Level.CONFIG, 
                    "Grbl version = " + this.grblVersion + this.grblVersionLetter);
            Logger.getLogger(GrblController.class.getName()).log(Level.CONFIG,
                    "Real time mode = " + this.realTimeCapable);
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
    protected void isReadyToStreamFileEvent() throws Exception {
        if (this.isReady == false) {
            throw new Exception("Grbl has not finished booting.");
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
        throw new Exception("No supported homing method for " + this.getGrblVersion());
    }
    
    @Override
    public void returnToHome() throws Exception {
        if (this.isCommOpen()) {
            String command = GrblUtils.getReturnToHomeCommand(this.grblVersion, this.grblVersionLetter);
            if (!"".equals(command)) {
                this.queueStringForComm(command);
                return;
            }
        }
        throw new Exception("No supported homing method for " + this.getGrblVersion());
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
        throw new Exception("No supported kill alarm lock method for " + this.getGrblVersion());
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
        throw new Exception("No supported toggle check mode method for " + this.getGrblVersion());
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
        throw new Exception("No supported view parser state method for " + this.getGrblVersion());
    }
    
    /**
     * If it is supported, a soft reset real-time command will be issued.
     */
    @Override
    public void issueSoftReset() throws IOException {
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
            return str.toString();
        }
        return "<not connected>";
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
                            messageForConsole("IOException while sending status command: " + ex.getMessage() + "\n");
                        }
                    }
                });
                
            }
        };
        
        return new Timer(pollingRate, actionListener);
    }
    /**
     * Begin issuing GRBL status request commands.
     */
    private void beginPollingPosition() {
        if (this.positionPollTimer.isRunning() == false) {
            this.outstandingPolls = 0;
            this.positionPollTimer.start();
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
         
            this.dispatchStatusString(this.grblState, this.machineLocation, this.workLocation);
        }
    }
}
    