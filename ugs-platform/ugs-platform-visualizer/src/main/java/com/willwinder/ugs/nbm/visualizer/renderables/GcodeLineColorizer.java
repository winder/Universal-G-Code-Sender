/*
    Copyright 2023 Will Winder

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
package com.willwinder.ugs.nbm.visualizer.renderables;

import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_ARC;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_COMPLETE;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_LINEAR;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_LINEAR_MIN_SPEED;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_PLUNGE;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_RAPID;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_SPINDLE_MAX_SPEED;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_SPINDLE_MIN_SPEED;
import com.willwinder.universalgcodesender.visualizer.LineSegment;

import java.awt.Color;

/**
 * Generates a color based on the current line segment
 *
 * @author Joacim Breiler
 */
public class GcodeLineColorizer {
    private double maxSpindleSpeed = 0;
    private double maxFeedRate = 0;
    private Color feedMinColor = Color.BLACK;
    private Color feedMaxColor = Color.BLACK;
    private Color spindleMinColor = Color.BLACK;
    private Color spindleMaxColor = Color.BLACK;
    private Color rapidColor = Color.BLACK;
    private Color arcColor = Color.BLACK;
    private Color plungeColor = Color.BLACK;
    private Color completedColor = Color.BLACK;

    public void reloadPreferences(VisualizerOptions vo) {
        feedMaxColor = vo.getOptionForKey(VISUALIZER_OPTION_LINEAR).value;
        feedMinColor = vo.getOptionForKey(VISUALIZER_OPTION_LINEAR_MIN_SPEED).value;
        spindleMaxColor = vo.getOptionForKey(VISUALIZER_OPTION_SPINDLE_MAX_SPEED).value;
        spindleMinColor = vo.getOptionForKey(VISUALIZER_OPTION_SPINDLE_MIN_SPEED).value;
        rapidColor = vo.getOptionForKey(VISUALIZER_OPTION_RAPID).value;
        arcColor = vo.getOptionForKey(VISUALIZER_OPTION_ARC).value;
        plungeColor = vo.getOptionForKey(VISUALIZER_OPTION_PLUNGE).value;
        completedColor = vo.getOptionForKey(VISUALIZER_OPTION_COMPLETE).value;
    }

    public Color getColor(LineSegment lineSegment, long currentCommandNumber) {
        if (lineSegment.getLineNumber() < currentCommandNumber) {
            return completedColor;
        } else if (lineSegment.isArc()) {
            return arcColor;
        } else if (lineSegment.isFastTraverse()) {
            return rapidColor;
        } else if (lineSegment.isZMovement()) {
            return plungeColor;
        } else {
            return getFeedColor(lineSegment.getFeedRate(), lineSegment.getSpindleSpeed());
        }
    }

    private Color getFeedColor(double feedRate, double spindleSpeed) {
        double currentSpindleSpeed = Math.max(spindleSpeed, 0.1);
        double currentFeedRate = Math.max(feedRate, 0.1);

        double feedRatePercent = currentFeedRate / maxFeedRate;
        Color feedColor = maxFeedRate < 0.01 ? feedMaxColor : getColor(feedMinColor, feedMaxColor, feedRatePercent);

        double speedPercent = currentSpindleSpeed / maxSpindleSpeed;
        Color speedColor = maxSpindleSpeed < 0.1 ? spindleMaxColor : getColor(spindleMinColor, spindleMaxColor, speedPercent);
        return blend(speedColor, feedColor);
    }

    /**
     * Blends multiple colors with the equal amount to one color
     *
     * @param colors a list of colors to blend
     * @return a blended color
     */
    public static Color blend(Color... colors) {
        if (colors == null || colors.length == 0) {
            return null;
        }
        float ratio = 1f / (colors.length);

        int a = 0;
        int r = 0;
        int g = 0;
        int b = 0;

        for (Color color : colors) {
            int rgb = color.getRGB();
            int a1 = (rgb >> 24 & 0xff);
            int r1 = ((rgb & 0xff0000) >> 16);
            int g1 = ((rgb & 0xff00) >> 8);
            int b1 = (rgb & 0xff);
            a += a1 * ratio;
            r += r1 * ratio;
            g += g1 * ratio;
            b += b1 * ratio;
        }

        return new Color(a << 24 | r << 16 | g << 8 | b);
    }

    private Color getColor(Color minColor, Color maxColor, double percent) {
        int red = Math.min(255, minColor.getRed() + (int) (percent * (maxColor.getRed() - minColor.getRed())));
        int green = Math.min(255, minColor.getGreen() + (int) (percent * (maxColor.getGreen() - minColor.getGreen())));
        int blue = Math.min(255, minColor.getBlue() + (int) (percent * (maxColor.getBlue() - minColor.getBlue())));
        int alpha = Math.min(255, minColor.getAlpha() + (int) (percent * (maxColor.getAlpha() - minColor.getAlpha())));
        return new Color(red, green, blue, alpha);
    }

    public void setMaxSpindleSpeed(double maxSpindleSpeed) {
        this.maxSpindleSpeed = maxSpindleSpeed;
    }

    public void setMaxFeedRate(double maxFeedRate) {
        this.maxFeedRate = maxFeedRate;
    }
}
