/*
    Copyright 2021 Will Winder

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
package com.willwinder.universalgcodesender.model.events;

import com.willwinder.universalgcodesender.model.UGSEvent;

/**
 * An event that will be dispatched when a new file has been set in
 * program.
 */
public class FileStateEvent implements UGSEvent {
    private final FileState fileState;
    private final String file;
    private final boolean success;

    /**
     * Create a file state event
     * FILE_LOADING: This event provides a path to an unprocessed gcode file.
     * FILE_LOADED: This event provides a path to a processed gcode file which
     * should be opened with a GcodeStreamReader.
     *
     * @param state    the new file state.
     * @param filepath the file related to the file event.
     */
    public FileStateEvent(FileState state, String filepath) {
        this(state, filepath, true);
    }

    /**
     * Create a file state event
     * FILE_LOADING: This event provides a path to an unprocessed gcode file.
     * FILE_LOADED: This event provides a path to a processed gcode file which
     * should be opened with a GcodeStreamReader.
     *
     * @param state    the new file state.
     * @param filepath the file related to the file event.
     * @param success if the given file state was a success, usually used together with {@link FileState#FILE_STREAM_COMPLETE} to indicate if file was sent
     */
    public FileStateEvent(FileState state, String filepath, boolean success) {
        this.fileState = state;
        this.file = filepath;
        this.success = success;
    }

    public FileState getFileState() {
        return fileState;
    }

    public String getFile() {
        return file;
    }

    public boolean isSuccess() {
        return success;
    }
}
