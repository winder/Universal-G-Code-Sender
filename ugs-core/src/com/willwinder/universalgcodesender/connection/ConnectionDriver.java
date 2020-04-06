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

import java.util.HashMap;
import java.util.Map;

/**
 * An enum for describing the connection drivers available to the system.
 *
 * @author Joacim Breiler
 */
public enum ConnectionDriver {
    JSERIALCOMM("JSerialComm", "jserialcomm://"),
    JSSC("JSSC", "jssc://"),
    TCP("TCP", "tcp://"),
    WS("WebSocket", "ws://");

    private final String prettyName;
    private final String protocol;

    ConnectionDriver(String prettyName, String protocol) {
        this.prettyName = prettyName;
        this.protocol = protocol;
    }

    public String getPrettyName() {
        return prettyName;
    }

    public String getProtocol() {
        return protocol;
    }

    public static String[] getPrettyNames() {
        ConnectionDriver[] vals = values();
        String[] res = new String[vals.length];
        for(int i = 0; i < vals.length; ++i) {
            res[i] = vals[i].getPrettyName();
        }
        return res;
    }

    public static ConnectionDriver prettyNameToEnum(String prettyName) {
        return prettyName2Enum.get(prettyName);
    }

    private static Map<String, ConnectionDriver> prettyName2Enum = new HashMap<>();
    static {
        for(ConnectionDriver v : values()) {
            prettyName2Enum.put(v.getPrettyName(), v);
        }
    }
}
