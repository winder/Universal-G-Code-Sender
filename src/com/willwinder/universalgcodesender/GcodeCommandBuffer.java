/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender;

import java.io.*;
import java.util.*;

/**
 *
 * @author wwinder
 */
public class GcodeCommandBuffer {
    private Queue<GcodeCommand> commandQueue;
    private GcodeCommand currentCommand = null;
    private int numCommands = 0;
    
    GcodeCommandBuffer() {
        this.commandQueue = new LinkedList<GcodeCommand>();
    }
    
    int size() {
        return this.commandQueue.size();
    }

    Boolean hasNext() {
        return this.commandQueue.size() > 0;
    }
    
    GcodeCommand currentCommand() {
        return currentCommand;
    }
    
    GcodeCommand nextCommand() {
        // Leave the "currentCommand" alone if we've exausted the queue.
        if (this.hasNext()) {
            this.currentCommand = this.commandQueue.remove();
        }
        
        return this.currentCommand();
    }
    
    GcodeCommand appendCommandString(String commandString) {
        GcodeCommand command = new GcodeCommand(commandString);
        command.setCommandNumber(this.numCommands++);
        this.commandQueue.add(command);
        
        // Preload first command, or next command if the first batch finished.
        if (this.currentCommand == null || this.currentCommand.isSent()) {
            this.nextCommand();
        }
        
        return command;
    }
    
    void clearBuffer() {
        this.currentCommand = null;
        this.commandQueue.clear();
    }
}
