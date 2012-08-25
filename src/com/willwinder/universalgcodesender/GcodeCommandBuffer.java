/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender;

import java.io.*;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;

/**
 *
 * @author wwinder
 */
public class GcodeCommandBuffer {
    private LinkedList<GcodeCommand> commandList;
    private File gcodeFile;
    private ListIterator<GcodeCommand> listIterator;
    private GcodeCommand currentCommand;
    
    GcodeCommandBuffer(File file) throws FileNotFoundException, IOException {
        this.gcodeFile = file;
        this.commandList = GcodeCommandBuffer.setFile(file);
    }
    
    GcodeCommandBuffer() {
        this.commandList = new LinkedList<GcodeCommand>();
    }
    
    File getFile() {
        return this.gcodeFile;
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
    
    void appendCommand(GcodeCommand command) {
        this.commandList.add(command);
        
        if (this.currentCommand != null) {
            // Reset iterator to get back to where we were.
            this.listIterator = this.commandList.listIterator();
            while (this.listIterator.hasNext() && (this.currentCommand != this.listIterator.next()))
            {/* don't do anything, we're just cycling back to the current command. */};
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
}
