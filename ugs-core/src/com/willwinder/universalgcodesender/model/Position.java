/*
    Copyright 2016-2017 Will Winder

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
package com.willwinder.universalgcodesender.model;

import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import org.apache.commons.lang3.builder.ToStringBuilder;

import java.util.Objects;

public class Position extends CNCPoint {

    private final Units units;

    public Position() {
        this.units = Units.UNKNOWN;
    }

    public Position(Position other) {
        this(other.x, other.y, other.z, other.a, other.b, other.c, other.units);
    }

    public Position(double x, double y, double z) {
        super(x, y, z, 0, 0, 0);
        this.units = Units.UNKNOWN;
    }

    public Position(double x, double y, double z, Units units) {
        super(x, y, z, 0, 0, 0);
        this.units = units;
    }

    public Position(double x, double y, double z, double a, double b, double c, Units units) {
        super(x, y, z, a, b, c);
        this.units = units;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof Position) {
            return equals((Position) other);
        }
        return false;
    }

    @Override
    public boolean equals(final CNCPoint o) {
        if (o instanceof Position) {
            return super.equals(o) && units == ((Position)o).units;
        }
        return super.equals(o);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

    /**
     * Check that the positions are the same ignoring units.
     */
    public boolean isSamePositionIgnoreUnits(final Position o) {
        if (units != o.getUnits()) {
            return equals(o.getPositionIn(units));
        }
        return equals(o);
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.units);
        hash = 83 * hash + super.hashCode();
        return hash;
    }

    public Units getUnits() {
        return units;
    }

    public Position getPositionIn(Units units) {
        double scale = UnitUtils.scaleUnits(this.units, units);
        return new Position(x*scale, y*scale, z*scale, units);
    }

    public double get(Axis axis) {
        switch (axis) {
            case X:
                return getX();
            case Y:
                return getY();
            case Z:
                return getZ();
            case A:
                return getA();
            case B:
                return getB();
            case C:
                return getC();
            default:
                return 0;
        }
    }
}
