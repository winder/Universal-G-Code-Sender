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
import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

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
}