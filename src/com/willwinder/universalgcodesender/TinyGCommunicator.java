/*
 * TinyG serial port interface class.
 */

/*
    Copywrite 2012-2013 Will Winder

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
package com.willwinder.universalgcodesender;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.SerialPortEvent;
import gnu.io.SerialPortEventListener;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.TooManyListenersException;

/**
 *
 * @author wwinder
 */
public class TinyGCommunicator extends SerialCommunicator implements SerialPortEventListener {

    TinyGCommunicator() {
        this.setLineTerminator("\r\n");
    }
    
    /**
     * This constructor is for dependency injection so a mock serial device can
     * act as GRBL.
     */
    protected TinyGCommunicator(final InputStream in, final OutputStream out,
            LinkedList<String> cb, LinkedList<String> asl) {
        // Base constructor.
        this();
        
        this.in = in;
        this.out = out;
        //this.commandBuffer = cb;
        //this.activeStringList = asl;
    }
    
    @Override
    protected void commPortOpenedEvent() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void commPortClosedEvent() {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    protected void responseMessage(String response) {
        //throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void queueStringForComm(String input) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void sendByteImmediately(byte b) throws IOException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean areActiveCommands() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void streamCommands() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void pauseSend() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void resumeSend() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void cancelSend() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void softReset() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}
