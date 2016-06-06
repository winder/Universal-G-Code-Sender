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

import com.willwinder.universalgcodesender.AbstractCommunicator;
import com.willwinder.universalgcodesender.GrblUtils;
import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.types.PointSegment;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import javax.vecmath.Point3d;
import jssc.SerialPort;

/**
 *
 * @author wwinder
 */
public class LoopBackConnection extends Connection {
    private BlockingQueue<String> sent;
    private boolean exit = false;
    private boolean open = false;
    Thread  okThread;
    private int ms = 0;

    private static void initialize(AbstractCommunicator comm) {
        comm.responseMessage(" ");
        comm.responseMessage("Grbl 9.9z [ugs diagnostic mode]");
        comm.responseMessage(" ");
        comm.responseMessage("This is a diagnostic end point which responds to each gcode");
        comm.responseMessage("command as fast as possible while doing nothing else.");
    }

    Runnable okRunnable = () -> {
        try {
            Thread.sleep(1000);
        } catch (Exception e) {}
        // This is nested beneath a GrblController, notify it that we're ready.
        initialize(comm);

        int count = 0;
        Point3d lastCommand = null;
        while (true) {
            GcodeParser gcp = new GcodeParser();
            try {
                String command = sent.take();
                Thread.sleep(ms);

                String response;
                if (command.equals(Byte.toString(GrblUtils.GRBL_STATUS_COMMAND))) {
                    String xyz = "0,0,0";
                    if (lastCommand != null) {
                        Point3d p = lastCommand;
                        xyz = String.format("%f,%f,%f", p.x, p.y, p.z);
                    }
                    comm.responseMessage(String.format("<Run,MPos:%s,WPos:%s>", xyz, xyz));
                } else {
                    count++;
                    if (count == 2) {
                        initialize(comm);
                    }
                    else if (count > 2) {
                        try {
                            gcp.addCommand(command);
                            lastCommand = gcp.getCurrentState().currentPoint;
                        } catch (Exception e) {
                        }
                        comm.responseMessage("ok");
                    }
                }
            } catch (InterruptedException ex) {
                if (exit) return;
            }
        }
    };

    public LoopBackConnection(int ms) {
        this("\r\n");
        this.ms = ms;
        sent = new LinkedBlockingQueue<>();
        okThread = new Thread(okRunnable);
    }
    
    public LoopBackConnection(String terminator) {
    }

    @Override
    synchronized public boolean openPort(String name, int baud) throws Exception {
        okThread.start();
        exit = false;

        open = true;
        return isOpen();
    }

    @Override
    public boolean isOpen() {
        return open;
    }

    @Override
    public void closePort() throws Exception {
        exit = true;
        open = false;
        okThread.interrupt();
    }
    
    @Override
    public void sendStringToComm(String command) throws Exception {
        this.sent.put(command);
    }
        
    @Override
    public void sendByteImmediately(byte b) throws Exception {
        this.sent.put(Byte.toString(b));
    }
    
    public static boolean supports(String portname, int baud) {
        return false;
    }        
}

