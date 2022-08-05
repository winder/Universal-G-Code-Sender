/*
    Copyright 2013-2022 Will Winder

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

import com.willwinder.universalgcodesender.connection.xmodem.XModemResponseMessageHandler;

import java.io.IOException;

/**
 * Abstract Connection
 *
 * @author wwinder
 * @author Joacim Breiler
 */
public abstract class AbstractConnection implements Connection {

    protected IResponseMessageHandler responseMessageHandler = new ResponseMessageHandler();

    @Override
    public void addListener(IConnectionListener connectionListener) {
        responseMessageHandler.addListener(connectionListener);
    }

    @Override
    public byte[] xmodemReceive() throws IOException {
        // Switch to a special XModem response handler
        IResponseMessageHandler previousResponseMessageHandler = responseMessageHandler;
        try {
            XModemResponseMessageHandler xModemResponseMessageHandler = new XModemResponseMessageHandler(this);
            responseMessageHandler = xModemResponseMessageHandler;
            return xModemResponseMessageHandler.xmodemReceive();
        } finally {
            // Restore the old response message handler
            responseMessageHandler = previousResponseMessageHandler;
        }
    }

    @Override
    public void xmodemSend(byte[] data) throws IOException {
        // Switch to a special XModem response handler
        IResponseMessageHandler previousResponseMessageHandler = responseMessageHandler;
        try {
            XModemResponseMessageHandler xModemResponseMessageHandler = new XModemResponseMessageHandler(this);
            responseMessageHandler = xModemResponseMessageHandler;
            xModemResponseMessageHandler.xmodemSend(data);
        } finally {
            // Restore the old response message handler
            responseMessageHandler = previousResponseMessageHandler;
        }
    }

    public IResponseMessageHandler getResponseMessageHandler() {
        return responseMessageHandler;
    }
}
