package com.willwinder.universalgcodesender.communicator.event;

import com.willwinder.universalgcodesender.communicator.ICommunicatorListener;
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
        eventDispatcher.reset();
    }

    @Test
    public void dispatchShouldQueueEventsUntilStarted() throws TimeoutException {
        ICommunicatorListener listener = mock(ICommunicatorListener.class);
        eventDispatcher.addListener(listener);

        eventDispatcher.communicatorPausedOnError();

        assertEquals(1, eventDispatcher.getEventCount());

        waitUntil(() -> eventDispatcher.getEventCount() == 0, 1, TimeUnit.SECONDS);
    }

    @Test
    public void stopShouldInterruptListener() throws InterruptedException {
        // Simulate a long running listener
        ICommunicatorListener listener = mock(ICommunicatorListener.class);
        doAnswer(invocation -> {
            Thread.sleep(1000);
            return null;
        }).when(listener).commandSent(any());
        eventDispatcher.addListener(listener);
        eventDispatcher.commandSent(null);

        Thread.sleep(100);

        assertFalse(eventDispatcher.isStopped());
        eventDispatcher.reset();

        assertTrue(eventDispatcher.isStopped());
    }

    @Test
    public void dispatchShouldStopOnListenerException() throws InterruptedException {
        // Simulate a long running listener
        ICommunicatorListener listener = mock(ICommunicatorListener.class);
        doThrow(new RuntimeException()).when(listener).commandSent(any());
        eventDispatcher.addListener(listener);

        eventDispatcher.commandSent(null);

        Thread.sleep(100);
        assertTrue(eventDispatcher.isStopped());
    }
}
