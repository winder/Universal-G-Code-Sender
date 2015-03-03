/**
 * API used by front ends to interface with the model.
 */
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

import com.willwinder.universalgcodesender.AbstractController;
import com.willwinder.universalgcodesender.Settings;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.Utils.ControlState;
import com.willwinder.universalgcodesender.pendantui.SystemStateBean;
import java.io.File;

public interface BackendAPI {
        // Config options
        public void setFile(File file) throws Exception;
        public File getFile();
        public void applySettings(Settings settings) throws Exception;

        // Control options
        public void connect(String firmware, String port, int baudRate) throws Exception;
        public void disconnect() throws Exception;
        public void sendGcodeCommand(String commandText) throws Exception;
	public void adjustManualLocation(int dirX, int dirY, int dirZ, double stepSize, Utils.Units units) throws Exception;
	public void send() throws Exception;
	public void pauseResume() throws Exception;
	public void cancel() throws Exception;
        public void returnToZero() throws Exception;
	public void resetCoordinatesToZero() throws Exception;
        public void resetCoordinateToZero(char coordinate) throws Exception;
        
        public void killAlarmLock() throws Exception;
        public void performHomingCycle() throws Exception;
        public void toggleCheckMode() throws Exception;
        public void issueSoftReset() throws Exception;
        public void requestParserState() throws Exception;
        
        // Controller status
        public boolean isConnected();
        public boolean isSending();
        public boolean isPaused();
        public boolean canPause();
	public boolean canCancel();
        public boolean canSend();
        public ControlState getControlState();
        
        // Send status
        public long getNumRows();
        public long getNumSentRows();
        public long getNumRemainingRows();

        public long getSendDuration();
        public long getSendRemainingDuration();
        public String getPauseResumeText();
        
        // Bulk status getter.
	public void updateSystemState(SystemStateBean systemStateBean);	
        
	// Shouldn't be needed often.
	public Settings getSettings();
	public AbstractController getController();
        public void applySettingsToController(Settings settings, AbstractController controller) throws Exception;
}