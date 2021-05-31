package com.willwinder.ugs.nbp.designer.gui.entities;

import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import org.junit.Test;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import static org.junit.Assert.*;

public class RectangleTest {

    @Test
    public void isWithinShouldReturnTrueIfPointIsWithin() {
        Rectangle rectangle = new Rectangle(10, 10);
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        assertTrue(rectangle.isWithin(new Point2D.Double(11, 11)));
    }

    @Test
    public void isWithinShouldReturnFalseIfPointIsNotWithin() {
        Rectangle rectangle = new Rectangle(10, 10);
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        assertFalse(rectangle.isWithin(new Point2D.Double(9, 11)));
    }

    @Test
    public void getPositionShouldReturnTheTransformedPosition() {
        Rectangle rectangle = new Rectangle(10, 20);
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        assertEquals(Double.valueOf(10), Double.valueOf(rectangle.getPosition().getX()));
        assertEquals(Double.valueOf(20), Double.valueOf(rectangle.getPosition().getY()));
    }

    @Test
    public void getCenterShouldReturnTheTransformedCenterPosition() {
        Rectangle rectangle = new Rectangle(10, 20);
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        assertEquals(Double.valueOf(15), Double.valueOf(rectangle.getCenter().getX()));
        assertEquals(Double.valueOf(25), Double.valueOf(rectangle.getCenter().getY()));
    }

    @Test
    public void setRotationShouldRotateAroundItsCenter() {
        Rectangle rectangle = new Rectangle(10, 10);
        rectangle.setWidth(10);
        rectangle.setHeight(10);

        assertEquals(Double.valueOf(15), Double.valueOf(rectangle.getCenter().getX()));
        assertEquals(Double.valueOf(15), Double.valueOf(rectangle.getCenter().getY()));

        rectangle.rotate(Math.PI * 4);

        assertEquals(Double.valueOf(15), Double.valueOf(rectangle.getCenter().getX()));
        assertEquals(Double.valueOf(15), Double.valueOf(rectangle.getCenter().getY()));
    }

    @Test
    public void moveShouldTranslateTheEntity() {
        Rectangle rectangle = new Rectangle(10, 10);
        rectangle.setWidth(10);
        rectangle.setHeight(10);

        assertEquals(Double.valueOf(15), Double.valueOf(rectangle.getCenter().getX()));
        assertEquals(Double.valueOf(15), Double.valueOf(rectangle.getCenter().getY()));

        rectangle.move(new Point2D.Double(-5, -5));

        assertEquals(Double.valueOf(10), Double.valueOf(rectangle.getCenter().getX()));
        assertEquals(Double.valueOf(10), Double.valueOf(rectangle.getCenter().getY()));
        assertEquals(Double.valueOf(5), Double.valueOf(rectangle.getPosition().getX()));
        assertEquals(Double.valueOf(5), Double.valueOf(rectangle.getPosition().getY()));
    }

    @Test
    public void moveShouldTranslateScaledEntity() {
        Rectangle rectangle = new Rectangle(10, 10);
        rectangle.applyTransform(AffineTransform.getScaleInstance(2, 2));
        rectangle.setWidth(10);
        rectangle.setHeight(10);

        assertEquals(Double.valueOf(10), Double.valueOf(rectangle.getPosition().getX()));
        assertEquals(Double.valueOf(10), Double.valueOf(rectangle.getPosition().getY()));
        assertEquals(Double.valueOf(20), Double.valueOf(rectangle.getCenter().getX()));
        assertEquals(Double.valueOf(20), Double.valueOf(rectangle.getCenter().getY()));

        rectangle.move(new Point2D.Double(-5, -5));

        assertEquals(Double.valueOf(15), Double.valueOf(rectangle.getCenter().getX()));
        assertEquals(Double.valueOf(15), Double.valueOf(rectangle.getCenter().getY()));
        assertEquals(Double.valueOf(5), Double.valueOf(rectangle.getPosition().getX()));
        assertEquals(Double.valueOf(5), Double.valueOf(rectangle.getPosition().getY()));
    }

    @Test
    public void rotateShouldRotateAroundItsCenter() {
        Rectangle rectangle = new Rectangle(10, 10);
        rectangle.setWidth(10);
        rectangle.setHeight(10);

        assertEquals(Double.valueOf(15), Double.valueOf(rectangle.getCenter().getX()));
        assertEquals(Double.valueOf(15), Double.valueOf(rectangle.getCenter().getY()));

        rectangle.rotate(90);

        assertEquals(Double.valueOf(10), Double.valueOf(rectangle.getPosition().getX()));
        assertEquals(Double.valueOf(10), Double.valueOf(rectangle.getPosition().getY()));
        assertEquals(Double.valueOf(15), Double.valueOf(rectangle.getCenter().getX()));
        assertEquals(Double.valueOf(15), Double.valueOf(rectangle.getCenter().getY()));
    }

    @Test
    public void getShapeShouldReturnATransformedShape() {
        Rectangle rectangle = new Rectangle(10, 10);
        rectangle.setWidth(10);
        rectangle.setHeight(10);

        AffineTransform transform = new AffineTransform();
        transform.translate(10, 10);
        rectangle.applyTransform(transform);

        assertEquals(20d, rectangle.getShape().getBounds().getX(), 0.01);
        assertEquals(20d, rectangle.getShape().getBounds().getY(), 0.01);
    }

    @Test
    public void getShapeShouldNotReturnATransformedShapeIfAddedAfterTransform() {
        AffineTransform transform = new AffineTransform();
        transform.translate(10, 10);
        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        rectangle.setTransform(transform);

        transform = new AffineTransform();
        transform.translate(10, 10);
        EntityGroup entityGroup = new EntityGroup();
        entityGroup.setTransform(transform);
        entityGroup.addChild(rectangle);

        assertEquals(10d, rectangle.getShape().getBounds().getX(), 0.01);
        assertEquals(10d, rectangle.getShape().getBounds().getY(), 0.01);
    }

    @Test
    public void setPositionShouldTranslateTheRectangle() {
        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        rectangle.move(new Point2D.Double(10, 0));

        assertEquals(rectangle.getPosition().getX(), 10, 0.1);
        assertEquals(rectangle.getPosition().getY(), 0, 0.1);
    }

    @Test
    public void rotateShouldRotateAroundRectangleCenter() {
        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        rectangle.move(new Point2D.Double(10, 10));

        // Rotate 360
        rectangle.rotate(-90);
        rectangle.rotate(-90);
        rectangle.rotate(-90);
        rectangle.rotate(-90);

        // Rotate additional 90 degrees
        rectangle.rotate(-90);

        assertEquals(15, rectangle.getCenter().getX(), 0.01);
        assertEquals(15, rectangle.getCenter().getY(), 0.01);
        assertEquals(-90, rectangle.getRotation(), 0.01);
    }

    @Test
    public void rotateAroundPointShouldRotateFromRectangleCenter() {
        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        rectangle.setCenter(new Point2D.Double(10, 0));

        assertEquals(5, rectangle.getPosition().getX(), 0.01);
        assertEquals(-5, rectangle.getPosition().getY(), 0.01);

        rectangle.rotate(new Point2D.Double(0,0), -90);

        assertEquals(-5, rectangle.getPosition().getX(), 0.01);
        assertEquals(-15, rectangle.getPosition().getY(), 0.01);
        assertEquals(-90, rectangle.getRotation(), 0.01);
    }
}
