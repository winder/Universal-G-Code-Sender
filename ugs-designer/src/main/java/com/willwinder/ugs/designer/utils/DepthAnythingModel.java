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
package com.willwinder.ugs.designer.utils;

import ai.onnxruntime.OrtException;

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.util.logging.Logger;

/**
 * Depth Anything V2. Takes a square {@code [1, 3, S, S]} input where {@code S} is a
 * multiple of the ViT patch size, and outputs relative inverse depth (larger = closer)
 *
 * @author Joacim Breiler
 */
public class DepthAnythingModel extends AbstractOnnxDepthModel {

    private static final Logger LOGGER = Logger.getLogger(DepthAnythingModel.class.getSimpleName());

    /**
     * ViT patch size - the model input side length must be a multiple of this.
     */
    private static final int PATCH_SIZE = 14;

    /**
     * The side length the published onnx-community exports were traced with. Used as a fallback when a
     * higher requested resolution is rejected by a model with fixed input dimensions.
     */
    private static final int FALLBACK_INPUT_SIZE = 518;

    /**
     * A higher model input side length resolves finer detail at the cost of slower inference and more
     * memory. Overridable via the system property below; rounded to a multiple of PATCH_SIZE.
     */
    private static final String INPUT_SIZE_PROPERTY = "ugs.designer.depthInputSize";
    private static final int DEFAULT_INPUT_SIZE = 1036;

    private final int inputSize;

    public DepthAnythingModel(Path modelPath) {
        this(modelPath, resolveInputSize());
    }

    public DepthAnythingModel(Path modelPath, int inputSize) {
        super(modelPath);
        this.inputSize = roundToPatchSize(inputSize);
    }

    private static int resolveInputSize() {
        String configured = System.getProperty(INPUT_SIZE_PROPERTY);
        if (configured != null && !configured.isBlank()) {
            try {
                return Integer.parseInt(configured.trim());
            } catch (NumberFormatException e) {
                // fall through to the default
            }
        }
        return DEFAULT_INPUT_SIZE;
    }

    private static int roundToPatchSize(int size) {
        int rounded = Math.round(size / (float) PATCH_SIZE) * PATCH_SIZE;
        return Math.max(PATCH_SIZE, rounded);
    }


    public float[][] estimateDepth(BufferedImage source) {
        requireAvailable();

        int origW = source.getWidth();
        int origH = source.getHeight();

        try {
            float[][][] raw;
            try {
                raw = runAt(source, inputSize);
            } catch (OrtException e) {
                if (inputSize == FALLBACK_INPUT_SIZE) {
                    throw e;
                }
                // A model with fixed input dimensions rejects the higher resolution: fall back to 518
                LOGGER.info("Depth model rejected input size " + inputSize + ", retrying at " + FALLBACK_INPUT_SIZE);
                raw = runAt(source, FALLBACK_INPUT_SIZE);
            }
            return resizeBilinear(raw[0], raw[0].length, raw[0][0].length, origH, origW);
        } catch (OrtException e) {
            throw new IllegalStateException("Failed to generate depth map", e);
        }
    }

    private float[][][] runAt(BufferedImage source, int size) throws OrtException {
        // Depth Anything V2 takes a 4-dim [batch, 3, H, W] input
        return runOnce(modelPath, preprocess(source, size, size), new long[]{1, 3, size, size});
    }
}
