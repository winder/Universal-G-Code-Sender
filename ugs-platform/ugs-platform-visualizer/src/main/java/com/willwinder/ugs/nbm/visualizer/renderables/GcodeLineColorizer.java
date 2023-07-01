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
import com.willwinder.universalgcodesender.visualizer.LineSegment;

import java.awt.Color;

import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_ARC;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_COMPLETE;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_LINEAR;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_LINEAR_MIN_SPEED;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_PLUNGE;
import static com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions.VISUALIZER_OPTION_RAPID;

/**
 * Generates a color based on the current line segment
 *
 * @author Joacim Breiler
 */
public class GcodeLineColorizer {
    private double maxSpindleSpeed = 0;
    private Color feedMinColor = Color.BLACK;
    private Color feedMaxColor = Color.BLACK;
    private Color rapidColor = Color.BLACK;
    private Color arcColor = Color.BLACK;
    private Color plungeColor = Color.BLACK;
    private Color completedColor = Color.BLACK;

    public void reloadPreferences(VisualizerOptions vo) {
        feedMaxColor = vo.getOptionForKey(VISUALIZER_OPTION_LINEAR).value;
        feedMinColor = vo.getOptionForKey(VISUALIZER_OPTION_LINEAR_MIN_SPEED).value;
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
            return getFeedColor(lineSegment.getSpindleSpeed());
        }
    }

    private Color getFeedColor(double spindleSpeed) {
        if (maxSpindleSpeed == 0) {
            return feedMaxColor;
        }

        double currentSpindleSpeed = Math.max(spindleSpeed, 0.1);

        double speedPercent = currentSpindleSpeed / maxSpindleSpeed;
        int red = Math.min(255, feedMinColor.getRed() + (int) (speedPercent * (feedMaxColor.getRed() - feedMinColor.getRed())));
        int green = Math.min(255, feedMinColor.getGreen() + (int) (speedPercent * (feedMaxColor.getGreen() - feedMinColor.getGreen())));
        int blue = Math.min(255, feedMinColor.getBlue() + (int) (speedPercent * (feedMaxColor.getBlue() - feedMinColor.getBlue())));
        int alpha = Math.min(255, feedMinColor.getAlpha() + (int) (speedPercent * (feedMaxColor.getAlpha() - feedMinColor.getAlpha())));
        return new Color(red, green, blue, alpha);
    }

    public void setMaxSpindleSpeed(double maxSpindleSpeed) {
        this.maxSpindleSpeed = maxSpindleSpeed;
    }
}
