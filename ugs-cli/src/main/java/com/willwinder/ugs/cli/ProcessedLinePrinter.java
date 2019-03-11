/*
    Copyright 2016-2019 Will Winder

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
package com.willwinder.ugs.cli;

import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.model.Alarm;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.apache.commons.lang3.StringUtils;

/**
 * A simple class for printing out commands
 *
 * @author Joacim Breiler
 */
public class ProcessedLinePrinter implements ControllerListener {

    @Override
    public void controlStateChange(UGSEvent.ControlState state) {
    }

    @Override
    public void fileStreamComplete(String filename, boolean success) {
    }

    @Override
    public void receivedAlarm(Alarm alarm) {
        System.err.println("Alarm: " + alarm.name());
    }

    @Override
    public void commandSkipped(GcodeCommand command) {
        System.out.println("#" + command.getCommandNumber() + " - " + command.getOriginalCommandString() + " [skipped]");
    }

    @Override
    public void commandSent(GcodeCommand command) {
        if (command.getCommandNumber() > 0) {
            System.out.println("#" + command.getCommandNumber() + " - " + command.getOriginalCommandString());
        }
    }

    @Override
    public void commandComplete(GcodeCommand command) {
        if (command.getCommandNumber() > 0 && !StringUtils.equalsIgnoreCase(command.getResponse(), "ok")) {
            System.err.println("#" + command.getCommandNumber() + " - " + command.getOriginalCommandString() + " [" + command.getResponse() + "]");
        }
    }

    @Override
    public void commandComment(String comment) {

    }

    @Override
    public void probeCoordinates(Position p) {

    }

    @Override
    public void statusStringListener(ControllerStatus status) {
    }
}
