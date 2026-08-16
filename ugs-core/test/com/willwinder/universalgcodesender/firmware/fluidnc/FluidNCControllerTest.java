package com.willwinder.universalgcodesender.firmware.fluidnc;

import com.willwinder.universalgcodesender.GrblUtils;
import com.willwinder.universalgcodesender.communicator.ICommunicator;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.listeners.ControllerListener;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.ControllerStatusBuilder;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.model.Alarm;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.services.MessageService;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;
import com.willwinder.universalgcodesender.utils.SimpleGcodeStreamReader;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.startsWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import org.mockito.MockitoAnnotations;

import java.io.IOException;
import java.util.List;

public class FluidNCControllerTest {

    private FluidNCController target;
    private ICommunicator communicator;

    @Mock
    private MessageService messageService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        communicator = mock(ICommunicator.class);
        target = spy(new FluidNCController(communicator));
        target.setMessageService(messageService);
    }

    @Test
    public void executeReturnToHomeShouldAddSafetyHeightWhenBelow() throws Exception {
        when(target.isIdle()).thenReturn(true);
        mockGcodeState();
        mockControllerStatus(ControllerState.IDLE, new Position(0, 0, 5, UnitUtils.Units.MM));

        ArgumentCaptor<GcodeCommand> commandArgumentCaptor = ArgumentCaptor.forClass(GcodeCommand.class);
        doNothing().when(target).sendCommandImmediately(commandArgumentCaptor.capture());

        target.returnToHome(10);

        verify(target, times(3)).sendCommandImmediately(any());
        List<GcodeCommand> commands = commandArgumentCaptor.getAllValues();
        assertEquals("G21G90 G0Z10", commands.get(0).getCommandString());
        assertEquals("G90 G0 X0 Y0", commands.get(1).getCommandString());
        assertEquals("G90 G0 Z0", commands.get(2).getCommandString());
    }

    @Test
    public void executeReturnToHomeShouldNotAddSafetyHeightWhenOver() throws Exception {
        when(target.isIdle()).thenReturn(true);
        mockGcodeState();
        mockControllerStatus(ControllerState.IDLE, new Position(0, 0, 11, UnitUtils.Units.MM));

        ArgumentCaptor<GcodeCommand> commandArgumentCaptor = ArgumentCaptor.forClass(GcodeCommand.class);
        doNothing().when(target).sendCommandImmediately(commandArgumentCaptor.capture());

        target.returnToHome(10);

        verify(target, times(2)).sendCommandImmediately(any());
        List<GcodeCommand> commands = commandArgumentCaptor.getAllValues();
        assertEquals("G90 G0 X0 Y0", commands.get(0).getCommandString());
        assertEquals("G90 G0 Z0", commands.get(1).getCommandString());
    }

    @Test
    public void restoreParserModalStateShouldRestoreUnitsToMM() {
        target.updateParserModalState(new GcodeCommand("G21"));

        ArgumentCaptor<GcodeCommand> commandArgumentCaptor = ArgumentCaptor.forClass(GcodeCommand.class);
        doNothing().when(communicator).queueCommand(commandArgumentCaptor.capture());
        target.restoreParserModalState();

        assertEquals("G21", commandArgumentCaptor.getValue().getCommandString());
    }

    @Test
    public void restoreParserModalStateShouldRestoreUnitsToInches() {
        target.updateParserModalState(new GcodeCommand("G20"));

        ArgumentCaptor<GcodeCommand> commandArgumentCaptor = ArgumentCaptor.forClass(GcodeCommand.class);
        doNothing().when(communicator).queueCommand(commandArgumentCaptor.capture());
        target.restoreParserModalState();

        assertEquals("G20", commandArgumentCaptor.getValue().getCommandString());
    }

    @Test
    public void restoreParserModalStateShouldRestoreAbsoluteMode() {
        target.updateParserModalState(new GcodeCommand("G90"));

        ArgumentCaptor<GcodeCommand> commandArgumentCaptor = ArgumentCaptor.forClass(GcodeCommand.class);
        doNothing().when(communicator).queueCommand(commandArgumentCaptor.capture());
        target.restoreParserModalState();

        assertEquals("G90", commandArgumentCaptor.getValue().getCommandString());
    }

    @Test
    public void beginStreamingShouldSendEvents() {
        ControllerListener listener = mock(ControllerListener.class);
        target.addListener(listener);

        IGcodeStreamReader gcodeStream = new SimpleGcodeStreamReader("G0 X1", "G0 X0");
        target.queueStream(gcodeStream);
        target.beginStreaming();

        assertTrue(target.isStreaming());
        verify(listener, times(1)).streamStarted();
        verify(listener, times(1)).statusStringListener(any());
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void streamCompleteShouldBeExecutedWhenStreamIsFinished() throws IOException, InterruptedException {
        ControllerListener listener = mock(ControllerListener.class);
        InOrder inOrder = inOrder(listener);
        target.addListener(listener);

        IGcodeStreamReader gcodeStream = new SimpleGcodeStreamReader("G0 X1", "G0 X0");
        target.queueStream(gcodeStream);
        target.beginStreaming();

        GcodeCommand nextCommand = gcodeStream.getNextCommand();
        target.commandSent(nextCommand);
        nextCommand.appendResponse("ok");
        target.rawResponseListener("ok");

        nextCommand = gcodeStream.getNextCommand();
        target.commandSent(nextCommand);
        nextCommand.appendResponse("ok");
        target.rawResponseListener("ok");

        Thread.sleep(100);

        inOrder.verify(listener, times(1)).statusStringListener(any());
        inOrder.verify(listener, times(1)).streamStarted();
        inOrder.verify(listener, times(1)).commandSent(any());
        inOrder.verify(listener, times(1)).commandComplete(any());
        inOrder.verify(listener, times(1)).commandSent(any());
        inOrder.verify(listener, times(1)).commandComplete(any());
        inOrder.verify(listener, times(1)).streamComplete();
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    public void rawResponseListenerShouldReportErrorOnCommandIfThereAreMoreActiveCommands() throws IOException, InterruptedException {
        ControllerListener listener = mock(ControllerListener.class);
        InOrder messagesInOrder = inOrder(messageService);
        InOrder inOrder = inOrder(listener);
        target.addListener(listener);

        IGcodeStreamReader gcodeStream = new SimpleGcodeStreamReader("G0 X1", "G0 X0");
        target.queueStream(gcodeStream);
        target.beginStreaming();

        GcodeCommand nextCommand = gcodeStream.getNextCommand();
        nextCommand.appendResponse("error:20");
        target.commandSent(nextCommand);

        nextCommand = gcodeStream.getNextCommand();
        target.commandSent(nextCommand);

        target.rawResponseListener("error:20");

        Thread.sleep(100);

        inOrder.verify(listener, times(1)).statusStringListener(any());
        inOrder.verify(listener, times(1)).streamStarted();
        inOrder.verify(listener, times(2)).commandSent(any());
        inOrder.verify(listener, times(1)).commandComplete(any());
        inOrder.verify(listener, times(1)).streamComplete();
        inOrder.verifyNoMoreInteractions();

        messagesInOrder.verify(messageService, times(1)).dispatchMessage(MessageType.INFO, "> G0 X1\n");
        messagesInOrder.verify(messageService, times(1)).dispatchMessage(MessageType.ERROR, "An error was detected while sending 'G0 X1': error:20. Streaming has been paused.\n");
        messagesInOrder.verify(messageService, times(1)).dispatchMessage(eq(MessageType.INFO), startsWith("\n**** Finished sending file in"));
        messagesInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void rawResponseListenerShouldNotReportErrorOnCommandIfOnlyOneActiveCommands() throws IOException, InterruptedException {
        ControllerListener listener = mock(ControllerListener.class);
        InOrder messagesInOrder = inOrder(messageService);
        InOrder inOrder = inOrder(listener);
        target.addListener(listener);

        IGcodeStreamReader gcodeStream = new SimpleGcodeStreamReader("G0 X1");
        target.queueStream(gcodeStream);
        target.beginStreaming();

        GcodeCommand nextCommand = gcodeStream.getNextCommand();
        nextCommand.appendResponse("error:20");
        target.commandSent(nextCommand);

        target.rawResponseListener("error:20");

        Thread.sleep(100);

        inOrder.verify(listener, times(1)).statusStringListener(any());
        inOrder.verify(listener, times(1)).streamStarted();
        inOrder.verify(listener, times(1)).commandSent(any());
        inOrder.verify(listener, times(1)).commandComplete(any());
        inOrder.verify(listener, times(1)).streamComplete();
        inOrder.verifyNoMoreInteractions();

        messagesInOrder.verify(messageService, times(1)).dispatchMessage(MessageType.INFO, "> G0 X1\n");
        messagesInOrder.verify(messageService, times(1)).dispatchMessage(MessageType.INFO, "error:20\n");
        messagesInOrder.verify(messageService, times(1)).dispatchMessage(eq(MessageType.INFO), startsWith("\n**** Finished sending file in"));
        messagesInOrder.verifyNoMoreInteractions();
    }

    @Test
    public void restoreParserModalStateShouldRestoreRelativeMode() {
        target.updateParserModalState(new GcodeCommand("G91"));

        ArgumentCaptor<GcodeCommand> commandArgumentCaptor = ArgumentCaptor.forClass(GcodeCommand.class);
        doNothing().when(communicator).queueCommand(commandArgumentCaptor.capture());
        target.restoreParserModalState();

        assertEquals("G91", commandArgumentCaptor.getValue().getCommandString());
    }

    @Test
    public void rawResponseListener_shouldSetAlarmStateAndDispatchAlarm() {
        ControllerListener listener = mock(ControllerListener.class);
        target.addListener(listener);

        target.rawResponseListener("ALARM:1");

        assertEquals(ControllerState.ALARM, target.getControllerStatus().getState());
        verify(listener, times(1)).receivedAlarm(Alarm.HARD_LIMIT);
        verify(messageService, times(1)).dispatchMessage(MessageType.ERROR, "ALARM:1\n");
    }

    @Test
    public void rawResponseListener_shouldStopStreamOnFluidNCAlarmMessage() throws Exception {
        ControllerListener listener = mock(ControllerListener.class);
        target.addListener(listener);
        target.queueStream(new SimpleGcodeStreamReader("G0 X1", "G0 X0"));
        target.beginStreaming();

        target.rawResponseListener("[MSG:INFO: ALARM: Soft Limit]");

        assertFalse(target.isStreaming());
        assertEquals(ControllerState.ALARM, target.getControllerStatus().getState());
        verify(listener, times(1)).receivedAlarm(Alarm.UNKONWN);
        verify(listener, times(1)).streamCanceled();
        verify(communicator, times(1)).cancelSend();
        verify(communicator, never()).sendByteImmediately(GrblUtils.GRBL_PAUSE_COMMAND);
    }

    @Test
    public void rawResponseListener_shouldStopStreamWhenStatusReportsAlarmState() throws Exception {
        ControllerListener listener = mock(ControllerListener.class);
        target.addListener(listener);
        target.queueStream(new SimpleGcodeStreamReader("G0 X1", "G0 X0"));
        target.beginStreaming();

        target.rawResponseListener("<Alarm|MPos:0.000,0.000,-9.171|FS:0,12000>");

        assertFalse(target.isStreaming());
        verify(listener, times(1)).streamCanceled();
        verify(communicator, times(1)).cancelSend();
        verify(communicator, never()).sendByteImmediately(GrblUtils.GRBL_PAUSE_COMMAND);
    }

    @Test
    public void communicatorPausedOnError_shouldStopStreamWithoutPausingWhenInAlarmState() throws Exception {
        target.queueStream(new SimpleGcodeStreamReader("G0 X1", "G0 X0"));
        target.beginStreaming();
        target.rawResponseListener("[MSG:INFO: ALARM: Soft Limit]");

        target.communicatorPausedOnError();

        verify(communicator, never()).pauseSend();
        verify(communicator, never()).resumeSend();
        verify(communicator, never()).sendByteImmediately(GrblUtils.GRBL_PAUSE_COMMAND);
    }

    @Test
    public void communicatorPausedOnError_shouldPauseStreamWhenControllerIsRunning() throws Exception {
        target.queueStream(new SimpleGcodeStreamReader("G0 X1", "G0 X0"));
        target.beginStreaming();

        target.communicatorPausedOnError();

        assertTrue(target.isStreaming());
        verify(communicator, times(1)).pauseSend();
        verify(communicator, never()).resumeSend();
    }

    @Test
    public void onConnectionClosedShouldDisconnectController() throws Exception {
        when(target.isIdle()).thenReturn(true);
        target.rawResponseListener("<Idle>");

        assertEquals(ControllerState.IDLE, target.getControllerStatus().getState());

        target.onConnectionClosed();

        verify(communicator, times(1)).disconnect();
        assertEquals(ControllerState.DISCONNECTED, target.getControllerStatus().getState());
    }

    private void mockControllerStatus(ControllerState state, Position workPosition) {
        ControllerStatus controllerStatus = ControllerStatusBuilder.newInstance()
                .setState(state)
                .setWorkCoord(workPosition)
                .build();
        when(target.getControllerStatus()).thenReturn(controllerStatus);
    }

    private void mockGcodeState() {
        GcodeState state = new GcodeState();
        state.units = Code.G21;
        when(target.getCurrentGcodeState()).thenReturn(state);
    }
}
