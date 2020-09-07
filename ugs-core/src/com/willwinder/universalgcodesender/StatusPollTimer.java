package com.willwinder.universalgcodesender;

import javax.swing.*;
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

    private Timer timer;
    private IController controller;
    private int outstandingPolls;

    public StatusPollTimer(IController controller) {
        this.controller = controller;
    }

    /**
     * Begin issuing status request commands.
     */
    public void start() {
        if (controller.getStatusUpdatesEnabled()) {
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

        return new Timer(controller.getStatusUpdateRate(), actionEvent -> {
            try {
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
            } catch (Exception ex) {
                LOGGER.log(Level.SEVERE, "Couldn't poll for status reports", ex);
                stop();
            }
        });
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
}
