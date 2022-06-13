/*
    Copyright 2022 Will Winder

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
package com.willwinder.universalgcodesender.utils;

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.types.CommandListener;
import com.willwinder.universalgcodesender.types.GcodeCommand;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @author Joacim Breiler
 */
public class ControllerUtils {

    private static final int MAX_EXECUTION_TIME = 2000;

    /**
     * Sends a command and blocks the thread until the command is done - either with an ok or error.
     *
     * @param controller       the controller to send the command through
     * @param command          a command to send
     * @param maxExecutionTime the max number of milliseconds to wait before throwing a timeout error
     * @throws Exception if the command could not be sent or a timeout occurred
     */
    public static GcodeCommand sendAndWaitForCompletion(IController controller, GcodeCommand command, long maxExecutionTime) throws Exception {
        final AtomicBoolean isDone = new AtomicBoolean(false);
        CommandListener listener = c -> isDone.set(c.isDone());
        command.addListener(listener);
        controller.sendCommandImmediately(command);

        long startTime = System.currentTimeMillis();
        while (!isDone.get()) {
            if (System.currentTimeMillis() > startTime + maxExecutionTime) {
                throw new RuntimeException("The command \"" + command.getCommandString() + "\" has timed out as it wasn't finished within " + maxExecutionTime + "ms");
            }
            Thread.sleep(10);
        }

        return command;
    }

    /**
     * Sends a command and blocks the thread until the command is done - either with an ok or error.
     *
     * @param controller the controller to send the command through
     * @param command    a command
     * @throws Exception if the command could not be sent or a timeout occurred
     */
    public static GcodeCommand sendAndWaitForCompletion(IController controller, GcodeCommand command) throws Exception {
        return sendAndWaitForCompletion(controller, command, MAX_EXECUTION_TIME);
    }
}
