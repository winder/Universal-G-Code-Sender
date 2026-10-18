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

import com.willwinder.ugs.designer.entities.cuttable.ToolPathDirection;

/**
 * @author Joacim Breiler
 */
public enum ToolPathDirectionTypeV1 {
    /**
     * Legacy value from designs saved before the direction of travel could be chosen,
     * kept so that those files can still be read.
     */
    HORIZONTAL,
    /**
     * Legacy value from designs saved before the direction of travel could be chosen,
     * kept so that those files can still be read.
     */
    VERTICAL,
    LEFT_RIGHT,
    RIGHT_LEFT,
    TOP_BOTTOM,
    BOTTOM_TOP;

    public static ToolPathDirectionTypeV1 fromDirection(ToolPathDirection direction) {
        return switch (direction) {
            case LEFT_RIGHT -> LEFT_RIGHT;
            case RIGHT_LEFT -> RIGHT_LEFT;
            case TOP_BOTTOM -> TOP_BOTTOM;
            case BOTTOM_TOP -> BOTTOM_TOP;
        };
    }

    public static ToolPathDirection toDirection(ToolPathDirectionTypeV1 direction) {
        if (direction == null) {
            return ToolPathDirection.LEFT_RIGHT;
        }

        return switch (direction) {
            case HORIZONTAL, LEFT_RIGHT -> ToolPathDirection.LEFT_RIGHT;
            case RIGHT_LEFT -> ToolPathDirection.RIGHT_LEFT;
            case VERTICAL, TOP_BOTTOM -> ToolPathDirection.TOP_BOTTOM;
            case BOTTOM_TOP -> ToolPathDirection.BOTTOM_TOP;
        };
    }
}
