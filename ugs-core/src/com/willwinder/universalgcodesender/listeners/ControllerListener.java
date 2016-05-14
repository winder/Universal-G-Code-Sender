/*
 * Controler Listener event interface
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
package com.willwinder.universalgcodesender.listeners;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent.ControlState;
import com.willwinder.universalgcodesender.types.GcodeCommand;

/**
 *
 * @author wwinder
 */
public interface ControllerListener {
    /**
     * The controller has modified the state by itself, such as pausing a job on
     * an error.
     */
    void controlStateChange(ControlState state);

    /**
     * The file streaming has completed.
     */
    void fileStreamComplete(String filename, boolean success);
    
    /**
     * A command in the stream has been skipped.
     */
    void commandSkipped(GcodeCommand command);
    
    /**
     * A command has successfully been sent to the controller.
     */
    void commandSent(GcodeCommand command);
    
    /**
     * A command has been processed by the the controller.
     */
    void commandComplete(GcodeCommand command);
    
    /**
     * A comment has been processed.
     */
    void commandComment(String comment);
    
    enum MessageType {
        VERBOSE("verbose"),
        INFO("info"),
        ERROR("error");

        private final String key;

        private MessageType(String key) {
            this.key = key;
        }

        public String getLocalizedString() {
            return Localization.getString(key);
        }
    }

    /**
     * A console message from the controller.
     */
    void messageForConsole(MessageType type, String msg);
    
    /**
     * Controller status information.
     */
    void statusStringListener(String state, Position machineCoord, Position workCoord);
    
    /**
     * Data gathered while preprocessing commands for queue.
     */
    void postProcessData(int numRows);
}