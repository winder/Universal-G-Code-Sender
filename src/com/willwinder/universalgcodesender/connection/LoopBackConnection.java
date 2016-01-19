/**
 * A diagnostic class to test application speed, this is a connection that
 * responds with "ok" as fast as possible.
 */

/*
    Copywrite 2016 Will Winder

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
package com.willwinder.universalgcodesender.connection;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import jssc.SerialPort;

/**
 *
 * @author wwinder
 */
public class LoopBackConnection extends Connection {
    private BlockingQueue<Integer> sent;
    private boolean exit = false;
    Thread  okThread;

    Runnable okRunnable = () -> {
        try {
            Thread.sleep(1000);
        } catch (Exception e) {}
        // This is nested beneath a GrblController, notify it that we're ready.
        comm.responseMessage(" ");
        comm.responseMessage("Grbl 9.9z [ugs diagnostic mode]");
        comm.responseMessage(" ");
        comm.responseMessage("This is a diagnostic end point which responds to each gcode");
        comm.responseMessage("command as fast as possible while doing nothing else.");

        while (true) {
            try {
                sent.take();
                comm.responseMessage("ok");
            } catch (InterruptedException ex) {
                if (exit) return;
            }
        }
    };

    public LoopBackConnection() {
        this("\r\n");
        sent = new LinkedBlockingQueue<>();
        okThread = new Thread(okRunnable);
    }
    
    public LoopBackConnection(String terminator) {
    }

    @Override
    synchronized public boolean openPort(String name, int baud) throws Exception {
        okThread.start();
        exit = false;

        return true;
    }
        
    @Override
    public void closePort() throws Exception {
        exit = true;
        okThread.interrupt();
    }
    
    @Override
    public void sendStringToComm(String command) throws Exception {
        this.sent.put(1);
    }
        
    @Override
    public void sendByteImmediately(byte b) throws Exception {
    }
    
    public static boolean supports(String portname, int baud) {
        return false;
    }        
}

