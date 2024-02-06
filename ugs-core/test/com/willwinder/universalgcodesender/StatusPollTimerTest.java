package com.willwinder.universalgcodesender;

import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

public class StatusPollTimerTest {
    private StatusPollTimer statusPollTimer;

    @Mock
    private IController controller;

    @Before
    public void setUp() {
        MockitoAnnotations.initMocks(this);
        statusPollTimer = new StatusPollTimer(controller);
    }

    @Test
    public void setUpdateIntervalShouldSetTheIntervalWhenTimerIsNotRunning() {
        statusPollTimer.setUpdateInterval(100);
        assertEquals(100, statusPollTimer.getUpdateInterval());
    }
}