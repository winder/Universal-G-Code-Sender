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

import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.CommunicatorState;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Test TinyG controller implementation
 *
 * @author Joacim Breiler
 */
public class TinyGControllerTest {

    @Mock
    private AbstractCommunicator communicator;

    private TinyGController controller;
    private ArgumentCaptor<GcodeCommand> queueCommandArgumentCaptor;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        controller = new TinyGController(communicator);

        queueCommandArgumentCaptor = ArgumentCaptor.forClass(GcodeCommand.class);
        doNothing().when(communicator).queueCommand(queueCommandArgumentCaptor.capture());
    }

    @Test
    public void rawResponseWithStatusReport() {
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
    public void rawResponseWithResultForNoCommandShouldNotDispatchCommandComplete() {
        // Given
        ControllerListener controllerListener = mock(ControllerListener.class);
        controller.addListener(controllerListener);

        // When
        controller.rawResponseHandler("{\"r\":{}}");

        // Then
        verify(controllerListener, times(0)).commandComplete(any());
    }

    @Test
    public void rawResponseWithResultForCommandShouldDispatchCommandComplete() {
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
        assertEquals(CommunicatorState.COMM_IDLE, controller.getControlState());

        controller.rawResponseHandler("{\"sr\":{\"stat\": 2}}");
        assertEquals(ControllerState.ALARM, controller.getControllerStatus().getState());
        assertEquals(CommunicatorState.COMM_IDLE, controller.getControlState());

        controller.rawResponseHandler("{\"sr\":{\"stat\": 3}}");
        assertEquals(ControllerState.IDLE, controller.getControllerStatus().getState());
        assertEquals(CommunicatorState.COMM_IDLE, controller.getControlState());

        controller.rawResponseHandler("{\"sr\":{\"stat\": 4}}");
        assertEquals(ControllerState.IDLE, controller.getControllerStatus().getState());
        assertEquals(CommunicatorState.COMM_IDLE, controller.getControlState());

        controller.rawResponseHandler("{\"sr\":{\"stat\": 5}}");
        assertEquals(ControllerState.RUN, controller.getControllerStatus().getState());
        assertEquals(CommunicatorState.COMM_SENDING, controller.getControlState());

        controller.rawResponseHandler("{\"sr\":{\"stat\": 6}}");
        assertEquals(ControllerState.HOLD, controller.getControllerStatus().getState());
        assertEquals(CommunicatorState.COMM_SENDING_PAUSED, controller.getControlState());

        controller.rawResponseHandler("{\"sr\":{\"stat\": 7}}");
        assertEquals(ControllerState.UNKNOWN, controller.getControllerStatus().getState());
        assertEquals(CommunicatorState.COMM_IDLE, controller.getControlState());

        controller.rawResponseHandler("{\"sr\":{\"stat\": 8}}");
        assertEquals(ControllerState.UNKNOWN, controller.getControllerStatus().getState());
        assertEquals(CommunicatorState.COMM_IDLE, controller.getControlState());

        controller.rawResponseHandler("{\"sr\":{\"stat\": 9}}");
        assertEquals(ControllerState.HOME, controller.getControllerStatus().getState());
        assertEquals(CommunicatorState.COMM_IDLE, controller.getControlState());

        controller.rawResponseHandler("{\"sr\":{\"stat\": 10}}");
        assertEquals(ControllerState.JOG, controller.getControllerStatus().getState());
        assertEquals(CommunicatorState.COMM_SENDING, controller.getControlState());

        controller.rawResponseHandler("{\"sr\":{\"stat\": 11}}");
        assertEquals(ControllerState.UNKNOWN, controller.getControllerStatus().getState());
        assertEquals(CommunicatorState.COMM_IDLE, controller.getControlState());

        controller.rawResponseHandler("{\"sr\":{\"stat\": 12}}");
        assertEquals(ControllerState.UNKNOWN, controller.getControllerStatus().getState());
        assertEquals(CommunicatorState.COMM_IDLE, controller.getControlState());

        controller.rawResponseHandler("{\"sr\":{\"stat\": 13}}");
        assertEquals(ControllerState.ALARM, controller.getControllerStatus().getState());
        assertEquals(CommunicatorState.COMM_IDLE, controller.getControlState());
    }

    @Test
    public void cancelSend() throws Exception {
        // Given
        when(communicator.isConnected()).thenReturn(true);
        InOrder orderVerifier = inOrder(communicator);

        // When
        controller.cancelSend();

        // Then
        orderVerifier.verify(communicator).cancelSend();
        orderVerifier.verify(communicator).sendByteImmediately(TinyGUtils.COMMAND_PAUSE);
        orderVerifier.verify(communicator).sendByteImmediately(TinyGUtils.COMMAND_QUEUE_FLUSH);
        orderVerifier.verify(communicator).cancelSend(); // Work around for clearing buffers and counters in communicator
    }

    @Test
    public void jogMachine() throws Exception {
        // Given
        when(communicator.isConnected()).thenReturn(true);

        // Simulate that the machine is running in inches
        controller.getCurrentGcodeState().units = Code.G21;

        // When
        InOrder orderVerifier = inOrder(communicator);
        controller.jogMachine(new PartialPosition(100., 100., 100., UnitUtils.Units.MM), 1000);

        // Then
        orderVerifier.verify(communicator, times(1)).queueCommand(any(GcodeCommand.class));
        orderVerifier.verify(communicator).streamCommands();

        GcodeCommand command = queueCommandArgumentCaptor.getAllValues().get(0);
        assertEquals("G21G91G1X100Y100Z100F1000", command.getCommandString());
        assertTrue(command.isGenerated());
        assertTrue(command.isTemporaryParserModalChange());
    }

    @Test
    public void jogMachineWhenUsingInchesShouldConvertCoordinates() throws Exception {
        // Given
        when(communicator.isConnected()).thenReturn(true);

        // Simulate that the machine is running in inches
        controller.getCurrentGcodeState().units = Code.G20;

        // When
        InOrder orderVerifier = inOrder(communicator);
        controller.jogMachine(new PartialPosition(100., 100., 100., UnitUtils.Units.MM), 1000);

        // Then
        orderVerifier.verify(communicator, times(1)).queueCommand(any(GcodeCommand.class));
        orderVerifier.verify(communicator).streamCommands();

        GcodeCommand command = queueCommandArgumentCaptor.getAllValues().get(0);
        assertEquals("G20G91G1X3.937Y3.937Z3.937F39.37", command.getCommandString());
        assertTrue(command.isGenerated());
        assertTrue(command.isTemporaryParserModalChange());
    }

    @Test
    public void jogMachineTo() throws Exception {
        // Given
        when(communicator.isConnected()).thenReturn(true);

        // Simulate that the machine is running in mm
        controller.getCurrentGcodeState().units = Code.G21;

        // When
        InOrder orderVerifier = inOrder(communicator);
        controller.jogMachineTo(new PartialPosition(1.0, 2.0, 3.0, UnitUtils.Units.MM), 1000);

        // Then
        orderVerifier.verify(communicator, times(1)).queueCommand(any(GcodeCommand.class));
        orderVerifier.verify(communicator).streamCommands();

        GcodeCommand command = queueCommandArgumentCaptor.getAllValues().get(0);
        assertEquals("G21G90G1X1Y2Z3F1000", command.getCommandString());
        assertTrue(command.isGenerated());
        assertTrue(command.isTemporaryParserModalChange());
    }

    @Test
    public void jogMachineToWhenUsingInchesShouldConvertCoordinates() throws Exception {
        // Given
        when(communicator.isConnected()).thenReturn(true);

        // Simulate that the machine is running in inches
        controller.getCurrentGcodeState().units = Code.G20;

        // When
        InOrder orderVerifier = inOrder(communicator);
        controller.jogMachineTo(new PartialPosition(1.0, 2.0, 3.0, UnitUtils.Units.MM), 1000);

        // Then
        orderVerifier.verify(communicator, times(1)).queueCommand(any(GcodeCommand.class));
        orderVerifier.verify(communicator).streamCommands();

        GcodeCommand command = queueCommandArgumentCaptor.getAllValues().get(0);
        assertEquals("G20G90G1X0.039Y0.079Z0.118F39.37", command.getCommandString());
        assertTrue(command.isGenerated());
        assertTrue(command.isTemporaryParserModalChange());
    }
}
