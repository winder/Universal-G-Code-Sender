package com.willwinder.universalgcodesender.model;

import java.util.Objects;
import javax.vecmath.Point3d;

public class Position extends Point3d {

    private final Utils.Units units;

    public Position() {
        this.units = Utils.Units.UNKNOWN;
    }

    public Position(Position other) {
        this(other.x, other.y, other.z, other.units);
    }

    public Position(double x, double y, double z, Utils.Units units) {
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

    public Utils.Units getUnits() {
        return units;
    }

    public Position getPositionIn(Utils.Units units) {
        double scale = Utils.scaleUnits(this.units, units);
        return new Position(x*scale, y*scale, z*scale, units);
    }
}
