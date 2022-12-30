package com.willwinder.universalgcodesender.communicator.event;

import com.willwinder.universalgcodesender.listeners.CommunicatorListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.willwinder.universalgcodesender.utils.ThreadHelper.waitUntil;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;

public class AsyncCommunicatorEventDispatcherTest {
    private AsyncCommunicatorEventDispatcher eventDispatcher;

    @Before
    public void setUp() {
        eventDispatcher = new AsyncCommunicatorEventDispatcher();
    }

    @After
    public void tearDown() {
        eventDispatcher.stop();
        eventDispatcher.reset();
    }

    @Test
    public void dispatchShouldQueueEventsUntilStarted() throws TimeoutException {
        CommunicatorListener listener = mock(CommunicatorListener.class);
        eventDispatcher.addListener(listener);

        CommunicatorEvent event = new CommunicatorEvent(CommunicatorEventType.PAUSED, null, null);
        eventDispatcher.dispatch(event);

        assertEquals(1, eventDispatcher.getEventCount());

        eventDispatcher.start();
        waitUntil(() -> eventDispatcher.getEventCount() == 0, 1, TimeUnit.SECONDS);
    }

    @Test
    public void stopShouldInterruptListener() throws InterruptedException {
        // Simulate a long running listener
        CommunicatorListener listener = mock(CommunicatorListener.class);
        doAnswer(invocation -> {
            Thread.sleep(1000);
            return null;
        }).when(listener).commandSent(any());
        eventDispatcher.addListener(listener);
        eventDispatcher.dispatch(new CommunicatorEvent(CommunicatorEventType.COMMAND_SENT, null, null));
        eventDispatcher.start();
        Thread.sleep(100);

        assertFalse(eventDispatcher.isStopped());
        eventDispatcher.stop();

        assertTrue(eventDispatcher.isStopped());
    }

    @Test
    public void dispatchShouldStopOnListenerException() throws InterruptedException {
        // Simulate a long running listener
        CommunicatorListener listener = mock(CommunicatorListener.class);
        doThrow(new RuntimeException()).when(listener).commandSent(any());
        eventDispatcher.addListener(listener);

        eventDispatcher.dispatch(new CommunicatorEvent(CommunicatorEventType.COMMAND_SENT, null, null));
        eventDispatcher.start();

        Thread.sleep(100);
        assertTrue(eventDispatcher.isStopped());
    }
}
