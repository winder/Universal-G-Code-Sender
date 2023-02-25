/*
	Copyright 2015-2023 Will Winder

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

import com.willwinder.universalgcodesender.services.MdnsService;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ConnectException;
import java.net.NoRouteToHostException;
import java.net.Socket;
import java.net.SocketException;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * A TCP connection object implementing the connection API.
 *
 * @author Adam Carmicahel <carneeki@carneeki.net>
 */
public class TCPConnection extends AbstractConnection implements Runnable, Connection {

    private static final Logger LOGGER = Logger.getLogger(TCPConnection.class.getSimpleName());
    private static final String MDNS_SERVICE = "_telnet._tcp.local.";
    private String host;
    private int port;
    // General variables
    private Socket client;
    private OutputStream bufOut;
    private BufferedInputStream inStream;
    private Thread replyThread;

    TCPConnection() {
        MdnsService.getInstance().registerListener(MDNS_SERVICE);
    }

    @Override
    public void setUri(String uri) {
        try {
            host = StringUtils.substringBetween(uri, ConnectionDriver.TCP.getProtocol(), ":");
            port = Integer.parseInt(StringUtils.substringAfterLast(uri, ":"));
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

    @Override
    public boolean openPort() throws Exception {
        try {
            client = new Socket(host, port);
        } catch (BindException e) {
            throw new ConnectionException("Could not bind a local port.", e);
        } catch (NoRouteToHostException e) {
            throw new ConnectionException("No route to host. The remote host may not be running, blocked by a firewall, or disconnected.", e);
        } catch (ConnectException e) {
            throw new ConnectionException("The port is unreachable on the remote host. The server may not be running, or blocked by a firewall.", e);
        }

        bufOut = client.getOutputStream();
        inStream = new BufferedInputStream(client.getInputStream());

        // start thread so replies can be handled
        replyThread = new Thread(this);
        replyThread.start();

        return client.isConnected();
    }

    /**
     * TODO: toggle the disconnect/connect icon; investigate how...
     *       UGS correctly goes into offline state when called, potentially a bug elsewhere?
     */
    @Override
    public void closePort() throws Exception {
        if (client != null) {
            try {
                replyThread.interrupt();
                client.close();
            } catch (SocketException e) {
                // ignore socketexception if connection was broken early
            } finally {
                client = null;
            }
        }
    }

    @Override
    public boolean isOpen() {
        return (client != null) && (!client.isClosed());
    }

    /**
     * Sends a command to remote host.
     *
     * @param command Command to be sent to remote host.
     */
    public void sendStringToComm(String command) throws Exception {
        try {
            bufOut.write(command.getBytes());
            bufOut.flush();
        } catch (IOException e) {
            closePort(); // very likely we got disconnected, attempt to disconnect gracefully
            throw e;
        }
    }

    /**
     * Immediately sends a byte, used for real-time commands.
     */
    public void sendByteImmediately(byte b) throws Exception {
        try {
            bufOut.write(b);
            bufOut.flush();
        } catch (IOException e) {
            closePort(); // very likely we got disconnected, attempt to disconnect gracefully
            throw e;
        }
    }

    /**
     * Thread to accept data from remote host, and pass it to responseHandler
     */
    public void run() {
        byte[] buffer = new byte[1024];
        while (!Thread.interrupted() && !client.isClosed()) {
            try {
                int readBytes = inStream.read(buffer);
                if (readBytes > 0) {
                    responseMessageHandler.handleResponse(buffer, 0, readBytes);
                }
            } catch (IOException e) {
                LOGGER.info("Got a socket exception: " + e.getMessage());
                return; // terminate thread if disconnected
            }
        }
    }

    @Override
    public List<String> getPortNames() {
        return getDevices().stream()
                .map(IConnectionDevice::getAddress)
                .collect(Collectors.toList());
    }

    @Override
    public List<IConnectionDevice> getDevices() {
        return MdnsService.getInstance().getServices(MDNS_SERVICE).stream()
                .map(service -> new DefaultConnectionDevice(service.getHost(), service.getPort(), service.getName()))
                .collect(Collectors.toList());
    }
}
