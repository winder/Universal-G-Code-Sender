package com.willwinder.ugs.designer.io.gcode.toolpaths;

import static com.willwinder.ugs.designer.io.gcode.toolpaths.ToolPathUtils.addGeometriesToCoordinatesList;
import static com.willwinder.ugs.designer.io.gcode.toolpaths.ToolPathUtils.bufferAndCollectGeometries;
import static com.willwinder.ugs.designer.io.gcode.toolpaths.ToolPathUtils.convertAreaToGeometry;
import com.willwinder.ugs.designer.io.ugsd.UgsDesignReader;
import com.willwinder.ugs.designer.model.Design;
import com.willwinder.universalgcodesender.model.PartialPosition;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Polygon;

import java.awt.geom.Area;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

public class ToolPathUtilsTest {

    /**
     * When using a tool diameter that is larger than parts of the shape the buffer may result in smaller lines.
     * Make sure that the spindle is moved up between these line passes
     */
    @Test
    public void addGeometriesToCoordinatesList_shouldHandleLineStringAsSeparateGeometries() {
        UgsDesignReader reader = new UgsDesignReader();
        Design design = reader.read(PocketToolPathTest.class.getResourceAsStream("/x.ugsd")).orElseThrow(RuntimeException::new);

        double toolDiameter = 1.2;

        Geometry geometryCollection = convertAreaToGeometry(new Area(design.getEntities().getFirst().getShape()), new GeometryFactory(), 0.1);
        Geometry shell = geometryCollection.buffer(-toolDiameter / 2d);
        List<Geometry> geometries = bufferAndCollectGeometries(geometryCollection, toolDiameter, 1, 0.1);
        assertEquals(4, geometries.size());

        List<List<PartialPosition>> coordinateList = new ArrayList<>();
        addGeometriesToCoordinatesList(shell, geometries, coordinateList, 0);
        assertEquals(3, coordinateList.size());
    }

    @Test
    public void convertAreaToGeometry_shouldKeepHolesAndIslands() {
        Area area = new Area(new Rectangle2D.Double(0, 0, 100, 100));
        area.subtract(new Area(new Rectangle2D.Double(20, 20, 60, 60)));
        area.add(new Area(new Rectangle2D.Double(40, 40, 20, 20)));

        Geometry geometry = convertAreaToGeometry(area, new GeometryFactory(), 0.1);

        assertEquals(100 * 100 - 60 * 60 + 20 * 20, geometry.getArea(), 0.001);
        assertEquals(2, geometry.getNumGeometries());
        assertEquals(1, ((Polygon) geometry.getGeometryN(0)).getNumInteriorRing()
                + ((Polygon) geometry.getGeometryN(1)).getNumInteriorRing());
    }

    @Test
    public void convertAreaToGeometry_shouldNotFillAHoleBoundedByATinyCurvedRing() {
        // Flattening a very small ellipse with a coarse tolerance folds its ring over itself.
        Area area = new Area(new Rectangle2D.Double(0, 0, 10, 10));
        area.subtract(new Area(new Ellipse2D.Double(4.8, 4.8, 0.4, 0.4)));
        double expected = 100 - Math.PI * 0.2 * 0.2;

        Geometry geometry = convertAreaToGeometry(area, new GeometryFactory(), 0.1);

        assertEquals(expected, geometry.getArea(), 0.1);
    }

    @Test
    public void convertAreaToGeometry_shouldJoinOverlappingSubPathsIntoOnePolygon() {
        // An area splits a self touching outline into rings that share an edge; the pocket must
        // still see one region without a wall along the seam.
        Path2D outline = new Path2D.Double();
        outline.append(new Rectangle2D.Double(0, 0, 30, 10), false);
        outline.append(new Ellipse2D.Double(10, -5, 20, 20), false);
        outline.append(new Rectangle2D.Double(25, 5, 20, 30), false);
        Area area = new Area(outline);

        Geometry geometry = convertAreaToGeometry(area, new GeometryFactory(), 0.1);

        assertEquals(1, geometry.getNumGeometries());
        assertEquals(areaOf(area), geometry.getArea(), 1.0);
    }

    private static double areaOf(Area area) {
        Geometry union = new GeometryFactory().createPolygon();
        java.util.List<?> rings = org.locationtech.jts.awt.ShapeReader.toCoordinates(area.getPathIterator(null, 0.1));
        for (Object ring : rings) {
            Polygon polygon = new GeometryFactory().createPolygon((org.locationtech.jts.geom.Coordinate[]) ring);
            union = union.symDifference(polygon);
        }
        return union.getArea();
    }
}
