package com.willwinder.universalgcodesender.connection;

/**
 * An interface for listening on response messages from a connection
 */
public interface IConnectionListener {

    /**
     * Method is invoked when a new response message is received from
     * the connection.
     *
     * @param response a response message
     */
    void handleResponseMessage(String response);
}
