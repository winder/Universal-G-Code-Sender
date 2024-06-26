/*
    Copyright 2013-2024 Will Winder

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

import com.willwinder.universalgcodesender.connection.xmodem.XModemConnectionListenerHandler;

import java.io.IOException;
import java.util.Arrays;

/**
 * Abstract Connection
 *
 * @author wwinder
 * @author Joacim Breiler
 */
public abstract class AbstractConnection implements Connection {

    protected IConnectionListenerManager connectionListenerManager = new ConnectionListenerManager();

    @Override
    public void addListener(IConnectionListener connectionListener) {
        connectionListenerManager.addListener(connectionListener);
    }

    @Override
    public byte[] xmodemReceive() throws IOException {
        // Switch to a special XModem response handler
        XModemConnectionListenerHandler reader = new XModemConnectionListenerHandler(this, connectionListenerManager);
        try {
            connectionListenerManager = reader;
            byte[] result = reader.xmodemReceive();

            return trimEOF(result);
        } finally {
            // Restore the old response message handler
            connectionListenerManager = reader.unwrap();
        }
    }

    protected static byte[] trimEOF(byte[] result) {
        // The result contains trailing EOF, trim those
        int i = result.length - 1;
        while(i >= 0 && result[i] == 0x1A) {
            i--;
        }

        return Arrays.copyOfRange(result, 0, i + 1);
    }

    @Override
    public void xmodemSend(byte[] data) throws IOException {
        // Switch to a special XModem response handler
        XModemConnectionListenerHandler reader = new XModemConnectionListenerHandler(this, connectionListenerManager);
        try {
            connectionListenerManager = reader;
            reader.xmodemSend(data);
        } finally {
            // Restore the old response message handler
            connectionListenerManager = reader.unwrap();
        }
    }

    public IConnectionListenerManager getConnectionListenerManager() {
        return connectionListenerManager;
    }
}
