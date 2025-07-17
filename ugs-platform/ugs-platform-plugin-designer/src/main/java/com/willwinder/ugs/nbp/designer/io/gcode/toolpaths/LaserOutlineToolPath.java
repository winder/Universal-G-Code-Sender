package com.willwinder.ugs.nbp.designer.io.gcode.toolpaths;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.nbp.designer.io.gcode.path.Segment;
import com.willwinder.ugs.nbp.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.nbp.designer.model.Settings;
import com.willwinder.universalgcodesender.model.PartialPosition;
import org.locationtech.jts.geom.Geometry;

import java.awt.geom.Area;
import java.util.List;

public class LaserOutlineToolPath extends AbstractToolPath {
    private final Cuttable source;

    public LaserOutlineToolPath(Settings settings, Cuttable source) {
        super(settings);
        this.source = source;
    }

    public void appendGcodePath(GcodePath gcodePath, Settings settings) {
        gcodePath.addSegment(new Segment(SegmentType.SEAM, null, null, (int) Math.round(settings.getMaxSpindleSpeed() * (source.getSpindleSpeed() / 100d)), source.getFeedRate()));

        List<Geometry> geometries = getGeometries();
        geometries.forEach(g -> addGeometrySegments(g, gcodePath));
    }

    private List<Geometry> getGeometries() {
        if (ToolPathUtils.isClosedGeometry(source.getShape())) {
            Geometry geometry = ToolPathUtils.convertAreaToGeometry(new Area(source.getShape()), getGeometryFactory(), settings.getFlatnessPrecision());
            return ToolPathUtils.toGeometryList(geometry);
        } else {
            return ToolPathUtils.convertShapeToGeometry(source.getShape(), getGeometryFactory(), settings.getFlatnessPrecision());
        }
    }

    private void addGeometrySegments(Geometry geometry, GcodePath gcodePath) {
        List<PartialPosition> geometryCoordinates = ToolPathUtils.geometryToCoordinates(geometry);

        int currentPass = 0;
        while (currentPass < source.getPasses()) {
            currentPass++;
            List<PartialPosition> partialPosition = geometryCoordinates.stream()
                    .map(numericCoordinate -> PartialPosition.builder(numericCoordinate).build()).toList();

            gcodePath.addSegment(SegmentType.MOVE, partialPosition.get(0), " Pass " + currentPass + " of " + source.getPasses());
            partialPosition.forEach(c -> gcodePath.addSegment(SegmentType.LINE, c));
        }
    }
}
