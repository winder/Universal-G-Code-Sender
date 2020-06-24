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
package com.willwinder.ugs.designer.gcode.path;


import java.util.EnumMap;

/**
 * Numeric coordinates.
 * <p>A numeric coordinates value is known at "compile time"
 *
 * @author Calle Laakkonen
 */
public final class NumericCoordinate extends Coordinate {
    private final EnumMap<Axis, Double> axes;

    private NumericCoordinate(EnumMap<Axis, Double> axes) {
        this.axes = axes;
    }

    /**
     * The default constructor.
     * All axises are undefined.
     */
    public NumericCoordinate() {
        this.axes = new EnumMap<>(Axis.class);
    }

    /**
     * A constructor with defaults for x,y and z axises.
     *
     * @param x position in x
     * @param y position in y
     * @param z position in z
     */
    public NumericCoordinate(Double x, Double y, Double z) {
        this();
        if (x != null)
            axes.put(Axis.X, x);
        if (y != null)
            axes.put(Axis.Y, y);
        if (z != null && !Double.isNaN(z))
            axes.put(Axis.Z, z);
    }

    public NumericCoordinate(Double x, Double y) {
        this(x, y, null);
    }

    @Override
    public NumericCoordinate set(Axis axis, double value) {
        axes.put(axis, value);
        return this;
    }


    /**
     * Get the numeric value for the axis
     *
     * @param axis         the axis to get the value for
     * @param defaultValue default value if axis is not defined
     * @return value or default
     */
    public double getOrDefault(Axis axis, double defaultValue) {
        Double value = axes.get(axis);
        return (value != null && !value.isNaN()) ? value : defaultValue;
    }

    public double get(Axis axis) {
        Double d = axes.get(axis);
        if ((d == null || d.isNaN())) {
            return Double.NaN;
        }

        // normalize
        if (Math.abs(d) < 0.001) {
            d = 0.0;
        }
        return d;
    }

    public boolean isDefined(Axis a) {
        return axes.get(a) != null;
    }

    public void set(Axis a, Double value) {
        if (value == null)
            axes.remove(a);
        else
            axes.put(a, value);
    }

    public Coordinate offset(Coordinate offset, boolean invert, boolean override) {
        NumericCoordinate o = (NumericCoordinate) offset;
        EnumMap<Axis, Double> c = new EnumMap<>(Axis.class);
        for (Axis a : Axis.values()) {
            Double val = axes.get(a);
            Double oVal = o.axes.get(a);
            if (val != null) {
                if (oVal != null) {
                    if (invert)
                        val -= oVal;
                    else
                        val += oVal;
                }
                c.put(a, val);
            } else if (override && oVal != null) {
                if (invert)
                    c.put(a, -oVal);
                else
                    c.put(a, oVal);
            }
        }
        return new NumericCoordinate(c);
    }
}