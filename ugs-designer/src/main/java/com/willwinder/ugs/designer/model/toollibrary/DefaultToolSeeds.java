/*
    Copyright 2026 Damian Nikodem

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
package com.willwinder.ugs.designer.model.toollibrary;

import com.willwinder.universalgcodesender.model.UnitUtils;

import java.util.ArrayList;
import java.util.List;

public final class DefaultToolSeeds {
    public static final String CUSTOM_SENTINEL_ID = "builtin:custom";

    private static final double[] DIAMETERS_INCH = {1.0 / 16.0, 1.0 / 8.0, 1.0 / 4.0, 1.0 / 2.0, 1.0};
    private static final String[] DIAMETER_SLUGS = {"1_16in", "1_8in", "1_4in", "1_2in", "1in"};
    private static final String[] DIAMETER_LABELS = {"1/16\"", "1/8\"", "1/4\"", "1/2\"", "1\""};
    private static final double BASELINE_INCH = 0.25;

    /** Baseline feed / plunge / depth at 1/4" reference diameter (softwood, small hobby CNC). */
    private record Baseline(int feed, int plunge, double depth, double stepOver) {}

    private DefaultToolSeeds() {
    }

    public static List<ToolDefinition> create() {
        List<ToolDefinition> tools = new ArrayList<>();
        addShapeSeeds(tools, EndmillShape.UPCUT, "Upcut", new Baseline(900, 300, 2.0, 0.4));
        addShapeSeeds(tools, EndmillShape.DOWNCUT, "Downcut", new Baseline(800, 250, 1.5, 0.4));
        addShapeSeeds(tools, EndmillShape.BALL, "Ball", new Baseline(800, 250, 1.2, 0.25));
        addShapeSeeds(tools, EndmillShape.COMPRESSION, "Compression", new Baseline(900, 300, 2.0, 0.4));
        addShapeSeeds(tools, EndmillShape.STRAIGHT, "Straight", new Baseline(700, 250, 1.5, 0.4));
        addVBitSeeds(tools, 30);
        addVBitSeeds(tools, 60);
        addVBitSeeds(tools, 90);
        tools.add(createCustomSentinel());
        return tools;
    }

    private static void addShapeSeeds(List<ToolDefinition> tools, EndmillShape shape, String shapeLabel, Baseline baseline) {
        String slug = shape.name().toLowerCase();
        for (int i = 0; i < DIAMETERS_INCH.length; i++) {
            tools.add(buildTool(
                    "builtin:" + slug + ":" + DIAMETER_SLUGS[i],
                    DIAMETER_LABELS[i] + " " + shapeLabel,
                    shape,
                    null,
                    DIAMETERS_INCH[i],
                    baseline));
        }
    }

    private static void addVBitSeeds(List<ToolDefinition> tools, int angleDegrees) {
        Baseline baseline = new Baseline(1200, 300, 0.8, 0.4);
        for (int i = 0; i < DIAMETERS_INCH.length; i++) {
            tools.add(buildTool(
                    "builtin:vbit:" + angleDegrees + "deg:" + DIAMETER_SLUGS[i],
                    DIAMETER_LABELS[i] + " V-bit " + angleDegrees + "°",
                    EndmillShape.V_BIT,
                    (double) angleDegrees,
                    DIAMETERS_INCH[i],
                    baseline));
        }
    }

    private static ToolDefinition buildTool(String id, String name, EndmillShape shape, Double vAngle,
                                            double diameterInch, Baseline baseline) {
        double scale = diameterInch / BASELINE_INCH;
        int feed = (int) Math.max(200, Math.round(baseline.feed() * scaleFeed(scale)));
        int plunge = (int) Math.max(120, Math.round(baseline.plunge() * scaleFeed(scale)));
        double depth = Math.max(0.3, baseline.depth() * scaleDepth(scale));

        ToolDefinition tool = new ToolDefinition();
        tool.setId(id);
        tool.setName(name);
        tool.setShape(shape);
        tool.setVBitAngleDegrees(vAngle);
        tool.setDiameter(diameterInch);
        tool.setDiameterUnit(UnitUtils.Units.INCH);
        tool.setFeedSpeed(feed);
        tool.setPlungeSpeed(plunge);
        tool.setDepthPerPass(round(depth, 2));
        tool.setStepOverPercent(baseline.stepOver());
        tool.setMaxSpindleSpeed(18000);
        tool.setSpindleDirection("M3");
        tool.setBuiltIn(true);
        return tool;
    }

    /**
     * Feed scales sub-linearly with diameter so small bits aren't crawlingly slow and large bits
     * aren't recklessly fast. sqrt-ish curve: 0.25x → ~0.5x feed, 4x → ~2x feed.
     */
    private static double scaleFeed(double diameterRatio) {
        return Math.sqrt(diameterRatio);
    }

    /** Depth scales linearly with diameter (a common rule of thumb: DOC ≈ 0.5–1.0 × D). */
    private static double scaleDepth(double diameterRatio) {
        return diameterRatio;
    }

    private static double round(double value, int decimals) {
        double factor = Math.pow(10, decimals);
        return Math.round(value * factor) / factor;
    }

    public static ToolDefinition createCustomSentinel() {
        ToolDefinition custom = new ToolDefinition();
        custom.setId(CUSTOM_SENTINEL_ID);
        custom.setName("Custom");
        custom.setShape(EndmillShape.CUSTOM);
        custom.setDiameter(3.0);
        custom.setDiameterUnit(UnitUtils.Units.MM);
        custom.setFeedSpeed(1000);
        custom.setPlungeSpeed(400);
        custom.setDepthPerPass(1.0);
        custom.setStepOverPercent(0.3);
        custom.setMaxSpindleSpeed(18000);
        custom.setSpindleDirection("M3");
        custom.setBuiltIn(true);
        custom.setCustomSentinel(true);
        return custom;
    }
}
