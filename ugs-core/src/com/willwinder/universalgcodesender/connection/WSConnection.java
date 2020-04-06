package com.willwinder.universalgcodesender.connection;

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
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
            port = Integer.valueOf(StringUtils.substringAfterLast(uri, ":"));
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
    public void onOpen(Session userSession) throws IOException {
        this.userSession = userSession;
    }

    @OnClose
    public void onClose(Session userSession, CloseReason reason) {
        this.userSession = null;
    }

    @OnMessage
    public void onMessage(String message) throws IOException {
        responseMessageHandler.handleResponse(message + "\n");
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
        this.userSession.getBasicRemote().sendBinary(ByteBuffer.wrap(command.getBytes("UTF-8")), true);
    }

    @Override
    public boolean isOpen() {
        return this.userSession != null && this.userSession.isOpen();
    }

    @Override
    public List<String> getPortNames() {
        return new ArrayList<>();
    }
}
