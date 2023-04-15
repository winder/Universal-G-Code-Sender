package com.willwinder.universalgcodesender.connection;

import java.util.Optional;

/**
 * An identified device that can be used to connect to using the {@link ConnectionFactory#getConnection(String)}
 *
 * @author Joacim Breiler
 */
public interface IConnectionDevice {
    /**
     * Get the address of the device
     *
     * @return the address of the device
     */
    String getAddress();

    /**
     * Get an optional port of the device
     *
     * @return the port of the device
     */
    Optional<Integer> getPort();

    /**
     * Get the an optional description of the device
     *
     * @return the description of the device
     */
    Optional<String> getDescription();
}
