/*
    Copyright 2020 Will Winder

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
package com.willwinder.universalgcodesender.services;

import com.willwinder.universalgcodesender.gcode.processors.RotateProcessor;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;

/**
 * A service for rotating the loaded gcode model. When this service is created it will load a gcode processor to
 * the backend which can be used to rotate the currently loaded gcode program model.
 *
 * @author Joacim Breiler
 */
public class RotateModelService implements UGSEventListener {
    private RotateProcessor rotateProcessor = new RotateProcessor();
    private final BackendAPI backend;

    public RotateModelService(BackendAPI backend) {
        this.backend = backend;
        try {
            this.backend.applyCommandProcessor(rotateProcessor);
        } catch (Exception e) {
            // Never mind this
        }
        this.backend.addUGSEventListener(this);
    }

    public void rotateModel(Position center, double radians) {
        rotateProcessor.setCenter(center);
        rotateProcessor.setRotation(radians);

        try {
            this.backend.applyCommandProcessor(rotateProcessor);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public double getRotation() {
        return rotateProcessor.getRotation();
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if(evt.isFileChangeEvent() && evt.getFileState() == UGSEvent.FileState.OPENING_FILE) {
            rotateProcessor.setRotation(0);
        }
    }
}
