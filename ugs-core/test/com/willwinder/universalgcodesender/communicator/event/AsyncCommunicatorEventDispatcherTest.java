package com.willwinder.universalgcodesender.communicator.event;

import com.willwinder.universalgcodesender.communicator.ICommunicatorListener;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static com.willwinder.universalgcodesender.utils.ThreadHelper.waitUntil;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

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
    public void dispatchShouldStartWorkerThread() throws TimeoutException, InterruptedException {
        ICommunicatorListener listener = mock(ICommunicatorListener.class);
        eventDispatcher.addListener(listener);

        eventDispatcher.communicatorPausedOnError();

        waitUntil(() -> eventDispatcher.getEventCount() == 0, 1100, TimeUnit.MILLISECONDS);

        verify(listener, times(1)).communicatorPausedOnError();
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
        Thread.sleep(100);
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
