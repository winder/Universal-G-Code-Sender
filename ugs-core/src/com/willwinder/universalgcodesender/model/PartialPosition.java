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
        return builder().setValue(axis, value).setUnits(units).build();
    }


    public static PartialPosition from(Position position) {
        return new PartialPosition(position.getX(), position.getY(), position.getZ(), position.getUnits());
    }

    public static PartialPosition fromXY(Position position) {
        return new PartialPosition(position.getX(), position.getY(), position.getUnits());
    }


    public boolean hasX() {
        return x != null;
    }

    public boolean hasY() {
        return y != null;
    }

    public boolean hasZ() {
        return z != null;
    }

    public boolean hasA() {
        return a != null;
    }

    public boolean hasB() {
        return b != null;
    }

    public boolean hasC() {
        return c != null;
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
            throw new IllegalArgumentException("Tried to get y-axis which is not set");
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
        Builder builder = builder();
        for (Map.Entry<Axis, Double> axis : getAll().entrySet()) {
            double mul = axis.getKey().isLinear() ? scale : 1.0;
            builder.setValue(axis.getKey(), axis.getValue() * mul);
        }
        builder.setUnits(units);
        return builder.build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private Double x = null;
        private Double y = null;
        private Double z = null;
        private Double a = null;
        private Double b = null;
        private Double c = null;
        private UnitUtils.Units units = UnitUtils.Units.UNKNOWN;

        public PartialPosition build() {
            if (units == UnitUtils.Units.UNKNOWN) {
                throw new RuntimeException("No units was supplied to the PartialPosition!");
            }
            return new PartialPosition(x, y, z, a, b, c, units);
        }

        public Builder setValue(Axis axis, Double value) {
            switch (axis) {
                case X:
                    this.x = value;
                    break;
                case Y:
                    this.y = value;
                    break;
                case Z:
                    this.z = value;
                    break;
                case A:
                    this.a = value;
                    break;
                case B:
                    this.b = value;
                    break;
                case C:
                    this.c = value;
                    break;
            }
            return this;
        }

        public Builder setX(Double x) {
            this.x = x;
            return this;
        }

        public Builder setY(Double y) {
            this.y = y;
            return this;
        }

        public Builder setZ(Double z) {
            this.z = z;
            return this;
        }

        public Builder setA(Double a) {
            this.a = a;
            return this;
        }

        public Builder setB(Double b) {
            this.b = b;
            return this;
        }

        public Builder setC(Double c) {
            this.c = c;
            return this;
        }

        public Builder copy(Position position) {
            this.x = position.getX();
            this.y = position.getY();
            this.z = position.getZ();
            this.a = position.getA();
            this.b = position.getB();
            this.c = position.getC();
            this.units = position.getUnits();
            return this;
        }

        public Builder setUnits(UnitUtils.Units units) {
            this.units = units;
            return this;
        }

        public Builder copy(PartialPosition position) {
            this.x = position.x;
            this.y = position.y;
            this.z = position.z;
            this.a = position.a;
            this.b = position.b;
            this.c = position.c;
            this.units = position.units;
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
