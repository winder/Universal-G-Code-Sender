package com.willwinder.ugs.nbp.core.services;

import com.willwinder.ugs.nbp.core.actions.ContinuousAction;
import com.willwinder.ugs.nbp.lib.services.ActionReference;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import javax.swing.JLabel;
import java.awt.event.KeyEvent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

public class ContinuousActionExecutorTest {

    private ContinuousActionExecutor target;

    @Mock
    private KeyEvent keyEvent;

    @Mock
    private ContinuousAction action;

    @Mock
    private ActionReference actionReference;

    @Before
    public void setup() {
        target = new ContinuousActionExecutor(100);

        MockitoAnnotations.initMocks(this);
        when(keyEvent.getSource()).thenReturn(new JLabel());
        when(keyEvent.getID()).thenReturn(1);
        when(actionReference.getAction()).thenReturn(action);
    }

    @Test
    public void keyPressAndKeyReleaseShouldTriggerNormalAction() {
        target.setCurrentAction(actionReference);
        target.keyPressed(keyEvent);
        target.keyReleased(keyEvent);

        verify(action, times(1)).actionPerformed(any());
        verifyNoMoreInteractions(action);
    }

    @Test
    public void longKeyPressAndKeyReleaseShouldTriggerContinuousAction() throws InterruptedException {
        target.setCurrentAction(actionReference);
        target.keyPressed(keyEvent);
        Thread.sleep(120);
        target.keyReleased(keyEvent);

        verify(action, times(1)).actionActivate();
        verify(action, times(1)).actionDeactivated();
        verifyNoMoreInteractions(action);
    }

    @Test
    public void longKeyPressAndChangeActionShouldAbortFirstAction() throws InterruptedException {
        target.setCurrentAction(actionReference);
        target.keyPressed(keyEvent);
        Thread.sleep(120);
        target.setCurrentAction(new ActionReference());

        verify(action, times(1)).actionActivate();
        verify(action, times(1)).actionDeactivated();
        verifyNoMoreInteractions(action);
    }

    @Test
    public void keyPressAndChangeActionShouldAbortFirstAction() {
        target.setCurrentAction(actionReference);
        target.keyPressed(keyEvent);
        target.setCurrentAction(new ActionReference());
        verifyNoMoreInteractions(action);
    }
}
