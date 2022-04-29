/*
    Copyright 2022 Will Winder

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
package com.willwinder.ugs.nbp.designer.gui.imagetracer;

/**
 * @author Joacim Breiler
 */
public class TraceSettings {
    private float lineThreshold;
    private float quadThreshold;
    private int pathOmit;
    private int numberOfColors;
    private int colorQuantize;
    private int blurRadius;
    private int blurDelta;
    private int startColor;
    private int endColor;

    public float getLineThreshold() {
        return lineThreshold;
    }

    public void setLineThreshold(float lineThreshold) {
        this.lineThreshold = lineThreshold;
    }

    public float getQuadThreshold() {
        return quadThreshold;
    }

    public void setQuadThreshold(float quadThreshold) {
        this.quadThreshold = quadThreshold;
    }

    public int getPathOmit() {
        return pathOmit;
    }

    public void setPathOmit(int pathOmit) {
        this.pathOmit = pathOmit;
    }

    public int getNumberOfColors() {
        return numberOfColors;
    }

    public void setNumberOfColors(int numberOfColors) {
        this.numberOfColors = numberOfColors;
    }

    public int getColorQuantize() {
        return colorQuantize;
    }

    public void setColorQuantize(int colorQuantize) {
        this.colorQuantize = colorQuantize;
    }

    public int getBlurRadius() {
        return blurRadius;
    }

    public void setBlurRadius(int blurRadius) {
        this.blurRadius = blurRadius;
    }

    public int getBlurDelta() {
        return blurDelta;
    }

    public void setBlurDelta(int blurDelta) {
        this.blurDelta = blurDelta;
    }

    public int getStartColor() {
        return startColor;
    }

    public void setStartColor(int startColor) {
        this.startColor = startColor;
    }

    public int getEndColor() {
        return endColor;
    }

    public void setEndColor(int endColor) {
        this.endColor = endColor;
    }
}
