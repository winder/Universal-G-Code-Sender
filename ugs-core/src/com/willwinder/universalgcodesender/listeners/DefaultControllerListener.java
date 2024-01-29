/*
    Copyright 2024 Will Winder

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

import com.willwinder.universalgcodesender.model.Alarm;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.types.GcodeCommand;

/**
 * A controller listener that has empty default implementations of the interface.
 * Override only the things that you need.
 *
 * @author Joacim Breiler
 */
public class DefaultControllerListener implements ControllerListener {
    @Override
    public void streamCanceled() {
        // Not implemented
    }

    @Override
    public void streamStarted() {
        // Not implemented
    }

    @Override
    public void streamPaused() {
        // Not implemented
    }

    @Override
    public void streamResumed() {
        // Not implemented
    }

    @Override
    public void streamComplete() {
        // Not implemented
    }

    @Override
    public void receivedAlarm(Alarm alarm) {
        // Not implemented
    }

    @Override
    public void commandSkipped(GcodeCommand command) {
        // Not implemented
    }

    @Override
    public void commandSent(GcodeCommand command) {
        // Not implemented
    }

    @Override
    public void commandComplete(GcodeCommand command) {
        // Not implemented
    }

    @Override
    public void probeCoordinates(Position p) {
        // Not implemented
    }

    @Override
    public void statusStringListener(ControllerStatus status) {
        // Not implemented
    }
}
