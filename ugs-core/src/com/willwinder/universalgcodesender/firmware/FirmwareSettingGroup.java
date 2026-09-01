/*
    Copyright 2026 Will Winder

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
package com.willwinder.universalgcodesender.firmware;

import java.util.List;

/**
 * A named group of firmware settings, used for presenting the settings in sections.
 *
 * @param name     the name of the group. Settings that the controller has not placed in a group are
 *                 collected in a group with an empty name, which should be presented without a heading
 * @param settings the settings belonging to the group, ordered by their key
 * @author Joacim Breiler
 */
public record FirmwareSettingGroup(String name, List<FirmwareSetting> settings) {
    public boolean hasName() {
        return name != null && !name.isEmpty();
    }
}
