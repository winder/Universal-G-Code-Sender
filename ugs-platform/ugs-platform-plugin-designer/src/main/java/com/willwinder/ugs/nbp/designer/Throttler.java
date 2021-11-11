/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.designer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * A throttler class that only allows one execution at the time within the given delay
 * time in milliseconds. First execution will happen immediately, the second will be delayed
 * with the given milliseconds and any subsequent executions will be ignored.
 *
 * @author Joacim Breiler
 */
public class Throttler {
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
    private final Runnable runnable;
    private final long delayMillis;
    private long lastRunTime = -1;
    private boolean isScheduled = false;

    public Throttler(Runnable runnable, long delayMillis) {
        this.runnable = runnable;
        this.delayMillis = delayMillis;
    }

    public synchronized void run() {
        long currentTime = System.currentTimeMillis();
        if (isScheduled) {
            // Never mind!
        } else if (shouldRunNow(currentTime)) {
            lastRunTime = currentTime;
            runnable.run();
        } else {
            isScheduled = true;
            scheduleRunnable(this::executeRunnable, delayMillis);
        }
    }

    private boolean shouldRunNow(long currentTime) {
        return lastRunTime == -1 || lastRunTime + delayMillis < currentTime;
    }

    private void scheduleRunnable(Runnable runnable, long delayMillis) {
        scheduler.schedule(runnable, delayMillis, TimeUnit.MILLISECONDS);
    }

    private synchronized void executeRunnable() {
        isScheduled = false;
        lastRunTime = System.currentTimeMillis();
        runnable.run();
    }
}
