/*
    Copywrite 2016-2017 Will Winder

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

import java.util.Objects;
import javax.vecmath.Point3d;

public class Position extends Point3d {

    private final UnitUtils.Units units;

    public Position() {
        this.units = UnitUtils.Units.UNKNOWN;
    }

    public Position(Position other) {
        this(other.x, other.y, other.z, other.units);
    }

    public Position(double x, double y, double z, UnitUtils.Units units) {
        super(x, y, z);
        this.units = units;
    }

    @Override
    public boolean equals(final Object other) {
        if (other instanceof Position) {
            Position o = (Position) other;
            return x == o.x && y == o.y && z == o.z && units == o.units;
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 83 * hash + Objects.hashCode(this.units);
        return hash;
    }

    public UnitUtils.Units getUnits() {
        return units;
    }

    public Position getPositionIn(UnitUtils.Units units) {
        double scale = UnitUtils.scaleUnits(this.units, units);
        return new Position(x*scale, y*scale, z*scale, units);
    }
}
