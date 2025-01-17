/*
    Copyright 2019-2021 Will Winder

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

import com.google.common.collect.ImmutableMap;
import com.willwinder.universalgcodesender.Utils;

import java.text.NumberFormat;
import java.util.Map;
import java.util.Objects;

/**
 * Represents a maybe partial coordinate (ie. only certain axis-values are known, eg. for moves where only some axis
 * are changed)
 */
public class PartialPosition {
    private final Double x;
    private final Double y;
    private final Double z;
    private final Double a;
    private final Double b;
    private final Double c;

    private final UnitUtils.Units units;

    public PartialPosition(Double x, Double y, UnitUtils.Units units) {
        this.x = x;
        this.y = y;
        this.z = null;
        this.a = null;
        this.b = null;
        this.c = null;

        this.units = units;
    }

    public PartialPosition(Double x, Double y, Double z, UnitUtils.Units units) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.a = null;
        this.b = null;
        this.c = null;

        this.units = units;
    }

    public PartialPosition(Double x, Double y, Double z, Double a, Double b, Double c, UnitUtils.Units units) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.a = a;
        this.b = b;
        this.c = c;

        this.units = units;
    }

    /**
     * Creates a partial position with only one axis
     * @param axis the axis to set
     * @param value the position
     * @param units the units of the position
     * @return a partial position
     */
    public static PartialPosition from(Axis axis, Double value, UnitUtils.Units units) {
        return builder(units).setValue(axis, value).build();
    }


    public static PartialPosition from(Position position) {
        return new PartialPosition(position.getX(), position.getY(), position.getZ(), position.getUnits());
    }

    public static PartialPosition fromXY(Position position) {
        return new PartialPosition(position.getX(), position.getY(), position.getUnits());
    }


    public boolean hasX() {
        return x != null && !x.isNaN();
    }

    public boolean hasY() {
        return y != null && !y.isNaN();
    }

    public boolean hasZ() {
        return z != null && !z.isNaN();
    }

    public boolean hasA() {
        return a != null && !a.isNaN();
    }

    public boolean hasB() {
        return b != null && !b.isNaN();
    }

    public boolean hasC() {
        return c != null && !c.isNaN();
    }

    public Double getX() {
        if (x == null) {
            throw new IllegalArgumentException("Tried to get x-axis which is not set");
        }
        return x;
    }

    public Double getY() {
        if (y == null) {
            throw new IllegalArgumentException("Tried to get y-axis which is not set");
        }
        return y;
    }

    public Double getZ() {
        if (z == null) {
            throw new IllegalArgumentException("Tried to get z-axis which is not set");
        }
        return z;
    }

    public Double getA() {
        if (a == null) {
            throw new IllegalArgumentException("Tried to get a-axis which is not set");
        }
        return a;
    }

    public Double getB() {
        if (b == null) {
            throw new IllegalArgumentException("Tried to get b-axis which is not set");
        }
        return b;
    }

    public Double getC() {
        if (c == null) {
            throw new IllegalArgumentException("Tried to get c-axis which is not set");
        }
        return c;
    }

    public UnitUtils.Units getUnits(){
        return units;
    }

    public Map<Axis, Double> getAll() {
        ImmutableMap.Builder<Axis, Double> allSetCoords = ImmutableMap.builder();
        if (hasX()) {
            allSetCoords.put(Axis.X, x);
        }
        if (hasY()) {
            allSetCoords.put(Axis.Y, y);
        }
        if (hasZ()) {
            allSetCoords.put(Axis.Z, z);
        }
        if (hasA()) {
            allSetCoords.put(Axis.A, a);
        }
        if (hasB()) {
            allSetCoords.put(Axis.B, b);
        }
        if (hasC()) {
            allSetCoords.put(Axis.C, c);
        }
        return allSetCoords.build();
    }

    public boolean hasAxis(Axis axis) {
        switch (axis) {
            case X:
                return hasX();
            case Y:
                return hasY();
            case Z:
                return hasZ();
            case A:
                return hasA();
            case B:
                return hasB();
            case C:
                return hasC();
            default:
                return false;
        }
    }

    public double getAxis(Axis axis) {
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

    public PartialPosition getPositionIn(UnitUtils.Units units) {
        if (units == this.units) return this;
        double scale = UnitUtils.scaleUnits(this.units, units);
        Builder builder = builder(units);
        for (Map.Entry<Axis, Double> axis : getAll().entrySet()) {
            double mul = axis.getKey().isLinear() ? scale : 1.0;
            builder.setValue(axis.getKey(), axis.getValue() * mul);
        }
        return builder.build();
    }

    public static Builder builder(UnitUtils.Units units) {
        return new Builder(units);
    }

    public static Builder builder(PartialPosition p) {
        Builder b = builder(p.getUnits());
        b.x = p.x;
        b.y = p.y;
        b.z = p.z;
        b.a = p.a;
        b.b = p.b;
        b.c = p.c;
        return b;
    }

    public static Builder builder(Position p) {
        return builder(p.getUnits())
                .setX(p.getX())
                .setY(p.getY())
                .setZ(p.getZ())
                .setA(p.getA())
                .setB(p.getB())
                .setC(p.getC());
    }

    public static final class Builder {
        private Double x = null;
        private Double y = null;
        private Double z = null;
        private Double a = null;
        private Double b = null;
        private Double c = null;
        private final UnitUtils.Units units;

        public Builder(UnitUtils.Units units) {
            this.units = units;
        }

        public PartialPosition build() {
            if (units == UnitUtils.Units.UNKNOWN) {
                throw new RuntimeException("No units was supplied to the PartialPosition!");
            }
            return new PartialPosition(x, y, z, a, b, c, units);
        }

        public Builder setValue(Axis axis, Double value) {
            switch (axis) {
                case X:
                    setX(value);
                    break;
                case Y:
                    setY(value);
                    break;
                case Z:
                    setZ(value);
                    break;
                case A:
                    setA(value);
                    break;
                case B:
                    setB(value);
                    break;
                case C:
                    setC(value);
                    break;
            }
            return this;
        }

        public Builder clearX() {
            this.x = null;
            return this;
        }

        public Builder clearY() {
            this.y = null;
            return this;
        }

        public Builder clearZ() {
            this.z = null;
            return this;
        }

        public Builder setX(Double x) {
            if (Double.isFinite(x)) {
                this.x = x;
            } else {
                this.x = null;
            }
            return this;
        }

        public Builder setY(Double y) {
            if (Double.isFinite(y)) {
                this.y = y;
            } else {
                this.y = null;
            }
            return this;
        }

        public Builder setZ(Double z) {
            if (Double.isFinite(z)) {
                this.z = z;
            } else {
                this.z = null;
            }
            return this;
        }

        public Builder setA(Double a) {
            if (Double.isFinite(a)) {
                this.a = a;
            } else {
                this.a = null;
            }
            return this;
        }

        public Builder setB(Double b) {
            if (Double.isFinite(b)) {
                this.b = b;
            } else {
                this.b = null;
            }
            return this;
        }

        public Builder setC(Double c) {
            if (Double.isFinite(c)) {
                this.c = c;
            } else {
                this.c = null;
            }
            return this;
        }

        public Builder clearABC() {
            this.a = null;
            this.b = null;
            this.c = null;
            return this;
        }
    }

    public String getFormattedGCode() {
        return getFormattedGCode(Utils.formatter);

    }

    public String getFormattedGCode(NumberFormat formatter) {
        StringBuilder sb = new StringBuilder();
        if (this.hasX()) {
            sb.append("X").append(formatter.format(this.getX()));
        }
        if (this.hasY()) {
            sb.append("Y").append(formatter.format(this.getY()));
        }
        if (this.hasZ()) {
            sb.append("Z").append(formatter.format(this.getZ()));
        }
        if (this.hasA()) {
            sb.append("A").append(formatter.format(this.getA()));
        }
        if (this.hasB()) {
            sb.append("B").append(formatter.format(this.getB()));
        }
        if (this.hasC()) {
            sb.append("C").append(formatter.format(this.getC()));
        }
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PartialPosition)) return false;
        PartialPosition that = (PartialPosition) o;
        return Objects.equals(x, that.x) &&
                Objects.equals(y, that.y) &&
                Objects.equals(z, that.z) &&
                Objects.equals(a, that.a) &&
                Objects.equals(b, that.b) &&
                Objects.equals(c, that.c) &&
                Objects.equals(units, that.units);

    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z, a, b, c, units);
    }

    @Override
    public String toString() {
        return "PartialPosition{" + getFormattedGCode() + " ["+ units + "]}";
    }
}
