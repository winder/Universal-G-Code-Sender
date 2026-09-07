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
package com.willwinder.universalgcodesender.fx.component.visualizer.scene.renderables;

import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Bounds3;
import com.willwinder.universalgcodesender.fx.component.visualizer.scene.LineMeshBuilder;
import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;
import com.willwinder.universalgcodesender.gcode.DefaultCommandCreator;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;
import com.willwinder.universalgcodesender.visualizer.GcodeViewParse;
import com.willwinder.universalgcodesender.visualizer.LineSegment;
import com.willwinder.universalgcodesender.visualizer.VisualizerUtils;
import javafx.scene.paint.Color;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.willwinder.universalgcodesender.fx.helper.Colors.blend;
import static com.willwinder.universalgcodesender.fx.helper.Colors.interpolate;

/**
 * Parses a program into a {@link com.willwinder.universalgcodesender.fx.component.visualizer.scene.VertexLayout#LINE}
 * vertex array, one segment per motion, coloured by motion type, feed and spindle speed and
 * tagged with the number of the command that produced it.
 */
public final class GcodeLines {
    private static final double ARC_TOLERANCE = 0.02;

    private GcodeLines() {
    }

    public record Model(float[] vertices, int vertexCount, int maxCommandNumber, Bounds3 bounds) {
    }

    /**
     * The colours the toolpath is drawn with. Snapshotted on the JavaFX application thread so
     * the vertices can be built on a background thread without touching the settings properties.
     */
    public record Palette(Color rapid, Color arc, Color plunge, Color feedMin, Color feedMax,
                          Color spindleMin, Color spindleMax) {
        public static Palette fromSettings() {
            VisualizerSettings settings = VisualizerSettings.getInstance();
            return new Palette(
                    Color.web(settings.colorRapidProperty().getValue()),
                    Color.web(settings.colorArcProperty().getValue()),
                    Color.web(settings.colorPlungeProperty().getValue()),
                    Color.web(settings.colorFeedMinProperty().getValue()),
                    Color.web(settings.colorFeedMaxProperty().getValue()),
                    Color.web(settings.colorSpindleMinProperty().getValue()),
                    Color.web(settings.colorSpindleMaxProperty().getValue()));
        }
    }

    public static Model load(File file, Palette palette) throws IOException, GcodeParserException {
        GcodeViewParse parser = new GcodeViewParse();
        List<LineSegment> segments = parse(parser, file);
        return toModel(segments, parser, palette);
    }

    private static List<LineSegment> parse(GcodeViewParse parser, File file)
            throws IOException, GcodeParserException {
        try (IGcodeStreamReader reader = new GcodeStreamReader(file, new DefaultCommandCreator())) {
            return parser.toObjFromReaderWithArcTolerance(reader, ARC_TOLERANCE, 0);
        } catch (GcodeStreamReader.NotGcodeStreamFile e) {
            List<String> lines = VisualizerUtils.readFiletoArrayList(file.getAbsolutePath());
            return parser.toObjReduxWithArcTolerance(lines, ARC_TOLERANCE, 0);
        }
    }

    private static Model toModel(List<LineSegment> segments, GcodeViewParse parser, Palette palette) {
        LineMeshBuilder builder = new LineMeshBuilder(segments.size());
        Bounds3 bounds = null;
        int maxCommandNumber = 0;
        for (LineSegment segment : segments) {
            LineSegment cartesian = VisualizerUtils.toCartesian(segment);
            Position start = cartesian.getStart().getPositionIn(UnitUtils.Units.MM);
            Position end = cartesian.getEnd().getPositionIn(UnitUtils.Units.MM);
            Color color = color(cartesian, parser, palette);
            int commandNumber = cartesian.getLineNumber();
            maxCommandNumber = Math.max(maxCommandNumber, commandNumber);

            double x1 = zeroIfNaN(start.getX());
            double y1 = zeroIfNaN(start.getY());
            double z1 = zeroIfNaN(start.getZ());
            double x2 = zeroIfNaN(end.getX());
            double y2 = zeroIfNaN(end.getY());
            double z2 = zeroIfNaN(end.getZ());
            builder.add(x1, y1, z1, x2, y2, z2, color, commandNumber);

            Bounds3 segmentBounds = Bounds3.ofPoint(x1, y1, z1).include(x2, y2, z2);
            bounds = bounds == null ? segmentBounds : bounds.union(segmentBounds);
        }
        if (bounds == null) {
            bounds = new Bounds3(0, 0, 0, 0, 0, 0);
        }
        return new Model(builder.build(), builder.vertexCount(), maxCommandNumber, bounds);
    }

    private static Color color(LineSegment segment, GcodeViewParse parser, Palette palette) {
        if (segment.isArc()) {
            return palette.arc();
        } else if (segment.isFastTraverse()) {
            return palette.rapid();
        } else if (segment.isZMovement()) {
            return palette.plunge();
        }
        return feedColor(segment.getFeedRate(), segment.getSpindleSpeed(), parser, palette);
    }

    private static Color feedColor(double feedRate, double spindleSpeed, GcodeViewParse parser, Palette palette) {
        double maxFeedRate = parser.getMaxFeedRate();
        double maxSpindleSpeed = parser.getMaxSpindleSpeed();
        Color feed = maxFeedRate < 0.01
                ? palette.feedMax()
                : interpolate(palette.feedMin(), palette.feedMax(), Math.max(feedRate, 0.1) / maxFeedRate);
        Color spindle = maxSpindleSpeed < 0.1
                ? palette.spindleMax()
                : interpolate(palette.spindleMin(), palette.spindleMax(), Math.max(spindleSpeed, 0.1) / maxSpindleSpeed);
        return blend(spindle, feed);
    }

    private static double zeroIfNaN(double value) {
        return Double.isNaN(value) ? 0 : value;
    }
}
