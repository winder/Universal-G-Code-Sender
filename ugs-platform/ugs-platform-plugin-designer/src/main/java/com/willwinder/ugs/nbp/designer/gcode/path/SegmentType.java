/*
 * This file is part of JGCGen.
 *
 * JGCGen is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * JGCGen is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with JGCGen.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.nbp.designer.gcode.path;

import com.willwinder.universalgcodesender.gcode.util.Code;

/**
 * Segment types
 *
 * @author Calle Laakkonen
 */
public enum SegmentType {
    /**
     * A marker for splitting the path
     */
    SEAM("---"),

    /**
     * Drill down at this point
     */
    POINT(Code.G1.name()),

    /**
     * Move in a straight line
     */
    LINE(Code.G1.name()),

    /**
     * Clockwise arc
     */
    CWARC(Code.G2.name()),

    /**
     * Counter clockwise arc
     */
    CCWARC(Code.G3.name()),

    /**
     * Rapid to location (usually through safe height)
     */
    MOVE(Code.G0.name());

    public final String gcode;

    SegmentType(String gc) {
        gcode = gc;
    }
}
