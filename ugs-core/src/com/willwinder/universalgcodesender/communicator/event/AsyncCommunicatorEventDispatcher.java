/*
    Copyright 2012-2022 Will Winder

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
package com.willwinder.universalgcodesender.communicator.event;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An asynchronous communicator event dispatcher that will dispatch events
 * from the communicator to all listeners.
 * <p>
 * If commands complete very fast, like several comments in a row being
 * skipped, then multiple event handlers could process them out of order. To
 * prevent that from happening we use a blocking queue to add events in the
 * main thread, and process them in order with a single event thread.
 * <p>
 * This will also increase send performance as the communicator does not need
 * to wait for the UI to be refreshed before continuing sending the next command.
 *
 * @author winder
 * @author Joacim Breiler
 */
public class AsyncCommunicatorEventDispatcher extends CommunicatorEventDispatcher implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(AsyncCommunicatorEventDispatcher.class.getSimpleName());

    private final LinkedBlockingDeque<CommunicatorEvent> eventQueue = new LinkedBlockingDeque<>();
    private final Thread eventThread = new Thread(this);
    private boolean stop = false;

    @Override
    public void start() {
        eventThread.start();
    }

    @Override
    public void stop() {
        stop = true;
        eventThread.interrupt();
    }

    @Override
    public void dispatch(CommunicatorEvent event) {
        this.eventQueue.add(event);
    }

    @Override
    public void reset() {
        eventQueue.clear();
    }

    public int getEventCount() {
        return eventQueue.size();
    }

    @Override
    public void run() {
        while (!stop) {
            try {
                CommunicatorEvent e = eventQueue.take();
                sendEvent(e.event, e.string, e.command);
            } catch (InterruptedException ignored) {
                stop = true;
            } catch (Exception e) {
                LOGGER.log(Level.WARNING, "Couldn't send event", e);
                stop = true;
            }
        }
    }

    public boolean isStopped() {
        return stop;
    }
}
