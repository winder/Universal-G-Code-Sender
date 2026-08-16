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
package com.willwinder.ugs.designer.io.ugsd.v1;

import com.willwinder.ugs.designer.entities.cuttable.PlungeType;

/**
 * @author Joacim Breiler
 */
public enum PlungeTypeV1 {
    STRAIGHT,
    LINEAR_RAMP;

    public static PlungeTypeV1 fromPlungeType(PlungeType plungeType) {
        return switch (plungeType) {
            case STRAIGHT -> STRAIGHT;
            case LINEAR_RAMP -> LINEAR_RAMP;
        };
    }

    public static PlungeType toPlungeType(PlungeTypeV1 plungeType) {
        if (plungeType == null) {
            return PlungeType.STRAIGHT;
        }

        return switch (plungeType) {
            case STRAIGHT -> PlungeType.STRAIGHT;
            case LINEAR_RAMP -> PlungeType.LINEAR_RAMP;
        };
    }
}
