package com.willwinder.ugs.nbp.designer.io.gcode.toolpaths;

import com.google.common.collect.Lists;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.io.gcode.path.Segment;
import com.willwinder.ugs.nbp.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.nbp.designer.model.Settings;
import com.willwinder.universalgcodesender.model.PartialPosition;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.CoordinateXY;
import org.locationtech.jts.geom.Envelope;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.LineString;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

public class LaserFillToolPath extends AbstractToolPath {
    private final Cuttable source;

    public LaserFillToolPath(Settings settings, Cuttable source) {
        super(settings);
        this.source = source;
    }

    private static void addGeometriesToCoordinateList(ArrayList<List<PartialPosition>> coordinateList, List<PartialPosition> geometryCoordinates) {
        coordinateList.add(geometryCoordinates.stream()
                .map(numericCoordinate -> PartialPosition.builder(numericCoordinate).build())
                .toList());
    }

    public void appendGcodePath(GcodePath gcodePath, Settings settings) {
        gcodePath.addSegment(new Segment(SegmentType.SEAM, null, null, (int) Math.round(settings.getMaxSpindleSpeed() * (source.getSpindleSpeed() / 100d))));

        List<Geometry> geometries;
        if (ToolPathUtils.isClosedGeometry(source.getShape())) {
            Geometry geometry = ToolPathUtils.convertAreaToGeometry(new Area(source.getShape()), getGeometryFactory());
            geometries = List.of(geometry);
        } else {
            geometries = ToolPathUtils.convertShapeToGeometry(source.getShape(), getGeometryFactory());
        }


        geometries.forEach(g -> {
            Envelope envelope = g.getEnvelopeInternal();


            int currentPass = 0;
            while (currentPass < source.getPasses()) {
                currentPass++;

                boolean reverse = false;
                double currentY = envelope.getMinY();
                while (currentY <= envelope.getMaxY()) {
                    LineString lineString = getGeometryFactory().createLineString(new Coordinate[]{
                            new CoordinateXY(envelope.getMinX(), currentY),
                            new CoordinateXY(envelope.getMaxX(), currentY),
                    });

                    Geometry intersection = g.intersection(lineString);
                    List<PartialPosition> geometryCoordinates = ToolPathUtils.geometryToCoordinates(intersection);
                    List<PartialPosition> partialPosition = geometryCoordinates.stream()
                            .map(numericCoordinate -> PartialPosition.builder(numericCoordinate).build()).toList();

                    if (partialPosition.size() > 1) {
                        if (reverse) {
                            partialPosition = Lists.reverse(partialPosition);
                        }

                        for (int i = 0; i < partialPosition.size(); i += 2) {
                            gcodePath.addSegment(SegmentType.MOVE, partialPosition.get(i));
                            gcodePath.addSegment(SegmentType.LINE, partialPosition.get(i + 1));
                        }
                    }


                    currentY += settings.getLaserDiameter();
                    reverse = !reverse;
                }
            }
        });

    }
}
