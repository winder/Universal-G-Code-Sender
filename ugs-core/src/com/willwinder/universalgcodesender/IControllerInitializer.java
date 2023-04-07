package com.willwinder.universalgcodesender;

/**
 * A controller initializer that can be used for more complex initialization flows
 */
public interface IControllerInitializer {

    /**
     * Attempts to initialize the controller
     *
     * @return true if the controller was properly initialized
     */
    boolean initialize();

    /**
     * Clears the initialization state
     */
    void reset();

    /**
     * Returns if the controller is initialized
     *
     * @return true if the controller is initialized
     */
    boolean isInitialized();
}
