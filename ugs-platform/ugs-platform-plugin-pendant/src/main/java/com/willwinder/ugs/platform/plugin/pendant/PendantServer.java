/**
 * The pendant server uses web sockets to communicate with a web client.
 * It listens to UGSEvents to send state down to the client and listens from
 * the client for commands to execute.
 */

/*
    Copywrite 2016 Will Winder

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
package com.willwinder.ugs.platform.plugin.pendant;

import com.google.gson.Gson;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.listeners.ControllerStateListener;
import com.willwinder.universalgcodesender.listeners.ControllerStatus;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import java.io.ByteArrayOutputStream;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;
import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;
import org.webbitserver.BaseWebSocketHandler;
import org.webbitserver.WebServer;
import org.webbitserver.WebServers;
import org.webbitserver.WebSocketConnection;
import org.webbitserver.WebSocketHandler;

public class PendantServer extends BaseWebSocketHandler implements UGSEventListener, ControllerStateListener {

    private static final Logger logger = Logger.getLogger(PendantServer.class.getName());

    private final BackendAPI backend;
    private int port = 9988;

    private WebServer server;
    private final List<WebSocketConnection> connections = new ArrayList<>();
    private final PendantCommandHandler commandHandler;

    public PendantServer() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this);
        backend.addControllerStateListener(this);
        commandHandler = new PendantCommandHandler();
    }

    /**
     * When a new web socket connection from a client is established.
     *
     * @param connection
     */
    @Override
    public void onOpen(WebSocketConnection connection) {
        connections.add(connection);
        connection.send(buildStateString(null));
    }

    /**
     * When a client terminates a connection.
     *
     * @param connection
     */
    @Override
    public void onClose(WebSocketConnection connection) {
        logger.info("Pendant client disconnected");
    }

    /**
     * When a client sends a web socket message.
     *
     * @param connection
     * @param message
     */
    @Override
    public void onMessage(WebSocketConnection connection, String message) {
        commandHandler.handleCommand(message);
    }

    /**
     * Called by the GUI Component to start the server.
     *
     * @throws Exception
     */
    public void start() throws Exception {
        server = WebServers.createWebServer(port);

//        server.add("/", new StaticFileHandler("./"));
        server.add("/ws", this);
        server.start();

        logger.info("Pendant server is running at " + getAddress());
    }

    /**
     * Called by the GUI Component to stop the server.
     */
    public void stop() {
        server.stop();
    }

    /**
     * Send a message to all the current connections.
     *
     * @param data
     */
    public void broadcast(String data) {
        connections.stream().forEach(conn -> conn.send(data));
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        broadcast(buildStateString(evt.getControllerStatus()));
    }

    /**
     * Sends the current UGS state down to the clients.
     */
    private String buildStateString(ControllerStatus cs) {
        Map<String, Object> obj = new HashMap<>();

        obj.put("availableCommands", PendantCommand.class.getEnumConstants());
        obj.put("controllerStatus", cs);
        obj.put("settings", backend.getSettings());
        obj.put("isManualControlEnabled", isManualControlEnabled());

        if (backend.getController() != null) {
            Map<String, Object> fileObj = new HashMap<>();
            obj.put("fileState", fileObj);
            fileObj.put("file", backend.getGcodeFile());
            fileObj.put("numRows", backend.getNumRows());
            fileObj.put("numSentRows", backend.getNumSentRows());
            fileObj.put("numRemainingRows", backend.getNumRemainingRows());
            fileObj.put("sendDuration", backend.getSendDuration());
            fileObj.put("sendRemainingDuration", backend.getSendRemainingDuration());
        }

        String json = new Gson().toJson(obj);
        return json;
    }

    private boolean isManualControlEnabled() {
        switch (backend.getControlState()) {
            case COMM_DISCONNECTED:
                return false;
            case COMM_IDLE:
                return true;
            case COMM_SENDING:
                return false;
            default:
                return true;
        }
    }

    public String getPendantClientAddress() {
        return "http://ugs-pendant.s3-website-us-east-1.amazonaws.com/#" + getAddress();
    }

    public byte[] getQr() {
        String url = getPendantClientAddress();
        ByteArrayOutputStream bout = QRCode.from(url).to(ImageType.PNG).stream();
        return bout.toByteArray();
    }

    public String getAddress() {
        String url = "";

        Enumeration<NetworkInterface> networkInterfaceEnum;
        try {
            networkInterfaceEnum = NetworkInterface.getNetworkInterfaces();

            while (networkInterfaceEnum.hasMoreElements()) {
                NetworkInterface networkInterface = networkInterfaceEnum.nextElement();

                Enumeration<InetAddress> addressEnum = networkInterface.getInetAddresses();
                while (addressEnum.hasMoreElements()) {
                    InetAddress addr = addressEnum.nextElement();
                    String hostAddress = addr.getHostAddress();
                    if (!hostAddress.contains(":")
                            && !"127.0.0.1".equals(hostAddress)) {
                        url += hostAddress + ":" + port;
                    }
                }
            }
        } catch (SocketException e) {
            logger.warning("Error getting network interfaces");
        }
        return url;
    }

}
