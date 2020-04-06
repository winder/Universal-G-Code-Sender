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

import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * A factory for creating a serial connection object using the settings
 *
 * @author wwinder
 */
public class ConnectionFactory {

    /**
     * Returns a new instance of a connection from an uri. The uri should be start with a protocol
     * {@link ConnectionDriver#getProtocol()} that defines which driver to use. The driver may then
     * have different styles for defining paths, ex: jserialcomm://{portname}:{baudrate}
     *
     * @param uri the uri for the hardware to connect to
     * @return a connection
     * @throws ConnectionException if something went wron while creating the connection
     */
    static public Connection getConnection(String uri) throws ConnectionException{
        for (ConnectionDriver connectionDriver : ConnectionDriver.values()) {
            if (StringUtils.startsWithIgnoreCase(uri, connectionDriver.getProtocol())) {
                Connection connection = getConnection(connectionDriver).orElseThrow(() -> new ConnectionException("Couldn't load connection driver " + connectionDriver + " for uri: " + uri));
                connection.setUri(uri);
                return connection;
            }
        }

        throw new ConnectionException("Couldn't find connection driver for uri: " + uri);
    }

    public static List<String> getPortNames(ConnectionDriver connectionDriver) {
        return getConnection(connectionDriver)
                .map(Connection::getPortNames)
                .orElseGet(Collections::emptyList);
    }

    public static Optional<Connection> getConnection(ConnectionDriver connectionDriver) {
        switch (connectionDriver) {
            case JSERIALCOMM:
                return Optional.of(new JSerialCommConnection());
            case JSSC:
                return Optional.of(new JSSCConnection());
            case TCP:
                return Optional.of(new TCPConnection());
            case WS:
                return Optional.of(new WSConnection());
        }
        return Optional.empty();
    }
}
