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
package com.willwinder.ugs.nbp.designer.io.gcode.toolpaths;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Raster;
import com.willwinder.ugs.nbp.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.io.gcode.path.Segment;
import com.willwinder.ugs.nbp.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.nbp.designer.model.Settings;
import static com.willwinder.ugs.nbp.designer.utils.GeometryUtils.generateLineString;
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

public class LaserRasterToolPath extends AbstractToolPath {
    private final Raster source;
    private final double toolPathAngle;

    public LaserRasterToolPath(Settings settings, Cuttable source) {
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

    private int intensityToSpindle(double intensity) {
        intensity = clamp(intensity, 0.0, 1.0);

        double power = 1.0 - intensity; // invert: black = strong
        return (int) Math.round(
                settings.getMaxSpindleSpeed() * (power * source.getSpindleSpeed() / 100.0)
        );
    }

    public void appendGcodePath(GcodePath gcodePath, Settings settings) {
        gcodePath.addSegment(new Segment(SegmentType.SEAM, null, null, (int) Math.round(settings.getMaxSpindleSpeed() * (source.getSpindleSpeed() / 100d)), source.getFeedRate()));

        List<Geometry> geometries = getGeometries();
        geometries.forEach(g -> {
            Envelope envelope = g.getEnvelopeInternal();

            int currentPass = 0;
            while (currentPass < source.getPasses()) {
                currentPass++;
                generateBothPath(gcodePath, envelope);
            }
        });
    }

    private void generateBothPath(GcodePath gcodePath, Envelope envelope) {
        boolean reverse = false;
        double currentY = envelope.getMinY() - (envelope.getHeight() * 2);
        while (currentY < envelope.getMaxY() + (envelope.getHeight() * 2)) {
            addReversibleLineSegment(gcodePath, envelope, currentY, reverse);
            reverse = !reverse;
            currentY += settings.getLaserDiameter();
        }
    }

    private void addReversibleLineSegment(
            GcodePath gcodePath,
            Envelope envelope,
            double offsetAlongNormal,
            boolean reverse
    ) {
        LineString lineString = generateLineString(envelope, offsetAlongNormal, toolPathAngle);
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

        double maxStep = settings.getLaserDiameter() * 0.5;
        int steps = Math.max(1, (int) Math.ceil(length / maxStep));

        double sx = dx / steps;
        double sy = dy / steps;

        double cx = c0.x;
        double cy = c0.y;

        boolean laserActive = false;
        Integer currentLaserPower = null;
        PartialPosition lastPoint = null;

        for (int i = 0; i < steps; i++) {
            double nx = cx + sx;
            double ny = cy + sy;

            double mx = (cx + nx) * 0.5;
            double my = (cy + ny) * 0.5;

            int laserPower = getLaserPowerAt(mx, my);

            PartialPosition next = PartialPosition.builder(UnitUtils.Units.MM)
                    .setX(nx)
                    .setY(ny)
                    .build();

            if (!laserActive) {
                if (laserPower > 0) {
                    // First time we hit energy: rapid to start
                    PartialPosition start = PartialPosition.builder(UnitUtils.Units.MM)
                            .setX(cx)
                            .setY(cy)
                            .build();

                    gcodePath.addSegment(
                            new Segment(
                                    SegmentType.MOVE,
                                    start,
                                    null,
                                    null,
                                    null
                            )
                    );

                    currentLaserPower = laserPower;
                    laserActive = true;
                }
            } else {
                if (laserPower != currentLaserPower) {
                    // Flush previous segment
                    gcodePath.addSegment(
                            new Segment(
                                    SegmentType.LINE,
                                    lastPoint,
                                    null,
                                    currentLaserPower,
                                    source.getFeedRate()
                            )
                    );
                    currentLaserPower = laserPower;

                    if (laserPower == 0) {
                        laserActive = false;
                    }
                }
            }

            lastPoint = next;
            cx = nx;
            cy = ny;
        }

        // Flush trailing segment
        if (laserActive) {
            gcodePath.addSegment(
                    new Segment(
                            SegmentType.LINE,
                            lastPoint,
                            null,
                            currentLaserPower,
                            source.getFeedRate()
                    )
            );
        }
    }

    private int getLaserPowerAt(double mx, double my) {
        double intensity = source.getIntensityAt(new Point2D.Double(mx, my));
        return intensityToSpindle(intensity);
    }
}
