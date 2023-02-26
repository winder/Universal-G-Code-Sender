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

/**
 * An abstract connection device implementation
 *
 * @author Joacim Breiler
 */
public abstract class AbstractConnectionDevice implements IConnectionDevice {
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof IConnectionDevice)) return false;
        IConnectionDevice that = (IConnectionDevice) o;

        return StringUtils.equals(getAddress(), that.getAddress());
    }

    @Override
    public int hashCode() {
        return getAddress().hashCode();
    }

    @Override
    public String toString() {
        return getAddress();
    }
}
