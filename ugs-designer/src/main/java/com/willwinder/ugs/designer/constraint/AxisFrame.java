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
package com.willwinder.ugs.designer.constraint;

import com.willwinder.ugs.designer.entities.Entity;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

/**
 * The directions a constraint measures along and draws its dimension across, as unit vectors.
 * The size of an entity is measured along the entity's own axes so it follows the entity's
 * rotation, and a constraint between points measures along axes of its own that turn with
 * its entities when they are turned together, see {@link PointConstraint}.
 *
 * @param along  the direction the distance is measured in
 * @param across the direction perpendicular to it, where the dimension is offset
 */
public record AxisFrame(Point2D along, Point2D across) {
    private static final double PARALLEL_TOLERANCE = 1e-6;

    /**
     * The axis of the design.
     */
    public static AxisFrame of(Axis axis) {
        return axis == Axis.HORIZONTAL
                ? new AxisFrame(new Point2D.Double(1, 0), new Point2D.Double(0, 1))
                : new AxisFrame(new Point2D.Double(0, 1), new Point2D.Double(1, 0));
    }

    /**
     * The axis of the entity, which is the axis of the design turned as the entity is rotated,
     * and mirrored as the entity is mirrored.
     */
    public static AxisFrame of(Axis axis, Entity entity) {
        AffineTransform transform = entity.getTransform();
        Point2D localX = transform.deltaTransform(new Point2D.Double(1, 0), null);
        double length = localX.distance(0, 0);
        if (length == 0) {
            return of(axis);
        }
        double handedness = transform.getDeterminant() < 0 ? -1 : 1;
        Point2D x = new Point2D.Double(localX.getX() / length, localX.getY() / length);
        Point2D y = new Point2D.Double(-x.getY() * handedness, x.getX() * handedness);
        return axis == Axis.HORIZONTAL ? new AxisFrame(x, y) : new AxisFrame(y, x);
    }

    /**
     * The entity's own axes, along its horizontal axis, which its sizes are measured in.
     */
    public static AxisFrame ownOf(Entity entity) {
        return of(Axis.HORIZONTAL, entity);
    }

    /**
     * The frame a new constraint between points on the two entities measures in: the entities'
     * own axis when both points are on the same entity, as for a size, or when the entities are
     * rotated alike, otherwise the design's axis, see {@link PointConstraint}.
     */
    public static AxisFrame of(Axis axis, Entity reference, Entity target) {
        AxisFrame referenceFrame = of(axis, reference);
        return reference == target || referenceFrame.isAlike(of(axis, target)) ? referenceFrame : of(axis);
    }

    boolean isAlike(AxisFrame other) {
        return along.distance(other.along) < PARALLEL_TOLERANCE && across.distance(other.across) < PARALLEL_TOLERANCE;
    }

    /**
     * The frame with the directions along and across it swapped, as the vertical axis of the
     * frame of a horizontal axis.
     */
    public AxisFrame swapped() {
        return new AxisFrame(across, along);
    }

    /**
     * The frame whose direction along it is at the angle, with the direction across it turned
     * a quarter counterclockwise from that, or clockwise when mirrored.
     *
     * @param angle the angle in radians counterclockwise from the x axis
     */
    public static AxisFrame ofAngle(double angle, boolean mirrored) {
        Point2D along = new Point2D.Double(Math.cos(angle), Math.sin(angle));
        double handedness = mirrored ? -1 : 1;
        return new AxisFrame(along, new Point2D.Double(-along.getY() * handedness, along.getX() * handedness));
    }

    /**
     * Whether the direction across the frame is turned clockwise from the direction along it,
     * as for a mirrored entity.
     */
    public boolean isMirrored() {
        return along.getX() * across.getY() - along.getY() * across.getX() < 0;
    }

    /**
     * The other frame carried along as this frame is carried to the given one, as when an
     * entity is rotated or mirrored, keeping its directions the same in this frame's
     * coordinates.
     */
    public AxisFrame carry(AxisFrame frame, AxisFrame to) {
        return new AxisFrame(to.point(coordinate(frame.along), crossCoordinate(frame.along)),
                to.point(coordinate(frame.across), crossCoordinate(frame.across)));
    }

    /**
     * The coordinate of the point along the frame.
     */
    public double coordinate(Point2D point) {
        return point.getX() * along.getX() + point.getY() * along.getY();
    }

    /**
     * The coordinate of the point across the frame.
     */
    public double crossCoordinate(Point2D point) {
        return point.getX() * across.getX() + point.getY() * across.getY();
    }

    /**
     * A movement of the given length along the frame.
     */
    public Point2D delta(double length) {
        return new Point2D.Double(along.getX() * length, along.getY() * length);
    }

    /**
     * The point at the given coordinates along and across the frame.
     */
    public Point2D point(double alongCoordinate, double acrossCoordinate) {
        return new Point2D.Double(along.getX() * alongCoordinate + across.getX() * acrossCoordinate,
                along.getY() * alongCoordinate + across.getY() * acrossCoordinate);
    }

    /**
     * The bounds of the entity's shape in the frame's coordinates, along the frame as x and
     * across it as y.
     */
    public Rectangle2D boundsOf(Entity entity) {
        return AffineTransform.getRotateInstance(-angle()).createTransformedShape(entity.getShape()).getBounds2D();
    }

    /**
     * Whether the direction across the frame is closer to vertical than to horizontal.
     */
    public boolean isAcrossVertical() {
        return Math.abs(across.getY()) >= Math.abs(across.getX());
    }

    /**
     * The angle of the direction along the frame, in radians counterclockwise from the x axis.
     */
    public double angle() {
        return Math.atan2(along.getY(), along.getX());
    }
}
