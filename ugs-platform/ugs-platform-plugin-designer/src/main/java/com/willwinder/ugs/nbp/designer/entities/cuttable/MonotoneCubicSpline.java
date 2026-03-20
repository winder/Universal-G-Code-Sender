/*
    Copyright 2026 Will Winder

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
package com.willwinder.ugs.nbp.designer.entities.cuttable;

import com.willwinder.universalgcodesender.utils.MathUtils;

import java.awt.geom.Path2D;
import java.awt.geom.PathIterator;
import java.util.Arrays;

/**
 * Shared power-curve utility for both raster LUT generation and the Swing editor preview.
 * <p>
 * Converts a set of user-defined control points into a smooth Catmull-Rom cubic Bézier path,
 * then samples that path into a 256-entry lookup table (LUT) mapping brightness → laser power.
 * Both the visual curve in {@code PowerCurvePanel} and the pixel-processing in {@code Raster}
 * use the same path, so what the user sees matches what the laser does.
 */
public class MonotoneCubicSpline {

    private MonotoneCubicSpline() {
    }

    /** Returns the default linear (identity) control points: bottom-left to top-right. */
    public static int[][] defaultControlPoints() {
        return new int[][]{{0, 0}, {255, 255}};
    }

    /** Returns {@code true} if the control points represent a perfect linear mapping. */
    public static boolean isIdentity(int[][] controlPoints) {
        return Arrays.deepEquals(controlPoints, defaultControlPoints());
    }

    /**
     * Builds a smooth Catmull-Rom path in logical space (0–255 on both axes).
     * This is the single source of truth shared by the UI renderer and the LUT builder.
     *
     * @param controlPoints array of [x, y] pairs
     */
    public static Path2D buildCurvePath(int[][] controlPoints) {
        return catmullRomPath(toDoubles(controlPoints));
    }

    /**
     * Builds a 256-entry LUT from control points.
     * Calls {@link #buildCurvePath} internally, so the LUT exactly matches the visual curve.
     *
     * @param controlPoints array of [x, y] pairs
     * @return int[256] where {@code lut[brightness]} gives the output power (0–255)
     */
    public static int[] buildLut(int[][] controlPoints) {
        return samplePathToLut(buildCurvePath(controlPoints));
    }

    /** Deep-clones an {@code int[][]} array so inner arrays are not shared. */
    public static int[][] deepClone(int[][] points) {
        int[][] copy = new int[points.length][];
        for (int i = 0; i < points.length; i++) {
            copy[i] = points[i].clone();
        }
        return copy;
    }

    // -------------------------------------------------------------------------
    // Private helpers
    // -------------------------------------------------------------------------

    /** Converts int[][] control points to double[][] for the path builder. */
    private static double[][] toDoubles(int[][] pts) {
        double[][] result = new double[pts.length][2];
        for (int i = 0; i < pts.length; i++) {
            result[i][0] = pts[i][0];
            result[i][1] = pts[i][1];
        }
        return result;
    }

    /**
     * Builds a Catmull-Rom → cubic Bézier path through the given sorted points.
     * With only 2 points this degenerates to a straight line; with 3+ it is smooth.
     */
    private static Path2D catmullRomPath(double[][] pts) {
        Path2D path = new Path2D.Double();
        int n = pts.length;
        path.moveTo(pts[0][0], pts[0][1]);

        if (n == 2) {
            path.lineTo(pts[1][0], pts[1][1]);
            return path;
        }

        for (int i = 0; i < n - 1; i++) {
            double[] p0 = pts[Math.max(i - 1, 0)];
            double[] p1 = pts[i];
            double[] p2 = pts[i + 1];
            double[] p3 = pts[Math.min(i + 2, n - 1)];

            // Catmull-Rom tangent → cubic Bézier control points (tension factor = 1/6)
            double cp1x = p1[0] + (p2[0] - p0[0]) / 6.0;
            double cp1y = p1[1] + (p2[1] - p0[1]) / 6.0;
            double cp2x = p2[0] - (p3[0] - p1[0]) / 6.0;
            double cp2y = p2[1] - (p3[1] - p1[1]) / 6.0;

            path.curveTo(cp1x, cp1y, cp2x, cp2y, p2[0], p2[1]);
        }

        return path;
    }

    /**
     * Flattens a Path2D into small line segments, then samples at each integer x in 0–255
     * using linear interpolation between the two nearest segment endpoints.
     */
   private static int[] samplePathToLut(Path2D path) {
        int[] lut = new int[256];
        Arrays.fill(lut, -1);

        double[] coords = new double[6];
        PathIterator it = path.getPathIterator(null, 0.5);
        while (!it.isDone()) {
            int type = it.currentSegment(coords);
            if (type == PathIterator.SEG_MOVETO || type == PathIterator.SEG_LINETO) {
                int x = clamp(coords[0]);
                if (lut[x] == -1) {
                    lut[x] = clamp(coords[1]);
                }
            }
            it.next();
        }

        // Fill any gaps with linear interpolation between known neighbours
        for (int x = 0; x < 256; x++) {
            if (lut[x] != -1) continue;
            int lo = x - 1;
            int hi = x + 1;
            while (hi < 256 && lut[hi] == -1) hi++;
            double t = (double)(x - lo) / (hi - lo);
            lut[x] = clamp(lut[lo] + t * (lut[hi] - lut[lo]));
        }

        return lut;
    }

    private static int clamp(double value) {
        return MathUtils.clamp((int)Math.round(value), 0, 255);
    }
}
