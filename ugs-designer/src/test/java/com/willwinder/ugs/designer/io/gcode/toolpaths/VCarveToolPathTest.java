/*
    Copyright 2026 Will Winder

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

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.designer.entities.cuttable.Path;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.io.gcode.path.Segment;
import com.willwinder.ugs.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.designer.io.ugsd.UgsDesignReader;
import com.willwinder.ugs.designer.model.Design;
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.ugs.designer.model.Size;
import com.willwinder.universalgcodesender.model.Axis;
import org.junit.Test;

import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class VCarveToolPathTest {

    @Test
    public void appendGcodePath_shouldCarveTheCentreLineOfTheShape() {
        Settings settings = createSettings(90);
        Rectangle stroke = createRectangle(20, 4);

        List<Segment> cuttingSegments = cuttingSegments(settings, stroke, 10);

        // The centre line of a 4mm wide stroke runs 2mm in from each of its long sides
        long alongTheCentre = cuttingSegments.stream()
                .filter(segment -> Math.abs(segment.getPoint().getAxis(Axis.X) - 10) < 5)
                .filter(segment -> Math.abs(segment.getPoint().getAxis(Axis.Y) - 2) < 0.2)
                .count();
        assertTrue("The carve should follow the centre line of the stroke, got "
                + describe(cuttingSegments), alongTheCentre > 5);
    }

    @Test
    public void appendGcodePath_shouldSetTheDepthFromHowWideTheShapeIs() {
        Settings settings = createSettings(90);

        // A 90 degree bit opens up one millimeter to each side for every millimeter it goes down,
        // so a 4mm wide stroke is fully carved at 2mm and an 8mm wide one at 4mm
        assertEquals(2, deepestCut(settings, createRectangle(20, 4), 10), 0.15);
        assertEquals(4, deepestCut(settings, createRectangle(20, 8), 10), 0.15);
    }

    @Test
    public void appendGcodePath_shouldCutDeeperForNarrowerBits() {
        double wideBitDepth = deepestCut(createSettings(90), createRectangle(20, 4), 100);
        double narrowBitDepth = deepestCut(createSettings(30), createRectangle(20, 4), 100);

        // Reaching 2mm to each side takes 2mm of depth with a 90 degree bit but 7.46mm with a 30
        assertEquals(2, wideBitDepth, 0.15);
        assertEquals(2 / Math.tan(Math.toRadians(15)), narrowBitDepth, 0.15);
    }

    @Test
    public void appendGcodePath_shouldTaperTowardsTheEndsOfAStroke() {
        Settings settings = createSettings(90);

        List<Segment> cuttingSegments = cuttingSegments(settings, createRectangle(20, 4), 10);

        double depthAtTheEnd = cuttingSegments.stream()
                .filter(segment -> segment.getPoint().getAxis(Axis.X) < 0.5)
                .mapToDouble(segment -> -segment.getPoint().getAxis(Axis.Z))
                .max()
                .orElseThrow();
        assertTrue("The carve should be shallow where the stroke runs out, was " + depthAtTheEnd,
                depthAtTheEnd < 0.75);
    }

    @Test
    public void appendGcodePath_shouldNotCutDeeperThanTheTargetDepth() {
        Settings settings = createSettings(90);
        double targetDepth = 3;

        List<Segment> cuttingSegments = cuttingSegments(settings, createRectangle(20, 20), targetDepth);

        cuttingSegments.forEach(segment -> assertTrue(
                "Cut at " + segment.getPoint().getAxis(Axis.Z) + " went past the target depth",
                segment.getPoint().getAxis(Axis.Z) >= -targetDepth - 0.001));
        assertEquals("The bottom should be carved at the target depth",
                targetDepth, deepestCut(settings, createRectangle(20, 20), targetDepth), 0.001);
    }

    @Test
    public void appendGcodePath_shouldPocketWhatIsWiderThanTheBitReachesAtTheTargetDepth() {
        Settings settings = createSettings(90);

        // A 90 degree bit only reaches 3mm to each side at 3mm deep, leaving the middle 14mm of a
        // 20mm wide shape to be cleared as a flat bottomed pocket
        List<Segment> cuttingSegments = cuttingSegments(settings, createRectangle(20, 20), 3);

        long insideTheCore = cuttingSegments.stream()
                .filter(segment -> isWithin(segment, 3, 17))
                .filter(segment -> segment.getPoint().getAxis(Axis.Z) < -2.99)
                .count();
        assertTrue("The part out of reach of the bit should be pocketed at the target depth",
                insideTheCore > 10);
    }

    @Test
    public void appendGcodePath_shouldNotPocketWhenTheBitReachesTheWholeShape() {
        Settings settings = createSettings(90);

        List<Segment> cuttingSegments = cuttingSegments(settings, createRectangle(20, 4), 10);

        long atTheTargetDepth = cuttingSegments.stream()
                .filter(segment -> segment.getPoint().getAxis(Axis.Z) < -9.99)
                .count();
        assertEquals("A 4mm wide stroke is carved out entirely, so nothing is left to pocket",
                0, atTheTargetDepth);
    }

    @Test
    public void appendGcodePath_shouldCarveTheCentreLineOfAShapeWithAHole() {
        Settings settings = createSettings(90);
        Area ring = new Area(new Ellipse2D.Double(0, 0, 20, 20));
        ring.subtract(new Area(new Ellipse2D.Double(4, 4, 12, 12)));

        List<Segment> cuttingSegments = cuttingSegments(settings, new Path(ring), 10);

        // The ring is 4mm wide all the way around, so its centre line is a circle 8mm out from the
        // middle carved at an even 2mm depth
        cuttingSegments.forEach(segment -> {
            double radius = Math.hypot(segment.getPoint().getAxis(Axis.X) - 10, segment.getPoint().getAxis(Axis.Y) - 10);
            assertEquals("Expected the carve to follow the middle of the ring", 8, radius, 0.6);
            assertEquals("Expected an even depth all the way around the ring",
                    -2, segment.getPoint().getAxis(Axis.Z), 0.3);
        });
    }

    @Test
    public void appendGcodePath_shouldCarveACircleToItsMiddle() {
        Settings settings = createSettings(90);

        // A circle has no centre line to speak of, only a middle — it should still be carved to
        // the depth where the bit is as wide as the circle, and no deeper
        double deepestCut = deepestCut(settings, new Path(new Ellipse2D.Double(0, 0, 20, 20)), 100);

        assertEquals(10, deepestCut, 0.5);
    }

    @Test
    public void appendGcodePath_shouldCarveTheShapesOfARealDesign() {
        Settings settings = createSettings(60);
        settings.setArcFitting(true);
        Design design = new UgsDesignReader()
                .read(VCarveToolPathTest.class.getResourceAsStream("/pocket-test.ugsd"))
                .orElseThrow();

        for (Entity entity : design.getEntities()) {
            List<Segment> cuttingSegments = cuttingSegments(settings, (Cuttable) entity, 4);

            assertFalse("Every shape of the design should be carved", cuttingSegments.isEmpty());
            cuttingSegments.forEach(segment -> assertTrue(
                    "Cut at " + segment.getPoint().getAxis(Axis.Z) + " went past the target depth",
                    segment.getPoint().getAxis(Axis.Z) >= -4.001));
        }
    }

    @Test
    public void appendGcodePath_shouldStayWithinTheShape() {
        Settings settings = createSettings(90);

        List<Segment> cuttingSegments = cuttingSegments(settings, createRectangle(20, 20), 3);

        cuttingSegments.forEach(segment -> assertTrue(
                "The tool left the shape at " + segment.getPoint(), isWithin(segment, 0, 20)));
    }

    @Test
    public void appendGcodePath_shouldStartFromTheStartDepth() {
        Settings settings = createSettings(90);
        VCarveToolPath toolPath = new VCarveToolPath(settings, createRectangle(20, 4));
        toolPath.setStartDepth(2);
        toolPath.setTargetDepth(10);

        double deepestCut = cuttingSegments(toolPath).stream()
                .mapToDouble(segment -> -segment.getPoint().getAxis(Axis.Z))
                .max()
                .orElseThrow();

        assertEquals("Carving 2mm of remaining width below an already cut depth of 2mm",
                4, deepestCut, 0.15);
    }

    @Test
    public void appendGcodePath_shouldNotGenerateAnythingWhenTargetDepthIsAtTheStartDepth() {
        Settings settings = createSettings(90);
        VCarveToolPath toolPath = new VCarveToolPath(settings, createRectangle(20, 20));
        toolPath.setStartDepth(5);
        toolPath.setTargetDepth(5);

        assertTrue("There is nothing left to carve", toolPath.toGcodePath().getSegments().isEmpty());
    }

    @Test
    public void appendGcodePath_shouldRetractToSafeHeightBeforeCarving() {
        Settings settings = createSettings(90);
        settings.setSafeHeight(4);
        VCarveToolPath toolPath = new VCarveToolPath(settings, createRectangle(20, 4));
        toolPath.setTargetDepth(10);

        List<Segment> moves = toolPath.toGcodePath().getSegments().stream()
                .filter(segment -> segment.getType() == SegmentType.MOVE && segment.getPoint().hasAxis(Axis.Z))
                .toList();

        assertFalse("The tool has to be lifted over the material", moves.isEmpty());
        assertEquals("The first move should go to the safe height", 4,
                moves.get(0).getPoint().getAxis(Axis.Z), 0.001);
    }

    private static Rectangle createRectangle(double width, double height) {
        Rectangle rectangle = new Rectangle();
        rectangle.setSize(new Size(width, height));
        return rectangle;
    }

    private static Settings createSettings(double vBitAngle) {
        Settings settings = new Settings();
        settings.setVBitAngle(vBitAngle);
        settings.setToolDiameter(3);
        settings.setToolStepOver(0.3);
        settings.setDepthPerPass(1);
        settings.setSafeHeight(1);
        settings.setArcFitting(false);
        return settings;
    }

    private static List<Segment> cuttingSegments(Settings settings, Cuttable shape, double targetDepth) {
        VCarveToolPath toolPath = new VCarveToolPath(settings, shape);
        toolPath.setTargetDepth(targetDepth);
        return cuttingSegments(toolPath);
    }

    private static List<Segment> cuttingSegments(VCarveToolPath toolPath) {
        return toolPath.toGcodePath().getSegments().stream()
                .filter(segment -> segment.getType() == SegmentType.POINT || segment.getType() == SegmentType.LINE)
                .toList();
    }

    private static double deepestCut(Settings settings, Cuttable shape, double targetDepth) {
        return cuttingSegments(settings, shape, targetDepth).stream()
                .mapToDouble(segment -> -segment.getPoint().getAxis(Axis.Z))
                .max()
                .orElseThrow();
    }

    private static boolean isWithin(Segment segment, double min, double max) {
        double x = segment.getPoint().getAxis(Axis.X);
        double y = segment.getPoint().getAxis(Axis.Y);
        return x >= min && x <= max && y >= min && y <= max;
    }

    private static String describe(List<Segment> segments) {
        return segments.stream().limit(10).map(segment -> segment.getPoint().toString()).toList().toString();
    }
}
