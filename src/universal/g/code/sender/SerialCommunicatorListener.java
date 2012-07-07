/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package universal.g.code.sender;

/**
 *
 * @author wwinder
 */
public interface SerialCommunicatorListener {
    void fileStreamComplete(String filename, boolean success);
    void commandSent(GcodeCommand command);
    void commandComplete(GcodeCommand command);
    void messageForConsole(String msg);
    String preprocessCommand(String command);
}
