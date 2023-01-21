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
import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class ControllerUtilsTest {

    @Test
    public void sendAndWaitForCompletionShouldBlockAndReturnWhenCommandIsDone() throws Exception {
        IController controller = mock(IController.class);
        GcodeCommand command = new GcodeCommand("blah");

        AtomicBoolean isDoneBlocking = new AtomicBoolean(false);
        Thread thread = new Thread(() -> {
            try {
                ControllerUtils.sendAndWaitForCompletion(controller, command, 1000);
                isDoneBlocking.set(true);
            } catch (Exception e) {
                fail("Got unwanted exception" + e.getMessage());
            }
        });
        thread.start();

        // Wait for the command to be queued and sent
        Thread.sleep(100);

        // Simulate that the command has been completed
        command.setDone(true);

        // Wait for the command event has been processed
        Thread.sleep(100);

        assertTrue(isDoneBlocking.get());
    }

    @Test
    public void sendAndWaitForCompletionShouldTimeOut() throws Exception {
        IController controller = mock(IController.class);
        GcodeCommand command = new GcodeCommand("blah");
        assertThrows("The command \"blah\" has timed out as it wasn't finished within 100ms", RuntimeException.class, () -> ControllerUtils.sendAndWaitForCompletion(controller, command, 100));
    }

    @Test
    public void sendAndWaitForCompletionWithRetryShouldRetryOnErrors() throws Exception {
        IController controller = mock(IController.class);
        when(controller.isCommOpen()).thenReturn(true);

        // First command will generate an error, the second will succeed
        doAnswer(answer -> {
            GcodeCommand gcodeCommand = answer.getArgument(0);
            if (gcodeCommand.getCommandString().equals("0")) {
                gcodeCommand.setError(true);
                gcodeCommand.setDone(true);
            } else {
                gcodeCommand.setOk(true);
                gcodeCommand.setDone(true);
            }
            return answer;
        }).when(controller).sendCommandImmediately(any(GcodeCommand.class));

        // Function that will create new commands and count up
        AtomicInteger commandNumber = new AtomicInteger();
        Supplier<? extends GcodeCommand> supplier = (Supplier<GcodeCommand>) () -> {
            int number = commandNumber.getAndAdd(1);
            return new GcodeCommand("" + number);
        };

        GcodeCommand gcodeCommand = ControllerUtils.sendAndWaitForCompletionWithRetry(supplier, controller, 1000, 10, integer -> {});

        verify(controller, times(2)).sendCommandImmediately(any());
        assertEquals("1", gcodeCommand.getCommandString());
    }

    @Test
    public void sendAndWaitForCompletionWithRetryShouldRetryOnTimeout() throws Exception {
        IController controller = mock(IController.class);
        when(controller.isCommOpen()).thenReturn(true);

        // First command will time out, the second will succeed
        doAnswer(answer -> {
            GcodeCommand gcodeCommand = answer.getArgument(0);
            if (gcodeCommand.getCommandString().equals("0")) {
                // Make the first command time out
                Thread.sleep(300);
            } else {
                gcodeCommand.setOk(true);
                gcodeCommand.setDone(true);
            }
            return answer;
        }).when(controller).sendCommandImmediately(any(GcodeCommand.class));

        // Function that will create new commands and count up
        AtomicInteger commandNumber = new AtomicInteger();
        Supplier<? extends GcodeCommand> supplier = (Supplier<GcodeCommand>) () -> {
            int number = commandNumber.getAndAdd(1);
            return new GcodeCommand("" + number);
        };

        GcodeCommand gcodeCommand = ControllerUtils.sendAndWaitForCompletionWithRetry(supplier, controller, 200, 10, integer -> {});

        verify(controller, times(2)).sendCommandImmediately(any());
        assertEquals("1", gcodeCommand.getCommandString());
    }

    @Test
    public void sendAndWaitForCompletionWithWhenDisconnectedShouldReturn() throws Exception {
        IController controller = mock(IController.class);
        when(controller.isCommOpen()).thenReturn(false);

        // Function that will create new commands and count up
        AtomicInteger commandNumber = new AtomicInteger();
        Supplier<? extends GcodeCommand> supplier = (Supplier<GcodeCommand>) () -> {
            int number = commandNumber.getAndAdd(1);
            return new GcodeCommand("" + number);
        };

        ControllerUtils.sendAndWaitForCompletionWithRetry(supplier, controller, 200, 10, integer -> {});
        verify(controller, times(0)).sendCommandImmediately(any());
    }

    @Test
    public void getCommunicatorStateShouldReturnPausedWhenControllerIdleAndStreaming() {
        IController controller = mock(IController.class);
        when(controller.isStreaming()).thenReturn(true);
        ICommunicator communicator = mock(ICommunicator.class);

        CommunicatorState communicatorState = ControllerUtils.getCommunicatorState(ControllerState.IDLE, controller, communicator);

        assertEquals(CommunicatorState.COMM_SENDING_PAUSED, communicatorState);
    }

    @Test
    public void getCommunicatorStateShouldReturnPausedWhenControllerCheckAndStreaming() {
        IController controller = mock(IController.class);
        when(controller.isStreaming()).thenReturn(true);
        ICommunicator communicator = mock(ICommunicator.class);
        when(communicator.isPaused()).thenReturn(true);

        CommunicatorState communicatorState = ControllerUtils.getCommunicatorState(ControllerState.CHECK, controller, communicator);

        assertEquals(CommunicatorState.COMM_SENDING_PAUSED, communicatorState);
    }
}
