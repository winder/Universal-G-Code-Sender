/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * This thread continuously polls a string buffer for data then writes it to an
 * output stream.
 * 
 * @author wwinder
 */

public class SerialWriter implements Runnable {
    private StringBuffer lineBuffer;
    private OutputStream out;
    public boolean exit = false;

    // For synchronized operations
    private final Object syncObject = new Object();
    
    /**
     * Creates the thread object with required parameters
     * @param os            The output stream that lineBuffer will be written to
     * @param lineBuffer    A StringBuffer which other threads can write to,
     *                      this thread will notice new content and write it to
     *                      the output stream.
     */
    public SerialWriter(OutputStream os, StringBuffer lineBuffer) {
        this.out = os;
        this.lineBuffer = lineBuffer;
    }

    @Override
    public void run() {
        try {
            String s = "";
            while (!exit) {
                // Fetch data and clear buffer.
                if (lineBuffer.length() > 0) {
                    synchronized(syncObject) {
                        s = lineBuffer.toString();
                        lineBuffer.setLength(0);
                    }
                }
                
                // Send it out
                if (s.length() > 0) {
                    PrintStream printStream = new PrintStream(this.out);
                    printStream.print(s);
                    printStream.close();    
                }
                
                // Sleep
                synchronized (this) {
                    this.wait(1000);
                }
            }
        } catch (InterruptedException ex) {
            System.out.println("SerialWriter thread died.");
            ex.printStackTrace();
            System.exit(-1);
        }
    }
}
