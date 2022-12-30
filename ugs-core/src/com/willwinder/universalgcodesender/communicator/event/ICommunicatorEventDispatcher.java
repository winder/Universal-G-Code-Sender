package com.willwinder.universalgcodesender.communicator.event;

import com.willwinder.universalgcodesender.listeners.CommunicatorListener;

/**
 * An event dispatcher responsible for notifying listeners with events
 * from the the communicator.
 *
 * @author winder
 * @author Joacim Breiler
 */
public interface ICommunicatorEventDispatcher {
    /**
     * Starts the event dispatcher and begin sending events
     * to its listeners.
     */
    void start();

    /**
     * Stops the event dispatcher from processing queued events.
     */
    void stop();

    /**
     * Removes a communicator listener
     *
     * @param listener a listener for communicator events
     */
    void removeListener(CommunicatorListener listener);

    /**
     * Adds a communicator listener
     *
     * @param listener a listener for communicator events
     */
    void addListener(CommunicatorListener listener);

    /**
     * Dispatches the communicator event to all listeners
     *
     * @param event the event data to be dispatched
     */
    void dispatch(CommunicatorEvent event);

    /**
     * Clears any buffered events and stops the dispatcher from processing events.
     */
    void reset();
}
