package com.willwinder.universalgcodesender.model;

import javax.vecmath.Point3d;

public class Position extends Point3d {

    private final Utils.Units units;

    public Position() {
        this.units = Utils.Units.UNKNOWN;
    }

    public Position(double x, double y, double z, Utils.Units units) {
        super(x, y, z);
        this.units = units;
    }

    public Utils.Units getUnits() {
        return units;
    }

    public Position getPositionIn(Utils.Units units) {
        double scale = Utils.scaleUnits(this.units, units);
        return new Position(x*scale, y*scale, z*scale, units);
    }
}
