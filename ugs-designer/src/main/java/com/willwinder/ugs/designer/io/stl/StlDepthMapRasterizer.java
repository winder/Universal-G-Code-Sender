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
package com.willwinder.ugs.designer.io.stl;

import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferUShort;
import java.util.Arrays;

/**
 * Renders a mesh as a grayscale depth map seen from above: for every pixel the highest surface found
 * along -Z is kept, the brightest pixel being the top of the model and black being its lowest point.
 * This matches the height map convention used when carving a
 * {@link com.willwinder.ugs.designer.entities.cuttable.Raster}, so the depth map can be carved as a
 * relief of the original mesh.
 * <p>
 * Areas the mesh does not cover are left at the lowest point, meaning they are cleared down to the
 * bottom of the model.
 *
 * @author Joacim Breiler
 */
public class StlDepthMapRasterizer {
    public static final double DEFAULT_PIXELS_PER_MM = 10;
    private static final int MAX_SAMPLE = 65535;
    private static final int MIN_PIXELS = 2;
    private static final int MAX_PIXELS = 2048;

    private final double pixelsPerMm;

    public StlDepthMapRasterizer() {
        this(DEFAULT_PIXELS_PER_MM);
    }

    public StlDepthMapRasterizer(double pixelsPerMm) {
        this.pixelsPerMm = pixelsPerMm;
    }

    public BufferedImage rasterize(StlMesh mesh) {
        Rectangle2D.Double bounds = mesh.getBounds();
        int width = pixelCount(bounds.width, bounds.height);
        int height = pixelCount(bounds.height, bounds.width);

        float[] depthBuffer = renderDepthBuffer(mesh, bounds, width, height);
        return toGrayscale(depthBuffer, width, height, mesh.getMinZ(), mesh.getMaxZ());
    }

    /**
     * The resolution follows the physical size of the model, but the longest side is capped so that a
     * large model does not produce an unreasonably large image.
     */
    private int pixelCount(double size, double otherSize) {
        double longestSide = Math.max(size, otherSize);
        double scale = Math.min(1, MAX_PIXELS / Math.max(1e-9, longestSide * pixelsPerMm));
        return (int) Math.max(MIN_PIXELS, Math.round(size * pixelsPerMm * scale));
    }

    private static float[] renderDepthBuffer(StlMesh mesh, Rectangle2D.Double bounds, int width, int height) {
        float[] depthBuffer = new float[width * height];
        Arrays.fill(depthBuffer, Float.NEGATIVE_INFINITY);

        // Pixel centers sample the model, so world and pixel space are offset by half a pixel
        double pixelWidth = bounds.width / width;
        double pixelHeight = bounds.height / height;
        if (pixelWidth <= 0 || pixelHeight <= 0) {
            return depthBuffer;
        }

        float[] coordinates = mesh.getCoordinates();
        for (int i = 0; i < coordinates.length; i += StlMesh.COORDINATES_PER_TRIANGLE) {
            // Rows are ordered top down while the world is Y-up, so the Y axis is flipped here
            double x0 = (coordinates[i] - bounds.x) / pixelWidth - 0.5;
            double y0 = (bounds.getMaxY() - coordinates[i + 1]) / pixelHeight - 0.5;
            double x1 = (coordinates[i + 3] - bounds.x) / pixelWidth - 0.5;
            double y1 = (bounds.getMaxY() - coordinates[i + 4]) / pixelHeight - 0.5;
            double x2 = (coordinates[i + 6] - bounds.x) / pixelWidth - 0.5;
            double y2 = (bounds.getMaxY() - coordinates[i + 7]) / pixelHeight - 0.5;

            rasterizeTriangle(depthBuffer, width, height,
                    x0, y0, coordinates[i + 2],
                    x1, y1, coordinates[i + 5],
                    x2, y2, coordinates[i + 8]);
        }

        return depthBuffer;
    }

    private static void rasterizeTriangle(float[] depthBuffer, int width, int height,
                                          double x0, double y0, float z0,
                                          double x1, double y1, float z1,
                                          double x2, double y2, float z2) {
        double area = edge(x0, y0, x1, y1, x2, y2);
        if (area == 0) {
            // Seen edge on from above, so it contributes no surface
            return;
        }

        int minX = (int) Math.max(0, Math.ceil(Math.min(x0, Math.min(x1, x2))));
        int maxX = (int) Math.min(width - 1, Math.floor(Math.max(x0, Math.max(x1, x2))));
        int minY = (int) Math.max(0, Math.ceil(Math.min(y0, Math.min(y1, y2))));
        int maxY = (int) Math.min(height - 1, Math.floor(Math.max(y0, Math.max(y1, y2))));

        // A tolerance relative to the triangle size keeps pixels exactly on a shared edge covered
        double tolerance = Math.abs(area) * 1e-9;

        for (int y = minY; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                double w0 = edge(x1, y1, x2, y2, x, y) / area;
                double w1 = edge(x2, y2, x0, y0, x, y) / area;
                double w2 = edge(x0, y0, x1, y1, x, y) / area;
                if (w0 < -tolerance || w1 < -tolerance || w2 < -tolerance) {
                    continue;
                }

                float z = (float) (w0 * z0 + w1 * z1 + w2 * z2);
                int index = y * width + x;
                if (z > depthBuffer[index]) {
                    depthBuffer[index] = z;
                }
            }
        }
    }

    private static double edge(double ax, double ay, double bx, double by, double px, double py) {
        return (bx - ax) * (py - ay) - (by - ay) * (px - ax);
    }

    /**
     * The depth map is 16-bit so that the height resolution is limited by the mesh rather than by the
     * 256 steps an 8-bit image would allow.
     */
    private static BufferedImage toGrayscale(float[] depthBuffer, int width, int height, double minZ, double maxZ) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_USHORT_GRAY);
        short[] pixels = ((DataBufferUShort) image.getRaster().getDataBuffer()).getData();

        double range = maxZ - minZ;
        for (int i = 0; i < depthBuffer.length; i++) {
            float z = depthBuffer[i];
            if (z == Float.NEGATIVE_INFINITY) {
                pixels[i] = 0;
            } else if (range <= 0) {
                pixels[i] = (short) MAX_SAMPLE;
            } else {
                double normalized = (z - minZ) / range;
                pixels[i] = (short) Math.round(Math.max(0, Math.min(1, normalized)) * MAX_SAMPLE);
            }
        }

        return image;
    }
}
