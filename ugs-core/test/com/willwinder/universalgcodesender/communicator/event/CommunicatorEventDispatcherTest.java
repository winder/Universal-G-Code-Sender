package com.willwinder.universalgcodesender.communicator.event;

import com.willwinder.universalgcodesender.listeners.CommunicatorListener;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class CommunicatorEventDispatcherTest {

    private CommunicatorEventDispatcher eventDispatcher;

    @Before
    public void setUp() {
        eventDispatcher = new CommunicatorEventDispatcher();
    }

    @Test
    public void dispatchPauseEventShouldTriggerListener() {
        CommunicatorListener listener = mock(CommunicatorListener.class);
        eventDispatcher.addListener(listener);

        CommunicatorEvent event = new CommunicatorEvent(CommunicatorEventType.PAUSED, null, null);
        eventDispatcher.dispatch(event);

        verify(listener, times(1)).communicatorPausedOnError();
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void dispatchSentEventShouldTriggerListener() {
        CommunicatorListener listener = mock(CommunicatorListener.class);
        eventDispatcher.addListener(listener);

        CommunicatorEvent event = new CommunicatorEvent(CommunicatorEventType.COMMAND_SENT, null, null);
        eventDispatcher.dispatch(event);

        verify(listener, times(1)).commandSent(any());
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void dispatchSkippedEventShouldTriggerListener() {
        CommunicatorListener listener = mock(CommunicatorListener.class);
        eventDispatcher.addListener(listener);

        CommunicatorEvent event = new CommunicatorEvent(CommunicatorEventType.COMMAND_SKIPPED, null, null);
        eventDispatcher.dispatch(event);

        verify(listener, times(1)).commandSkipped(any());
        verifyNoMoreInteractions(listener);
    }

    @Test
    public void removeListenerShouldMakeEventsNoLongerDispatch() {
        CommunicatorListener listener = mock(CommunicatorListener.class);
        eventDispatcher.addListener(listener);
        eventDispatcher.removeListener(listener);

        CommunicatorEvent event = new CommunicatorEvent(CommunicatorEventType.COMMAND_SKIPPED, null, null);
        eventDispatcher.dispatch(event);

        verifyNoMoreInteractions(listener);
    }
}
