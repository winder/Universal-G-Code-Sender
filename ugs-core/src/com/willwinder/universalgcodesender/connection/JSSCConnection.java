/*
    Copyright 2015-2018 Will Winder

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

import jssc.SerialPort;
import jssc.SerialPortEvent;
import jssc.SerialPortEventListener;
import jssc.SerialPortList;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.List;

/**
 * A serial connection object implementing the connection API.
 *
 * @author wwinder
 */
public class JSSCConnection extends AbstractConnection implements SerialPortEventListener {

    private int baudRate;
    private String portName;

    // General variables
    private SerialPort serialPort;

    @Override
    public void setUri(String uri) {
        try {
            portName = StringUtils.substringBetween(uri, ConnectionDriver.JSSC.getProtocol(), ":");
            baudRate = Integer.valueOf(StringUtils.substringAfterLast(uri, ":"));
        } catch (Exception e) {
            throw new ConnectionException("Couldn't parse connection string " + uri, e);
        }
    }

    @Override
    public boolean openPort() throws Exception {
        if (StringUtils.isEmpty(portName) || baudRate == 0) {
            throw new ConnectionException("Couldn't open port " + portName + " using baud rate " + baudRate);
        }
        this.serialPort = new SerialPort(portName);
        this.serialPort.openPort();
        this.serialPort.setParams(baudRate, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE, true, true);
        this.serialPort.addEventListener(this);

        if (this.serialPort == null) {
            throw new ConnectionException("Serial port not found.");
        }
        return serialPort.isOpened();
    }

    @Override
    public void closePort() throws Exception {
        if (this.serialPort != null) {
            try {
                this.serialPort.removeEventListener();

                if (this.serialPort.isOpened()) {
                    this.serialPort.closePort();
                }
            } finally {
                this.serialPort = null;
            }
        }
    }

    @Override
    public boolean isOpen() {
        return serialPort != null && serialPort.isOpened();
    }

    @Override
    public List<String> getPortNames() {
        return Arrays.asList(SerialPortList.getPortNames());
    }

    /**
     * Sends a command to the serial device. This actually streams the bits to
     * the comm port.
     * @param command   Command to be sent to serial device.
     */
    @Override
    public void sendStringToComm(String command) throws Exception {
        this.serialPort.writeString(command);
    }
        
    /**
     * Immediately sends a byte, used for real-time commands.
     */
    @Override
    public void sendByteImmediately(byte b) throws Exception {
        this.serialPort.writeByte(b);
    }
    
    /**
     * Reads data from the serial port. RXTX SerialPortEventListener method.
     */
    @Override
    public void serialEvent(SerialPortEvent evt) {
        try {
            byte[] buf = this.serialPort.readBytes();
            if (buf == null || buf.length <= 0) {
                return;
            }

            String s = new String(buf, 0, buf.length);
            responseMessageHandler.handleResponse(s);
        } catch ( Exception e ) {
            e.printStackTrace();
            System.exit(-1);
        }
    }
}

