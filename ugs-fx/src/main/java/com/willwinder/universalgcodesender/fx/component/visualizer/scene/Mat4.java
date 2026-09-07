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
package com.willwinder.universalgcodesender.fx.component.visualizer.scene;

/**
 * Column major 4x4 matrices as {@code float[16]}, the layout a shader {@code mat4} expects.
 */
public final class Mat4 {
    public static final int ELEMENTS = 16;

    private Mat4() {
    }

    public static float[] identity() {
        float[] m = new float[ELEMENTS];
        m[0] = 1;
        m[5] = 1;
        m[10] = 1;
        m[15] = 1;
        return m;
    }

    public static float[] multiply(float[] left, float[] right) {
        float[] result = new float[ELEMENTS];
        for (int column = 0; column < 4; column++) {
            for (int row = 0; row < 4; row++) {
                float sum = 0;
                for (int k = 0; k < 4; k++) {
                    sum += left[k * 4 + row] * right[column * 4 + k];
                }
                result[column * 4 + row] = sum;
            }
        }
        return result;
    }

    /**
     * Right-handed perspective projection for Vulkan clip space: depth maps to 0..1 and the
     * Y axis is flipped, since Vulkan's viewport origin is the top-left corner.
     */
    public static float[] perspective(double verticalFieldOfView, double aspect, double near, double far) {
        float focalLength = (float) (1.0 / Math.tan(verticalFieldOfView / 2.0));
        float[] m = new float[ELEMENTS];
        m[0] = (float) (focalLength / aspect);
        m[5] = -focalLength;
        m[10] = (float) (far / (near - far));
        m[11] = -1;
        m[14] = (float) ((near * far) / (near - far));
        return m;
    }

    /**
     * Right-handed orthographic projection for Vulkan clip space, centred on the view axis.
     * Depth maps to 0..1 between {@code near} and {@code far} and the Y axis is flipped.
     */
    public static float[] orthographic(double halfWidth, double halfHeight, double near, double far) {
        float[] m = new float[ELEMENTS];
        m[0] = (float) (1.0 / halfWidth);
        m[5] = (float) (-1.0 / halfHeight);
        m[10] = (float) (-1.0 / (far - near));
        m[14] = (float) (-near / (far - near));
        m[15] = 1;
        return m;
    }

    public static float[] translation(double x, double y, double z) {
        float[] m = identity();
        m[12] = (float) x;
        m[13] = (float) y;
        m[14] = (float) z;
        return m;
    }

    /**
     * A 2D affine transform on the XY plane lifted to 3D, with the given Z translation. The
     * arguments follow {@code java.awt.geom.AffineTransform}: {@code x' = scaleX*x + shearX*y +
     * translateX} and {@code y' = shearY*x + scaleY*y + translateY}.
     */
    public static float[] affine2D(double scaleX, double shearY, double shearX, double scaleY,
                                   double translateX, double translateY, double z) {
        float[] m = identity();
        m[0] = (float) scaleX;
        m[1] = (float) shearY;
        m[4] = (float) shearX;
        m[5] = (float) scaleY;
        m[12] = (float) translateX;
        m[13] = (float) translateY;
        m[14] = (float) z;
        return m;
    }

    public static float[] scale(double x, double y, double z) {
        float[] m = identity();
        m[0] = (float) x;
        m[5] = (float) y;
        m[10] = (float) z;
        return m;
    }

    public static float[] rotationX(double radians) {
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        float[] m = identity();
        m[5] = cos;
        m[6] = sin;
        m[9] = -sin;
        m[10] = cos;
        return m;
    }

    public static float[] rotationZ(double radians) {
        float cos = (float) Math.cos(radians);
        float sin = (float) Math.sin(radians);
        float[] m = identity();
        m[0] = cos;
        m[1] = sin;
        m[4] = -sin;
        m[5] = cos;
        return m;
    }

    /**
     * The matrix that takes normals along with a model transform: the inverse transpose of its
     * upper left 3x3, as a column major {@code float[9]}. For a rotation this is the rotation
     * itself; a non uniform scale is compensated so normals stay perpendicular to the surface.
     * A singular transform falls back to the identity.
     */
    public static float[] normalMatrix(float[] model) {
        float[] inverse;
        try {
            inverse = invert(model);
        } catch (IllegalArgumentException e) {
            inverse = identity();
        }
        // Transposing swaps rows and columns of the upper left 3x3 while keeping column major
        // storage: element (row, column) of the result is element (column, row) of the inverse.
        return new float[]{
                inverse[0], inverse[4], inverse[8],
                inverse[1], inverse[5], inverse[9],
                inverse[2], inverse[6], inverse[10]
        };
    }

    /**
     * Transforms a point and returns the homogeneous result {@code {x, y, z, w}}, in double
     * precision so that projecting and unprojecting round trips within a small fraction of a
     * pixel.
     */
    public static double[] transform(float[] m, double x, double y, double z) {
        return transform(m, x, y, z, 1);
    }

    public static double[] transform(float[] m, double x, double y, double z, double w) {
        return new double[]{
                m[0] * x + m[4] * y + m[8] * z + m[12] * w,
                m[1] * x + m[5] * y + m[9] * z + m[13] * w,
                m[2] * x + m[6] * y + m[10] * z + m[14] * w,
                m[3] * x + m[7] * y + m[11] * z + m[15] * w
        };
    }

    /**
     * The inverse, computed by cofactors in double precision.
     *
     * @throws IllegalArgumentException if the matrix is singular
     */
    public static float[] invert(float[] m) {
        double[] inv = new double[ELEMENTS];
        inv[0] = m[5] * m[10] * m[15] - m[5] * m[11] * m[14] - m[9] * m[6] * m[15]
                + m[9] * m[7] * m[14] + m[13] * m[6] * m[11] - m[13] * m[7] * m[10];
        inv[4] = -m[4] * m[10] * m[15] + m[4] * m[11] * m[14] + m[8] * m[6] * m[15]
                - m[8] * m[7] * m[14] - m[12] * m[6] * m[11] + m[12] * m[7] * m[10];
        inv[8] = m[4] * m[9] * m[15] - m[4] * m[11] * m[13] - m[8] * m[5] * m[15]
                + m[8] * m[7] * m[13] + m[12] * m[5] * m[11] - m[12] * m[7] * m[9];
        inv[12] = -m[4] * m[9] * m[14] + m[4] * m[10] * m[13] + m[8] * m[5] * m[14]
                - m[8] * m[6] * m[13] - m[12] * m[5] * m[10] + m[12] * m[6] * m[9];
        inv[1] = -m[1] * m[10] * m[15] + m[1] * m[11] * m[14] + m[9] * m[2] * m[15]
                - m[9] * m[3] * m[14] - m[13] * m[2] * m[11] + m[13] * m[3] * m[10];
        inv[5] = m[0] * m[10] * m[15] - m[0] * m[11] * m[14] - m[8] * m[2] * m[15]
                + m[8] * m[3] * m[14] + m[12] * m[2] * m[11] - m[12] * m[3] * m[10];
        inv[9] = -m[0] * m[9] * m[15] + m[0] * m[11] * m[13] + m[8] * m[1] * m[15]
                - m[8] * m[3] * m[13] - m[12] * m[1] * m[11] + m[12] * m[3] * m[9];
        inv[13] = m[0] * m[9] * m[14] - m[0] * m[10] * m[13] - m[8] * m[1] * m[14]
                + m[8] * m[2] * m[13] + m[12] * m[1] * m[10] - m[12] * m[2] * m[9];
        inv[2] = m[1] * m[6] * m[15] - m[1] * m[7] * m[14] - m[5] * m[2] * m[15]
                + m[5] * m[3] * m[14] + m[13] * m[2] * m[7] - m[13] * m[3] * m[6];
        inv[6] = -m[0] * m[6] * m[15] + m[0] * m[7] * m[14] + m[4] * m[2] * m[15]
                - m[4] * m[3] * m[14] - m[12] * m[2] * m[7] + m[12] * m[3] * m[6];
        inv[10] = m[0] * m[5] * m[15] - m[0] * m[7] * m[13] - m[4] * m[1] * m[15]
                + m[4] * m[3] * m[13] + m[12] * m[1] * m[7] - m[12] * m[3] * m[5];
        inv[14] = -m[0] * m[5] * m[14] + m[0] * m[6] * m[13] + m[4] * m[1] * m[14]
                - m[4] * m[2] * m[13] - m[12] * m[1] * m[6] + m[12] * m[2] * m[5];
        inv[3] = -m[1] * m[6] * m[11] + m[1] * m[7] * m[10] + m[5] * m[2] * m[11]
                - m[5] * m[3] * m[10] - m[9] * m[2] * m[7] + m[9] * m[3] * m[6];
        inv[7] = m[0] * m[6] * m[11] - m[0] * m[7] * m[10] - m[4] * m[2] * m[11]
                + m[4] * m[3] * m[10] + m[8] * m[2] * m[7] - m[8] * m[3] * m[6];
        inv[11] = -m[0] * m[5] * m[11] + m[0] * m[7] * m[9] + m[4] * m[1] * m[11]
                - m[4] * m[3] * m[9] - m[8] * m[1] * m[7] + m[8] * m[3] * m[5];
        inv[15] = m[0] * m[5] * m[10] - m[0] * m[6] * m[9] - m[4] * m[1] * m[10]
                + m[4] * m[2] * m[9] + m[8] * m[1] * m[6] - m[8] * m[2] * m[5];

        double determinant = m[0] * inv[0] + m[1] * inv[4] + m[2] * inv[8] + m[3] * inv[12];
        if (Math.abs(determinant) < 1e-30) {
            throw new IllegalArgumentException("The matrix is singular");
        }

        float[] result = new float[ELEMENTS];
        for (int i = 0; i < ELEMENTS; i++) {
            result[i] = (float) (inv[i] / determinant);
        }
        return result;
    }
}
