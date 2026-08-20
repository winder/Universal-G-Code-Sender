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
package com.willwinder.ugs.designer.io.gcode.toolpaths;

import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.io.gcode.path.Segment;
import com.willwinder.ugs.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.ugs.designer.model.Size;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.List;

public class PlotterFillToolPathTest {

    @Test
    public void appendGcodePath_shouldFillTheShapeWithHorizontalLines() {
        List<Segment> segments = fill(0, 2);

        List<Segment> lines = linesIn(segments);
        assertEquals(6, lines.size());
        for (int i = 0; i < lines.size(); i++) {
            // Every line spans the full width of the shape at its own step of the spacing
            assertEquals(i * 2, lines.get(i).point.getY(), 0.01);
            assertEquals(10, Math.abs(lines.get(i).point.getX() - movesIn(segments).get(i).point.getX()), 0.01);
        }
    }

    @Test
    public void appendGcodePath_shouldStartEveryLineWhereThePreviousOneEnded() {
        List<Segment> segments = fill(0, 2);

        List<Segment> lines = linesIn(segments);
        List<Segment> moves = movesIn(segments);
        for (int i = 1; i < lines.size(); i++) {
            assertEquals(lines.get(i - 1).point.getX(), moves.get(i).point.getX(), 0.01);
        }
    }

    @Test
    public void appendGcodePath_shouldPutMoreLinesInTheShapeWithASmallerSpacing() {
        assertEquals(11, linesIn(fill(0, 1)).size());
        assertEquals(6, linesIn(fill(0, 2)).size());
        assertEquals(3, linesIn(fill(0, 5)).size());
    }

    @Test
    public void appendGcodePath_shouldFillTheShapeAtTheGivenAngle() {
        List<Segment> segments = fill(90, 2);

        List<Segment> lines = linesIn(segments);
        assertEquals(6, lines.size());
        for (int i = 0; i < lines.size(); i++) {
            // Turned a quarter around the lines run up and down the shape instead of across it
            assertEquals(i * 2, lines.get(i).point.getX(), 0.01);
            assertEquals(10, Math.abs(lines.get(i).point.getY() - movesIn(segments).get(i).point.getY()), 0.01);
        }
    }

    @Test
    public void appendGcodePath_shouldLiftThePenBetweenTheLines() {
        List<Segment> segments = fill(0, 2);

        for (int i = 0; i + 3 < segments.size(); i += 4) {
            assertEquals(SegmentType.PEN_UP, segments.get(i).type);
            assertEquals(SegmentType.MOVE, segments.get(i + 1).type);
            assertEquals(SegmentType.PEN_DOWN, segments.get(i + 2).type);
            assertEquals(SegmentType.LINE, segments.get(i + 3).type);
        }
        assertEquals(SegmentType.PEN_UP, segments.get(segments.size() - 1).type);
    }

    @Test
    public void appendGcodePath_shouldNotGenerateAnyDepths() {
        List<Segment> segments = fill(0, 2);

        assertTrue(segments.stream().filter(segment -> segment.point != null).noneMatch(segment -> segment.point.hasZ()));
    }

    private static List<Segment> fill(double angle, double lineSpacing) {
        return fill(angle, lineSpacing, 0);
    }

    private static List<Segment> fill(double angle, double lineSpacing, double penWidth) {
        Rectangle rectangle = new Rectangle(0, 0);
        rectangle.setSize(new Size(10, 10));
        rectangle.setFeedRate(1500);
        rectangle.setToolPathAngle(angle);
        rectangle.setLineSpacing(lineSpacing);

        Settings settings = new Settings();
        settings.setPenWidth(penWidth);
        return new PlotterFillToolPath(settings, rectangle).toGcodePath().getSegments();
    }

    @Test
    public void appendGcodePath_shouldKeepTheHatchHalfAPenWidthInsideTheShape() {
        List<Segment> segments = fill(0, 2, 2);

        List<Segment> lines = linesIn(segments);
        List<Segment> moves = movesIn(segments);
        assertEquals(5, lines.size());
        for (int i = 0; i < lines.size(); i++) {
            // A two millimeter pen leaves a shape running from 1 to 9 in both directions
            assertEquals(1 + i * 2, lines.get(i).point.getY(), 0.01);
            assertEquals(8, Math.abs(lines.get(i).point.getX() - moves.get(i).point.getX()), 0.01);
        }
    }

    @Test
    public void appendGcodePath_shouldNotDrawAShapeNarrowerThanThePen() {
        List<Segment> segments = fill(0, 2, 12);

        assertTrue(linesIn(segments).isEmpty());
    }

    private static List<Segment> linesIn(List<Segment> segments) {
        return segments.stream().filter(segment -> segment.type == SegmentType.LINE).toList();
    }

    private static List<Segment> movesIn(List<Segment> segments) {
        return segments.stream().filter(segment -> segment.type == SegmentType.MOVE).toList();
    }
}
