package com.willwinder.universalgcodesender.fx.component.visualizer;

import javafx.scene.image.PixelFormat;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;

public class GcodeModelMaterial extends PhongMaterial {
    private final WritableImage originalTexture;
    private final WritableImage texture;

    public GcodeModelMaterial(int numberOfLines) {
        super();
        numberOfLines = Math.max(numberOfLines, 1);

        originalTexture = new WritableImage(1, numberOfLines);
        texture = new WritableImage(1, numberOfLines);

        PixelWriter writer = originalTexture.getPixelWriter();
        for (int i = 0; i < numberOfLines; i++) {
            writer.setColor(0, i, Color.BLACK);
        }

        setDiffuseMap(texture);
    }

    /**
     * Sets the initial line color
     *
     * @param lineIndex the line to set
     * @param color     the color to set
     */
    public void setLineColor(int lineIndex, Color color) {
        originalTexture.getPixelWriter().setColor(0, lineIndex, color);
        texture.getPixelWriter().setColor(0, lineIndex, color);
    }

    /**
     * Updates the current line color temporarily and will be reset back using with the {@link #reset()} function
     *
     * @param lineIndex the line to update
     * @param color     the color to temporarily use
     */
    public void updateLineColor(int lineIndex, Color color) {
        texture.getPixelWriter().setColor(0, lineIndex, color);
    }

    /**
     * Resets all line colors back to the initial color
     */
    public void reset() {
        copyTexture(originalTexture, texture);
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

}
