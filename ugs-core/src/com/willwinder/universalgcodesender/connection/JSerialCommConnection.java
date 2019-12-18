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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Serial connection using JSerialComm
 *
 * @author Joacim Breiler
 */
public class JSerialCommConnection extends AbstractConnection implements SerialPortDataListener {

    private final byte[] buffer = new byte[1024];
    private SerialPort serialPort;

    @Override
    public void setUri(String uri) {
        try {
            String portName = StringUtils.substringBetween(uri, ConnectionDriver.JSERIALCOMM.getProtocol(), ":");
            int baudRate = Integer.valueOf(StringUtils.substringAfterLast(uri, ":"));
            initSerialPort(portName, baudRate);
        } catch (Exception e) {
            throw new ConnectionException("Couldn't parse connection string " + uri, e);
        }
    }

    @Override
    public boolean openPort() throws Exception {
        if (serialPort == null) {
            throw new ConnectionException("The connection wasn't initialized");
        }

        return serialPort.openPort();
    }

    private void initSerialPort(String name, int baud) throws Exception {
        if (serialPort != null && serialPort.isOpen()) {
            closePort();
        }

        serialPort = SerialPort.getCommPort(name);
        serialPort.setParity(SerialPort.NO_PARITY);
        serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        serialPort.setNumDataBits(8);
        serialPort.addDataListener(this);
        serialPort.setBaudRate(baud);
    }

    @Override
    public void closePort() throws Exception {
        if (serialPort != null) {
            serialPort.removeDataListener();
            serialPort.closePort();
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
        return serialPort.isOpen();
    }

    @Override
    public List<String> getPortNames() {
        return Arrays.stream(SerialPort.getCommPorts())
                .map(SerialPort::getSystemPortName)
                .collect(Collectors.toList());
    }


    @Override
    public int getListeningEvents() {
        return SerialPort.LISTENING_EVENT_DATA_AVAILABLE;
    }

    @Override
    public void serialEvent(com.fazecast.jSerialComm.SerialPortEvent event) {
        if (event.getEventType() != SerialPort.LISTENING_EVENT_DATA_AVAILABLE) {
            return;
        }

        int bytesAvailable = serialPort.bytesAvailable();
        if (bytesAvailable <= 0) {
            return;
        }

        int bytesRead = serialPort.readBytes(buffer, Math.min(buffer.length, bytesAvailable));
        String response = new String(buffer, 0, bytesRead);

        responseMessageHandler.handleResponse(response);
    }
}
