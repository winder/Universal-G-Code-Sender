/*
    Copyright 2018 Will Winder

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

import com.fazecast.jSerialComm.SerialPort;
import com.fazecast.jSerialComm.SerialPortDataListener;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * Serial connection using JSerialComm
 *
 * @author Joacim Breiler
 */
public class JSerialCommConnection extends AbstractConnection implements SerialPortDataListener {

    private SerialPort serialPort;

    public JSerialCommConnection() {
        // Empty implementation
    }

    public JSerialCommConnection(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    @Override
    public void setUri(String uri) {
        try {
            String portName = StringUtils.substringBetween(uri, ConnectionDriver.JSERIALCOMM.getProtocol(), ":");
            int baudRate = Integer.parseInt(StringUtils.substringAfterLast(uri, ":"));
            initSerialPort(portName, baudRate);
        } catch (ConnectionException e) {
            throw e;
        } catch (Exception e) {
            throw new ConnectionException("Couldn't parse connection string " + uri, e);
        }
    }

    @Override
    public boolean openPort() throws Exception {
        if (serialPort == null) {
            throw new ConnectionException("The connection wasn't initialized");
        }

        if (serialPort.isOpen()) {
            throw new ConnectionException("Can not connect, serial port is already open");
        }

        return serialPort.openPort();
    }

    private void initSerialPort(String name, int baud) throws Exception {
        if (serialPort != null && serialPort.isOpen()) {
            closePort();
        }

        serialPort = SerialPort.getCommPort(name);
        checkPermissions();

        serialPort.setParity(SerialPort.NO_PARITY);
        serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        serialPort.setNumDataBits(8);
        serialPort.addDataListener(this);
        serialPort.setBaudRate(baud);
    }

    private void checkPermissions() {
        if (!SystemUtils.IS_OS_LINUX) {
            return;
        }

        File port = new File(serialPort.getSystemPortPath());
        if (!port.canWrite() || !port.canRead() ) {
            throw new ConnectionException("Do not have required permissions to open the device on " + serialPort.getSystemPortPath());
        }
    }

    @Override
    public void closePort() throws Exception {
        if (serialPort != null) {
            serialPort.removeDataListener();
            serialPort.closePort();
            serialPort = null;
        }
    }

    @Override
    public void sendByteImmediately(byte b) throws Exception {
        serialPort.writeBytes(new byte[]{b}, 1);
    }

    @Override
    public void sendStringToComm(String command) throws Exception {
        serialPort.writeBytes(command.getBytes(), command.length());
    }

    @Override
    public boolean isOpen() {
        return serialPort != null && serialPort.isOpen();
    }

    @Override
    public List<? extends IConnectionDevice> getDevices() {
        return Arrays.stream(SerialPort.getCommPorts())
                .map(JSerialCommConnectionDevice::new)
                .toList();
    }

    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE | SerialPort.LISTENING_EVENT_PORT_DISCONNECTED;
    }

    @Override
    public void serialEvent(com.fazecast.jSerialComm.SerialPortEvent event) {
        switch (event.getEventType()) {
            case SerialPort.LISTENING_EVENT_PORT_DISCONNECTED -> {
                try {
                    connectionListenerManager.onConnectionClosed();
                } catch (Exception e) {
                    // Never mind
                }
            }
            case SerialPort.LISTENING_EVENT_DATA_AVAILABLE -> {
                byte[] newData = new byte[serialPort.bytesAvailable()];
                int numRead = serialPort.readBytes(newData, newData.length);
                getConnectionListenerManager().handleResponse(newData, 0, numRead);
            }
            default -> {
                // Never mind
            }
        }
    }
}
