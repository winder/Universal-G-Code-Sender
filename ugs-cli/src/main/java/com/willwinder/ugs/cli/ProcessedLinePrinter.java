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

import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.AlarmEvent;
import com.willwinder.universalgcodesender.model.events.CommandEvent;
import com.willwinder.universalgcodesender.model.events.CommandEventType;
import com.willwinder.universalgcodesender.types.GcodeCommand;

/**
 * A simple class for printing out commands
 *
 * @author Joacim Breiler
 */
public class ProcessedLinePrinter implements UGSEventListener {

    @Override
    public void UGSEvent(UGSEvent event) {
        if (event instanceof CommandEvent) {
            CommandEvent commandEvent = ((CommandEvent) event);
            GcodeCommand command = commandEvent.getCommand();
            if (commandEvent.getCommandEventType() == CommandEventType.COMMAND_COMPLETE) {
                onCommandComplete(command);
            } else if (commandEvent.getCommandEventType() == CommandEventType.COMMAND_SENT) {
                onCommandSent(command);
            } else if (commandEvent.getCommandEventType() == CommandEventType.COMMAND_SKIPPED) {
                onCommandSkipped(command);
            }
        } else if (event instanceof AlarmEvent) {
            System.err.println("Alarm: " + ((AlarmEvent) event).getAlarm().name());
        }
    }

    private void onCommandSkipped(GcodeCommand command) {
        System.out.println("#" + command.getCommandNumber() + " - " + command.getOriginalCommandString() + " [skipped]");
    }

    private void onCommandSent(GcodeCommand command) {
        if (command.getCommandNumber() > 0 && !command.isGenerated()) {
            System.out.println("#" + command.getCommandNumber() + " - " + command.getOriginalCommandString());
        } else {
            System.out.println("> " + command.getOriginalCommandString());
        }
    }

    private void onCommandComplete(GcodeCommand command) {
        if (command.getCommandNumber() > 0 && (command.isError() || command.isSkipped())) {
            System.err.println("#" + command.getCommandNumber() + " - " + command.getOriginalCommandString() + " [" + command.getResponse() + "]");
        }
    }
}
