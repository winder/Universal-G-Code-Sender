package com.willwinder.ugs.designer.io.gcode.toolpaths;

import com.willwinder.ugs.designer.entities.cuttable.Raster;
import com.willwinder.ugs.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.designer.io.gcode.path.Segment;
import com.willwinder.ugs.designer.model.Settings;
import org.junit.Test;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.util.List;

import static com.willwinder.ugs.designer.io.gcode.path.SegmentType.LINE;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class HeightMapToolPathTest {

    private static Settings createSettings() {
        Settings settings = new Settings();
        settings.setMaxSpindleSpeed(10000);
        settings.setToolDiameter(2);
        settings.setToolStepOver(0.5);
        settings.setDepthPerPass(1);
        return settings;
    }

    private static BufferedImage createImage(Color color) {
        BufferedImage image = new BufferedImage(8, 8, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                image.setRGB(x, y, color.getRGB());
            }
        }
        return image;
    }

    private static double minZ(List<Segment> segments) {
        return segments.stream()
                .filter(s -> s.type == LINE)
                .mapToDouble(s -> s.getPoint().getZ())
                .min()
                .orElse(Double.NaN);
    }

    private static BufferedImage createHalfImage(int width, int height, int boundaryX) {
        BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                image.setRGB(x, y, (x < boundaryX ? Color.WHITE : Color.BLACK).getRGB());
            }
        }
        return image;
    }

    private static double deepestZBetween(List<Segment> segments, double minX, double maxX) {
        return segments.stream()
                .filter(s -> s.point != null && s.point.hasX() && s.point.hasZ())
                .filter(s -> s.point.getX() >= minX && s.point.getX() <= maxX)
                .mapToDouble(s -> s.point.getZ())
                .min()
                .orElse(Double.NaN);
    }

    @Test
    public void appendGcodePath_ShouldCutBlackAreasToTargetDepth() {
        Raster raster = new Raster(createImage(Color.BLACK));
        raster.setRoughing(false);
        HeightMapToolPath toolPath = new HeightMapToolPath(createSettings(), raster);
        toolPath.setStartDepth(0);
        toolPath.setTargetDepth(3);

        GcodePath gcodePath = toolPath.toGcodePath();

        assertEquals("Black pixels should be carved to the full target depth", -3, minZ(gcodePath.getSegments()), 0.01);
    }

    @Test
    public void appendGcodePath_ShouldLeaveWhiteAreasAtSurface() {
        Raster raster = new Raster(createImage(Color.WHITE));
        raster.setRoughing(false);
        HeightMapToolPath toolPath = new HeightMapToolPath(createSettings(), raster);
        toolPath.setStartDepth(0);
        toolPath.setTargetDepth(3);

        GcodePath gcodePath = toolPath.toGcodePath();

        List<Segment> lineSegments = gcodePath.getSegments().stream().filter(s -> s.type == LINE).toList();
        assertTrue("Expected the height map to produce line segments", !lineSegments.isEmpty());
        lineSegments.forEach(segment ->
                assertEquals("White pixels should stay at the stock surface", 0, segment.getPoint().getZ(), 0.01));
    }

    @Test
    public void appendGcodePath_ShouldProduceMoreSegmentsWhenRoughingEnabled() {
        Raster roughingRaster = new Raster(createImage(Color.BLACK));
        roughingRaster.setRoughing(true);
        HeightMapToolPath roughingToolPath = new HeightMapToolPath(createSettings(), roughingRaster);
        roughingToolPath.setStartDepth(0);
        roughingToolPath.setTargetDepth(3);

        Raster finishingRaster = new Raster(createImage(Color.BLACK));
        finishingRaster.setRoughing(false);
        HeightMapToolPath finishingToolPath = new HeightMapToolPath(createSettings(), finishingRaster);
        finishingToolPath.setStartDepth(0);
        finishingToolPath.setTargetDepth(3);

        int roughingSegments = roughingToolPath.toGcodePath().getSegments().size();
        int finishingSegments = finishingToolPath.toGcodePath().getSegments().size();

        assertTrue("Roughing should add clearing passes before the finishing pass",
                roughingSegments > finishingSegments);
    }

    @Test
    public void appendGcodePath_ShouldNotCutDeepSideWithinToolRadiusOfHigherMaterial() {
        // Left half is raised (white), right half is deep (black); a 4mm tool (radius 2) must not plunge
        // to full depth within 2mm of the boundary or it would gouge the raised half.
        Raster raster = new Raster(createHalfImage(20, 20, 10));
        raster.setRoughing(false);
        Settings settings = createSettings();
        settings.setToolDiameter(4);
        HeightMapToolPath toolPath = new HeightMapToolPath(settings, raster);
        toolPath.setStartDepth(0);
        toolPath.setTargetDepth(3);

        List<Segment> segments = toolPath.toGcodePath().getSegments();

        double deepInterior = deepestZBetween(segments, 14, 16);
        double nearBoundary = deepestZBetween(segments, 10.5, 11.5);

        assertEquals("Deep side beyond the tool radius should reach the target depth", -3, deepInterior, 0.01);
        assertTrue("Tool must not cut the deep side within its radius of the raised half (was " + nearBoundary + ")",
                nearBoundary > -0.5);
    }

    @Test
    public void appendGcodePath_ShouldNotReCutClearedRoughingLayers() {
        // A uniform mid-gray surface only needs ~0.5mm of relief, but the target depth is 3mm with a 1mm
        // depth-per-pass. Naive roughing would scan the whole area at all three layers; the optimized
        // version should clear it in a single layer and skip the rest, so it must not balloon the path.
        BufferedImage shallow = createImage(new Color(212, 212, 212));

        Raster finishingOnly = new Raster(shallow);
        finishingOnly.setRoughing(false);
        HeightMapToolPath finishingPath = new HeightMapToolPath(createSettings(), finishingOnly);
        finishingPath.setStartDepth(0);
        finishingPath.setTargetDepth(3);
        int oneFullPass = countCutting(finishingPath.toGcodePath().getSegments());

        Raster roughingRaster = new Raster(shallow);
        roughingRaster.setRoughing(true);
        HeightMapToolPath roughingPath = new HeightMapToolPath(createSettings(), roughingRaster);
        roughingPath.setStartDepth(0);
        roughingPath.setTargetDepth(3);
        int withRoughing = countCutting(roughingPath.toGcodePath().getSegments());

        // Roughing (1 needed layer) + finishing ~= 2 passes. Re-cutting all 3 layers would exceed 3x.
        assertTrue("Roughing should not re-cut already-cleared layers (was " + withRoughing
                        + " cutting moves vs " + oneFullPass + " for a single pass)",
                withRoughing < 3 * oneFullPass);
    }

    @Test
    public void appendGcodePath_ShouldCoverFullAreaWithAngledToolPath() {
        Raster straight = new Raster(createImage(Color.BLACK));
        straight.setRoughing(false);
        HeightMapToolPath straightPath = new HeightMapToolPath(createSettings(), straight);
        straightPath.setStartDepth(0);
        straightPath.setTargetDepth(3);
        List<Segment> straightSegments = straightPath.toGcodePath().getSegments();

        Raster angled = new Raster(createImage(Color.BLACK));
        angled.setRoughing(false);
        angled.setToolPathAngle(45);
        HeightMapToolPath angledPath = new HeightMapToolPath(createSettings(), angled);
        angledPath.setStartDepth(0);
        angledPath.setTargetDepth(3);
        List<Segment> angledSegments = angledPath.toGcodePath().getSegments();

        double cornerX = maxCoordinate(straightSegments, true);
        double cornerY = maxCoordinate(straightSegments, false);
        assertTrue("Angled tool path should still cover the far corner of the area (nearest point was "
                        + distanceToCorner(angledSegments, cornerX, cornerY) + "mm away)",
                distanceToCorner(angledSegments, cornerX, cornerY) < 1.0);
    }

    private static double maxCoordinate(List<Segment> segments, boolean useX) {
        return segments.stream()
                .filter(s -> s.point != null && s.point.hasX() && s.point.hasY())
                .mapToDouble(s -> useX ? s.point.getX() : s.point.getY())
                .max()
                .orElse(Double.NaN);
    }

    private static double distanceToCorner(List<Segment> segments, double cornerX, double cornerY) {
        return segments.stream()
                .filter(s -> s.point != null && s.point.hasX() && s.point.hasY())
                .mapToDouble(s -> Math.hypot(s.point.getX() - cornerX, s.point.getY() - cornerY))
                .min()
                .orElse(Double.NaN);
    }

    private static int countCutting(List<Segment> segments) {
        return (int) segments.stream()
                .filter(s -> s.type == LINE || s.type == com.willwinder.ugs.designer.io.gcode.path.SegmentType.POINT)
                .count();
    }
}
