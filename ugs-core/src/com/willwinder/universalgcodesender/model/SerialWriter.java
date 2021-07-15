/*
 * DEPRECATED
 * 
 * A thread which continuously polls a shared StringBuffer object and writes it
 * to an OutputStream. The OutputStream in this case was supposed to go to a
 * SerialPort.
 */

/*
    Copyright 2012 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.willwinder.universalgcodesender.model;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * This thread continuously polls a string buffer for data then writes it to an
 * output stream.
 * 
 * @author wwinder
 */

public class SerialWriter implements Runnable {
    private final StringBuffer lineBuffer;
    private final OutputStream out;
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
                    try (PrintStream printStream = new PrintStream(this.out)) {
                        printStream.print(s);
                    }    
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
