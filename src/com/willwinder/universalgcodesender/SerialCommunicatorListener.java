/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender;

/**
 *
 * @author wwinder
 */
public interface SerialCommunicatorListener {
    void fileStreamComplete(String filename, boolean success);
    void commandQueued(GcodeCommand command);
    void commandSent(GcodeCommand command);
    void commandComplete(GcodeCommand command);
    void messageForConsole(String msg);
    String preprocessCommand(String command);
}
