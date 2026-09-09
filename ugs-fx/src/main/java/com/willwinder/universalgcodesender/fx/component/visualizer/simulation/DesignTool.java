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
package com.willwinder.universalgcodesender.fx.component.visualizer.simulation;

import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;

/**
 * The tool a design is set up to be cut with, read straight from the design settings. The
 * diameter, shape and V-bit angle in the settings are what the tool paths are generated with, so
 * they describe the cutter even when the design is bound to a library tool whose values were later
 * edited. The library tool snapshot is only a fallback for settings without a diameter. Designs use
 * a single tool, so the profile is the same for every segment of the exported program.
 */
public final class DesignTool {
    private DesignTool() {
    }

    public static ToolProfile fromSettings(Settings settings) {
        double diameter = settings.getToolDiameter();
        if (diameter <= 0) {
            ToolDefinition snapshot = settings.getCurrentToolSnapshot();
            return snapshot != null && snapshot.getDiameter() > 0 ? ToolProfile.fromDefinition(snapshot) : ToolProfile.DEFAULT;
        }
        return switch (settings.getToolShape()) {
            case BALL -> ToolProfile.ball(diameter);
            case V_BIT -> ToolProfile.vBit(diameter, settings.getVBitAngle());
            default -> ToolProfile.flat(diameter);
        };
    }

    public static ToolResolver resolver(Settings settings) {
        ResolvedTool tool = ResolvedTool.of(fromSettings(settings), ResolvedTool.Source.DESIGN, describe(settings));
        return cut -> tool;
    }

    /**
     * The design tool's name when it is bound to a library tool, otherwise its size and shape such
     * as {@code 6mm Upcut} or {@code 6mm V-bit 60°}.
     */
    public static String describe(Settings settings) {
        ToolDefinition snapshot = settings.getCurrentToolSnapshot();
        if (snapshot != null && snapshot.getName() != null && !snapshot.getName().isBlank()) {
            return ResolvedTool.describe(snapshot);
        }
        String description = trimNumber(settings.getToolDiameter()) + "mm " + settings.getToolShape().getDisplayName();
        if (settings.getToolShape().requiresAngle()) {
            description += " " + trimNumber(settings.getVBitAngle()) + "°";
        }
        return description;
    }

    private static String trimNumber(double value) {
        String text = String.format(java.util.Locale.ROOT, "%.3f", value);
        return text.contains(".") ? text.replaceAll("0+$", "").replaceAll("\\.$", "") : text;
    }
}
