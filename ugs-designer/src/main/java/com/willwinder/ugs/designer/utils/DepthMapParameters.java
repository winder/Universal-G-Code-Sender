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

import java.util.Objects;

/**
 * User-facing tuning for the depth-map post-processing. All four knobs are stored as normalized
 * amounts in [0, 1] so they map cleanly onto 0-100% sliders; the physical values used by the
 * pipeline are derived from them.
 *
 * @author Joacim Breiler
 */
public class DepthMapParameters {
    public static final double DEFAULT_DETAIL = 0.6;
    public static final double DEFAULT_SMOOTHING = 0.3;
    public static final double DEFAULT_CONTRAST = 0.1;
    public static final double DEFAULT_EMPHASIS = 0.33;

    private double detail = DEFAULT_DETAIL;
    private double smoothing = DEFAULT_SMOOTHING;
    private double contrast = DEFAULT_CONTRAST;
    private double emphasis = DEFAULT_EMPHASIS;

    private static double clamp01(double value) {
        return Math.max(0, Math.min(1, value));
    }

    public double getDetail() {
        return detail;
    }

    public void setDetail(double detail) {
        this.detail = clamp01(detail);
    }

    public double getSmoothing() {
        return smoothing;
    }

    public void setSmoothing(double smoothing) {
        this.smoothing = clamp01(smoothing);
    }

    public double getContrast() {
        return contrast;
    }

    public void setContrast(double contrast) {
        this.contrast = clamp01(contrast);
    }

    public double getEmphasis() {
        return emphasis;
    }

    public void setEmphasis(double emphasis) {
        this.emphasis = clamp01(emphasis);
    }

    /** How much fine photo texture to blend back in (0..1). */
    public float detailBlendAmount() {
        return (float) detail;
    }

    /** Denoise gaussian sigma in pixels (0..5). */
    public float smoothingSigma() {
        return (float) (smoothing * 5.0);
    }

    /** Fraction clipped off the dark end before contrast stretching (0..0.2). */
    public float percentileLow() {
        return (float) (contrast * 0.2);
    }

    /** Fraction clipped off the bright end before contrast stretching. */
    public float percentileHigh() {
        return 1f - percentileLow();
    }

    /** Gamma applied to emphasize the subject over the background (0.3..3.0). */
    public float gamma() {
        return (float) (0.3 + emphasis * 2.7);
    }

    public DepthMapParameters copy() {
        DepthMapParameters copy = new DepthMapParameters();
        copy.detail = detail;
        copy.smoothing = smoothing;
        copy.contrast = contrast;
        copy.emphasis = emphasis;
        return copy;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof DepthMapParameters that)) {
            return false;
        }
        return Double.compare(detail, that.detail) == 0
                && Double.compare(smoothing, that.smoothing) == 0
                && Double.compare(contrast, that.contrast) == 0
                && Double.compare(emphasis, that.emphasis) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(detail, smoothing, contrast, emphasis);
    }
}
