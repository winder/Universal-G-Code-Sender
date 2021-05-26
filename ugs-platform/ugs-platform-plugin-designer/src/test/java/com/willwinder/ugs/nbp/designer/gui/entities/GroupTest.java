package com.willwinder.ugs.nbp.designer.gui.entities;

import org.junit.Test;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GroupTest {

    @Test
    public void getChildrenAtShouldReturnEntitiesWithinPoint() {
        Group group = new Group();

        Rectangle rectangle = new Rectangle(0, 0);
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        group.addChild(rectangle);

        group.move(new Point2D.Double(10, 10));

        List<Entity> childrenAt = group.getChildrenAt(new Point2D.Double(11, 11));
        assertEquals(1, childrenAt.size());
        assertEquals(rectangle, childrenAt.get(0));

        childrenAt = group.getChildrenAt(new Point2D.Double(9, 9));
        assertEquals(0, childrenAt.size());
    }

    @Test
    public void getPositionOfChildrenShouldReturnRealPosition() {
        Group group = new Group();

        Rectangle rectangle = new Rectangle(10, 10);
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        group.addChild(rectangle);

        group.move(new Point2D.Double(10, 10));

        assertEquals(20, rectangle.getPosition().getX(), 0.1);
        assertEquals(20, rectangle.getPosition().getX(), 0.1);
    }

    @Test
    public void moveShouldMoveChildren() {
        Group group = new Group();
        Rectangle rectangle = new Rectangle(10, 10);
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        group.addChild(rectangle);

        group.move(new Point2D.Double(-10, -10));

        assertEquals(0, rectangle.getPosition().getX(), 0.1);
        assertEquals(0, rectangle.getPosition().getX(), 0.1);
    }


    @Test
    public void movingChildrenShouldIgnoreParentLocation() {
        Group group = new Group();
        group.move(new Point2D.Double(10, 10));

        Rectangle rectangle = new Rectangle(10, 10);
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        group.addChild(rectangle);

        rectangle.move(new Point2D.Double(-5, -5));
        rectangle.move(new Point2D.Double(-5, -5));

        assertEquals(0, rectangle.getPosition().getX(), 0.1);
        assertEquals(0, rectangle.getPosition().getX(), 0.1);
    }

    @Test
    public void scalingGroupShouldScaleChildren() {
        Group group = new Group();
        Rectangle rectangle = new Rectangle(10, 10);
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        group.addChild(rectangle);

        group.applyTransform(AffineTransform.getScaleInstance(2, 2));

        assertEquals(10, group.getPosition().getX(), 0.1);
        assertEquals(10, group.getPosition().getY(), 0.1);
        assertEquals(20, group.getSize().getWidth(), 0.1);
        assertEquals(20, group.getSize().getHeight(), 0.1);


        assertEquals(10, rectangle.getPosition().getX(), 0.1);
        assertEquals(10, rectangle.getPosition().getX(), 0.1);
        assertEquals(20, rectangle.getSize().getWidth(), 0.1);
        assertEquals(20, rectangle.getSize().getHeight(), 0.1);
    }


    //@Test
    public void rotateShouldRotateChildrenAsWell() {
        Group group = new Group();
        group.move(new Point2D.Double(10, 0));

        Rectangle rectangle = new Rectangle(10, 0);
        rectangle.setWidth(0);
        rectangle.setHeight(0);
        group.addChild(rectangle);

        group.rotate(90);

        assertEquals(0, rectangle.getPosition().getX(), 0.1);
        assertEquals(10, rectangle.getPosition().getY(), 0.1);
        assertEquals(90, rectangle.getRotation(), 0.1);
    }
}
