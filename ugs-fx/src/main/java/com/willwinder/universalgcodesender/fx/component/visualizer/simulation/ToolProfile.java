/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.component.visualizer.simulation;

import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;

/**
 * The cutting profile of a tool as a radial function: {@link #heightAt(double)} is the height of
 * the cutting edge above the tool tip at a given radial distance from the tool axis. A flat endmill
 * is zero everywhere, a ball nose follows a sphere and a V-bit a cone.
 */
public final class ToolProfile {
    /**
     * A 1/8" flat endmill, used when a program selects no tool and the library has none to offer.
     */
    public static final ToolProfile DEFAULT = flat(3.175);
    private static final double DEFAULT_V_BIT_ANGLE = 90;

    public enum Kind {FLAT, BALL, V_BIT}

    private final Kind kind;
    private final double radius;
    private final double cotHalfAngle;

    private ToolProfile(Kind kind, double radius, double cotHalfAngle) {
        if (radius <= 0) {
            throw new IllegalArgumentException("Tool radius must be positive, was " + radius);
        }
        this.kind = kind;
        this.radius = radius;
        this.cotHalfAngle = cotHalfAngle;
    }

    public static ToolProfile flat(double diameter) {
        return new ToolProfile(Kind.FLAT, diameter / 2, 0);
    }

    public static ToolProfile ball(double diameter) {
        return new ToolProfile(Kind.BALL, diameter / 2, 0);
    }

    /**
     * @param diameter      the diameter at the top of the cutting edge
     * @param includedAngle the full angle of the tip in degrees, such as 60 or 90
     */
    public static ToolProfile vBit(double diameter, double includedAngle) {
        if (includedAngle <= 0 || includedAngle >= 180) {
            return flat(diameter);
        }
        return new ToolProfile(Kind.V_BIT, diameter / 2, 1 / Math.tan(Math.toRadians(includedAngle / 2)));
    }

    /**
     * Builds the profile for a tool from the tool library. The diameter is read in millimeters.
     */
    public static ToolProfile fromDefinition(ToolDefinition tool) {
        double diameter = tool.getDiameterInMm();
        if (diameter <= 0) {
            return DEFAULT;
        }
        EndmillShape shape = tool.getShape();
        return switch (shape) {
            case BALL -> ball(diameter);
            case V_BIT -> vBit(diameter, tool.getVBitAngleDegrees() == null ? DEFAULT_V_BIT_ANGLE : tool.getVBitAngleDegrees());
            default -> flat(diameter);
        };
    }

    public Kind kind() {
        return kind;
    }

    public double radius() {
        return radius;
    }

    public double diameter() {
        return radius * 2;
    }

    /**
     * The height of the cutting edge above the tip at radial distance {@code r} from the axis,
     * for {@code 0 <= r <= radius}.
     */
    public double heightAt(double r) {
        return switch (kind) {
            case FLAT -> 0;
            case BALL -> radius - Math.sqrt(Math.max(0, radius * radius - r * r));
            case V_BIT -> r * cotHalfAngle;
        };
    }

    @Override
    public String toString() {
        return kind + " " + diameter() + "mm";
    }
}
