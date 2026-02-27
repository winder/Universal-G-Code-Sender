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
package com.willwinder.universalgcodesender.fx.component.visualizer;

import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

import java.awt.Point;
import java.util.logging.Logger;

public class GcodeModelMaterial extends PhongMaterial {
    private static final Logger LOGGER = Logger.getLogger(GcodeModelMaterial.class.getSimpleName());
    private static final int MAX_TEXTURE_WIDTH = 8192;

    private final int numberOfLines;
    private final int textureWidth;
    private final int textureHeight;

    private final WritableImage originalTexture;
    private final WritableImage texture;

    public GcodeModelMaterial(int numberOfLines) {
        super();
        this.numberOfLines = Math.max(numberOfLines, 1);
        textureWidth = MAX_TEXTURE_WIDTH;
        textureHeight = Math.max(1, (int) Math.ceil((double) numberOfLines / (double) textureWidth));
        LOGGER.info(String.format("Generated Gcode texture: %sx%s for %d lines", textureWidth, textureHeight, numberOfLines));

        originalTexture = new WritableImage(textureWidth, textureHeight);
        texture = new WritableImage(textureWidth, textureHeight);

        // Initialize all pixels to BLACK (including unused padding pixels)
        PixelWriter writer = originalTexture.getPixelWriter();
        for (int y = 0; y < textureHeight; y++) {
            for (int x = 0; x < textureWidth; x++) {
                writer.setColor(x, y, Color.BLACK);
            }
        }

        copyTexture(originalTexture, texture);
        setDiffuseMap(texture);
    }

    /**
     * Sets the initial line color
     *
     * @param lineIndex the line to set
     * @param color     the color to set
     */
    public void setLineColor(int lineIndex, Color color) {
        Point pixel = getTexturePosition(lineIndex);
        originalTexture.getPixelWriter().setColor(pixel.x, pixel.y, color);
        texture.getPixelWriter().setColor(pixel.x, pixel.y, color);
    }

    /**
     * Updates the current line color temporarily and will be reset back using with the {@link #reset()} function
     *
     * @param lineIndex the line to update
     * @param color     the color to temporarily use
     */
    public void updateLineColor(int lineIndex, Color color) {
        Point pixel = getTexturePosition(lineIndex);
        texture.getPixelWriter().setColor(pixel.x, pixel.y, color);
    }

    /**
     * Resets all line colors back to the initial color
     */
    public void reset() {
        copyTexture(originalTexture, texture);
    }

    public Point getTexturePosition(int lineIndex) {
        if (lineIndex < 0 || lineIndex >= numberOfLines) {
            return new Point(0, 0);
        }
        int x = lineIndex % textureWidth;
        int y = lineIndex / textureWidth;
        return new Point(x, y);
    }

    private void copyTexture(WritableImage source, WritableImage destination) {
        PixelReader reader = source.getPixelReader();
        PixelWriter writer = destination.getPixelWriter();

        int width = (int) source.getWidth();
        int height = (int) source.getHeight();

        byte[] buffer = new byte[width * height * 4]; // BGRA format (4 bytes per pixel)
        reader.getPixels(0, 0, width, height, PixelFormat.getByteBgraInstance(), buffer, 0, width * 4);
        writer.setPixels(0, 0, width, height, PixelFormat.getByteBgraInstance(), buffer, 0, width * 4);
    }

    /**
     * UV coordinate for the *center* of the cell, to avoid sampling borders.
     */
    public float[] getTextureUV(int lineIndex) {
        Point p = getTexturePosition(lineIndex);

        float u = (float) (p.x + 0.5) / (float) textureWidth;
        float v = (float) (p.y + 0.5) / (float) textureHeight;

        return new float[]{u, v};
    }
}
