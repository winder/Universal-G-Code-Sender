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

    public PartialPosition(Double x, Double y) {
        this.x = x;
        this.y = y;
        this.z = null;
    }

    public PartialPosition(Double x, Double y, Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    // shortcut to builder (needed, because of final coords)
    public static PartialPosition from(Axis axis, Double value) {
        return new Builder().setValue(axis, value).build();
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
        return allSetCoords.build();
    }

    public double getAxis(Axis axis) {
        switch (axis) {
            case X:
                return getX();
            case Y:
                return getY();
            case Z:
                return getZ();
            default:
                return 0;
        }
    }

    public static final class Builder {
        private Double x = null;
        private Double y = null;
        private Double z = null;

        public PartialPosition build() {
            return new PartialPosition(x, y, z);
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
    }

    public String getFormatted() {
        return getFormatted(Utils.formatter);

    }

    public String getFormatted(NumberFormat formatter) {
        StringBuilder sb = new StringBuilder();
        if (this.hasX()) {
            sb.append("X").append(formatter.format(this.getX())).append(" ");
        }
        if (this.hasY()) {
            sb.append("Y").append(formatter.format(this.getY())).append(" ");
        }
        if (this.hasZ()) {
            sb.append("Z").append(formatter.format(this.getZ())).append(" ");
        }
        return sb.toString().trim();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PartialPosition)) return false;
        PartialPosition that = (PartialPosition) o;
        return Objects.equals(x, that.x) &&
                Objects.equals(y, that.y) &&
                Objects.equals(z, that.z);
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y, z);
    }
}
