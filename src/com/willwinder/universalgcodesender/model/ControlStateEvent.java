/*
 * These objects are passed around by the GUI API to notify listeners of state
 * changes.
 */

/*
    Copywrite 2012-2015 Will Winder

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

import com.willwinder.universalgcodesender.model.Utils.ControlState;

/**
 *
 * @author wwinder
 */
public class ControlStateEvent {
    public enum event {
        STATE_CHANGED,
        FILE_CHANGED,
    }
    
    event evt = null;
    ControlState controlState = null;
    String file = null;
    
    public ControlStateEvent(ControlState state) {
        evt = event.STATE_CHANGED;
        controlState = state;
    }
    
    public ControlStateEvent(String filepath) {
        evt = event.FILE_CHANGED;
        file = filepath;
    }
    
    public event getEventType() {
        return evt;
    }
    
    public ControlState getState() {
        return controlState;
    }
    
    public String getFile() {
        return file;
    }
}
