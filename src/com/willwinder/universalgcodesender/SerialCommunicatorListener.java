/*
 * This is the interface which the SerialCommunicator class uses to notify
 * external programs of important events during communication.
 */

/*
    Copywrite 2012 Will Winder

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

/**
 *
 * @author wwinder
 */
public interface SerialCommunicatorListener {
    void fileStreamComplete(String filename, boolean success);
    void commandQueued(GcodeCommand command);
    void commandSent(GcodeCommand command);
    void commandComplete(GcodeCommand command);
    void commandComment(String comment);
    void messageForConsole(String msg);
    void verboseMessageForConsole(String msg);
    void capabilitiesListener(CommUtils.Capabilities capability);
    void positionStringListener(String position);
    String preprocessCommand(String command);
}
