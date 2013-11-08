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
    
    /**
     * A command has been added to the output queue.
     */
    void commandQueued(GcodeCommand command);
    
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
    
    /**
     * A console message from the controller.
     */
    void messageForConsole(String msg, Boolean verbose);
    
    /**
     * Controller status information.
     */
    void statusStringListener(String state, Point3d machineCoord, Point3d workCoord);
    
    /**
     * Data gathered while preprocessing commands for queue.
     */
    void postProcessData(int numRows);
}