/*
    Copyright 2016-2018 Will Winder

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

import com.willwinder.universalgcodesender.GrblUtils;
import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.model.Position;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A diagnostic class to test application speed, this is a connection that
 * responds with "ok" as fast as possible.
 * 
 * @author wwinder
 */
public class LoopBackConnection extends AbstractConnection {
    private BlockingQueue<String> sent;
    private boolean exit = false;
    private boolean open = false;
    private Thread  okThread;
    private int ms = 0;

    private void initialize() {
        responseMessageHandler.handleResponse(" ");
        responseMessageHandler.handleResponse("Grbl 0.9z [ugs diagnostic mode]");
        responseMessageHandler.handleResponse(" ");
        responseMessageHandler.handleResponse("This is a diagnostic end point which responds to each gcode");
        responseMessageHandler.handleResponse("command as fast as possible while doing nothing else.");
    }

    Runnable okRunnable = () -> {
        try {
            Thread.sleep(1000);
        } catch (Exception e) {}
        // This is nested beneath a GrblController, notify it that we're ready.
        initialize();

        int count = 0;
        Position lastCommand = null;
        while (true) {
            GcodeParser gcp = new GcodeParser();
            try {
                String command = sent.take().trim();
                Thread.sleep(ms);

                if (command.equals(Byte.toString(GrblUtils.GRBL_STATUS_COMMAND))) {
                    String xyz = "0,0,0";
                    if (lastCommand != null) {
                        xyz = String.format("%f,%f,%f", lastCommand.x, lastCommand.y, lastCommand.z);
                    }
                    responseMessageHandler.handleResponse(String.format("<Idle,MPos:%s,WPos:%s>", xyz, xyz));
                } else if (command.equals("G61")) {
                    responseMessageHandler.handleResponse("error: G61 not supported.");
                } else {
                    count++;
                    if (count == 2) {
                        initialize();
                    }
                    else if (count > 2) {
                        try {
                            gcp.addCommand(command);
                            lastCommand = gcp.getCurrentState().currentPoint;
                        } catch (Exception e) {
                        }
                        responseMessageHandler.handleResponse("ok");
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
    public void setUri(String uri) {

    }

    @Override
    synchronized public boolean openPort() throws Exception {
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
    public List<String> getPortNames() {
        return Arrays.asList("loopback");
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
}

