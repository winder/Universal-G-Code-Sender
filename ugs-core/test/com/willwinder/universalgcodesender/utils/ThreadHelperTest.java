/*
    Copyright 2018 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender.utils;

import org.apache.commons.lang3.time.StopWatch;
import org.junit.Test;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.assertTrue;

/**
 * @author Joacim Breiler
 */
public class ThreadHelperTest {
    private boolean waitUntilCondition = false;

    @Test
    public void waitUntilWhenFalseShouldTimeout() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        try {
            ThreadHelper.waitUntil(() -> false, 200, TimeUnit.MILLISECONDS);
        } catch (TimeoutException ignored) {
            // Never mind
        }

        stopWatch.stop();
        assertTrue(stopWatch.getTime() >= 200);
    }

    @Test
    public void waitUntilWhenTrueShouldReturnOk() throws TimeoutException {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ThreadHelper.waitUntil(() -> true, 200, TimeUnit.MILLISECONDS);
        stopWatch.stop();
        assertTrue(stopWatch.getTime() < 200);
    }

    @Test
    public void waitUntilWhenChangeToTrueAfterTimeShouldReturnOk() throws TimeoutException {
        waitUntilCondition = false;

        // Start a timer that switches the condition after some time
        Timer timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                waitUntilCondition = true;
            }
        }, 500);

        // Wait until the condition switches
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
        ThreadHelper.waitUntil(() -> waitUntilCondition, 1000, TimeUnit.MILLISECONDS);
        stopWatch.stop();

        // Make sure it was within the time interval
        assertTrue(stopWatch.getTime() < 1000);
        assertTrue(stopWatch.getTime() >= 500);
    }
}
