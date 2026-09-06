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

import javafx.scene.paint.Color;

import java.util.Arrays;

/**
 * Builds vertex arrays in the {@link VertexLayout#LINE} layout: two vertices per segment, each
 * holding a position, a colour and the number of the command that produced it.
 */
public final class LineMeshBuilder {
    public static final int NO_COMMAND = -1;
    private static final int FLOATS_PER_SEGMENT = 2 * VertexLayout.LINE.floatsPerVertex();

    private float[] data;
    private int size;

    public LineMeshBuilder() {
        this(64);
    }

    public LineMeshBuilder(int expectedSegments) {
        data = new float[Math.max(expectedSegments, 1) * FLOATS_PER_SEGMENT];
    }

    public LineMeshBuilder add(double x1, double y1, double z1, double x2, double y2, double z2, Color color) {
        return add(x1, y1, z1, x2, y2, z2, color, NO_COMMAND);
    }

    public LineMeshBuilder add(double x1, double y1, double z1, double x2, double y2, double z2,
                               Color color, int command) {
        return add(x1, y1, z1, x2, y2, z2,
                (float) color.getRed(), (float) color.getGreen(), (float) color.getBlue(), (float) color.getOpacity(), command);
    }

    public LineMeshBuilder add(double x1, double y1, double z1, double x2, double y2, double z2,
                               float red, float green, float blue, int command) {
        return add(x1, y1, z1, x2, y2, z2, red, green, blue, 1, command);
    }

    public LineMeshBuilder add(double x1, double y1, double z1, double x2, double y2, double z2,
                               float red, float green, float blue, float alpha, int command) {
        ensureCapacity(size + FLOATS_PER_SEGMENT);
        write(x1, y1, z1, red, green, blue, alpha, command);
        write(x2, y2, z2, red, green, blue, alpha, command);
        return this;
    }

    public int segmentCount() {
        return size / FLOATS_PER_SEGMENT;
    }

    public int vertexCount() {
        return segmentCount() * 2;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * The vertices built so far, trimmed to the exact length.
     */
    public float[] build() {
        return Arrays.copyOf(data, size);
    }

    /**
     * A flat grid of lines on the plane at {@code z}, snapped outwards to whole steps so the
     * lines always land on round coordinates.
     */
    public static LineMeshBuilder grid(double minX, double minY, double maxX, double maxY,
                                       double step, double z, Color color) {
        double x1 = Math.floor(minX / step) * step;
        double y1 = Math.floor(minY / step) * step;
        double x2 = Math.ceil(maxX / step) * step;
        double y2 = Math.ceil(maxY / step) * step;
        int columns = (int) Math.round((x2 - x1) / step) + 1;
        int rows = (int) Math.round((y2 - y1) / step) + 1;
        LineMeshBuilder builder = new LineMeshBuilder(columns + rows);
        for (int column = 0; column < columns; column++) {
            double x = x1 + column * step;
            builder.add(x, y1, z, x, y2, z, color);
        }
        for (int row = 0; row < rows; row++) {
            double y = y1 + row * step;
            builder.add(x1, y, z, x2, y, z, color);
        }
        return builder;
    }

    private void write(double x, double y, double z, float red, float green, float blue, float alpha, int command) {
        data[size++] = (float) x;
        data[size++] = (float) y;
        data[size++] = (float) z;
        data[size++] = red;
        data[size++] = green;
        data[size++] = blue;
        data[size++] = alpha;
        data[size++] = command;
    }

    private void ensureCapacity(int required) {
        if (required > data.length) {
            data = Arrays.copyOf(data, Math.max(required, data.length * 2));
        }
    }
}
