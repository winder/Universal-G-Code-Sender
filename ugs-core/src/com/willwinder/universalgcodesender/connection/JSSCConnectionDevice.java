/*
    Copyright 2023 Will Winder

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

import java.util.Optional;

/**
 * A connection device based on a JSSC serial port.
 *
 * @author Joacim Breiler
 */
public class JSSCConnectionDevice extends AbstractConnectionDevice implements IConnectionDevice {
    private final SerialPort serialPort;

    public JSSCConnectionDevice(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    @Override
    public String getAddress() {
        return serialPort.getPortName();
    }

    @Override
    public Optional<String> getDescription() {
        return Optional.empty();
    }

    @Override
    public Optional<Integer> getPort() {
        return Optional.empty();
    }
}
