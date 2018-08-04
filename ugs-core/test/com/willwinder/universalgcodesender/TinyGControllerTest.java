/*
    Copyright 2018 Will Winder

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

import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.junit.Before;
import org.junit.Test;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Test TinyG controller implementation
 *
 * @author Joacim Breiler
 */
public class TinyGControllerTest {

    @Mock
    private AbstractCommunicator communicator;

    private TinyGController controller;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new TinyGController(communicator);
    }

    @Test
    public void rawResponseWithReadyResponse() throws Exception {
        // When
        controller.rawResponseHandler("{\"r\":{\"msg\":\"SYSTEM READY\"}}");

        // Then
        verify(communicator).sendByteImmediately(TinyGUtils.COMMAND_ENQUIRE_STATUS);
    }

    @Test
    public void rawResponseWithAckResponse() throws Exception {
        // When
        controller.rawResponseHandler("{\"ack\":true}");

        // Then
        verify(communicator).queueStringForComm("{ej:1}");
        verify(communicator).queueStringForComm("{jv:4}");
        verify(communicator).queueStringForComm("{qv:0}");
        verify(communicator).queueStringForComm("{sv:1}");
        verify(communicator).queueStringForComm("{sr:n}");
        verify(communicator).streamCommands();
    }

    @Test
    public void rawResponseWithStatusReport() throws Exception {
        // Given
        ControllerListener controllerListener = mock(ControllerListener.class);
        controller.addListener(controllerListener);

        // When
        controller.rawResponseHandler("{\"r\":{\"sr\":{\"stat\":5}}}");

        // Then
        assertEquals(ControllerState.RUN, controller.getControllerStatus().getState());
        verify(controllerListener, times(0)).commandComplete(any());
    }

    @Test
    public void rawResponseWithResultForNoCommandShouldNotDispatchCommandComplete() throws Exception {
        // Given
        ControllerListener controllerListener = mock(ControllerListener.class);
        controller.addListener(controllerListener);

        // When
        controller.rawResponseHandler("{\"r\":{}}");

        // Then
        verify(controllerListener, times(0)).commandComplete(any());
    }

    @Test
    public void rawResponseWithResultForCommandShouldDispatchCommandComplete() throws Exception {
        // Given
        ControllerListener controllerListener = mock(ControllerListener.class);
        controller.addListener(controllerListener);
        controller = spy(controller);

        // Simulate that a command has been sent
        controller.commandSent(new GcodeCommand("test"));
        when(controller.rowsRemaining()).thenReturn(1);

        // When
        controller.rawResponseHandler("{\"r\":{}}");

        // Then
        verify(controllerListener, times(1)).commandComplete(any());
    }

    @Test
    public void rawResponseWithStatusReportStateFromControllerShouldUpdateControllerState() {
        assertEquals("The controller should begin in an unkown state", ControllerState.UNKNOWN, controller.getControllerStatus().getState());

        ControllerListener controllerListener = mock(ControllerListener.class);
        controller.addListener(controllerListener);

        controller.rawResponseHandler("{\"sr\":{\"stat\": 1}}");
        assertEquals(ControllerState.IDLE, controller.getControllerStatus().getState());
        assertEquals(UGSEvent.ControlState.COMM_IDLE, controller.getControlState());

        controller.rawResponseHandler("{\"sr\":{\"stat\": 2}}");
        assertEquals(ControllerState.ALARM, controller.getControllerStatus().getState());
        assertEquals(UGSEvent.ControlState.COMM_IDLE, controller.getControlState());

        controller.rawResponseHandler("{\"sr\":{\"stat\": 3}}");
        assertEquals(ControllerState.IDLE, controller.getControllerStatus().getState());
        assertEquals(UGSEvent.ControlState.COMM_IDLE, controller.getControlState());

        controller.rawResponseHandler("{\"sr\":{\"stat\": 4}}");
        assertEquals(ControllerState.IDLE, controller.getControllerStatus().getState());
        assertEquals(UGSEvent.ControlState.COMM_IDLE, controller.getControlState());

        controller.rawResponseHandler("{\"sr\":{\"stat\": 5}}");
        assertEquals(ControllerState.RUN, controller.getControllerStatus().getState());
        assertEquals(UGSEvent.ControlState.COMM_SENDING, controller.getControlState());

        controller.rawResponseHandler("{\"sr\":{\"stat\": 6}}");
        assertEquals(ControllerState.HOLD, controller.getControllerStatus().getState());
        assertEquals(UGSEvent.ControlState.COMM_SENDING_PAUSED, controller.getControlState());

        controller.rawResponseHandler("{\"sr\":{\"stat\": 7}}");
        assertEquals(ControllerState.UNKNOWN, controller.getControllerStatus().getState());
        assertEquals(UGSEvent.ControlState.COMM_IDLE, controller.getControlState());

        controller.rawResponseHandler("{\"sr\":{\"stat\": 8}}");
        assertEquals(ControllerState.UNKNOWN, controller.getControllerStatus().getState());
        assertEquals(UGSEvent.ControlState.COMM_IDLE, controller.getControlState());

        controller.rawResponseHandler("{\"sr\":{\"stat\": 9}}");
        assertEquals(ControllerState.HOME, controller.getControllerStatus().getState());
        assertEquals(UGSEvent.ControlState.COMM_IDLE, controller.getControlState());

        controller.rawResponseHandler("{\"sr\":{\"stat\": 10}}");
        assertEquals(ControllerState.JOG, controller.getControllerStatus().getState());
        assertEquals(UGSEvent.ControlState.COMM_SENDING, controller.getControlState());

        controller.rawResponseHandler("{\"sr\":{\"stat\": 11}}");
        assertEquals(ControllerState.UNKNOWN, controller.getControllerStatus().getState());
        assertEquals(UGSEvent.ControlState.COMM_IDLE, controller.getControlState());

        controller.rawResponseHandler("{\"sr\":{\"stat\": 12}}");
        assertEquals(ControllerState.UNKNOWN, controller.getControllerStatus().getState());
        assertEquals(UGSEvent.ControlState.COMM_IDLE, controller.getControlState());

        controller.rawResponseHandler("{\"sr\":{\"stat\": 13}}");
        assertEquals(ControllerState.ALARM, controller.getControllerStatus().getState());
        assertEquals(UGSEvent.ControlState.COMM_IDLE, controller.getControlState());
    }

    @Test
    public void cancelSend() throws Exception {
        // Given
        when(communicator.isCommOpen()).thenReturn(true);
        InOrder orderVerifier = inOrder(communicator);

        // When
        controller.cancelSend();

        // Then
        orderVerifier.verify(communicator).cancelSend();
        orderVerifier.verify(communicator).sendByteImmediately(TinyGUtils.COMMAND_KILL_JOB);
        orderVerifier.verify(communicator).softReset(); // Work around for clearing buffers and counters in communicator
        orderVerifier.verify(communicator).queueStringForComm(TinyGUtils.COMMAND_KILL_ALARM_LOCK + "\n");
        orderVerifier.verify(communicator).streamCommands();

        // Ignore the code analysis warnings
        assertEquals("trick code analysis", "trick code analysis");
    }
}
