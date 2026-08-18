/*
    Copyright 2026 Maykol Rey

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
package com.willwinder.ugs.designer.model;

import com.willwinder.universalgcodesender.gcode.util.Code;

/**
 * How a generated program should turn its coolant on. Turning it off is always {@link Code#M9},
 * which is why only the start code varies.
 */
public enum CoolantMode {
    NONE("No coolant", null),
    FLOOD("Flood (M8)", Code.M8),
    MIST("Mist (M7)", Code.M7);

    private final String displayName;
    private final Code startCode;

    CoolantMode(String displayName, Code startCode) {
        this.displayName = displayName;
        this.startCode = startCode;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * @return the code that turns the coolant on, or null when there is nothing to turn on
     */
    public Code getStartCode() {
        return startCode;
    }

    public boolean isEnabled() {
        return startCode != null;
    }

    /**
     * The combo boxes in the tool settings dialogs render their values with {@code toString()}.
     * Settings are persisted using {@code name()}, so this is safe to change.
     */
    @Override
    public String toString() {
        return displayName;
    }
}
