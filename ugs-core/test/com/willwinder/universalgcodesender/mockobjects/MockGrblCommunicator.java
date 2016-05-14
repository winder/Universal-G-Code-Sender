/*
 * Mocks what a GrblCommunicator would do.
 */

/*
    Copywrite 2013-2015 Will Winder

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

package com.willwinder.universalgcodesender.mockobjects;

import com.willwinder.universalgcodesender.GrblCommunicator;
import java.io.IOException;
import java.util.ArrayList;
import java.util.TooManyListenersException;

/**
 *
 * @author wwinder
 */
public class MockGrblCommunicator extends GrblCommunicator {
    // Inputs.
    public String portName;
    public int    portRate;
    public String queuedString;
    public ArrayList<Byte>   sentBytes = new ArrayList<>();
    public Boolean open = false;
    public Boolean areActiveCommands = false;
    
    // Function calls.
    public int numOpenCommPortCalls;
    public int numCloseCommPortCalls;
    public int numQueueStringForCommCalls;
    public int numSendByteImmediatelyCalls;
    public int numAreActiveCommandsCalls;
    public int numStreamCommandsCalls;
    public int numPauseSendCalls;
    public int numResumeSendCalls;
    public int numCancelSendCalls;
    public int numSoftResetCalls;
    
    public void resetInputsAndFunctionCalls() {
        this.portName = "";
        this.portRate = 0;
        this.queuedString = "";
        this.open = false;
        
        this.numOpenCommPortCalls = 0;
        this.numCloseCommPortCalls = 0;
        this.numQueueStringForCommCalls = 0;
        this.numSendByteImmediatelyCalls = 0;
        this.numAreActiveCommandsCalls = 0;
        this.numStreamCommandsCalls = 0;
        this.numPauseSendCalls = 0;
        this.numResumeSendCalls = 0;
        this.numCancelSendCalls = 0;
        this.numSoftResetCalls = 0;
    }
    
    public MockGrblCommunicator() {
        //super();
        this.conn = new MockConnection();
        this.conn.setCommunicator(this);
    }   

    @Override
    public boolean openCommPort(String name, int baud) throws Exception {
        this.numOpenCommPortCalls++;

        this.portName = name;
        this.portRate = baud;
        this.open     = true;
        return true;
    }

    @Override
    public void closeCommPort() {
        this.numCloseCommPortCalls++;
        
        this.open = false;
    }

    @Override
    public boolean isCommOpen() {
        return open;
    }

    @Override
    public void queueStringForComm(String input) {
        this.numQueueStringForCommCalls++;
        
        this.queuedString = input;
    }

    @Override
    public void sendByteImmediately(byte b) throws IOException {
        this.numSendByteImmediatelyCalls++;

        this.sentBytes.add(b);
    }

    @Override
    public String activeCommandSummary() {
        return "";
    }

    @Override
    public boolean areActiveCommands() {
        this.numAreActiveCommandsCalls++;
        return this.areActiveCommands;
    }

    @Override
    public void streamCommands() {
        this.numStreamCommandsCalls++;
    }

    @Override
    public void pauseSend() {
        this.numPauseSendCalls++;
    }

    @Override
    public void resumeSend() {
        this.numResumeSendCalls++;
    }

    @Override
    public void cancelSend() {
        this.numCancelSendCalls++;
    }

    @Override
    public void softReset() {
        this.numSoftResetCalls++;
    }
}
