package com.willwinder.ugs.nbp.designer.io.gcode.toolpaths;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.nbp.designer.model.Size;
import static org.junit.Assert.assertEquals;
import org.junit.Test;
import org.locationtech.jts.geom.Geometry;
import org.locationtech.jts.geom.GeometryFactory;

import java.awt.geom.Area;
import java.awt.geom.Point2D;

public class GeometrySizeComparatorTest {

    @Test
    public void compareShouldCompareIfShapeFitsInAnother() {
        Rectangle rectangle1 = new Rectangle();
        rectangle1.setPosition(new Point2D.Double(0,0));
        rectangle1.setSize(new Size(10, 10));
        Geometry geometry1 = ToolPathUtils.convertAreaToGeometry(new Area(rectangle1.getShape()), new GeometryFactory(), 1);

        Rectangle rectangle2 = new Rectangle();
        rectangle2.setPosition(new Point2D.Double(2.5,2.5));
        rectangle2.setSize(new Size(5, 5));
        Geometry geometry2 = ToolPathUtils.convertAreaToGeometry(new Area(rectangle2.getShape()), new GeometryFactory(), 1);

        GeometrySizeComparator comparator = new GeometrySizeComparator();
        assertEquals(1, comparator.compare(geometry1, geometry2));
        assertEquals(-1, comparator.compare(geometry2, geometry1));
        assertEquals(1, comparator.compare(geometry1, geometry1));
    }

    @Test
    public void compareShouldTreatIntersectingShapesAsEquals() {
        Rectangle rectangle1 = new Rectangle();
        rectangle1.setPosition(new Point2D.Double(0,0));
        rectangle1.setSize(new Size(10, 10));
        Geometry geometry1 = ToolPathUtils.convertAreaToGeometry(new Area(rectangle1.getShape()), new GeometryFactory(), 1);

        Rectangle rectangle2 = new Rectangle();
        rectangle2.setPosition(new Point2D.Double(-1,-1));
        rectangle2.setSize(new Size(10, 10));
        Geometry geometry2 = ToolPathUtils.convertAreaToGeometry(new Area(rectangle2.getShape()), new GeometryFactory(), 1);

        GeometrySizeComparator comparator = new GeometrySizeComparator();
        assertEquals(0, comparator.compare(geometry1, geometry2));
        assertEquals(0, comparator.compare(geometry2, geometry1));
    }
}