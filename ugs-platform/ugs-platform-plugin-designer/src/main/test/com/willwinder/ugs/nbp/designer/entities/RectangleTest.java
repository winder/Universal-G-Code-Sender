package com.willwinder.ugs.nbp.designer.entities;

import org.junit.Test;

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

        rectangle.setRotation(Math.PI * 4);

        assertEquals(Double.valueOf(15), Double.valueOf(rectangle.getCenter().getX()));
        assertEquals(Double.valueOf(15), Double.valueOf(rectangle.getCenter().getY()));
    }
}