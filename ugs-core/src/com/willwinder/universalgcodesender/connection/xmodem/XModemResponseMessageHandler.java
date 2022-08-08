/*
    Copyright 2022 Will Winder

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
package com.willwinder.universalgcodesender.connection.xmodem;

import com.willwinder.universalgcodesender.connection.Connection;
import com.willwinder.universalgcodesender.connection.IConnectionListener;
import com.willwinder.universalgcodesender.connection.IResponseMessageHandler;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import static com.willwinder.universalgcodesender.connection.xmodem.XModemUtils.trimEOF;

/**
 * A response message handler for handling XModem communication to upload and download files from the controller.
 *
 * @author Joacim Breiler
 */
public class XModemResponseMessageHandler implements IResponseMessageHandler {
    private final RingBuffer buffer = new RingBuffer(4096);
    private final XModem modem;

    public XModemResponseMessageHandler(Connection connection) {
        modem = new XModem(buffer, new OutputStream() {
            @Override
            public void write(int b) throws IOException {
                try {
                    connection.sendByteImmediately((byte) (b & 0xFF));
                } catch (Exception e) {
                    throw new IOException(e);
                }
            }
        });
    }

    @Override
    public void addListener(IConnectionListener connectionListener) {
        // Not implemented
    }

    @Override
    public void notifyListeners(String message) {
        // Not implemented
    }

    @Override
    public void handleResponse(byte[] buffer, int offset, int length) {
        this.buffer.write(buffer, offset, length);
    }

    public byte[] xmodemReceive() throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        modem.receive(outputStream, false);
        return trimEOF(outputStream.toByteArray());
    }

    public void xmodemSend(byte[] data) throws IOException {
        modem.send(new ByteArrayInputStream(data), false);
    }
}
