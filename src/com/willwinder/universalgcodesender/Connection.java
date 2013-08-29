/*
 * Connection interface.
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

package com.willwinder.universalgcodesender;

import gnu.io.NoSuchPortException;
import gnu.io.PortInUseException;
import gnu.io.UnsupportedCommOperationException;
import java.io.IOException;
import java.util.TooManyListenersException;

/**
 *
 * @author wwinder
 */
public abstract class Connection {
    protected AbstractCommunicator comm;

    public void setCommunicator(AbstractCommunicator ac) {
        comm = ac;
    }

    abstract public boolean supports(String portname);
    
    abstract public boolean openPort(String name, int baud) 
            throws NoSuchPortException, PortInUseException, 
            UnsupportedCommOperationException, IOException, 
            TooManyListenersException, Exception;
    
    abstract public void closePort();
        
    abstract public void sendByteImmediately(byte b) throws IOException;
    
    abstract public void sendStringToComm(String command);
}
