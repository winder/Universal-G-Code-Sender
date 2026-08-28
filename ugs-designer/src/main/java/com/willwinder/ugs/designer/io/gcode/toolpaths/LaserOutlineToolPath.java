package com.willwinder.ugs.designer.io.gcode.toolpaths;

import com.willwinder.ugs.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.designer.io.gcode.path.GcodePath;
import com.willwinder.ugs.designer.io.gcode.path.Segment;
import com.willwinder.ugs.designer.io.gcode.path.SegmentType;
import com.willwinder.ugs.designer.model.Settings;
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
        List<Tabs.Section> sections = toSections(ToolPathUtils.geometryToCoordinates(geometry));

        int currentPass = 0;
        while (currentPass < source.getPasses()) {
            currentPass++;
            String label = " Pass " + currentPass + " of " + source.getPasses();
            for (Tabs.Section section : sections) {
                if (section.tab()) {
                    continue;
                }

                List<PartialPosition> coordinates = section.coordinates().stream()
                        .map(numericCoordinate -> PartialPosition.builder(numericCoordinate).build()).toList();

                gcodePath.addSegment(SegmentType.MOVE, coordinates.get(0), label);
                coordinates.forEach(c -> gcodePath.addSegment(SegmentType.LINE, c));
                label = null;
            }
        }
    }

    /**
     * A laser has no depth to leave a tab at, so a tab is a stretch that is not burnt through at
     * all. The laser is moved across it with a rapid, which leaves it off in laser mode.
     */
    private List<Tabs.Section> toSections(List<PartialPosition> geometryCoordinates) {
        if (!source.hasTabs()) {
            return List.of(new Tabs.Section(geometryCoordinates, false));
        }
        return Tabs.split(geometryCoordinates, source.getTabCount(), settings.getTabLength());
    }
}
