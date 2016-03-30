/*
 * These objects are passed around by the GUI API to notify listeners of state
 * changes.
 */

/*
    Copywrite 2012-2016 Will Winder

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

/**
 *
 * @author wwinder
 */
public class UGSEvent {
    eventType evt = null;
    ControlState controlState = null;
    FileState fileState = null;
    String file = null;
    
    private enum eventType {
        STATE_EVENT,
        FILE_EVENT,
    }

    public enum FileState {
        FILE_LOADING,
        FILE_LOADED
    }
        
    public enum ControlState {
        COMM_DISCONNECTED,
        COMM_IDLE,
        COMM_SENDING,
        COMM_SENDING_PAUSED,
    };
    
    public boolean isStateChangeEvent() {
        return evt == eventType.STATE_EVENT;
    }

    public boolean isFileChangeEvent() {
        return evt == eventType.FILE_EVENT;
    }

    /**
     * Create a control state event.
     * @param state the new state.
     */
    public UGSEvent(ControlState state) {
        evt = eventType.STATE_EVENT;
        controlState = state;
    }
    
    /**
     * Create a file state event
     * FILE_LOADING: This event provides a path to an unprocessed gcode file.
     * FILE_LOADED: This event provides a path to a processed gcode file which
     *              should be opened with a GcodeStreamReader.
     * @param state the new file state.
     * @param filepath the file related to the file event.
     */
    public UGSEvent(FileState state, String filepath) {
        evt = eventType.FILE_EVENT;
        fileState = state;
        file = filepath;
    }
    
    // Getters

    public ControlState getControlState() {
        return controlState;
    }

    public FileState getFileState() {
        return fileState;
    }
    
    public String getFile() {
        return file;
    }
}
