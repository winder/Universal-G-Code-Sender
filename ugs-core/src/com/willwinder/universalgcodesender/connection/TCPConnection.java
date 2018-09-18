/*
	Copyright 2015-2018 Will Winder

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

import java.nio.channels.SocketChannel;
import java.nio.ByteBuffer;
import java.io.IOException;
import java.net.*;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Arrays;
import java.util.ArrayList;

/**
 * A serial connection object implementing the connection API.
 *
 * @author wwinder
 */
public class TCPConnection extends AbstractConnection implements Runnable {

	private String host;
	private int port;

	// General variables
	private SocketChannel client;
	private ByteBuffer bufIn;
	private ByteBuffer bufOut;
	private Thread replyThread;
	private ResponseMessageHandler responseMessageHandler;

	@Override
	public void setUri(String uri) {
		try {
			host = StringUtils.substringBetween(uri, ConnectionDriver.TCP.getProtocol(), ":");
			port = Integer.valueOf(StringUtils.substringAfterLast(uri, ":"));
		} catch (Exception e) {
			throw new ConnectionException("Couldn't parse connection string " + uri, e);
		}
	}

	// TODO: ask about renaming the openPort() method to openConnection()
	@Override
	public boolean openPort() throws Exception {
		if (StringUtils.isEmpty(host)) {
			throw new ConnectionException("Empty host in connection string.");
		}
		if ((port >= 1) && (port <= 65535)) {
			throw new ConnectionException("Please ensure port is a port number between 1 and 65535.");
		}

		responseMessageHandler = new ResponseMessageHandler();

		try{
			client = SocketChannel.open(new InetSocketAddress(InetAddress.getByName(host), port));
		} catch (IOException e) {
			e.printStackTrace();
		}

		bufOut = ByteBuffer.allocate(256);
		bufIn = ByteBuffer.allocate(256);

		if (client == null) {
			throw new ConnectionException("Socket unable to connect.");
		}

		// start thread so replies can be handled
		replyThread = new Thread(this);
		replyThread.start();

		return client.isConnected();
	}

	// TODO: ask about renaming the openPort() method to openConnection()
	@Override
	public void closePort() throws Exception {
		if (client != null) {
			try {
				replyThread.stop();
				client.close();

				if (client.isConnected()) {
					replyThread.stop();
					client.close();
				}
			} finally {
				client = null;
			}
		}
	}

	@Override
	public boolean isOpen() {
		return client != null && client.isConnected();
	}

	/**
	 * Sends a command to remote host.
	 * @param command Command to be sent to serial device.
	 */
	public void sendStringToComm(String command) throws Exception {
		bufOut.put(command.getBytes());
		client.write(bufOut);
		bufOut.clear();
	}

	/**
	 * Immediately sends a byte, used for real-time commands.
	 */
	public void sendByteImmediately(byte b) throws Exception {
		bufOut.put(b);
		client.write(bufOut);
		bufOut.clear();
	}

	/**
	 * Reads data from the remote host.
	 */
	public void run() {
		try {
			if(client.read(bufIn) != 0)
			{
				responseMessageHandler.handleResponse(bufIn.toString(),comm);
			}
		} catch ( Exception e ) {
			e.printStackTrace();
			System.exit(-1);
		}
	}

	// TODO: maybe implement this better?
	@Override
	public List<String> getPortNames() {
		ArrayList<String> retval = new ArrayList<String>();
		return retval;
	}
}
