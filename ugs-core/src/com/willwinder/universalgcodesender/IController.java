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
package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.gcode.GcodeCommandCreator;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.Plane;
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
    /*
    Observable
    */
    public void addListener(ControllerListener cl);

    /*
    Actions
    */
    public void performHomingCycle() throws Exception;
    public void returnToHome() throws Exception;
    public void resetCoordinatesToZero() throws Exception;
    public void resetCoordinateToZero(final char coord) throws Exception;
    public void killAlarmLock() throws Exception;
    public void toggleCheckMode() throws Exception;
    public void viewParserState() throws Exception;
    public void issueSoftReset() throws Exception;

    /**
     * Jog control. Jogs the machine in the direction specified by vector dirX,
     * dirY, dirZ a distance specified by stepSize * units.
     */
    public void jogMachine(int dirX, int dirY, int dirZ,
            double stepSize, double feedRate, Units units) throws Exception;

    /**
     * Probe control
     */
    public void probe(String axis, double feedRate, double distance, UnitUtils.Units units) throws Exception;
    public void offsetTool(String axis, double offset, UnitUtils.Units units) throws Exception;

    /*
    Overrides
    */
    public void sendOverrideCommand(Overrides command) throws Exception;

    /*
    Behavior
    */
    public void setSingleStepMode(boolean enabled);
    public boolean getSingleStepMode();

    public void setStatusUpdatesEnabled(boolean enabled);
    public boolean getStatusUpdatesEnabled();
    
    public void setStatusUpdateRate(int rate);
    public int getStatusUpdateRate();
    
    public GcodeCommandCreator getCommandCreator();
    public long getJobLengthEstimate(File gcodeFile);
    
    /*
    Serial
    */
    public Boolean openCommPort(String port, int portRate) throws Exception;
    public Boolean closeCommPort() throws Exception;
    public Boolean isCommOpen();
    
    /*
    Stream information
    */
    public Boolean isReadyToReceiveCommands() throws Exception;
    public Boolean isReadyToStreamFile() throws Exception;
    public Boolean isStreaming();
    public long getSendDuration();
    public int rowsInSend();
    public int rowsSent();
    public int rowsRemaining();
    public GcodeCommand getActiveCommand();
    public GcodeState getCurrentGcodeState();
    
    /*
    Stream control
    */
    public void beginStreaming() throws Exception;
    public void pauseStreaming() throws Exception;
    public void resumeStreaming() throws Exception;
    public Boolean isPaused();
    public Boolean isIdle();
    public void cancelSend() throws Exception;
    public ControlState getControlState();

    /**
     * In case a controller reset is detected.
     */
    public void resetBuffers();

    /**
     * Indicator to abstract GUIBackend implementation that the contract class
     * will handle ALL state change events. When this returns true it means
     * things like completing the final command in a stream will not
     * automatically re-enable buttons.
     */
    public Boolean handlesAllStateChangeEvents();
    
    /*
    Stream content
    */
    public GcodeCommand createCommand(String gcode) throws Exception;
    public void sendCommandImmediately(GcodeCommand cmd) throws Exception;
    public void queueCommand(GcodeCommand cmd) throws Exception;
    public void queueStream(GcodeStreamReader r);
    public void queueRawStream(Reader r);

    public void restoreParserModalState();
    public void updateParserModalState(GcodeCommand command);
}
