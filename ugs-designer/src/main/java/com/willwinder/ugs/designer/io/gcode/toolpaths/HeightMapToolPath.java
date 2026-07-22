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

import com.willwinder.ugs.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.designer.entities.cuttable.Raster;
import com.willwinder.ugs.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.designer.io.gcode.path.Segment;
import com.willwinder.ugs.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.designer.model.Settings;
import static com.willwinder.ugs.designer.utils.GeometryUtils.generateLineString;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import static com.willwinder.universalgcodesender.utils.MathUtils.clamp;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

import java.awt.geom.Area;
import java.awt.geom.Point2D;
import java.util.List;

/**
 * Carves a relief from a {@link Raster} by treating its grayscale as a height map: brighter pixels
 * stay at the stock surface and darker pixels are cut deeper, down to the target depth.
 * <p>
 * When roughing is enabled the material is first cleared in flat step-down layers (leaving a small
 * amount of stock above the surface), followed by a single finishing pass that follows the surface
 * contour exactly.
 *
 * @author Joacim Breiler
 */
public class HeightMapToolPath extends AbstractToolPath {
    private static final double EPSILON = 1e-6;

    private final Raster source;
    private final double toolPathAngle;

    public HeightMapToolPath(Settings settings, Cuttable source) {
        super(settings);
        if (!(source instanceof Raster)) {
            throw new IllegalArgumentException("Source is not a Raster");
        }
        this.source = (Raster) source;
        this.toolPathAngle = source.getToolPathAngle();
    }

    private List<Geometry> getGeometries() {
        if (ToolPathUtils.isClosedGeometry(source.getShape())) {
            Geometry geometry = ToolPathUtils.convertAreaToGeometry(new Area(source.getShape()), getGeometryFactory(), settings.getFlatnessPrecision());
            return List.of(geometry);
        } else {
            return ToolPathUtils.convertShapeToGeometry(source.getShape(), getGeometryFactory(), settings.getFlatnessPrecision());
        }
    }

    @Override
    public void appendGcodePath(GcodePath gcodePath, Settings settings) {
        if (getTargetDepth() - getStartDepth() <= 0) {
            return;
        }

        // Make sure the depth map is generated so we sample the height field, not the original photo
        source.awaitDepthMap();

        int spindleSpeed = (int) Math.round(settings.getMaxSpindleSpeed() * (source.getSpindleSpeed() / 100d));
        gcodePath.addSegment(new Segment(SegmentType.SEAM, null, null, spindleSpeed, source.getFeedRate()));

        double lineSpacing = Math.max(0.1, settings.getToolDiameter() * clamp(settings.getToolStepOver(), 0.01, 1.0));

        for (Geometry geometry : getGeometries()) {
            Envelope envelope = geometry.getEnvelopeInternal();

            if (source.isRoughing()) {
                double depthPerPass = Math.max(0.01, settings.getDepthPerPass());
                double previousLayer = getStartDepth();
                double layerDepth = getStartDepth();
                while (layerDepth < getTargetDepth()) {
                    layerDepth = Math.min(getTargetDepth(), layerDepth + depthPerPass);
                    generateRoughingPass(gcodePath, envelope, lineSpacing, layerDepth, previousLayer);
                    previousLayer = layerDepth;
                }
            }

            generateFinishingPass(gcodePath, envelope, lineSpacing);
        }
    }

    /**
     * The finishing pass follows the surface across the whole area in one continuous boustrophedon path.
     */
    private void generateFinishingPass(GcodePath gcodePath, Envelope envelope, double lineSpacing) {
        addSafeHeightSegment(gcodePath, null, true);

        boolean reverse = false;
        boolean started = false;
        double[] offsetRange = offsetRange(envelope, toolPathAngle);
        double offset = offsetRange[0];
        while (offset <= offsetRange[1] + 1e-9) {
            started = addFinishingLine(gcodePath, envelope, offset, reverse, started);
            reverse = !reverse;
            offset += lineSpacing;
        }

        addSafeHeightSegment(gcodePath, null, true);
    }

    private boolean addFinishingLine(GcodePath gcodePath, Envelope envelope, double offset, boolean reverse, boolean started) {
        LineString lineString = generateLineString(envelope, offset, toolPathAngle);
        if (lineString == null) {
            return started;
        }

        Coordinate c0 = lineString.getCoordinateN(0);
        Coordinate c1 = lineString.getCoordinateN(1);
        if (reverse) {
            Coordinate tmp = c0;
            c0 = c1;
            c1 = tmp;
        }

        double dx = c1.x - c0.x;
        double dy = c1.y - c0.y;
        double length = Math.hypot(dx, dy);
        if (length <= 0) {
            return started;
        }

        double maxStep = Math.max(0.1, settings.getToolDiameter() * 0.25);
        int steps = Math.max(1, (int) Math.ceil(length / maxStep));
        double sx = dx / steps;
        double sy = dy / steps;

        double cx = c0.x;
        double cy = c0.y;
        PartialPosition start = position(cx, cy, finishDepth(cx, cy));
        if (!started) {
            gcodePath.addSegment(new Segment(SegmentType.MOVE, PartialPosition.builder(UnitUtils.Units.MM).setX(cx).setY(cy).build(), null, null, null));
            gcodePath.addSegment(new Segment(SegmentType.POINT, start, null, null, source.getFeedRate()));
            started = true;
        } else {
            gcodePath.addSegment(new Segment(SegmentType.LINE, start, null, null, source.getFeedRate()));
        }

        for (int i = 0; i < steps; i++) {
            cx += sx;
            cy += sy;
            PartialPosition next = position(cx, cy, finishDepth(cx, cy));
            gcodePath.addSegment(new Segment(SegmentType.LINE, next, null, null, source.getFeedRate()));
        }

        return started;
    }

    /**
     * A roughing layer only machines where material still remains above {@code layerDepth}. Within an
     * engaged run the tool rides over regions the previous, shallower layers already cleared at that
     * cleared height, so it descends and retracts once per run instead of plunging repeatedly.
     */
    private void generateRoughingPass(GcodePath gcodePath, Envelope envelope, double lineSpacing, double layerDepth, double previousLayer) {
        addSafeHeightSegment(gcodePath, null, true);

        boolean reverse = false;
        double[] offsetRange = offsetRange(envelope, toolPathAngle);
        double offset = offsetRange[0];
        while (offset <= offsetRange[1] + 1e-9) {
            addRoughingLine(gcodePath, envelope, offset, reverse, layerDepth, previousLayer);
            reverse = !reverse;
            offset += lineSpacing;
        }

        addSafeHeightSegment(gcodePath, null, true);
    }

    private void addRoughingLine(GcodePath gcodePath, Envelope envelope, double offset, boolean reverse,
                                 double layerDepth, double previousLayer) {
        LineString lineString = generateLineString(envelope, offset, toolPathAngle);
        if (lineString == null) {
            return;
        }

        Coordinate c0 = lineString.getCoordinateN(0);
        Coordinate c1 = lineString.getCoordinateN(1);
        if (reverse) {
            Coordinate tmp = c0;
            c0 = c1;
            c1 = tmp;
        }

        double dx = c1.x - c0.x;
        double dy = c1.y - c0.y;
        double length = Math.hypot(dx, dy);
        if (length <= 0) {
            return;
        }

        double maxStep = Math.max(0.1, settings.getToolDiameter() * 0.25);
        int steps = Math.max(1, (int) Math.ceil(length / maxStep));
        double sx = dx / steps;
        double sy = dy / steps;

        double[] xs = new double[steps + 1];
        double[] ys = new double[steps + 1];
        int firstEngaged = -1;
        int lastEngaged = -1;
        for (int i = 0; i <= steps; i++) {
            xs[i] = c0.x + sx * i;
            ys[i] = c0.y + sy * i;
            if (requiredRoughDepth(xs[i], ys[i]) > previousLayer + EPSILON) {
                if (firstEngaged < 0) {
                    firstEngaged = i;
                }
                lastEngaged = i;
            }
        }

        // Nothing left above this layer on the whole line, so skip it entirely.
        if (firstEngaged < 0) {
            return;
        }

        // Plunge once at the start of the engaged span. Where the surface dips into already-cleared
        // material within the span, ride over it at the previous cleared height instead of lifting to
        // safe height and plunging again.
        gcodePath.addSegment(new Segment(SegmentType.MOVE, PartialPosition.builder(UnitUtils.Units.MM).setX(xs[firstEngaged]).setY(ys[firstEngaged]).build(), null, null, null));
        gcodePath.addSegment(new Segment(SegmentType.POINT, roughPosition(xs[firstEngaged], ys[firstEngaged], layerDepth, previousLayer), null, null, source.getFeedRate()));
        for (int i = firstEngaged + 1; i <= lastEngaged; i++) {
            gcodePath.addSegment(new Segment(SegmentType.LINE, roughPosition(xs[i], ys[i], layerDepth, previousLayer), null, null, source.getFeedRate()));
        }

        addSafeHeightSegment(gcodePath, null, true);
    }

    private PartialPosition roughPosition(double x, double y, double layerDepth, double previousLayer) {
        double required = requiredRoughDepth(x, y);
        double depth = required > previousLayer + EPSILON
                ? Math.min(layerDepth, required)
                : Math.min(previousLayer, required);
        return position(x, y, depth);
    }

    /**
     * The passes step along the normal to the tool-path direction, starting from the envelope's
     * lower-left corner. This returns the range of offsets along that normal needed to sweep the whole
     * envelope; for angled paths it is wider than the envelope height, otherwise the sweep would stop
     * before reaching the far corners.
     */
    private static double[] offsetRange(Envelope envelope, double angleInDegrees) {
        double radians = Math.toRadians(-angleInDegrees);
        double dx = Math.cos(radians);
        double dy = Math.sin(radians);
        double nx = -dy;
        double ny = dx;

        double minOffset = Double.POSITIVE_INFINITY;
        double maxOffset = Double.NEGATIVE_INFINITY;
        for (double cx : new double[]{envelope.getMinX(), envelope.getMaxX()}) {
            for (double cy : new double[]{envelope.getMinY(), envelope.getMaxY()}) {
                double projection = (cx - envelope.getMinX()) * nx + (cy - envelope.getMinY()) * ny;
                minOffset = Math.min(minOffset, projection);
                maxOffset = Math.max(maxOffset, projection);
            }
        }
        return new double[]{minOffset, maxOffset};
    }

    private double finishDepth(double x, double y) {
        // Tool-radius compensated so the tool follows the shallowest surface under its footprint
        return clamp(footprintSurfaceDepth(x, y), getStartDepth(), getTargetDepth());
    }

    private double requiredRoughDepth(double x, double y) {
        // Roughing follows the true surface at this point (not the radius-compensated one) so it clears
        // material right up to the edges, leaving only the stock-to-leave for the finishing tool. The
        // finishing pass stays radius compensated to avoid gouging the final surface.
        return clamp(surfaceDepthAt(x, y) - source.getStockToLeave(), getStartDepth(), getTargetDepth());
    }

    private double surfaceDepthAt(double x, double y) {
        double intensity = source.getIntensityAt(new Point2D.Double(x, y));
        double reliefDepth = getTargetDepth() - getStartDepth();
        return getStartDepth() + reliefDepth * (1.0 - intensity);
    }

    /**
     * Returns the shallowest surface depth under the tool footprint at {@code (x, y)}, sampled on two
     * concentric rings plus the centre. The tool cannot cut below this without gouging material that
     * should remain, so it is the deepest the tool may safely descend at this position.
     */
    private double footprintSurfaceDepth(double x, double y) {
        double radius = settings.getToolDiameter() / 2.0;
        double minDepth = surfaceDepthAt(x, y);
        if (radius <= 0) {
            return minDepth;
        }

        int directions = 8;
        double[] radii = {radius * 0.5, radius};
        for (double r : radii) {
            for (int i = 0; i < directions; i++) {
                double angle = (2.0 * Math.PI * i) / directions;
                double sx = x + r * Math.cos(angle);
                double sy = y + r * Math.sin(angle);
                minDepth = Math.min(minDepth, surfaceDepthAt(sx, sy));
            }
        }
        return minDepth;
    }

    private PartialPosition position(double x, double y, double depth) {
        return PartialPosition.builder(UnitUtils.Units.MM)
                .setX(x)
                .setY(y)
                .setZ(-depth)
                .build();
    }
}
