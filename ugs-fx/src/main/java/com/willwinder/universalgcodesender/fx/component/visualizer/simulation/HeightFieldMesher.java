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

import com.willwinder.universalgcodesender.fx.component.visualizer.scene.VertexLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Turns a {@link HeightField} into a closed triangle mesh in the {@link VertexLayout#MESH}
 * layout: the surface on top, four walls down to the stock bottom and a bottom face. Cells whose
 * corners have all been cut down to the stock bottom are holes: they get neither a top nor a bottom
 * face, so a cut that reaches the bottom goes through the block.
 *
 * <p>The mesh layout carries no colour, so to colour the surface by depth it is split into depth
 * bands by {@link #meshByDepth}: one mesh per band, each drawn with its own colour. Band zero is
 * untouched material and also holds the walls and the bottom; the remaining bands divide the
 * distance from the top to the bottom evenly, so any cut at all leaves band zero.
 *
 * <p>Surface normals are central differences of the heights, so carved slopes shade smoothly.
 * Cells whose four corners are level, and whose corner normals point straight up, are merged
 * into runs along each row. Untouched stock and flat pocket floors, which make up most of a
 * typical part, collapse into a couple of triangles per row that way.
 */
public final class HeightFieldMesher {
    private static final int FLOATS_PER_VERTEX = VertexLayout.MESH.floatsPerVertex();
    private static final float[] UP = {0, 0, 1};
    private static final float[] DOWN = {0, 0, -1};
    private static final double UNTOUCHED_EPSILON = 1e-6;

    private HeightFieldMesher() {
    }

    public record Mesh(float[] vertices, int vertexCount) {
        public int triangleCount() {
            return vertexCount / 3;
        }

        public boolean isEmpty() {
            return vertexCount == 0;
        }
    }

    /**
     * The whole block as one mesh.
     */
    public static Mesh mesh(HeightField field) {
        return meshByDepth(field, 1).get(0);
    }

    /**
     * The block split into {@code bands} meshes by depth. See {@link #band} for how a height maps
     * to a band. Bands nothing falls into are empty meshes.
     */
    public static List<Mesh> meshByDepth(HeightField field, int bands) {
        int count = Math.max(1, bands);
        float[] normals = nodeNormals(field);
        FloatBuffer[] out = new FloatBuffer[count];
        for (int i = 0; i < count; i++) {
            out[i] = new FloatBuffer(i == 0 ? Math.max(1024, field.nodeCount() / 4) * FLOATS_PER_VERTEX : 1024);
        }
        surface(field, normals, out, count);
        walls(field, out[0]);
        bottom(field, out[0]);
        List<Mesh> meshes = new ArrayList<>(count);
        for (FloatBuffer buffer : out) {
            meshes.add(new Mesh(buffer.array(), buffer.size() / FLOATS_PER_VERTEX));
        }
        return meshes;
    }

    /**
     * The depth band a surface height belongs to. Band zero is the untouched top. The cut bands
     * divide the full thickness of the block evenly, so band one is the shallowest cut and the last
     * band reaches the bottom.
     */
    public static int band(HeightField field, double z, int bands) {
        if (bands <= 1) {
            return 0;
        }
        double depth = field.topZ() - z;
        if (depth <= UNTOUCHED_EPSILON) {
            return 0;
        }
        double thickness = field.topZ() - field.bottomZ();
        int cutBands = bands - 1;
        int index = thickness <= 0 ? cutBands - 1 : (int) Math.floor(depth / thickness * cutBands);
        return 1 + Math.max(0, Math.min(cutBands - 1, index));
    }

    private static void surface(HeightField field, float[] normals, FloatBuffer[] out, int bands) {
        int columns = field.columns();
        for (int row = 0; row < field.rows() - 1; row++) {
            int column = 0;
            while (column < columns - 1) {
                if (isLevel(field, normals, column, row)) {
                    float z = field.heightAt(column, row);
                    int end = column + 1;
                    while (end < columns - 1 && isLevel(field, normals, end, row) && field.heightAt(end, row) == z) {
                        end++;
                    }
                    if (!field.isThrough(column, row)) {
                        quad(out[band(field, z, bands)],
                                field.x(column), field.y(row), z, UP,
                                field.x(end), field.y(row), z, UP,
                                field.x(end), field.y(row + 1), z, UP,
                                field.x(column), field.y(row + 1), z, UP);
                    }
                    column = end;
                } else {
                    cell(field, normals, column, row, out, bands);
                    column++;
                }
            }
        }
    }

    /**
     * A cell is level when its four corner nodes share a height and all their neighbours do too,
     * so that the corner normals are exactly up and merging cells changes nothing visually.
     */
    private static boolean isLevel(HeightField field, float[] normals, int column, int row) {
        float z = field.heightAt(column, row);
        return field.heightAt(column + 1, row) == z
                && field.heightAt(column, row + 1) == z
                && field.heightAt(column + 1, row + 1) == z
                && isUp(normals, field, column, row)
                && isUp(normals, field, column + 1, row)
                && isUp(normals, field, column, row + 1)
                && isUp(normals, field, column + 1, row + 1);
    }

    private static boolean isUp(float[] normals, HeightField field, int column, int row) {
        int index = (row * field.columns() + column) * 3;
        return normals[index] == 0 && normals[index + 1] == 0;
    }

    private static void cell(HeightField field, float[] normals, int column, int row, FloatBuffer[] out, int bands) {
        int columns = field.columns();
        int n00 = (row * columns + column) * 3;
        int n10 = n00 + 3;
        int n01 = n00 + columns * 3;
        int n11 = n01 + 3;
        double x0 = field.x(column);
        double x1 = field.x(column + 1);
        double y0 = field.y(row);
        double y1 = field.y(row + 1);
        float z00 = field.heightAt(column, row);
        float z10 = field.heightAt(column + 1, row);
        float z01 = field.heightAt(column, row + 1);
        float z11 = field.heightAt(column + 1, row + 1);
        // A sloped cell is coloured by its mean height so both of its triangles land in one band
        FloatBuffer target = out[band(field, (z00 + z10 + z01 + z11) / 4.0, bands)];
        // Split along the diagonal whose end heights are closest, which keeps carved ridges and
        // grooves that run diagonally from looking stepped.
        if (Math.abs(z00 - z11) <= Math.abs(z10 - z01)) {
            triangle(target, x0, y0, z00, normals, n00, x1, y0, z10, normals, n10, x1, y1, z11, normals, n11);
            triangle(target, x0, y0, z00, normals, n00, x1, y1, z11, normals, n11, x0, y1, z01, normals, n01);
        } else {
            triangle(target, x0, y0, z00, normals, n00, x1, y0, z10, normals, n10, x0, y1, z01, normals, n01);
            triangle(target, x1, y0, z10, normals, n10, x1, y1, z11, normals, n11, x0, y1, z01, normals, n01);
        }
    }

    private static void walls(HeightField field, FloatBuffer out) {
        int columns = field.columns();
        int rows = field.rows();
        double bottom = field.bottomZ();
        double minX = field.x(0);
        double maxX = field.x(columns - 1);
        double minY = field.y(0);
        double maxY = field.y(rows - 1);

        // Walls along X at the minimum and maximum Y
        wall(out, columns, field::x, i -> field.heightAt(i, 0), minY, bottom, true, new float[]{0, -1, 0});
        wall(out, columns, field::x, i -> field.heightAt(i, rows - 1), maxY, bottom, true, new float[]{0, 1, 0});
        // Walls along Y at the minimum and maximum X
        wall(out, rows, field::y, i -> field.heightAt(0, i), minX, bottom, false, new float[]{-1, 0, 0});
        wall(out, rows, field::y, i -> field.heightAt(columns - 1, i), maxX, bottom, false, new float[]{1, 0, 0});
    }

    private interface IndexToDouble {
        double get(int index);
    }

    /**
     * One wall of the block: a strip from the bottom up to the surface heights along one edge.
     * Runs of nodes at the same height are merged into a single quad.
     */
    private static void wall(FloatBuffer out, int count, IndexToDouble along, IndexToDouble height,
                             double across, double bottom, boolean alongX, float[] normal) {
        int start = 0;
        while (start < count - 1) {
            double z = height.get(start);
            int end = start + 1;
            while (end < count - 1 && height.get(end) == z && height.get(end + 1) == z) {
                end++;
            }
            double a0 = along.get(start);
            double a1 = along.get(end);
            double zEnd = height.get(end);
            if (alongX) {
                quad(out, a0, across, bottom, normal, a1, across, bottom, normal, a1, across, zEnd, normal, a0, across, z, normal);
            } else {
                quad(out, across, a0, bottom, normal, across, a1, bottom, normal, across, a1, zEnd, normal, across, a0, z, normal);
            }
            start = end;
        }
    }

    /**
     * The underside, left open under cells that have been cut through. Runs of cells that still
     * have material are merged along each row.
     */
    private static void bottom(HeightField field, FloatBuffer out) {
        double z = field.bottomZ();
        int columns = field.columns();
        for (int row = 0; row < field.rows() - 1; row++) {
            int column = 0;
            while (column < columns - 1) {
                if (isHole(field, column, row)) {
                    column++;
                    continue;
                }
                int end = column + 1;
                while (end < columns - 1 && !isHole(field, end, row)) {
                    end++;
                }
                quad(out,
                        field.x(column), field.y(row), z, DOWN,
                        field.x(end), field.y(row), z, DOWN,
                        field.x(end), field.y(row + 1), z, DOWN,
                        field.x(column), field.y(row + 1), z, DOWN);
                column = end;
            }
        }
    }

    /**
     * A cell whose four corners are all cut down to the bottom has no material left.
     */
    private static boolean isHole(HeightField field, int column, int row) {
        return field.isThrough(column, row)
                && field.isThrough(column + 1, row)
                && field.isThrough(column, row + 1)
                && field.isThrough(column + 1, row + 1);
    }

    /**
     * Unit normals per node from central differences, one sided along the edges. Level nodes get
     * exactly {@code (0, 0, 1)} so the mesher can detect them.
     */
    static float[] nodeNormals(HeightField field) {
        int columns = field.columns();
        int rows = field.rows();
        float[] normals = new float[columns * rows * 3];
        double cell = field.cellSize();
        for (int row = 0; row < rows; row++) {
            for (int column = 0; column < columns; column++) {
                int left = Math.max(0, column - 1);
                int right = Math.min(columns - 1, column + 1);
                int below = Math.max(0, row - 1);
                int above = Math.min(rows - 1, row + 1);
                double dzdx = (field.heightAt(right, row) - field.heightAt(left, row)) / ((right - left) * cell);
                double dzdy = (field.heightAt(column, above) - field.heightAt(column, below)) / ((above - below) * cell);
                int index = (row * columns + column) * 3;
                if (dzdx == 0 && dzdy == 0) {
                    normals[index + 2] = 1;
                } else {
                    double length = Math.sqrt(dzdx * dzdx + dzdy * dzdy + 1);
                    normals[index] = (float) (-dzdx / length);
                    normals[index + 1] = (float) (-dzdy / length);
                    normals[index + 2] = (float) (1 / length);
                }
            }
        }
        return normals;
    }

    private static void quad(FloatBuffer out,
                             double x0, double y0, double z0, float[] n0,
                             double x1, double y1, double z1, float[] n1,
                             double x2, double y2, double z2, float[] n2,
                             double x3, double y3, double z3, float[] n3) {
        vertex(out, x0, y0, z0, n0, 0);
        vertex(out, x1, y1, z1, n1, 0);
        vertex(out, x2, y2, z2, n2, 0);
        vertex(out, x0, y0, z0, n0, 0);
        vertex(out, x2, y2, z2, n2, 0);
        vertex(out, x3, y3, z3, n3, 0);
    }

    private static void triangle(FloatBuffer out,
                                 double x0, double y0, double z0, float[] normals0, int n0,
                                 double x1, double y1, double z1, float[] normals1, int n1,
                                 double x2, double y2, double z2, float[] normals2, int n2) {
        vertex(out, x0, y0, z0, normals0, n0);
        vertex(out, x1, y1, z1, normals1, n1);
        vertex(out, x2, y2, z2, normals2, n2);
    }

    private static void vertex(FloatBuffer out, double x, double y, double z, float[] normals, int normalIndex) {
        out.add((float) x);
        out.add((float) y);
        out.add((float) z);
        out.add(normals[normalIndex]);
        out.add(normals[normalIndex + 1]);
        out.add(normals[normalIndex + 2]);
    }

    /**
     * A growable float array.
     */
    static final class FloatBuffer {
        private float[] data;
        private int size;

        FloatBuffer(int capacity) {
            data = new float[capacity];
        }

        void add(float value) {
            if (size == data.length) {
                data = Arrays.copyOf(data, Math.max(16, data.length * 2));
            }
            data[size++] = value;
        }

        int size() {
            return size;
        }

        float[] array() {
            return size == data.length ? data : Arrays.copyOf(data, size);
        }
    }
}
