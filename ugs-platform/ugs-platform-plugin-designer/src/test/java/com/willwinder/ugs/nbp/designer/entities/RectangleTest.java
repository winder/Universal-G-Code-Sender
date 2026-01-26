package com.willwinder.ugs.nbp.designer.entities;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.nbp.designer.model.Size;
import com.willwinder.ugs.nbp.designer.model.path.Segment;
import com.willwinder.ugs.nbp.designer.model.path.SegmentType;
import com.willwinder.ugs.nbp.designer.utils.PathUtils;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.awt.Shape;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;

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

        rectangle.setRotation(90);

        assertEquals(Double.valueOf(15), Double.valueOf(rectangle.getCenter().getX()));
        assertEquals(Double.valueOf(15), Double.valueOf(rectangle.getCenter().getY()));
    }

    @Test
    public void setCornerRadiusShouldRoundTheRectangle() {
        Rectangle rectangle = new Rectangle(0, 0);
        rectangle.setWidth(20);
        rectangle.setHeight(20);
        rectangle.setCornerRadius(5d);

        Shape shape = rectangle.getShape();
        List<Segment> segments = PathUtils.getSegments(shape);
        assertEquals(10, segments.size());
        assertSegment(0, 5, SegmentType.MOVE_TO, segments.get(0));
        assertSegment(0, 5, SegmentType.LINE_TO, segments.get(1));
        assertSegment(0, 15, SegmentType.CUBIC_TO, segments.get(2));
        assertSegment(5, 20, SegmentType.LINE_TO, segments.get(3));
        assertSegment(15, 20, SegmentType.CUBIC_TO, segments.get(4));
        assertSegment(20, 15, SegmentType.LINE_TO, segments.get(5));
        assertSegment(20, 5, SegmentType.CUBIC_TO, segments.get(6));
        assertSegment(15, 0, SegmentType.LINE_TO, segments.get(7));
        assertSegment(5, 0, SegmentType.CUBIC_TO, segments.get(8));
        assertSegment(0, 5, SegmentType.CLOSE, segments.get(9));
    }

    @Test
    public void scaleWithCornerRadiusShouldScaleRadiusToo() {
        Rectangle rectangle = new Rectangle(0, 0);
        rectangle.setWidth(20);
        rectangle.setHeight(20);
        rectangle.setCornerRadius(5d);

        rectangle.scale(0.5, 0.5);

        Shape shape = rectangle.getShape();
        List<Segment> segments = PathUtils.getSegments(shape);
        assertEquals(10, segments.size());
        assertSegment(0, 2.5d, SegmentType.MOVE_TO, segments.get(0));
        assertSegment(0, 2.5d, SegmentType.LINE_TO, segments.get(1));
        assertSegment(0, 7.5, SegmentType.CUBIC_TO, segments.get(2));
        assertSegment(2.5, 10, SegmentType.LINE_TO, segments.get(3));
        assertSegment(7.5, 10, SegmentType.CUBIC_TO, segments.get(4));
        assertSegment(10, 7.5, SegmentType.LINE_TO, segments.get(5));
        assertSegment(10, 2.5, SegmentType.CUBIC_TO, segments.get(6));
        assertSegment(7.5, 0, SegmentType.LINE_TO, segments.get(7));
        assertSegment(2.5, 0, SegmentType.CUBIC_TO, segments.get(8));
        assertSegment(0, 2.5, SegmentType.CLOSE, segments.get(9));
    }

    private static void assertSegment(double startX, double startY, SegmentType type, Segment segment) {
        assertEquals(type, segment.getType());
        assertEquals( new Point2D.Double(startX, startY), segment.getStartPoint());
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

        assertEquals(new Point2D.Double(20, 20), rectangle.getPosition());
        assertEquals(new Point2D.Double(25, 25), rectangle.getCenter());

        rectangle.move(new Point2D.Double(-5, -5));

        assertEquals(new Point2D.Double(15, 15), rectangle.getPosition());
        assertEquals(new Point2D.Double(20, 20), rectangle.getCenter());
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

        assertEquals(20d, rectangle.getShape().getBounds2D().getX(), 0.01);
        assertEquals(20d, rectangle.getShape().getBounds2D().getY(), 0.01);
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

        assertEquals(10d, rectangle.getShape().getBounds2D().getX(), 0.01);
        assertEquals(10d, rectangle.getShape().getBounds2D().getY(), 0.01);
    }

    @Test
    public void setPositionShouldTranslateTheRectangle() {
        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        rectangle.move(new Point2D.Double(10, 0));

        assertEquals(10, rectangle.getPosition().getX(), 0.1);
        assertEquals(0, rectangle.getPosition().getY(), 0.1);
    }

    @Test
    public void rotateShouldRotateAroundRectangleCenter() {
        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        rectangle.move(new Point2D.Double(10, 10));

        // Rotate 360
        rectangle.rotate(90);
        rectangle.rotate(90);
        rectangle.rotate(90);
        rectangle.rotate(90);

        // Rotate additional 90 degrees
        rectangle.rotate(90);

        assertEquals(15, rectangle.getCenter().getX(), 0.01);
        assertEquals(15, rectangle.getCenter().getY(), 0.01);
        assertEquals(90, rectangle.getRotation(), 0.01);
    }

    @Test
    public void rotateAroundPointShouldRotateFromRectangleCenter() {
        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        rectangle.setCenter(new Point2D.Double(10, 0));

        assertEquals(5, rectangle.getPosition().getX(), 0.01);
        assertEquals(-5, rectangle.getPosition().getY(), 0.01);

        rectangle.rotate(new Point2D.Double(0, 0), -90);

        assertEquals(-5, rectangle.getPosition().getX(), 0.01);
        assertEquals(5, rectangle.getPosition().getY(), 0.01);
        assertEquals(270, rectangle.getRotation(), 0.01);
    }

    @Test
    public void rotateAroundPointShouldRotateRectangle() {
        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(1);
        rectangle.setHeight(1);
        rectangle.setCenter(new Point2D.Double(10, 0));

        rectangle.rotate(new Point2D.Double(5, 0), 90);

        assertEquals(new Point2D.Double(4.5, -5.5), rectangle.getPosition());
        assertEquals(90, rectangle.getRotation(), 0.01);
    }

    @Test
    public void setPositionOnRotateRectangleShouldMoveToCorrectPosition() {
        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(200);
        rectangle.setHeight(50);
        rectangle.setRotation(270);
        rectangle.setPosition(new Point2D.Double(0.1, 0.1));

        assertEquals(0.1, rectangle.getPosition().getX(), 0.1);
        assertEquals(0.1, rectangle.getPosition().getY(), 0.1);
        assertEquals(270, rectangle.getRotation(), 0.01);
    }

    @Test
    public void scaleShouldChangeTheSize() {
        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(100);
        rectangle.setHeight(100);
        rectangle.setPosition(new Point2D.Double(1, 1));

        rectangle.scale(0.5, 0.5);
        assertEquals(50, rectangle.getSize().getWidth(), 0.1);
        assertEquals(50, rectangle.getSize().getHeight(), 0.1);
        assertEquals(1, rectangle.getPosition().getX(), 0.1);
        assertEquals(1, rectangle.getPosition().getY(), 0.1);
    }

    @Test
    public void scaleShouldChangeSizeWhenRotated() {
        Rectangle rectangle = new Rectangle();
        rectangle.setPosition(new Point2D.Double(1, 1));
        rectangle.setWidth(100);
        rectangle.setHeight(100);
        rectangle.setRotation(90);

        assertEquals(1, rectangle.getPosition().getY(), 0.1);
        assertEquals(1, rectangle.getPosition().getX(), 0.1);

        rectangle.scale(0.5, 0.5);
        assertEquals(50, rectangle.getSize().getWidth(), 0.1);
        assertEquals(50, rectangle.getSize().getHeight(), 0.1);
        assertEquals(1, rectangle.getPosition().getX(), 0.1);
        assertEquals(1, rectangle.getPosition().getY(), 0.1);
    }


    @Test
    public void setSizeWithAnotherWidthShouldOnlyScaleWidth() {
        Rectangle rectangle = new Rectangle();
        rectangle.setPosition(new Point2D.Double(1, 1));
        rectangle.setWidth(100);
        rectangle.setHeight(100);
        rectangle.setRotation(90);

        assertEquals(100, rectangle.getSize().getWidth(), 0.1);
        assertEquals(100, rectangle.getSize().getHeight(), 0.1);
        assertEquals(1, rectangle.getPosition().getX(), 0.1);
        assertEquals(1, rectangle.getPosition().getY(), 0.1);
        assertEquals(90, rectangle.getRotation(), 0.1);

        rectangle.setSize(new Size(200, 150));
        assertEquals(200, rectangle.getSize().getWidth(), 0.1);
        assertEquals(150, rectangle.getSize().getHeight(), 0.1);
    }

}
