package com.willwinder.ugs.nbp.joystick.action;


import com.willwinder.ugs.nbp.joystick.model.JoystickControl;
import com.willwinder.ugs.nbp.joystick.model.JoystickState;
import com.willwinder.ugs.nbp.lib.services.ActionReference;
import com.willwinder.universalgcodesender.utils.ContinuousJogWorker;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

import javax.swing.*;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyFloat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ActionDispatcherTest {
    private ActionDispatcher actionDispatcher;
    @Mock
    private ContinuousJogWorker continuousJogWorker;
    @Mock
    private ActionManager actionManager;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        actionDispatcher = new ActionDispatcher(actionManager, continuousJogWorker);
    }

    @Test
    public void onUpdateShouldPerformActionIfButtonWasPressed() {
        // Given
        Action action = mock(Action.class);
        when(action.isEnabled()).thenReturn(true);

        ActionReference actionReference = new ActionReference();
        actionReference.setAction(action);
        when(actionManager.getMappedAction(JoystickControl.Y)).thenReturn(Optional.of(actionReference));

        // When
        JoystickState state = new JoystickState();
        state.setButton(JoystickControl.Y, true);
        actionDispatcher.onUpdate(state);

        // Then
        verify(action, times(1)).actionPerformed(any());
    }

    @Test
    public void onUpdateShouldNotPerformActionIfActionIsNotEnabled() {
        // Given
        Action action = mock(Action.class);
        when(action.isEnabled()).thenReturn(false);

        ActionReference actionReference = new ActionReference();
        actionReference.setAction(action);
        when(actionManager.getMappedAction(JoystickControl.Y)).thenReturn(Optional.of(actionReference));

        // When
        JoystickState state = new JoystickState();
        state.setButton(JoystickControl.Y, true);
        actionDispatcher.onUpdate(state);

        // Then
        verify(action, times(0)).actionPerformed(any());
    }

    @Test
    public void onUpdateShouldSetValueOnAnalogAction() {
        // Given
        AnalogAction action = mock(AnalogAction.class);
        when(action.isEnabled()).thenReturn(true);

        ActionReference actionReference = new ActionReference();
        actionReference.setAction(action);
        when(actionManager.getMappedAction(JoystickControl.LEFT_X)).thenReturn(Optional.of(actionReference));

        // When
        JoystickState state = new JoystickState();
        state.setAxis(JoystickControl.LEFT_X, 1.0f);
        actionDispatcher.onUpdate(state);

        // Then
        verify(action, times(1)).setValue(eq(1.0f));
        verify(action, times(1)).actionPerformed(any());
    }
}
