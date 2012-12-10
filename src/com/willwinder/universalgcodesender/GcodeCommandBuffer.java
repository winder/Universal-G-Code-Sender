/*
 * A queue wrapper for GcodeCommand objects. The main purpose of this class is
 * to handle indexing of GcodeCommands and to pre-load the next command so that
 * whatever is "next" is available to the SerialCommunicator.
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

import java.io.*;
import java.util.*;

/**
 *
 * @author wwinder
 */
public class GcodeCommandBuffer {
    private Queue<GcodeCommand> commandQueue;
    private GcodeCommand currentCommand = null;
    private int numCommands = 0;
    
    GcodeCommandBuffer() {
        this.commandQueue = new LinkedList<GcodeCommand>();
    }
    
    int size() {
        return this.commandQueue.size();
    }

    Boolean hasNext() {
        return this.commandQueue.size() > 0;
    }
    
    GcodeCommand currentCommand() {
        return currentCommand;
    }
    
    GcodeCommand nextCommand() {
        // Leave the "currentCommand" alone if we've exausted the queue.
        if (this.hasNext()) {
            this.currentCommand = this.commandQueue.remove();
        }
        
        return this.currentCommand();
    }
    
    GcodeCommand appendCommandString(String commandString) {
        GcodeCommand command = new GcodeCommand(commandString);
        command.setCommandNumber(this.numCommands++);
        this.commandQueue.add(command);
        
        // Preload first command, or next command if the first batch finished.
        if (this.currentCommand == null || this.currentCommand.isSent()) {
            this.nextCommand();
        }
        
        return command;
    }
    
    void clearBuffer() {
        this.currentCommand = null;
        this.commandQueue.clear();
    }
}
