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

/**
 * Rasterizes the volume swept by a tool moving along a straight segment into a
 * {@link HeightField}.
 *
 * <p>For every node inside the segment's bounding box inflated by the tool radius, the surface is
 * lowered to {@code zTip(s) + h(d(s))}, where {@code s} is a position along the segment and
 * {@code d} the radial distance from the node to the tool axis at that position. For a horizontal
 * segment the closest point on the segment is the exact minimizer since the profile grows with
 * {@code d}. For a sloped segment the two ends of the interval where the tool disc covers the node
 * are evaluated as well, so a flat tool ramping down cuts everything behind its final position to
 * the final depth.
 */
public final class SegmentSweep {
    private static final double EPSILON = 1e-9;

    private SegmentSweep() {
    }

    public static void sweep(HeightField field, ToolProfile tool,
                             double x0, double y0, double z0, double x1, double y1, double z1) {
        double radius = tool.radius();
        double radius2 = radius * radius;
        int columnStart = field.columnCeil(Math.min(x0, x1) - radius);
        int columnEnd = field.columnFloor(Math.max(x0, x1) + radius);
        int rowStart = field.rowCeil(Math.min(y0, y1) - radius);
        int rowEnd = field.rowFloor(Math.max(y0, y1) + radius);
        if (columnStart > columnEnd || rowStart > rowEnd) {
            return;
        }

        double dx = x1 - x0;
        double dy = y1 - y0;
        double dz = z1 - z0;
        double length2 = dx * dx + dy * dy;
        boolean plunge = length2 < EPSILON * EPSILON;
        boolean sloped = Math.abs(dz) > EPSILON;
        double lowestTip = Math.min(z0, z1);

        for (int row = rowStart; row <= rowEnd; row++) {
            double wy = field.y(row) - y0;
            for (int column = columnStart; column <= columnEnd; column++) {
                double wx = field.x(column) - x0;
                if (plunge) {
                    double d2 = wx * wx + wy * wy;
                    if (d2 <= radius2) {
                        field.lower(column, row, lowestTip + tool.heightAt(Math.sqrt(d2)));
                    }
                    continue;
                }

                double along = wx * dx + wy * dy;
                double s = clamp(along / length2);
                double cx = wx - s * dx;
                double cy = wy - s * dy;
                double d2 = cx * cx + cy * cy;
                if (d2 > radius2) {
                    continue;
                }
                double z = z0 + s * dz + tool.heightAt(Math.sqrt(d2));

                if (sloped) {
                    double discriminant = along * along - length2 * (wx * wx + wy * wy - radius2);
                    if (discriminant >= 0) {
                        double root = Math.sqrt(discriminant);
                        z = Math.min(z, cutAt(clamp((along - root) / length2), tool, wx, wy, dx, dy, dz, z0));
                        z = Math.min(z, cutAt(clamp((along + root) / length2), tool, wx, wy, dx, dy, dz, z0));
                    }
                }
                field.lower(column, row, z);
            }
        }
    }

    private static double cutAt(double s, ToolProfile tool, double wx, double wy, double dx, double dy, double dz, double z0) {
        double cx = wx - s * dx;
        double cy = wy - s * dy;
        double d = Math.sqrt(cx * cx + cy * cy);
        if (d > tool.radius()) {
            // Rounding pushed the interval end just outside the disc; the closest point covers it.
            return Double.POSITIVE_INFINITY;
        }
        return z0 + s * dz + tool.heightAt(d);
    }

    private static double clamp(double s) {
        return s < 0 ? 0 : (s > 1 ? 1 : s);
    }
}
