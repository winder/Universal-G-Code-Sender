/*
    Copyright 2022 Will Winder

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

import com.willwinder.universalgcodesender.utils.ThreadHelper;

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Checks if the controller is still connected. If not this will attempt to
 * disconnect the controller and clean up controller resources.
 *
 * @author Joacim Breiler
 */
public class ConnectionWatchTimer {
    private static final Logger LOGGER = Logger.getLogger(ConnectionWatchTimer.class.getName());
    private static final long INTERVAL = 2000;

    private final IController controller;
    private ScheduledFuture<?> timer;

    public ConnectionWatchTimer(IController controller) {
        this.controller = controller;
    }

    /**
     * Begin issuing status request commands.
     */
    public void start() {
        if (timer != null) {
            timer.cancel(false);
        }

        timer = ThreadHelper.scheduleAtFixedRate(() -> {
            if (!controller.getCommunicator().isConnected()) {
                LOGGER.log(Level.SEVERE, "The connection was interrupted, attempting to clean up resources");
                disconnect();
                stop();
            }
        }, INTERVAL);
    }

    /**
     * Checks if the controller is still connected
     */
    private void disconnect() {
        try {
            controller.closeCommPort();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Failed attempt to disconnect controller", e);
        }
    }

    /**
     * Stop issuing status request commands.
     */
    public void stop() {
        if (timer != null) {
            timer.cancel(false);
            timer = null;
        }
    }
}
