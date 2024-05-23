package com.willwinder.ugs.nbp.designer.io.gcode.toolpaths;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.io.gcode.path.Segment;
import com.willwinder.ugs.nbp.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.nbp.designer.model.Settings;
import com.willwinder.universalgcodesender.model.PartialPosition;
import org.locationtech.jts.geom.Geometry;

import java.awt.geom.Area;
import java.util.ArrayList;
import java.util.List;

public class LaserOutlineToolPath extends AbstractToolPath {
    private final Cuttable source;

    public LaserOutlineToolPath(Settings settings, Cuttable source) {
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
            geometries = ToolPathUtils.toGeometryList(geometry);
        } else {
            geometries = ToolPathUtils.convertShapeToGeometry(source.getShape(), getGeometryFactory());
        }


        geometries.forEach(g -> {
            List<PartialPosition> geometryCoordinates = ToolPathUtils.geometryToCoordinates(g);

            int currentPass = 0;
            while (currentPass < source.getPasses()) {
                currentPass++;
                List<PartialPosition> partialPosition = geometryCoordinates.stream()
                        .map(numericCoordinate -> PartialPosition.builder(numericCoordinate).build()).toList();

                gcodePath.addSegment(SegmentType.MOVE, partialPosition.get(0), " Pass " + currentPass + " of " + source.getPasses());
                partialPosition.forEach(c -> gcodePath.addSegment(SegmentType.LINE, c));
            }
        });

    }
}
