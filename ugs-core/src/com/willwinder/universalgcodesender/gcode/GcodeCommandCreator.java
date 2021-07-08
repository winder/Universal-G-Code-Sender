/*
 * Simple class to increment gcode command numbers.
 */

/*
    Copyright 2013 Will Winder

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
package com.willwinder.universalgcodesender.gcode;

import com.willwinder.universalgcodesender.types.GcodeCommand;

/**
 *
 * @author wwinder
 */
public class GcodeCommandCreator {
    protected int numCommands = 0;
    
    public GcodeCommandCreator() {
    }
    
    public GcodeCommandCreator(int num) {
        this.numCommands = num;
    }
    
    public void resetNum() {
        this.numCommands = 0;
    }

    public int nextCommandNum() {
        return this.numCommands;
    }
    
    public GcodeCommand createCommand(String commandString) throws Exception {
        return new GcodeCommand(commandString, this.numCommands++);
    }
}
