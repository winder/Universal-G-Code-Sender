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
    private final StringBuilder inputBuffer = new StringBuilder();
    private SerialPort serialPort;

    @Override
    public boolean openPort(String name, int baud) throws Exception {
        if (serialPort != null && serialPort.isOpen()) {
            closePort();
        }

        serialPort = SerialPort.getCommPort(name);
        serialPort.setParity(SerialPort.NO_PARITY);
        serialPort.setNumStopBits(SerialPort.ONE_STOP_BIT);
        serialPort.setNumDataBits(8);
        serialPort.addDataListener(this);
        serialPort.setBaudRate(baud);
        return serialPort.openPort();
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
        if (bytesAvailable > 0) {
            int bytesRead = serialPort.readBytes(buffer, Math.min(buffer.length, bytesAvailable));
            String s = new String(buffer, 0, bytesRead);
            inputBuffer.append(s);

            // Check for line terminator and split out command(s).
            if (inputBuffer.toString().contains(comm.getLineTerminator())) {

                // Split with the -1 option will give an empty string at
                // the end if there is a terminator there as well.
                String[] commands = inputBuffer.toString().split(comm.getLineTerminator(), -1);
                for (int i = 0; i < commands.length; i++) {
                    // Make sure this isn't the last command.
                    if ((i + 1) < commands.length) {
                        comm.responseMessage(commands[i]);

                        // Append last command to input buffer because it didn't have a terminator.
                    } else {
                        inputBuffer.setLength(0);
                        inputBuffer.append(commands[i]);
                    }
                }
            }
        }
    }
}
