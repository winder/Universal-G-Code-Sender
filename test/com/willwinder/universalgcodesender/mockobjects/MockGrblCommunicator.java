/*
 * Mocks what a GrblCommunicator would do.
 */

/*
    Copywrite 2013 Will Winder

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

import com.willwinder.universalgcodesender.AbstractCommunicator;
import com.willwinder.universalgcodesender.GrblCommunicator;
import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.util.TooManyListenersException;

/**
 *
 * @author wwinder
 */
public class MockGrblCommunicator extends GrblCommunicator {

    public MockGrblCommunicator() {
        
    }   

    @Override
    public boolean openCommPort(String name, int baud) throws NoSuchPortException, PortInUseException, UnsupportedCommOperationException, IOException, TooManyListenersException, Exception {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void closeCommPort() {
        throw new UnsupportedOperationException("Not supported yet.");
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
