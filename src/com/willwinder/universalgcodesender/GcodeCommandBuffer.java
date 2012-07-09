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
    private List<GcodeCommand> commandList;
    private File gcodeFile;
    private ListIterator<GcodeCommand> listIterator;
    private GcodeCommand currentCommand;
    
    GcodeCommandBuffer(File file) throws FileNotFoundException, IOException {
        this.gcodeFile = file;
        this. commandList = GcodeCommandBuffer.setFile(file);
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
    
// TODO: REMOVE THIS LATER, IT IS FOR TESTING
GcodeCommand test(int index) {
    return this.commandList.get(index);
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
    
    // Helper to parse a file into a list of GcodeCommand's
    static List<GcodeCommand> setFile(File file) throws FileNotFoundException, IOException {
        List<GcodeCommand> commands = new LinkedList<GcodeCommand>();
        
        FileInputStream fstream = new FileInputStream(file);
        DataInputStream dis = new DataInputStream(fstream);
        BufferedReader fileStream = new BufferedReader(new InputStreamReader(dis));

        String line;
        GcodeCommand command;
        int commandNum = 0;
        while ((line = fileStream.readLine()) != null) {
            command = new GcodeCommand(line, commandNum++);
            commands.add(command);
        }
        
        return commands;
    }
}
