package com.willwinder.universalgcodesender.firmware.fluidnc;

import com.willwinder.universalgcodesender.communicator.ICommunicator;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.ControllerStatusBuilder;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class FluidNCControllerTest {

    private IController target;
    private ICommunicator communicator;

    @Before
    public void setUp() {
        communicator = mock(ICommunicator.class);
        target = spy(new FluidNCController(communicator));
    }

    @Test
    public void executeReturnToHomeShouldAddSafetyHeightWhenBelow() throws Exception {
        when(target.isIdle()).thenReturn(true);
        mockGcodeState();
        mockControllerStatus(new Position(0, 0, 5, UnitUtils.Units.MM));

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
        mockControllerStatus(new Position(0, 0, 11, UnitUtils.Units.MM));

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
    public void restoreParserModalStateShouldRestoreRelativeMode() {
        target.updateParserModalState(new GcodeCommand("G91"));

        ArgumentCaptor<GcodeCommand> commandArgumentCaptor = ArgumentCaptor.forClass(GcodeCommand.class);
        doNothing().when(communicator).queueCommand(commandArgumentCaptor.capture());
        target.restoreParserModalState();

        assertEquals("G91", commandArgumentCaptor.getValue().getCommandString());
    }

    private void mockControllerStatus(Position workPosition) {
        ControllerStatus controllerStatus = ControllerStatusBuilder.newInstance()
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
