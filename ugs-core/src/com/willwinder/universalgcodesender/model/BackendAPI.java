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
import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import java.io.File;
import java.io.IOException;

public abstract interface BackendAPI extends BackendAPIReadOnly {
    // Config options
    public void setGcodeFile(File file) throws Exception;
    public void setTempDir(File file) throws IOException;
    public void applySettings(Settings settings) throws Exception;

    /**
     * Modify the currently processed gcode with a provided gcode parser.
     * This can be used for post-processing tasks like rotating a gcode file.
     * @param gcp externally configured gcode parser.
     * @throws Exception 
     */
    public void applyGcodeParser(GcodeParser gcp) throws Exception;

    /**
     * Process the currently loaded gcode file and export it to a file.
     * Intended primarily as "save and export" style preprocessor option.
     * @param f location to export processed gcode
     */
    public void preprocessAndExportToFile(File f) throws Exception;
    
    // Control options
    public void connect(String firmware, String port, int baudRate) throws Exception;
    public void disconnect() throws Exception;
    public void sendGcodeCommand(String commandText) throws Exception;
    public void sendGcodeCommand(boolean restoreParserState, String commandText) throws Exception;
    public void sendGcodeCommand(GcodeCommand command) throws Exception;
    public void adjustManualLocation(int dirX, int dirY, int dirZ, double stepSize, double feedRate, Units units) throws Exception;

    public void probe(String axis, double feedRate, double distance, UnitUtils.Units units) throws Exception;
    public void offsetTool(String axis, double offset, UnitUtils.Units units) throws Exception;

    public void send() throws Exception;
    public void pauseResume() throws Exception;
    public void cancel() throws Exception;
    public void returnToZero() throws Exception;
    public void resetCoordinatesToZero() throws Exception;
    public void resetCoordinateToZero(char coordinate) throws Exception;
    public void restoreParserState() throws Exception;
    
    public void killAlarmLock() throws Exception;
    public void performHomingCycle() throws Exception;
    public void toggleCheckMode() throws Exception;
    public void issueSoftReset() throws Exception;
    public void requestParserState() throws Exception;

    public enum ACTIONS {
        RETURN_TO_ZERO,
        RESET_COORDINATES_TO_ZERO,
        KILL_ALARM_LOCK,
        HOMING_CYCLE,
        TOGGLE_CHECK_MODE,
        ISSUE_SOFT_RESET,
        REQUEST_PARSER_STATE
    }

    // Programatically call an action.
    public void performAction(ACTIONS action) throws Exception;

    // Programatically call an override.
    public void sendOverrideCommand(Overrides override) throws Exception;
           
    // Shouldn't be needed often.
    public IController getController();
    public void applySettingsToController(Settings settings, IController controller) throws Exception;

    void sendMessageForConsole(String msg);
}