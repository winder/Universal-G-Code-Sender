/*
 * The primary purpose of this class is to maintain a count of how many commands
 * have been created.
 */
package com.willwinder.universalgcodesender;

/**
 *
 * @author Owen
 */
public class GcodeCommandCreator {
    private int numCommands = 0;
    
    public GcodeCommandCreator() {
    }
    
    public GcodeCommandCreator(int num) {
        this.numCommands = num;
    }
    
    int nextCommandNum() {
        return this.numCommands;
    }
    
    GcodeCommand createCommand(String commandString) {
        GcodeCommand command = new GcodeCommand(commandString);
        command.setCommandNumber(this.numCommands++);
        return command;
    }
}
