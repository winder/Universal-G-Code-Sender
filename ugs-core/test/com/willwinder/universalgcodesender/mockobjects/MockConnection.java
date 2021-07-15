/*
 * Mocks what a Connection class would do.
 */

/*
    Copyright 2013-2015 Will Winder

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

import com.willwinder.universalgcodesender.connection.AbstractConnection;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author wwinder
 */
public class MockConnection extends AbstractConnection {
    protected final InputStream in;   // protected for unit testing.
    protected final OutputStream out; // protected for unit testing.

    public MockConnection() {
        in = null;
        out = null;
    }
    
    public MockConnection(final InputStream in, final OutputStream out) {
        this.in = in;
        this.out = out;
    }
    
    public void sendResponse(String str) {
        this.responseMessageHandler.notifyListeners(str);
    }

    @Override
    public void setUri(String uri) {

    }

    @Override
    public boolean openPort() throws Exception {
        return true;
    }

    @Override
    public void closePort() {
    }

    @Override
    public boolean isOpen() {
        return true;
    }

    @Override
    public List<String> getPortNames() {
        return Arrays.asList("port");
    }

    @Override
    public void sendByteImmediately(byte b) throws IOException {
        out.write(b);
    }

    @Override
    public void sendStringToComm(String command) {
        try {
            this.out.write(command.getBytes(Charset.defaultCharset().name()));
        } catch (IOException ex) {
            Logger.getLogger(MockConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public boolean supports(String portname) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}
