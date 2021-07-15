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
    ON_PATH,
    INSIDE_PATH,
    OUTSIDE_PATH;

    public static CutTypeV1 fromCutType(CutType cutType) {
        if (cutType == CutType.POCKET) {
            return POCKET;
        } else if (cutType == CutType.ON_PATH) {
            return ON_PATH;
        } else if (cutType == CutType.INSIDE_PATH) {
            return INSIDE_PATH;
        } else if (cutType == CutType.OUTSIDE_PATH) {
            return OUTSIDE_PATH;
        } else {
            return NONE;
        }
    }

    public static CutType toCutType(CutTypeV1 cutType) {
        if (cutType == POCKET) {
            return CutType.POCKET;
        } else if (cutType == ON_PATH) {
            return CutType.ON_PATH;
        } else if (cutType == INSIDE_PATH) {
            return CutType.INSIDE_PATH;
        } else if (cutType == OUTSIDE_PATH) {
            return CutType.OUTSIDE_PATH;
        } else {
            return CutType.NONE;
        }
    }
}

