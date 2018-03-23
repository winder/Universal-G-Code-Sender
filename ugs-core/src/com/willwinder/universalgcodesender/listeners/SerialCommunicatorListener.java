/*
    Copyright 2012-2018 Will Winder

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

import com.willwinder.universalgcodesender.types.GcodeCommand;

/**
 * This is the interface which the SerialCommunicator class uses to notify
 * external programs of important events during communication.
 *
 * @author wwinder
 */
public interface SerialCommunicatorListener {
    void rawResponseListener(String response);

    /**
     * This method will be called when a command has
     * @param command
     */
    void commandSent(GcodeCommand command);

    /**
     * This method will be called when a command has been skipped.
     *
     * @param command the command being skipped
     */
    void commandSkipped(GcodeCommand command);

    /**
     * This method will be called when the communicator is paused.
     */
    void communicatorPaused();


    void messageForConsole(String msg);
    void verboseMessageForConsole(String msg);
    void errorMessageForConsole(String msg);
}
