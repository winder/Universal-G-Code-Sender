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
package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.communicator.ICommunicator;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class SmoothieControllerTest {

    @Test
    public void commandCompleteShouldDispatchCommandEvent() throws Exception {
        ICommunicator communicator = mock(ICommunicator.class);
        when(communicator.isConnected()).thenReturn(true);
        SmoothieController instance = new SmoothieController(communicator);
        instance.rawResponseHandler("Smoothie");

        AtomicBoolean eventDispatched = new AtomicBoolean(false);
        GcodeCommand command = new GcodeCommand("blah");
        command.addListener(c -> eventDispatched.set(true));

        // Simulate sending and completing the command
        instance.commandSent(command);
        instance.commandComplete("ok");

        assertTrue("Should have sent an event notifying that the command has completed", eventDispatched.get());
    }
}
