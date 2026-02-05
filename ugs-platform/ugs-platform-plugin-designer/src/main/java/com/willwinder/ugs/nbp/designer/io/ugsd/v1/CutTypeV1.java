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

import com.willwinder.ugs.nbp.designer.entities.cuttable.CutType;

/**
 * @author Joacim Breiler
 */
public enum CutTypeV1 {
    NONE,
    POCKET,
    SURFACE,
    ON_PATH,
    INSIDE_PATH,
    OUTSIDE_PATH,
    CENTER_DRILL,
    LASER_ON_PATH,
    LASER_FILL,
    LASER_RASTER;

    public static CutTypeV1 fromCutType(CutType cutType) {
        return switch (cutType) {
            case NONE -> NONE;
            case POCKET -> POCKET;
            case SURFACE -> SURFACE;
            case ON_PATH -> ON_PATH;
            case INSIDE_PATH -> INSIDE_PATH;
            case OUTSIDE_PATH -> OUTSIDE_PATH;
            case LASER_ON_PATH -> LASER_ON_PATH;
            case LASER_FILL -> LASER_FILL;
            case LASER_RASTER -> LASER_RASTER;
            case CENTER_DRILL -> CENTER_DRILL;
        };
    }

    public static CutType toCutType(CutTypeV1 cutType) {
        return switch (cutType) {
            case NONE -> CutType.NONE;
            case POCKET -> CutType.POCKET;
            case SURFACE -> CutType.SURFACE;
            case ON_PATH -> CutType.ON_PATH;
            case INSIDE_PATH -> CutType.INSIDE_PATH;
            case OUTSIDE_PATH -> CutType.OUTSIDE_PATH;
            case CENTER_DRILL -> CutType.CENTER_DRILL;
            case LASER_ON_PATH -> CutType.LASER_ON_PATH;
            case LASER_FILL ->CutType.LASER_FILL;
            case LASER_RASTER -> CutType.LASER_RASTER;
        };
    }
}

