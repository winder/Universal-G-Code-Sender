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

/**
 * @author Calle Laakkonen
 */
public abstract class Coordinate {

    /**
     * Get the coordinate for the given axis.
     * If this the coordinate is symbolic, it will be wrapped in "[]"
     *
     * @param axis
     * @return coordinate string or null if not defined for this axis
     */
    public abstract double get(Axis axis);

    public abstract double getOrDefault(Axis axis, double defaultValue);


    /**
     * Is a coordinate defined for the given axis?
     *
     * @param axis axis to check
     * @return true if a value is set
     */
    public abstract boolean isDefined(Axis axis);

    /**
     * Get a copy of this coordinate set with an added offset.
     * If either this or the offset is symbolic, the returned
     * coordinates will be symbolic also.
     * <p>Offsets, even if defined, are not added to axes that are
     * undefined in this coordinate set.
     *
     * @param offset   offset
     * @param invert   subtract offset instead of adding
     * @param override offset even undefined axes
     * @return this -/+ offset
     */
    public abstract Coordinate offset(Coordinate offset, boolean invert, boolean override);

    /**
     * Get a copy of this coordinate set with an added offset.
     * If either this or the offset is symbolic, the returned
     * coordinates will be symbolic also.
     * <p>Offsets, even if defined, are not added to axes that are
     * undefined in this coordinate set.
     *
     * @param offset
     * @return this + offset
     */
    public final Coordinate offset(Coordinate offset) {
        return offset(offset, false, false);
    }

    /**
     * Convert this coordinate set to G-code.
     * E.g. If X and Y coordinates are set, this might produce
     * <kbd>X10.20 Y4.01</kbd>
     *
     * @return coordinates
     */
    public String toGcode() {
        StringBuilder sb = new StringBuilder();
        boolean first = true;
        for (Axis a : Axis.values()) {
            Double val = get(a);
            if (!Double.isNaN(val)) {
                if (!first)
                    sb.append(' ');
                else
                    first = false;
                sb.append(a.toString());
                sb.append(val);
            }
        }
        return sb.toString();
    }

    public String toString() {
        return toGcode();
    }

    public abstract Coordinate set(Axis axis, double value);

    public abstract Coordinate copy();
}
