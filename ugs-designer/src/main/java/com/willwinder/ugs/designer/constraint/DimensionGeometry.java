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

import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * The geometry the kinds of {@link DimensionLine} share. Sizes on screen are in pixels and
 * scaled to the design with the size of a pixel.
 */
final class DimensionGeometry {
    static final double LABEL_GAP_PX = 3;
    static final double STACK_GAP_PX = 2;
    static final double ALIGNMENT_EXTENSION_PX = 8;
    static final double ALIGNMENT_MARK_PX = 3.5;
    static final double COINCIDENT_RING_PX = 7;
    private static final double ARROW_HALF_WIDTH_PX = 2.5;
    private static final ThreadLocal<DecimalFormat> FORMAT =
            ThreadLocal.withInitial(() -> new DecimalFormat("0.##", DecimalFormatSymbols.getInstance(Locale.ROOT)));

    private DimensionGeometry() {
    }

    static String format(double distance) {
        return FORMAT.get().format(Math.abs(distance));
    }

    static Optional<Point2D> unit(Point2D start, Point2D end) {
        double length = start.distance(end);
        if (length == 0) {
            return Optional.empty();
        }
        return Optional.of(new Point2D.Double((end.getX() - start.getX()) / length, (end.getY() - start.getY()) / length));
    }

    static Point2D midpoint(Point2D a, Point2D b) {
        return new Point2D.Double((a.getX() + b.getX()) / 2, (a.getY() + b.getY()) / 2);
    }

    static Point2D offset(Point2D point, Point2D direction, double length) {
        return new Point2D.Double(point.getX() + direction.getX() * length, point.getY() + direction.getY() * length);
    }

    static Point2D reversed(Point2D direction) {
        return new Point2D.Double(-direction.getX(), -direction.getY());
    }

    static double distanceToSegment(Point2D start, Point2D end, Point2D point) {
        return Line2D.ptSegDist(start.getX(), start.getY(), end.getX(), end.getY(), point.getX(), point.getY());
    }

    static Point2D[] arrowhead(Point2D tip, Point2D towardsBase, double worldUnitsPerPixel) {
        double halfWidth = ARROW_HALF_WIDTH_PX * worldUnitsPerPixel;
        Point2D base = offset(tip, towardsBase, DimensionLine.ARROW_LENGTH_PX * worldUnitsPerPixel);
        return new Point2D[]{
                tip,
                new Point2D.Double(base.getX() - towardsBase.getY() * halfWidth, base.getY() + towardsBase.getX() * halfWidth),
                new Point2D.Double(base.getX() + towardsBase.getY() * halfWidth, base.getY() - towardsBase.getX() * halfWidth)};
    }

    // Ahead of the center along the direction, then to its left, behind it and to its right
    static Point2D[] diamondCorners(Point2D center, Point2D direction, double size) {
        return new Point2D[]{
                offset(center, direction, size),
                new Point2D.Double(center.getX() - direction.getY() * size, center.getY() + direction.getX() * size),
                offset(center, direction, -size),
                new Point2D.Double(center.getX() + direction.getY() * size, center.getY() - direction.getX() * size)};
    }

    // A filled diamond as two triangles
    static List<Point2D[]> diamond(Point2D center, Point2D direction, double worldUnitsPerPixel) {
        Point2D[] corners = diamondCorners(center, direction, ALIGNMENT_MARK_PX * worldUnitsPerPixel);
        return List.of(new Point2D[]{corners[0], corners[1], corners[2]}, new Point2D[]{corners[0], corners[2], corners[3]});
    }

    // Zero inside the label's box
    static double distanceToLabel(Point2D point, Point2D label, double labelWidthPx, double worldUnitsPerPixel) {
        double outsideX = Math.max(0, Math.abs(point.getX() - label.getX()) - labelWidthPx / 2 * worldUnitsPerPixel);
        double outsideY = Math.max(0, Math.abs(point.getY() - label.getY()) - DimensionLine.LABEL_HEIGHT_PX / 2 * worldUnitsPerPixel);
        return Math.hypot(outsideX, outsideY);
    }
}
