package com.willwinder.ugs.nbp.joystick.action;

import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.utils.ContinuousJogWorker;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

public class AnalogJogActionTest {

    @Test
    public void actionPerformedShouldNotGenerateJogCommandsWhenStateIsRunning() {
        ContinuousJogWorker worker = mock(ContinuousJogWorker.class);
        AnalogJogAction action = new AnalogJogAction(worker, Axis.Y);

        // Simulate the controller in running state
        action.UGSEvent(new ControllerStateEvent(ControllerState.RUN, ControllerState.IDLE));
        action.actionPerformed(null);

        assertFalse(action.isEnabled());
        verifyNoInteractions(worker);
    }

    @Test
    public void actionPerformedShouldNotGenerateJogCommandsWhenStateIsDoor() {
        ContinuousJogWorker worker = mock(ContinuousJogWorker.class);
        AnalogJogAction action = new AnalogJogAction(worker, Axis.Y);

        // Simulate the controller in running state
        action.UGSEvent(new ControllerStateEvent(ControllerState.DOOR, ControllerState.IDLE));
        action.actionPerformed(null);

        assertFalse(action.isEnabled());
        verifyNoInteractions(worker);
    }

    @Test
    public void actionPerformedShouldGenerateJogCommandsWhenStateIsIdle() {
        ContinuousJogWorker worker = mock(ContinuousJogWorker.class);
        AnalogJogAction action = new AnalogJogAction(worker, Axis.Y);

        // Simulate the controller in running state
        action.UGSEvent(new ControllerStateEvent(ControllerState.IDLE, ControllerState.IDLE));
        action.actionPerformed(null);

        assertTrue(action.isEnabled());
        verify(worker, times(1)).setDirection(eq(Axis.Y), anyFloat());
    }

    @Test
    public void actionPerformedShouldGenerateJogCommandsWhenStateIsJog() {
        ContinuousJogWorker worker = mock(ContinuousJogWorker.class);
        AnalogJogAction action = new AnalogJogAction(worker, Axis.Y);

        // Simulate the controller in running state
        action.UGSEvent(new ControllerStateEvent(ControllerState.JOG, ControllerState.IDLE));
        action.actionPerformed(null);

        assertTrue(action.isEnabled());
        verify(worker, times(1)).setDirection(eq(Axis.Y), anyFloat());
    }

    @Test
    public void setValueShouldSetThePercentageToUse() {
        ContinuousJogWorker worker = mock(ContinuousJogWorker.class);
        AnalogJogAction action = new AnalogJogAction(worker, Axis.Y);

        // Simulate the controller in running state
        action.setValue(0.6f);
        action.UGSEvent(new ControllerStateEvent(ControllerState.JOG, ControllerState.IDLE));
        action.actionPerformed(null);

        assertTrue(action.isEnabled());
        verify(worker, times(1)).setDirection(eq(Axis.Y), eq(0.6f));
    }
}
