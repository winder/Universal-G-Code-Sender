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

import org.apache.commons.lang3.StringUtils;

import java.util.Optional;

/**
 * A default connection device implementation
 *
 * @author Joacim Breiler
 */
public class DefaultConnectionDevice extends AbstractConnectionDevice {
    private final String address;
    private final Integer port;
    private final String description;

    public DefaultConnectionDevice(String address) {
        this(address, null, "");
    }

    public DefaultConnectionDevice(String address, Integer port, String description) {
        this.address = address;
        this.port = port;
        this.description = description;
    }

    @Override
    public String getAddress() {
        return this.address;
    }

    @Override
    public Optional<Integer> getPort() {
        return Optional.ofNullable(port);
    }

    @Override
    public Optional<String> getDescription() {
        if (StringUtils.isEmpty(description)) {
            return Optional.empty();
        }
        return Optional.of(description);
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
