package com.willwinder.universalgcodesender.fx.component.visualizer.designer;


import eu.mihosoft.vrl.v3d.CSG;
import org.junit.Test;

import java.awt.geom.Ellipse2D;
import java.awt.geom.Path2D;
import java.awt.geom.Rectangle2D;

import static org.junit.Assert.assertNotNull;

public class EntityShapeFactoryTest {
    @Test
    public void shapeToCSGShouldReturnCSGForEllipse() {
        CSG csg = EntityShapeFactory.shapeToCSG(new Ellipse2D.Double(0, 0, 10, 10), 0.1);

        assertNotNull(csg);
    }

    @Test
    public void shapeToCSGShouldReturnCSGForRectangle() {
        CSG csg = EntityShapeFactory.shapeToCSG(new Rectangle2D.Double(0, 0, 10, 10), 0.1);

        assertNotNull(csg);
    }

    @Test
    public void shapeToCSGShouldReturnCSGForCounterClockwisePath() {
        // A square traced in the opposite winding direction from Java's AWT shapes,
        // mimicking a hand-built or SVG-imported Path2D.
        Path2D.Double path = new Path2D.Double();
        path.moveTo(0, 0);
        path.lineTo(0, 10);
        path.lineTo(10, 10);
        path.lineTo(10, 0);
        path.closePath();

        CSG csg = EntityShapeFactory.shapeToCSG(path, 0.1);

        assertNotNull(csg);
    }
}