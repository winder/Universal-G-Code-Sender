/*
    Copyright 2015-2018 Will Winder

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
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.model.Overrides;
import com.willwinder.universalgcodesender.model.UGSEvent.ControlState;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;

import java.io.File;
import java.io.Reader;

/**
 *
 * @author will
 */
public interface IController {

    /**
     * Adds listener for observing controller events such as connection status,
     * file send status and machine status
     *
     * @param listener the listener to observe the controller
     */
    void addListener(ControllerListener listener);

    /**
     * Removes a listener for this controller
     *
     * @param listener to be removed
     */
    void removeListener(ControllerListener listener);

    /*
    Actions
    */
    void performHomingCycle() throws Exception;
    void returnToHome() throws Exception;
    void resetCoordinatesToZero() throws Exception;
    void resetCoordinateToZero(final char coord) throws Exception;
    void killAlarmLock() throws Exception;
    void toggleCheckMode() throws Exception;
    void viewParserState() throws Exception;
    void issueSoftReset() throws Exception;

    /**
     * Jog control. Jogs the machine in the direction specified by vector dirX,
     * dirY, dirZ a distance specified by stepSize * units.
     */
    void jogMachine(int dirX, int dirY, int dirZ,
                    double stepSize, double feedRate, Units units) throws Exception;

    /**
     * Probe control
     */
    void probe(String axis, double feedRate, double distance, UnitUtils.Units units) throws Exception;
    void offsetTool(String axis, double offset, UnitUtils.Units units) throws Exception;

    /*
    Overrides
    */
    void sendOverrideCommand(Overrides command) throws Exception;

    /*
    Behavior
    */
    void setSingleStepMode(boolean enabled);
    boolean getSingleStepMode();

    void setStatusUpdatesEnabled(boolean enabled);
    boolean getStatusUpdatesEnabled();
    
    void setStatusUpdateRate(int rate);
    int getStatusUpdateRate();
    
    GcodeCommandCreator getCommandCreator();
    long getJobLengthEstimate(File gcodeFile);
    
    /*
    Serial
    */
    Boolean openCommPort(String port, int portRate) throws Exception;
    Boolean closeCommPort() throws Exception;
    Boolean isCommOpen();
    
    /*
    Stream information
    */
    Boolean isReadyToReceiveCommands() throws Exception;
    Boolean isReadyToStreamFile() throws Exception;
    Boolean isStreaming();
    long getSendDuration();
    int rowsInSend();
    int rowsSent();
    int rowsRemaining();
    GcodeCommand getActiveCommand();
    GcodeState getCurrentGcodeState();
    
    /*
    Stream control
    */
    void beginStreaming() throws Exception;
    void pauseStreaming() throws Exception;
    void resumeStreaming() throws Exception;
    Boolean isPaused();
    Boolean isIdle();
    void cancelSend() throws Exception;
    ControlState getControlState();

    /**
     * In case a controller reset is detected.
     */
    void resetBuffers();

    /**
     * Indicator to abstract GUIBackend implementation that the contract class
     * will handle ALL state change events. When this returns true it means
     * things like completing the final command in a stream will not
     * automatically re-enable buttons.
     */
    Boolean handlesAllStateChangeEvents();
    
    /*
    Stream content
    */
    GcodeCommand createCommand(String gcode) throws Exception;
    void sendCommandImmediately(GcodeCommand cmd) throws Exception;
    void queueCommand(GcodeCommand cmd) throws Exception;
    void queueStream(GcodeStreamReader r);

    void restoreParserModalState();
    void updateParserModalState(GcodeCommand command);
}
