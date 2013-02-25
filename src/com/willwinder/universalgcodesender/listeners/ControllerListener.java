/*
 * Controler Listener event interface
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
package com.willwinder.universalgcodesender.listeners;

import com.willwinder.universalgcodesender.types.GcodeCommand;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public interface ControllerListener {
    void fileStreamComplete(String filename, boolean success);
    void commandQueued(GcodeCommand command);
    void commandSent(GcodeCommand command);
    void commandComplete(GcodeCommand command);
    void commandComment(String comment);
    void messageForConsole(String msg, Boolean verbose);
    void statusStringListener(String state, Point3d machineCoord, Point3d workCoord);
}