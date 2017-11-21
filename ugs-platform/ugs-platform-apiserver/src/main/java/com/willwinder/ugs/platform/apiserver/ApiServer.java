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
package com.willwinder.ugs.platform.apiserver;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.Configuration;
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
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.glxn.qrgen.QRCode;
import net.glxn.qrgen.image.ImageType;

public class ApiServer implements UGSEventListener, ControllerStateListener {

    private static final Logger logger = Logger.getLogger(ApiServer.class.getName());

    private final BackendAPI backend;
    private final int port = 9988;
    private Configuration config = null;
    private SocketIOServer server = null;

    public ApiServer() {
        config = new Configuration();
        config.setPort(port);
        server = new SocketIOServer(config);
        
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this);
        backend.addControllerStateListener(this);
    }

    public void start() {
        server.addEventListener("command", String.class, new CommandHandler());
        
        server.start();
        
        logger.log(Level.INFO, "Pendant server is running at {0}", getAddress());
    }

    public void stop() {
       server.stop();
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        server.getBroadcastOperations().sendEvent("state", buildStateString(evt.getControllerStatus()));
    }

    private String buildStateString(ControllerStatus cs) {
        Map<String, Object> obj = new HashMap<>();

        obj.put("availableCommands", ApiServerCommand.class.getEnumConstants());
        obj.put("controllerStatus", cs);
        obj.put("settings", backend.getSettings());

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
