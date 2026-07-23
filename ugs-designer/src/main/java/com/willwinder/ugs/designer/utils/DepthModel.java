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

/**
 * A monocular depth-estimation model. Implementations wrap a specific ONNX network (its expected input
 * dimensions, normalization and output convention) and produce a raw depth field for an image.
 *
 * @author Joacim Breiler
 */
public interface DepthModel {

    /**
     * @return the path to the ONNX model file this instance would load
     */
    Path getModelPath();

    /**
     * @return {@code true} if the model file exists and can be loaded
     */
    boolean isAvailable();

    /**
     * Runs depth estimation and returns the raw depth resized to the image dimensions. The returned
     * values follow a common convention across models: larger values are closer to the camera (so the
     * nearest parts of the subject end up raised in the carved relief).
     *
     * @param source the photo to estimate depth from
     * @return raw depth, indexed as {@code [row][column]}
     * @throws IllegalStateException if the model is not available or inference fails
     */
    float[][] estimateDepth(BufferedImage source);
}
