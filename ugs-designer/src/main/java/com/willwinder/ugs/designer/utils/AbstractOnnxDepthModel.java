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

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtSession;

import java.awt.RenderingHints;
import java.awt.image.BufferedImage;
import java.nio.FloatBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Map;

/**
 * Shared plumbing for ONNX-based {@link DepthModel} implementations: ImageNet preprocessing, running an
 * inference session and resizing the output. Subclasses supply the model-specific input dimensions and
 * output orientation.
 *
 * @author Joacim Breiler
 */
public abstract class AbstractOnnxDepthModel implements DepthModel {

    // ImageNet normalization
    private static final float[] MEAN = {0.485f, 0.456f, 0.406f};
    private static final float[] STD = {0.229f, 0.224f, 0.225f};

    protected final Path modelPath;

    protected AbstractOnnxDepthModel(Path modelPath) {
        this.modelPath = modelPath;
    }

    @Override
    public Path getModelPath() {
        return modelPath;
    }

    @Override
    public boolean isAvailable() {
        return modelPath != null && Files.isRegularFile(modelPath);
    }

    protected void requireAvailable() {
        if (!isAvailable()) {
            throw new IllegalStateException("Depth model not found at " + modelPath
                    + ". Configure a valid ONNX model path.");
        }
    }

    /**
     * Resize + ImageNet-normalize the image into the CHW float array expected by the model.
     */
    protected static float[] preprocess(BufferedImage image, int targetWidth, int targetHeight) {
        BufferedImage resized = new BufferedImage(targetWidth, targetHeight, BufferedImage.TYPE_INT_RGB);
        var graphics = resized.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        graphics.drawImage(image, 0, 0, targetWidth, targetHeight, null);
        graphics.dispose();

        float[] chw = new float[3 * targetWidth * targetHeight];
        int plane = targetWidth * targetHeight;

        for (int y = 0; y < targetHeight; y++) {
            for (int x = 0; x < targetWidth; x++) {
                int rgb = resized.getRGB(x, y);
                float r = ((rgb >> 16) & 0xFF) / 255f;
                float g = ((rgb >> 8) & 0xFF) / 255f;
                float b = (rgb & 0xFF) / 255f;

                int idx = y * targetWidth + x;
                chw[idx] = (r - MEAN[0]) / STD[0];
                chw[plane + idx] = (g - MEAN[1]) / STD[1];
                chw[2 * plane + idx] = (b - MEAN[2]) / STD[2];
            }
        }
        return chw;
    }

    /**
     * Load the model, run a single inference and return the raw output as an {@code [images][H][W]} array.
     */
    protected static float[][][] runOnce(Path modelPath, float[] chw, long[] shape) throws OrtException {
        try (OrtEnvironment env = OrtEnvironment.getEnvironment();
             OrtSession.SessionOptions opts = new OrtSession.SessionOptions()) {

            opts.setIntraOpNumThreads(Math.max(1, Runtime.getRuntime().availableProcessors()));

            try (OrtSession session = env.createSession(modelPath.toString(), opts)) {
                String inputName = session.getInputNames().iterator().next();
                try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(chw), shape)) {
                    Map<String, OnnxTensor> inputs = Collections.singletonMap(inputName, inputTensor);
                    try (OrtSession.Result result = session.run(inputs)) {
                        return extractDepth(result);
                    }
                }
            }
        }
    }

    /**
     * Pulls the depth output out of the result regardless of exact rank, peeling off leading singleton
     * dimensions until it finds a 2D {@code [H][W]} map.
     */
    protected static float[][][] extractDepth(OrtSession.Result result) throws OrtException {
        Object value = result.get(0).getValue();

        if (value instanceof float[][][]) {
            return (float[][][]) value;
        }
        if (value instanceof float[][][][] v4) {
            return v4[0];
        }
        if (value instanceof float[][][][][] v5) {
            float[][][][] firstImage = v5[0];
            return firstImage[0];
        }
        throw new IllegalStateException("Unexpected output tensor shape: " + value.getClass());
    }

    /**
     * Simple bilinear resize of a float depth map to an arbitrary target size.
     */
    protected static float[][] resizeBilinear(float[][] src, int srcH, int srcW, int dstH, int dstW) {
        float[][] out = new float[dstH][dstW];
        float scaleY = (float) srcH / dstH;
        float scaleX = (float) srcW / dstW;

        for (int y = 0; y < dstH; y++) {
            float sy = (y + 0.5f) * scaleY - 0.5f;
            int y0 = Math.max(0, Math.min(srcH - 1, (int) Math.floor(sy)));
            int y1 = Math.min(srcH - 1, y0 + 1);
            float fy = sy - y0;

            for (int x = 0; x < dstW; x++) {
                float sx = (x + 0.5f) * scaleX - 0.5f;
                int x0 = Math.max(0, Math.min(srcW - 1, (int) Math.floor(sx)));
                int x1 = Math.min(srcW - 1, x0 + 1);
                float fx = sx - x0;

                float top = src[y0][x0] * (1 - fx) + src[y0][x1] * fx;
                float bot = src[y1][x0] * (1 - fx) + src[y1][x1] * fx;
                out[y][x] = top * (1 - fy) + bot * fy;
            }
        }
        return out;
    }
}
