/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender;

import java.io.*;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author wwinder
 */
public class GcodeCommandBuffer {
    private LinkedList<GcodeCommand> commandList;
    private ListIterator<GcodeCommand> listIterator;
    private GcodeCommand currentCommand;
    
    GcodeCommandBuffer() {
        this.commandList = new LinkedList<GcodeCommand>();
        this.listIterator = null;
    }
    
    int size() {
        return this.commandList.size();
    }
    
    GcodeCommand getFinalCommand() {
        return this.commandList.getLast();
    }
    
    void resetIterator() {
        this.listIterator = this.commandList.listIterator();
    }
    
    void resetIteratorToNextUnsent() {
        this.resetIterator();
        while (this.listIterator.hasNext()){
            this.currentCommand = this.listIterator.next();
            if (this.currentCommand.isSent() == false)
            {
                return;
            }
        }
    }
    
    void resetIteratorTo(GcodeCommand theCommand) {
        this.resetIterator();
        while (this.listIterator.hasNext()){
            GcodeCommand command = this.listIterator.next();
            if (command == theCommand)
            {
                return;
            }
        }
    }
    
    void resetIteratorToCurrent() {
        if (this.currentCommand != null) {
            resetIteratorTo(this.currentCommand);
        } else {
            resetIterator();
        }
    }
    
    Boolean hasNext() {
        return this.listIterator.hasNext();
    }
    
    GcodeCommand currentCommand() {
        return this.currentCommand;
    }
    
    GcodeCommand nextCommand() {
        if (this.listIterator == null) {
            this.resetIterator();
        }
        
        
        this.currentCommand = listIterator.next();
        return this.currentCommand();
    }
    
    GcodeCommand appendCommand(String commandString) {
        GcodeCommand command = new GcodeCommand(commandString);
        command.setCommandNumber(this.size());

        this.commandList.add(command);
        
        return command;
    }
    
    /*
    void appendFile(File file) throws FileNotFoundException, IOException {
        this.gcodeFile = file;
                
        FileInputStream fstream = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(fstream);
        BufferedReader fileStream = new BufferedReader(new InputStreamReader(dis));

        String line;
        GcodeCommand command;
        int commandNum = this.size();
        while ((line = fileStream.readLine()) != null) {
            // Commands end with a newline.
            command = new GcodeCommand(line + '\n', commandNum++);
            this.commandList.add(command);
            //this.appendCommand(line);
        }
        
        if (this.currentCommand != null) {
            // Reset iterator to get back to where we were.
            //this.resetIteratorToNextUnsent();
            this.resetIteratorTo(this.currentCommand);
        }
    }
    
    // Helper to parse a file into a list of GcodeCommand's
    static LinkedList<GcodeCommand> setFile(File file) throws FileNotFoundException, IOException {
        LinkedList<GcodeCommand> commands = new LinkedList<GcodeCommand>();
        
        FileInputStream fstream = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(fstream);
        BufferedReader fileStream = new BufferedReader(new InputStreamReader(dis));

        String line;
        GcodeCommand command;
        int commandNum = 0;
        while ((line = fileStream.readLine()) != null) {
            // Commands end with a newline.
            command = new GcodeCommand(line + '\n', commandNum++);
            commands.add(command);
        }
        
        return commands;
    }
    */
}
