/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.designer.io.ugsd.v1;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Direction;

/**
 * @author Joacim Breiler
 */
public enum DirectionTypeV1 {
    CLIMB,
    CONVENTIONAL;

    public static DirectionTypeV1 fromDirection(Direction direction) {
        return switch (direction) {
            case CLIMB -> CLIMB;
            case CONVENTIONAL -> CONVENTIONAL;
        };
    }

    public static Direction toDirection(DirectionTypeV1 direction) {
        if (direction == null) {
            return Direction.CLIMB;
        }

        return switch (direction) {
            case CLIMB -> Direction.CLIMB;
            case CONVENTIONAL -> Direction.CONVENTIONAL;
        };
    }
}

