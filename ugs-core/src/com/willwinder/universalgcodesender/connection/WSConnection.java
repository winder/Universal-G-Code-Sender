/*
	Copyright 2023 Will Winder

	This file is part of Universal Gcode Sender (UGS).

	UGS is free software: you can redistribute it and/or modify
	it under the terms of the GNU General Public License as published by
	the Free Software Foundation, either version 3 of the License, or
	(at your option) any later version.

	UGS is distributed in the hope that it will be useful,
	but WITHOUT ANY WARRANTY; without even the implied warranty of
	MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
	GNU General Public License for more details.

	You should have received a copy of the GNU General Public License
	along with UGS. If not, see <http://www.gnu.org/licenses/>.
*/
package com.willwinder.universalgcodesender.connection;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import javax.websocket.*;

@ClientEndpoint
public class WSConnection  extends AbstractConnection implements Connection  {
    private String host;
    private int port;
    private URI uri;
    private Session userSession;

    @Override
    public void setUri(String uri) {
        try {
            host = StringUtils.substringBetween(uri, ConnectionDriver.WS.getProtocol(), ":");
            port = Integer.parseInt(StringUtils.substringAfterLast(uri, ":"));
            this.uri = URI.create(
                    ConnectionDriver.WS.getProtocol()+
                            StringUtils.substringBetween(uri, ConnectionDriver.WS.getProtocol(), "/")+
                            ":"+port+"/"+
                            StringUtils.substringAfter(host, "/"));
        } catch (Exception e) {
            throw new ConnectionException("Couldn't parse connection string " + uri, e);
        }

        if (StringUtils.isEmpty(host)) {
            throw new ConnectionException("Empty host in connection string.");
        }
        if ((port < 1) || (port > 65535)) {
            throw new ConnectionException("Please ensure port is a port number between 1 and 65535.");
        }
    }

    @OnOpen
    public void onOpen(Session userSession) {
        this.userSession = userSession;
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        this.userSession = null;
    }

    @OnMessage
    public void onMessage(String message) {
        responseMessageHandler.handleResponse(message.getBytes(), 0, message.length());
    }

    @Override
    public boolean openPort() throws Exception {
        try {
            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            container.connectToServer(this, uri);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return true;
    }

    @Override
    public void closePort() throws Exception {
        this.userSession.close();
    }

    @Override
    public void sendByteImmediately(byte b) throws Exception {
        final OutputStream sendStream = this.userSession.getBasicRemote().getSendStream();
        sendStream.write(b);
        sendStream.flush();
    }

    @Override
    public void sendStringToComm(String command) throws Exception {
        this.userSession.getBasicRemote().sendBinary(ByteBuffer.wrap(command.getBytes(StandardCharsets.UTF_8)), true);
    }

    @Override
    public boolean isOpen() {
        return this.userSession != null && this.userSession.isOpen();
    }

    @Override
    public List<String> getPortNames() {
        return new ArrayList<>();
    }

    @Override
    public List<IConnectionDevice> getDevices() {
        return new ArrayList<>();
    }
}
