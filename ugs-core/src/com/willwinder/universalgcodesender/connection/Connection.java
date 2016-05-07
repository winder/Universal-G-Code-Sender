/*
 * Connection interface.
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

package com.willwinder.universalgcodesender.connection;

import com.willwinder.universalgcodesender.AbstractCommunicator;
import java.io.IOException;

/**
 *
 * @author wwinder
 */
public abstract class Connection {
    protected AbstractCommunicator comm;

    public void setCommunicator(AbstractCommunicator ac) {
        comm = ac;
    }

    // Implement this as static in the connection class. Use static function in
    // ConnectionFactory.
    //abstract public boolean supports(String portname);
    
    abstract public boolean openPort(String name, int baud) throws Exception;
    
    abstract public void closePort() throws Exception;
        
    abstract public void sendByteImmediately(byte b) throws Exception;
    
    abstract public void sendStringToComm(String command) throws Exception;

    abstract public boolean isOpen();
}
