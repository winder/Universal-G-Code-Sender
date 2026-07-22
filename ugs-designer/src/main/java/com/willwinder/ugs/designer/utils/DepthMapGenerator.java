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

import java.awt.image.BufferedImage;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Generates a grayscale depth map from a photo for use as a CNC carving height map. Brighter output
 * pixels are closer to the camera. The expensive model inference is delegated to a pluggable
 * {@link DepthModel} ({@link DepthAnythingModel} this class owns the cheap,
 * user-tunable post-processing which is identical across models.
 *
 * @author Joacim Breiler
 */
public class DepthMapGenerator {
    private static final String MODEL_PROPERTY = "ugs.designer.depthModel";
    private static final String MODEL_ENVIRONMENT_VARIABLE = "UGS_DEPTH_MODEL";
    private static final String DEPTH_ANYTHING_DEFAULT_LOCATION = ".ugs/models/depth-anything-v2-small.onnx";

    /**
     * Gaussian sigma (px) for the fine (sharp micro-texture) detail high-pass.
     */
    private static final float DETAIL_FINE_SIGMA = 2.0f;

    /**
     * Gaussian sigma (px) for the coarse (broader shading) detail high-pass.
     */
    private static final float DETAIL_COARSE_SIGMA = 8.0f;

    /**
     * Relative weight of the coarse detail band against the fine one.
     */
    private static final float DETAIL_COARSE_WEIGHT = 0.6f;

    /**
     * Overall strength of the detail contribution relative to the full depth range.
     */
    private static final float DETAIL_SCALE = 0.35f;

    private final DepthModel model;

    public DepthMapGenerator() {
        this.model = new DepthAnythingModel(resolveModelPath());
    }

    public DepthMapGenerator(Path modelPath) {
        this.model = new DepthAnythingModel(modelPath);
    }

    private static Path resolveModelPath() {
        String configured = System.getProperty(MODEL_PROPERTY, System.getenv(MODEL_ENVIRONMENT_VARIABLE));
        if (configured != null && !configured.isBlank()) {
            return Paths.get(configured);
        }
        return Paths.get(System.getProperty("user.home"), DEPTH_ANYTHING_DEFAULT_LOCATION);
    }

    public Path getModelPath() {
        return model.getModelPath();
    }

    public boolean isModelAvailable() {
        return model.isAvailable();
    }

    /**
     * Runs depth estimation and returns the raw depth (before user-facing post-processing), resized to
     * the image dimensions. Cache this and reuse it with {@link #toDepthMap} whenever only the
     * post-processing parameters change.
     *
     * @param source the photo to estimate depth from
     * @return raw depth, indexed as {@code [row][column]}, larger = closer
     * @throws IllegalStateException if the model is not available or inference fails
     */
    public float[][] estimateDepth(BufferedImage source) {
        return model.estimateDepth(source);
    }

    /**
     * Runs depth estimation on the given image and returns an 8-bit grayscale depth map using default
     * post-processing.
     */
    public BufferedImage generateDepthMap(BufferedImage source) {
        return generateDepthMap(source, new DepthMapParameters());
    }

    /**
     * Runs depth estimation and post-processes it into a grayscale depth map.
     */
    public BufferedImage generateDepthMap(BufferedImage source, DepthMapParameters parameters) {
        return toDepthMap(estimateDepth(source), source, parameters);
    }

    /**
     * Applies the cheap, user-tunable post-processing to a raw depth estimate. This does not touch the
     * model, so it can run repeatedly as the user adjusts the depth-map controls.
     *
     * @param rawDepth   the raw depth from {@link #estimateDepth}, indexed as {@code [row][column]}
     * @param source     the original photo (used to blend fine texture back in)
     * @param parameters the post-processing tuning to apply
     * @return a grayscale depth map the same size as {@code source}
     */
    public BufferedImage toDepthMap(float[][] rawDepth, BufferedImage source, DepthMapParameters parameters) {
        int originalWidth = source.getWidth();
        int originalHeight = source.getHeight();

        float sigma = parameters.smoothingSigma();
        float[][] h = sigma > 0 ? gaussianBlur(rawDepth, sigma) : rawDepth;
        h = percentileClipNormalize(h, parameters.percentileLow(), parameters.percentileHigh());
        h = applyGamma(h, parameters.gamma());

        float[][] luma = computeLuma(source);
        h = detailBlend(h, luma, parameters.detailBlendAmount(), DETAIL_SCALE);
        clamp01(h);

        return toByteGray(h, originalWidth, originalHeight);
    }

    private static BufferedImage toByteGray(float[][] map, int width, int height) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
        var raster = image.getRaster();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int value = Math.round(Math.max(0f, Math.min(1f, map[y][x])) * 255f);
                raster.setSample(x, y, 0, value);
            }
        }
        return image;
    }

    private static float[][] gaussianBlur(float[][] src, float sigma) {
        int height = src.length;
        int width = src[0].length;
        int radius = Math.max(1, (int) Math.ceil(sigma * 3));
        float[] kernel = new float[2 * radius + 1];
        float sum = 0f;
        for (int i = -radius; i <= radius; i++) {
            float v = (float) Math.exp(-(i * i) / (2f * sigma * sigma));
            kernel[i + radius] = v;
            sum += v;
        }
        for (int i = 0; i < kernel.length; i++) {
            kernel[i] /= sum;
        }

        float[][] temp = new float[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float acc = 0f;
                for (int k = -radius; k <= radius; k++) {
                    int xx = Math.min(width - 1, Math.max(0, x + k));
                    acc += src[y][xx] * kernel[k + radius];
                }
                temp[y][x] = acc;
            }
        }
        float[][] out = new float[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float acc = 0f;
                for (int k = -radius; k <= radius; k++) {
                    int yy = Math.min(height - 1, Math.max(0, y + k));
                    acc += temp[yy][x] * kernel[k + radius];
                }
                out[y][x] = acc;
            }
        }
        return out;
    }

    private static float[][] percentileClipNormalize(float[][] depth, float loPercentile, float hiPercentile) {
        int height = depth.length;
        int width = depth[0].length;
        float[] flat = new float[height * width];
        int idx = 0;
        for (float[] row : depth) {
            for (float v : row) {
                flat[idx++] = v;
            }
        }
        float[] sorted = flat.clone();
        java.util.Arrays.sort(sorted);

        int loIdx = Math.max(0, Math.min(sorted.length - 1, (int) (loPercentile * (sorted.length - 1))));
        int hiIdx = Math.max(0, Math.min(sorted.length - 1, (int) (hiPercentile * (sorted.length - 1))));
        float floor = sorted[loIdx];
        float ceiling = sorted[hiIdx];
        if (ceiling <= floor) {
            ceiling = floor + 1e-6f;
        }

        float[][] out = new float[height][width];
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                float clipped = Math.max(floor, Math.min(ceiling, depth[y][x]));
                out[y][x] = (clipped - floor) / (ceiling - floor);
            }
        }
        return out;
    }

    private static float[][] applyGamma(float[][] h, float gamma) {
        int H = h.length, W = h[0].length;
        float[][] out = new float[H][W];
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                out[y][x] = (float) Math.pow(Math.max(0f, Math.min(1f, h[y][x])), gamma);
            }
        }
        return out;
    }

    private static float[][] computeLuma(BufferedImage img) {
        int w = img.getWidth(), h = img.getHeight();
        float[][] luma = new float[h][w];
        for (int y = 0; y < h; y++) {
            for (int x = 0; x < w; x++) {
                int rgb = img.getRGB(x, y);
                int r = (rgb >> 16) & 0xFF, g = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
                luma[y][x] = (0.299f * r + 0.587f * g + 0.114f * b) / 255f;
            }
        }
        return luma;
    }

    /**
     * Re-injects photo texture the depth model smoothed away, using two spatial scales: a fine
     * high-pass for sharp micro-detail and a coarser one for broader shading. The two are summed so the
     * detail control brings back both crisp texture (hair, grain) and mid-scale relief.
     */
    private static float[][] detailBlend(float[][] h, float[][] luma, float amount, float detailScale) {
        if (amount <= 0) {
            return h;
        }
        float[][] fineBlur = gaussianBlur(luma, DETAIL_FINE_SIGMA);
        float[][] coarseBlur = gaussianBlur(luma, DETAIL_COARSE_SIGMA);
        int H = h.length, W = h[0].length;
        float[][] out = new float[H][W];
        for (int y = 0; y < H; y++) {
            for (int x = 0; x < W; x++) {
                float fine = luma[y][x] - fineBlur[y][x];
                float coarse = luma[y][x] - coarseBlur[y][x];
                float detail = fine + DETAIL_COARSE_WEIGHT * coarse;
                out[y][x] = h[y][x] + amount * detailScale * detail;
            }
        }
        return out;
    }

    private static void clamp01(float[][] h) {
        for (float[] row : h) {
            for (int x = 0; x < row.length; x++) {
                row[x] = Math.max(0f, Math.min(1f, row[x]));
            }
        }
    }
}
