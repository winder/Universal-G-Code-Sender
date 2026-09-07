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
 * Builds triangle meshes in the {@link VertexLayout#MESH} layout for the fixed shapes the
 * visualizer draws itself.
 */
public final class SceneMeshes {
    public static final int FLOATS_PER_VERTEX = VertexLayout.MESH.floatsPerVertex();
    private static final double[] UP = {0, 0, 1};
    private static final double[] DOWN = {0, 0, -1};

    private SceneMeshes() {
    }

    /**
     * A closed cone with its tip at the origin and its base {@code height} above it, so the
     * tip marks the position it is translated to. Normals are per face, which gives the
     * faceted look of the JavaFX tool marker.
     */
    public static float[] cone(double radius, double height, int divisions) {
        float[] vertices = new float[divisions * 6 * FLOATS_PER_VERTEX];
        int offset = 0;
        for (int division = 0; division < divisions; division++) {
            double angle = 2 * Math.PI * division / divisions;
            double nextAngle = 2 * Math.PI * (division + 1) / divisions;
            double x1 = radius * Math.cos(angle);
            double y1 = radius * Math.sin(angle);
            double x2 = radius * Math.cos(nextAngle);
            double y2 = radius * Math.sin(nextAngle);

            // Side face, wound so the normal points away from the axis and down towards the tip.
            double[] normal = crossProduct(x2, y2, height, x1, y1, height);
            offset = writeVertex(vertices, offset, 0, 0, 0, normal);
            offset = writeVertex(vertices, offset, x2, y2, height, normal);
            offset = writeVertex(vertices, offset, x1, y1, height, normal);

            offset = writeVertex(vertices, offset, 0, 0, height, UP);
            offset = writeVertex(vertices, offset, x1, y1, height, UP);
            offset = writeVertex(vertices, offset, x2, y2, height, UP);
        }
        return vertices;
    }

    /**
     * A closed cylinder along the Z axis from {@code z = 0} to {@code z = height}.
     */
    public static float[] cylinder(double radius, double height, int divisions) {
        float[] vertices = new float[divisions * 12 * FLOATS_PER_VERTEX];
        int offset = 0;
        for (int division = 0; division < divisions; division++) {
            double angle = 2 * Math.PI * division / divisions;
            double nextAngle = 2 * Math.PI * (division + 1) / divisions;
            double x1 = radius * Math.cos(angle);
            double y1 = radius * Math.sin(angle);
            double x2 = radius * Math.cos(nextAngle);
            double y2 = radius * Math.sin(nextAngle);
            double[] normal1 = {Math.cos(angle), Math.sin(angle), 0};
            double[] normal2 = {Math.cos(nextAngle), Math.sin(nextAngle), 0};

            offset = writeVertex(vertices, offset, x1, y1, 0, normal1);
            offset = writeVertex(vertices, offset, x2, y2, 0, normal2);
            offset = writeVertex(vertices, offset, x2, y2, height, normal2);
            offset = writeVertex(vertices, offset, x1, y1, 0, normal1);
            offset = writeVertex(vertices, offset, x2, y2, height, normal2);
            offset = writeVertex(vertices, offset, x1, y1, height, normal1);

            offset = writeVertex(vertices, offset, 0, 0, height, UP);
            offset = writeVertex(vertices, offset, x1, y1, height, UP);
            offset = writeVertex(vertices, offset, x2, y2, height, UP);

            offset = writeVertex(vertices, offset, 0, 0, 0, DOWN);
            offset = writeVertex(vertices, offset, x2, y2, 0, DOWN);
            offset = writeVertex(vertices, offset, x1, y1, 0, DOWN);
        }
        return vertices;
    }

    /**
     * A square face of an axis aligned cube centred on the origin, facing along {@code normal},
     * which must be a unit axis vector such as {@code {0, 0, 1}}.
     */
    public static float[] cubeFace(double[] normal, double halfSize) {
        double[] u = Math.abs(normal[2]) > 0.5 ? new double[]{1, 0, 0} : new double[]{0, 0, 1};
        double[] v = {
                normal[1] * u[2] - normal[2] * u[1],
                normal[2] * u[0] - normal[0] * u[2],
                normal[0] * u[1] - normal[1] * u[0]
        };
        double[][] corners = new double[4][];
        double[][] signs = {{-1, -1}, {1, -1}, {1, 1}, {-1, 1}};
        for (int i = 0; i < 4; i++) {
            corners[i] = new double[3];
            for (int axis = 0; axis < 3; axis++) {
                corners[i][axis] = (normal[axis] + signs[i][0] * u[axis] + signs[i][1] * v[axis]) * halfSize;
            }
        }
        float[] vertices = new float[6 * FLOATS_PER_VERTEX];
        int offset = 0;
        for (int corner : new int[]{0, 1, 2, 0, 2, 3}) {
            offset = writeVertex(vertices, offset, corners[corner][0], corners[corner][1], corners[corner][2], normal);
        }
        return vertices;
    }

    private static int writeVertex(float[] vertices, int offset, double x, double y, double z, double[] normal) {
        vertices[offset] = (float) x;
        vertices[offset + 1] = (float) y;
        vertices[offset + 2] = (float) z;
        vertices[offset + 3] = (float) normal[0];
        vertices[offset + 4] = (float) normal[1];
        vertices[offset + 5] = (float) normal[2];
        return offset + FLOATS_PER_VERTEX;
    }

    private static double[] crossProduct(double ax, double ay, double az, double bx, double by, double bz) {
        double x = ay * bz - az * by;
        double y = az * bx - ax * bz;
        double z = ax * by - ay * bx;
        double length = Math.sqrt(x * x + y * y + z * z);
        if (length < 1e-9) {
            return UP;
        }
        return new double[]{x / length, y / length, z / length};
    }
}
