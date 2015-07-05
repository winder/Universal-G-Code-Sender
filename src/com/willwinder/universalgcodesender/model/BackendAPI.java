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

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.model.Utils.Units;
import java.io.File;

public interface BackendAPI extends BackendAPIReadOnly {
        // Config options
        public void setFile(File file) throws Exception;
        public void applySettings(Settings settings) throws Exception;

        public void preprocessAndExportToFile(File f) throws Exception;
        
        // Control options
        public void connect(String firmware, String port, int baudRate) throws Exception;
        public void disconnect() throws Exception;
        public void sendGcodeCommand(String commandText) throws Exception;
	public void adjustManualLocation(int dirX, int dirY, int dirZ, double stepSize, Units units) throws Exception;
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
               
        // Shouldn't be needed often.
        public IController getController();
        public void applySettingsToController(Settings settings, IController controller) throws Exception;
}