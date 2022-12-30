package com.willwinder.universalgcodesender.communicator.event;

import com.willwinder.universalgcodesender.communicator.ICommunicatorListener;

/**
 * An event dispatcher responsible for notifying listeners with events
 * from the communicator.
 *
 * @author winder
 * @author Joacim Breiler
 */
public interface ICommunicatorEventDispatcher extends ICommunicatorListener {
    /**
     * Removes a communicator listener
     *
     * @param listener a listener for communicator events
     */
    void removeListener(ICommunicatorListener listener);

    /**
     * Adds a communicator listener
     *
     * @param listener a listener for communicator events
     */
    void addListener(ICommunicatorListener listener);

    /**
     * Clears any buffered events and resets the dispatcher
     */
    void reset();
}
