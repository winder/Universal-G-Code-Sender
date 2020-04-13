package com.willwinder.ugs.nbp.joystick.action;


import org.junit.Test;
import org.mockito.Mockito;

import javax.swing.*;

import java.awt.event.ActionEvent;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class DigitalToAnalogActionAdapterTest {

    @Test
    public void actionShouldNotTriggerIfValueIsZero() {
        Action action = mock(Action.class);
        DigitalToAnalogActionAdapter adapter = new DigitalToAnalogActionAdapter(action);

        adapter.setValue(0);
        adapter.actionPerformed(new ActionEvent(this, 0, ""));

        verify(action, Mockito.times(0)).actionPerformed(any());
    }

    @Test
    public void actionShouldOnlyTriggerOnceIfValueIsLargerThanZero() {
        Action action = mock(Action.class);
        DigitalToAnalogActionAdapter adapter = new DigitalToAnalogActionAdapter(action);

        adapter.setValue(1);
        adapter.actionPerformed(new ActionEvent(this, 0, ""));
        adapter.actionPerformed(new ActionEvent(this, 0, ""));

        verify(action, Mockito.times(1)).actionPerformed(any());
    }

    @Test
    public void actionShouldTriggerIfValueChangesBackToZero() {
        Action action = mock(Action.class);
        DigitalToAnalogActionAdapter adapter = new DigitalToAnalogActionAdapter(action);

        adapter.setValue(1);
        adapter.actionPerformed(new ActionEvent(this, 0, ""));

        adapter.setValue(0);
        adapter.setValue(1);
        adapter.actionPerformed(new ActionEvent(this, 0, ""));

        verify(action, Mockito.times(2)).actionPerformed(any());
    }
}
