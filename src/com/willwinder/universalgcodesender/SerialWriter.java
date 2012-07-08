/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.willwinder.universalgcodesender;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 *
 * @author wwinder
 */
// This thread continuously polls a string buffer for data then writes it
// to an output stream.
public class SerialWriter implements Runnable {
    private StringBuffer lineBuffer;
    private OutputStream out;
    public boolean exit = false;

    public SerialWriter(OutputStream os, StringBuffer lineBuffer) {
        this.out = os;
        this.lineBuffer = lineBuffer;
    }

    public void run() {
        try {
            String s;
            while (!exit) {
                // Need to do 2 operations with lineBuffer in a row in here.
                // linBuffer should be some sort of custom class which has
                // a synchronized fetch & clear method.
                if (lineBuffer.length() < CommPortUtils.GRBL_RX_BUFFER_SIZE) {
                    s = lineBuffer.toString();
                    lineBuffer.setLength(0);
                } else {
                        s = lineBuffer.substring(0, CommPortUtils.GRBL_RX_BUFFER_SIZE-1);
                        lineBuffer.delete(0, CommPortUtils.GRBL_RX_BUFFER_SIZE-1);
                }

                if (s.length() > 0) {
                    PrintStream printStream = new PrintStream(this.out);
                    printStream.print(s);
                    printStream.close();    
                }
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