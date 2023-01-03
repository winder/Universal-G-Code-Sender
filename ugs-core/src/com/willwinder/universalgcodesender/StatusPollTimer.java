/*
    Copyright 2013-2020 Will Winder

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
package com.willwinder.universalgcodesender;

import javax.swing.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A status poll timer that will attempt request status reports from the controller at a fixed interval.
 * If the status report wasn't received it will wait until there was twenty outstanding polls, it will
 * then attempt to request a status report again.
 *
 * @author wwinder
 * @author Joacim Breiler
 */
public class StatusPollTimer {
    private static final Logger LOGGER = Logger.getLogger(StatusPollTimer.class.getName());
    private static final int MAX_OUTSTANDING_POLLS = 20;

    private final IController controller;
    private Timer timer;
    private int outstandingPolls;
    private int updateInterval;
    private boolean isEnabled = false;

    public StatusPollTimer(IController controller) {
        this.controller = controller;
    }

    /**
     * Begin issuing status request commands.
     */
    public void start() {
        if (isEnabled) {
            if (timer == null) {
                timer = createTimer();
            }

            if (!timer.isRunning()) {
                outstandingPolls = 0;
                timer.start();
            }
        }
    }

    /**
     * Create a timer which will execute  position polling mechanism.
     */
    private Timer createTimer() {
        stop();

        return new Timer(updateInterval, actionEvent -> {
            try {
                performPolling();
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Couldn't poll for status reports", ex);
                stop();
            }
        });
    }

    private void performPolling() throws Exception {
        if (!controller.isCommOpen()) {
            return;
        }

        if (outstandingPolls == 0) {
            outstandingPolls++;
            controller.requestStatusReport();
        } else {
            // If a poll is somehow lost after 20 intervals,
            // reset for sending another.
            outstandingPolls++;
            if (outstandingPolls >= MAX_OUTSTANDING_POLLS) {
                outstandingPolls = 0;
            }
        }
    }

    /**
     * Stop issuing status request commands.
     */
    public void stop() {
        if (timer != null) {
            timer.stop();
            timer = null;
        }
    }

    /**
     * Resets the outstanding polls, forcing a new status report request.
     */
    public void receivedStatus() {
        outstandingPolls = 0;
    }

    /**
     * Sets the update interval in milliseconds minimum value allowed is 10ms
     *
     * @param updateInterval the update interval in milliseconds
     */
    public void setUpdateInterval(int updateInterval) {
        this.updateInterval = Math.max(updateInterval, 10);
        if (timer.isRunning()) {
            stop();
            start();
        }
    }

    public int getUpdateInterval() {
        return updateInterval;
    }

    public void setEnabled(boolean isEnabled) {
        this.isEnabled = isEnabled;
        if (isEnabled) {
            start();
        } else {
            stop();
        }
    }

    public boolean isEnabled() {
        return this.isEnabled;
    }
}
