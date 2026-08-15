/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.ugs.designer.entities.cuttable;

import com.willwinder.universalgcodesender.i18n.Localization;

/**
 * How the tool engages the material when moving down to the next depth of cut.
 *
 * @author Joacim Breiler
 */
public enum PlungeType {
    /**
     * Moves straight down into the material
     */
    STRAIGHT(Localization.getString("platform.plugin.designer.plunge-type.straight")),

    /**
     * Gradually moves down into the material along the beginning of the tool path
     */
    LINEAR_RAMP(Localization.getString("platform.plugin.designer.plunge-type.linear-ramp"));

    private final String label;

    PlungeType(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
