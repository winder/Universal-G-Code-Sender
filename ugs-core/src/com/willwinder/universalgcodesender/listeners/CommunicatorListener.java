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
public interface CommunicatorListener {

    /**
     * Is called when a raw response message is received from the controller
     *
     * @param response a response message from the controller
     */
    void rawResponseListener(String response);

    /**
     * When a command has been sent by the controller
     *
     * @param command the command successfully sent to the controller
     */
    void commandSent(GcodeCommand command);

    /**
     * The command skipped and not sent by the controller
     *
     * @param command the command that has been skipped
     */
    void commandSkipped(GcodeCommand command);

    /**
     * This method will be called when the communicator is paused due to an error during
     * processing of commands.
     */
    void communicatorPausedOnError();
}
