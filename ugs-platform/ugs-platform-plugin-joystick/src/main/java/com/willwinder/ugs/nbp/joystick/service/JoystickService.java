package com.willwinder.ugs.nbp.joystick.service;

/**
 * A joystick service responsible for initializing and reading joystick data and
 * notify events for updates.
 *
 * @author Joacim Breiler
 */
public interface JoystickService {
    /**
     * Starts the service by initializing the gamepad/joystick and starts to listen to
     * controller data. Any state change will be notified to registered listeners.
     */
    void initialize();

    /**
     * Stops the service and releases any gamepad/joystick.
     */
    void destroy();

    /**
     * Adds a listener for any joystick state changes
     *
     * @param listener a listener
     */
    void addListener(JoystickServiceListener listener);

    /**
     * Removes a listener for any joystick state changes
     *
     * @param listener a registered listener
     */
    void removeListener(JoystickServiceListener listener);

    /**
     * Removes all listeners
     */
    void removeAllListeners();
}
