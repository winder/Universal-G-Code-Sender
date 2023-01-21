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
import com.willwinder.universalgcodesender.communicator.ICommunicator;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.CommunicatorState;
import com.willwinder.universalgcodesender.types.CommandListener;
import com.willwinder.universalgcodesender.types.GcodeCommand;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * @author Joacim Breiler
 */
public class ControllerUtils {

    private ControllerUtils() {
        // Can not be instanced
    }

    private static final int MAX_EXECUTION_TIME = 2000;

    /**
     * Sends a command and blocks the thread until the command is done - either with an ok or error.
     *
     * @param controller       the controller to send the command through
     * @param command          a command to send
     * @param maxExecutionTime the max number of milliseconds to wait before throwing a timeout error
     * @throws Exception if the command could not be sent or a timeout occurred
     */
    public static <T extends GcodeCommand> T sendAndWaitForCompletion(IController controller, T command, long maxExecutionTime) throws Exception {
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
     * @param <T>        a class extending from {@link GcodeCommand}
     * @return the executed command with the response
     * @throws Exception if the command could not be sent or a timeout occurred
     */

    public static <T extends GcodeCommand> T sendAndWaitForCompletion(IController controller, T command) throws Exception {
        return sendAndWaitForCompletion(controller, command, MAX_EXECUTION_TIME);
    }

    /**
     * Waits for all commands to complete before continuing
     *
     * @param controller the controller to check for active commands
     */
    public static void waitOnActiveCommands(IController controller) throws InterruptedException {
        long maxExecutionTime = MAX_EXECUTION_TIME;
        long startTime = System.currentTimeMillis();
        while (controller.getActiveCommand().isPresent()) {
            if (startTime + maxExecutionTime < System.currentTimeMillis()) {
                throw new RuntimeException("The command \"" + controller.getActiveCommand().get().getCommandString() + "\" has timed out as it wasn't finished within " + maxExecutionTime + "ms");
            }

            Thread.sleep(10);
        }
    }

    /**
     * Sends a command and blocks the thread until the command is done and was ok. It will retry to send the command in case of a timeout or an error.
     *
     * @param commandSupplier  a supplier for creating gcode commands to send
     * @param controller       the controller to send through
     * @param maxExecutionTime the maximum execution time in milliseconds
     * @param retryCount       the number of times to retry and send
     * @param onExecute        an action that should be executed before sending
     * @param <T>              a class extending from {@link GcodeCommand}
     * @return the last executed command with the response
     * @throws InterruptedException
     */
    public static <T extends GcodeCommand> T sendAndWaitForCompletionWithRetry(Supplier<T> commandSupplier, IController controller, long maxExecutionTime, int retryCount, Consumer<Integer> onExecute) throws InterruptedException {
        int times = 0;

        T command = null;
        while (times < retryCount && controller.isCommOpen()) {
            long startTime = System.currentTimeMillis();
            try {
                onExecute.accept(times + 1);
                command = commandSupplier.get();
                if (command == null) {
                    throw new IllegalArgumentException("The command generated was null");
                }

                command = sendAndWaitForCompletion(controller, command, maxExecutionTime);
            } catch (Exception e) {
                // Never mind
            }

            if (!command.isDone() || command.isError()) {
                // Make sure that we sleep at least the max execution time in case the command got a wierd response.
                if (startTime + maxExecutionTime > System.currentTimeMillis()) {
                    long sleepTime = startTime + maxExecutionTime - System.currentTimeMillis();
                    Thread.sleep(sleepTime);
                }
            } else if (command.isDone() && !command.isError()) {
                break;
            }
            times++;
        }

        return command;
    }

    /**
     * Converts the controller state to a communicator state.
     *
     * @param controllerState the current controller state
     * @param controller      the contrller
     * @param communicator    the communicator
     * @return the state of the communicator
     */
    public static CommunicatorState getCommunicatorState(ControllerState controllerState, IController controller, ICommunicator communicator) {
        switch (controllerState) {
            case JOG:
            case RUN:
                return CommunicatorState.COMM_SENDING;
            case HOLD:
            case DOOR:
                return CommunicatorState.COMM_SENDING_PAUSED;
            case IDLE:
                if (controller.isStreaming()) {
                    return CommunicatorState.COMM_SENDING_PAUSED;
                } else {
                    return CommunicatorState.COMM_IDLE;
                }
            case ALARM:
                return CommunicatorState.COMM_IDLE;
            case CHECK:
                if (controller.isStreaming() && communicator.isPaused()) {
                    return CommunicatorState.COMM_SENDING_PAUSED;
                } else if (controller.isStreaming() && !communicator.isPaused()) {
                    return CommunicatorState.COMM_SENDING;
                } else {
                    return CommunicatorState.COMM_CHECK;
                }
            default:
                return CommunicatorState.COMM_IDLE;
        }
    }
}
