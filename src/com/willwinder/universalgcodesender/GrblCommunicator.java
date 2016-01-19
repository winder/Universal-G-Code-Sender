/*
    Copywrite 2012-2016 Will Winder

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

import com.willwinder.universalgcodesender.connection.Connection;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import java.util.concurrent.LinkedBlockingDeque;

/**
 * @author wwinder
 */
public class GrblCommunicator extends BufferedCommunicator {
    
    protected GrblCommunicator() {}

    /**
     * This constructor is for dependency injection so a mock serial device can
     * act as GRBL.
     */
    protected GrblCommunicator(
            LinkedBlockingDeque<String> cb, LinkedBlockingDeque<GcodeCommand> asl, Connection c) {
        // Base constructor.
        //this();
        //TODO-f4grx-DONE: Mock connection
        this.conn = c;
        this.conn.setCommunicator(this);
        
        this.setQueuesForTesting(cb, asl);
    }

    @Override
    public int getBufferSize() {
        return GrblUtils.GRBL_RX_BUFFER_SIZE;
    }

    @Override
    public String getLineTerminator() {
        return "\r\n";
    }

    @Override
    protected boolean processedCommand(String response) {
        return GcodeCommand.isOkErrorResponse(response);
    }

    @Override
    protected void sendingCommand(String response) {
        // no-op for this protocol.
    }
}
