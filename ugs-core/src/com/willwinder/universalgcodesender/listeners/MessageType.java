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
package com.willwinder.universalgcodesender.listeners;

import com.willwinder.universalgcodesender.i18n.Localization;

/**
 * A type for describing a message verbosity.
 *
 * @author wwinder
 */
public enum MessageType {
    VERBOSE("verbose"),
    INFO("info"),
    ERROR("error");

    private final String key;

    MessageType(String key) {
        this.key = key;
    }

    public String getLocalizedString() {
        return Localization.getString(key);
    }
}
