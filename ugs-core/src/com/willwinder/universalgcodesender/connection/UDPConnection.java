/*
	Copyright 2015-2018 Will Winder
	Copyright 2020- Andras Huszti

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

import java.io.OutputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.BufferedReader;
import java.net.*;
import java.net.Socket;

import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.ArrayList;

/**
 * A UDP connection object implementing the connection API.
 *
 * @author Andras Huszti
 * @author Adam Carmicahel <carneeki@carneeki.net>
 */
public class UDPConnection extends AbstractConnection implements Runnable, Connection {

    private DatagramSocket socket;
    private InetAddress address;
    private int port;
    private String receivedString = null;
    private static final Object locker = new Object();
    private Thread thread;
    private boolean isRunning;
	private int receptionTimeout = 250;

	private String host;
	
	@Override
	public void setUri(String uri) {
		try {
			/* Passed uri contains the protocol doubled*/
			/* Passed uri still contains baudrate, which must be ignored*/
			/*udp://udp://192.168.178.101/5123:1234*/
			this.host = StringUtils.substringAfter(uri, ConnectionDriver.UDP.getProtocol());
			this.host = StringUtils.substringBetween(this.host, ConnectionDriver.UDP.getProtocol(), "/");

			String portTemp = StringUtils.substringAfter(uri, ConnectionDriver.UDP.getProtocol());
			portTemp = StringUtils.substringAfter(portTemp, ConnectionDriver.UDP.getProtocol());
			portTemp = StringUtils.substringBetween(portTemp, "/" , ":");
			this.port = Integer.parseInt(portTemp);
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
        this.isRunning = true;
        this.thread = new Thread(this, "UDP_grbl_thread");/*Always create named threads for debugging*/
        try {
            this.address = InetAddress.getByName(this.host);
            try {
                this.port = port;
                this.socket = new DatagramSocket(this.port);
            } catch (SocketException ex) {
                this.closePort();
				throw new ConnectionException("openPort failed");
            }
        } catch (UnknownHostException ex) {
			throw new ConnectionException("openPort address failed");
        }
        this.thread.start();

		return true;
	}

	/**
	 * TODO: toggle the disconnect/connect icon; investigate how...
	 *       UGS correctly goes into offline state when called, potentially a bug elsewhere?
	 *       TODO not checked, so left also here, but comes from the TCP file originally
	 */
	@Override
	public void closePort() throws Exception {
        this.isRunning = false;
		if (this.thread != null) {
			this.thread.join(this.receptionTimeout * 2); /* Give a little bit time so that the thread can exit.*/
			/*At this point the thread should be already finished*/
			
			try {
				/* But to make sure it is finished we will interrupt this.
				   Make sure the thread is interrupted if reception would stuck. Should not happen*/
				this.thread.interrupt(); 
			} catch (Exception ex) {
				/* Since the thread is already finished this will trow an exception which is not intresing and will be thrown away.*/
			}
		}
        if (this.socket != null) {
            this.socket.close();
        }
	}

	@Override
	public boolean isOpen() {
		boolean result = false;
        if (this.socket != null) {
			result = true;
		}
		return result;
	}

	/**
	 * Sends a command to remote host.
	 * @param command Command to be sent to remote host.
	 */
	public void sendStringToComm(String command) throws Exception {
		if (command.length() <= 1492) {
			byte[] buf = command.getBytes();
			DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
			try {
				this.socket.send(packet);
			} catch (IOException ex) {
				throw new ConnectionException("sendStringToComm failed");
			}
		} else {
			throw new ConnectionException("sendStringToComm too long command");
		}
	}

	/**
	 * Immediately sends a byte, used for real-time commands.
	 */
	public void sendByteImmediately(byte b) throws Exception {
        byte[] buf = new byte[1];
		buf[0] = b;
        DatagramPacket packet = new DatagramPacket(buf, buf.length, this.address, this.port);
        try {
            this.socket.send(packet);
        } catch (IOException ex) {
			throw new ConnectionException("sendByteImmediately failed");
        }
	}

	/**
	 * Thread to accept data from remote host, and pass it to responseHandler
	 */
	public void run() {
        try {
            this.socket.setSoTimeout(this.receptionTimeout);
            while ((this.isRunning) && (!Thread.interrupted())) {
                byte [] buf = new byte[1492];/*Todo change to constant value*/
                DatagramPacket packet = new DatagramPacket(buf, buf.length);
                try {
                    this.socket.receive(packet);
                    String received = new String(packet.getData(), 0, packet.getLength());
					responseMessageHandler.handleResponse(received);
                } catch (IOException ex) {
					/*Timeout exception is not intresting*/
				}
            }
        } catch (Exception ex) {
        }
	}

	/**
	 * TODO: Currently returns an empty list. Finding and enumerating all
	 *       possible hosts on a network does not seem like a good idea. Ask
	 *       @winder if an empty list is acceptable.
	 * Example URI: udp://examplehost.local/9001
	 */
	@Override
	public List<String> getPortNames() {
		ArrayList<String> retval = new ArrayList<String>();
		retval.add("udp://192.168.178.101/5123");
		/*retval.add("192.168.178.101");*/
		return retval;
	}
}
