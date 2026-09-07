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

import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.Shape3D;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;

/**
 * Turns the JavaFX geometry of a {@link Shape3D} into the interleaved position and normal
 * vertices the scene pipeline draws.
 *
 * <p>JavaFX indexes points, normals and texture coordinates separately per face corner, so the
 * faces have to be flattened into independent vertices. When a mesh carries no normals a face
 * normal is computed, which gives the same faceted look the JavaFX visualizer has.
 *
 * <p>{@link Box} and {@link Cylinder} keep their geometry private to the JavaFX rendering
 * pipeline, so their meshes are rebuilt here from the dimensions instead.
 */
public final class MeshConverter {
    private MeshConverter() {
    }

    public static boolean isSupported(Shape3D shape) {
        return (shape instanceof MeshView meshView && meshView.getMesh() instanceof TriangleMesh)
                || shape instanceof Box
                || shape instanceof Cylinder;
    }

    public static float[] toVertices(Shape3D shape) {
        if (shape instanceof MeshView meshView && meshView.getMesh() instanceof TriangleMesh mesh) {
            return fromTriangleMesh(mesh);
        } else if (shape instanceof Box box) {
            return fromBox(box);
        } else if (shape instanceof Cylinder cylinder) {
            return fromCylinder(cylinder);
        }
        throw new IllegalArgumentException("Unsupported shape " + shape.getClass().getName());
    }

    private static float[] fromTriangleMesh(TriangleMesh mesh) {
        float[] points = mesh.getPoints().toArray(null);
        float[] normals = mesh.getNormals().toArray(null);
        int[] faces = mesh.getFaces().toArray(null);

        VertexFormat format = mesh.getVertexFormat();
        int indexSize = format.getVertexIndexSize();
        int pointOffset = format.getPointIndexOffset();
        int normalOffset = format.getNormalIndexOffset();
        boolean hasNormals = normalOffset >= 0 && normals.length > 0;

        int triangles = faces.length / (indexSize * 3);
        float[] vertices = new float[triangles * 3 * SceneMeshes.FLOATS_PER_VERTEX];

        int target = 0;
        for (int triangle = 0; triangle < triangles; triangle++) {
            int face = triangle * indexSize * 3;
            int point0 = faces[face + pointOffset] * 3;
            int point1 = faces[face + indexSize + pointOffset] * 3;
            int point2 = faces[face + indexSize * 2 + pointOffset] * 3;
            float[] faceNormal = hasNormals ? null : faceNormal(points, point0, point1, point2);

            for (int corner = 0; corner < 3; corner++) {
                int cornerFace = face + indexSize * corner;
                int point = faces[cornerFace + pointOffset] * 3;
                vertices[target++] = points[point];
                vertices[target++] = points[point + 1];
                vertices[target++] = points[point + 2];

                if (hasNormals) {
                    int normal = faces[cornerFace + normalOffset] * 3;
                    vertices[target++] = normals[normal];
                    vertices[target++] = normals[normal + 1];
                    vertices[target++] = normals[normal + 2];
                } else {
                    vertices[target++] = faceNormal[0];
                    vertices[target++] = faceNormal[1];
                    vertices[target++] = faceNormal[2];
                }
            }
        }
        return vertices;
    }

    private static float[] fromBox(Box box) {
        float x = (float) box.getWidth() / 2;
        float y = (float) box.getHeight() / 2;
        float z = (float) box.getDepth() / 2;

        float[][] corners = {
                {-x, -y, -z}, {x, -y, -z}, {x, y, -z}, {-x, y, -z},
                {-x, -y, z}, {x, -y, z}, {x, y, z}, {-x, y, z}
        };
        int[][] quads = {
                {0, 3, 2, 1}, {4, 5, 6, 7},
                {0, 1, 5, 4}, {3, 7, 6, 2},
                {0, 4, 7, 3}, {1, 2, 6, 5}
        };

        float[] vertices = new float[quads.length * 6 * SceneMeshes.FLOATS_PER_VERTEX];
        int target = 0;
        for (int[] quad : quads) {
            float[] normal = normal(corners[quad[0]], corners[quad[1]], corners[quad[2]]);
            int[] triangles = {quad[0], quad[1], quad[2], quad[0], quad[2], quad[3]};
            for (int corner : triangles) {
                target = write(vertices, target, corners[corner], normal);
            }
        }
        return vertices;
    }

    /**
     * A cylinder along the Y axis centred on the origin, matching how JavaFX orients its own.
     */
    private static float[] fromCylinder(Cylinder cylinder) {
        int divisions = Math.max(cylinder.getDivisions(), 3);
        float radius = (float) cylinder.getRadius();
        float top = (float) cylinder.getHeight() / 2;
        float bottom = -top;

        float[] vertices = new float[divisions * 12 * SceneMeshes.FLOATS_PER_VERTEX];
        int target = 0;
        for (int division = 0; division < divisions; division++) {
            double angle = 2 * Math.PI * division / divisions;
            double nextAngle = 2 * Math.PI * (division + 1) / divisions;

            float[] outer = {(float) (radius * Math.cos(angle)), 0, (float) (radius * Math.sin(angle))};
            float[] nextOuter = {(float) (radius * Math.cos(nextAngle)), 0, (float) (radius * Math.sin(nextAngle))};

            float[] a = {outer[0], bottom, outer[2]};
            float[] b = {nextOuter[0], bottom, nextOuter[2]};
            float[] c = {nextOuter[0], top, nextOuter[2]};
            float[] d = {outer[0], top, outer[2]};

            float[] sideNormal = normalize(new float[]{outer[0], 0, outer[2]});
            float[] nextSideNormal = normalize(new float[]{nextOuter[0], 0, nextOuter[2]});

            target = write(vertices, target, a, sideNormal);
            target = write(vertices, target, b, nextSideNormal);
            target = write(vertices, target, c, nextSideNormal);
            target = write(vertices, target, a, sideNormal);
            target = write(vertices, target, c, nextSideNormal);
            target = write(vertices, target, d, sideNormal);

            float[] up = {0, 1, 0};
            target = write(vertices, target, new float[]{0, top, 0}, up);
            target = write(vertices, target, d, up);
            target = write(vertices, target, c, up);

            float[] down = {0, -1, 0};
            target = write(vertices, target, new float[]{0, bottom, 0}, down);
            target = write(vertices, target, b, down);
            target = write(vertices, target, a, down);
        }
        return vertices;
    }

    private static int write(float[] vertices, int target, float[] position, float[] normal) {
        vertices[target] = position[0];
        vertices[target + 1] = position[1];
        vertices[target + 2] = position[2];
        vertices[target + 3] = normal[0];
        vertices[target + 4] = normal[1];
        vertices[target + 5] = normal[2];
        return target + SceneMeshes.FLOATS_PER_VERTEX;
    }

    private static float[] faceNormal(float[] points, int point0, int point1, int point2) {
        return normal(
                new float[]{points[point0], points[point0 + 1], points[point0 + 2]},
                new float[]{points[point1], points[point1 + 1], points[point1 + 2]},
                new float[]{points[point2], points[point2 + 1], points[point2 + 2]});
    }

    private static float[] normal(float[] a, float[] b, float[] c) {
        float ux = b[0] - a[0];
        float uy = b[1] - a[1];
        float uz = b[2] - a[2];
        float vx = c[0] - a[0];
        float vy = c[1] - a[1];
        float vz = c[2] - a[2];
        return normalize(new float[]{uy * vz - uz * vy, uz * vx - ux * vz, ux * vy - uy * vx});
    }

    private static float[] normalize(float[] vector) {
        float length = (float) Math.sqrt(vector[0] * vector[0] + vector[1] * vector[1] + vector[2] * vector[2]);
        if (length < 1e-9f) {
            return new float[]{0, 0, 1};
        }
        return new float[]{vector[0] / length, vector[1] / length, vector[2] / length};
    }
}
