package com.willwinder.ugs.nbp.designer.entities;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.nbp.designer.model.Size;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class EntityGroupTest {

    @Test
    public void getChildrenAtShouldReturnEntitiesWithinPoint() {
        EntityGroup entityGroup = new EntityGroup();

        Rectangle rectangle = new Rectangle(0, 0);
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        entityGroup.addChild(rectangle);

        entityGroup.move(new Point2D.Double(10, 10));

        List<Entity> childrenAt = entityGroup.getChildrenAt(new Point2D.Double(11, 11));
        assertEquals(1, childrenAt.size());
        assertEquals(rectangle, childrenAt.get(0));

        childrenAt = entityGroup.getChildrenAt(new Point2D.Double(9, 9));
        assertEquals(0, childrenAt.size());
    }

    @Test
    public void getPositionOfChildrenShouldReturnRealPosition() {
        EntityGroup entityGroup = new EntityGroup();

        Rectangle rectangle = new Rectangle(10, 10);
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        entityGroup.addChild(rectangle);

        entityGroup.move(new Point2D.Double(10, 10));

        assertEquals(20, rectangle.getPosition().getX(), 0.1);
        assertEquals(20, rectangle.getPosition().getX(), 0.1);
    }

    @Test
    public void moveShouldMoveChildren() {
        EntityGroup entityGroup = new EntityGroup();
        Rectangle rectangle = new Rectangle(10, 10);
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        entityGroup.addChild(rectangle);

        entityGroup.move(new Point2D.Double(-10, -10));

        assertEquals(0, rectangle.getPosition().getX(), 0.1);
        assertEquals(0, rectangle.getPosition().getX(), 0.1);
    }

    @Test
    public void positionShouldBeDeterminedByChildren() {
        EntityGroup entityGroup = new EntityGroup();
        entityGroup.move(new Point2D.Double(10, 10));

        assertEquals(0, entityGroup.getPosition().getX(), 0.1);
        assertEquals(0, entityGroup.getPosition().getX(), 0.1);
    }

    @Test
    public void movingChildrenShouldIgnoreParentLocation() {
        EntityGroup entityGroup = new EntityGroup();
        entityGroup.move(new Point2D.Double(10, 10));

        Rectangle rectangle = new Rectangle(100, 100);
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        entityGroup.addChild(rectangle);

        rectangle.move(new Point2D.Double(-5, -5));
        rectangle.move(new Point2D.Double(-5, -5));

        assertEquals(90, rectangle.getPosition().getX(), 0.1);
        assertEquals(90, rectangle.getPosition().getY(), 0.1);
        assertEquals(90, entityGroup.getPosition().getX(), 0.1);
        assertEquals(90, entityGroup.getPosition().getX(), 0.1);
    }

    @Test
    public void scalingGroupShouldScaleChild() {
        EntityGroup entityGroup = new EntityGroup();
        Rectangle rectangle = new Rectangle(10, 10);
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        entityGroup.addChild(rectangle);

        entityGroup.scale(2, 2);

        assertEquals(10, entityGroup.getPosition().getX(), 0.1);
        assertEquals(10, entityGroup.getPosition().getY(), 0.1);
        assertEquals(20, entityGroup.getSize().getWidth(), 0.1);
        assertEquals(20, entityGroup.getSize().getHeight(), 0.1);

        assertEquals(10, rectangle.getPosition().getX(), 0.1);
        assertEquals(10, rectangle.getPosition().getX(), 0.1);
        assertEquals(20, rectangle.getSize().getWidth(), 0.1);
        assertEquals(20, rectangle.getSize().getHeight(), 0.1);
    }

    @Test
    public void scalingGroupShouldScaleChildrenAndTheirRelativePosition() {
        EntityGroup entityGroup = new EntityGroup();
        Rectangle rectangle1 = new Rectangle(10, 10);
        rectangle1.setWidth(10);
        rectangle1.setHeight(10);
        entityGroup.addChild(rectangle1);

        Rectangle rectangle2 = new Rectangle(20, 20);
        rectangle2.setWidth(10);
        rectangle2.setHeight(10);
        entityGroup.addChild(rectangle2);

        entityGroup.scale(2, 2);

        assertEquals(10, rectangle1.getPosition().getX(), 0.1);
        assertEquals(10, rectangle1.getPosition().getX(), 0.1);
        assertEquals(20, rectangle1.getSize().getWidth(), 0.1);
        assertEquals(20, rectangle1.getSize().getHeight(), 0.1);

        assertEquals(30, rectangle2.getPosition().getX(), 0.1);
        assertEquals(30, rectangle2.getPosition().getX(), 0.1);
        assertEquals(20, rectangle2.getSize().getWidth(), 0.1);
        assertEquals(20, rectangle2.getSize().getHeight(), 0.1);
    }

    @Test
    public void setSizeShouldResizeChildrenRelativly() {
        EntityGroup entityGroup = new EntityGroup();
        Rectangle rectangle1 = new Rectangle(10, 10);
        rectangle1.setWidth(10);
        rectangle1.setHeight(10);
        entityGroup.addChild(rectangle1);

        Rectangle rectangle2 = new Rectangle(20, 20);
        rectangle2.setWidth(10);
        rectangle2.setHeight(10);
        entityGroup.addChild(rectangle2);

        assertEquals(20, entityGroup.getSize().getWidth(), 0.1);
        assertEquals(20, entityGroup.getSize().getHeight(), 0.1);

        entityGroup.setSize(new Size(40, 40));

        assertEquals(10, rectangle1.getPosition().getX(), 0.1);
        assertEquals(10, rectangle1.getPosition().getX(), 0.1);
        assertEquals(20, rectangle1.getSize().getWidth(), 0.1);
        assertEquals(20, rectangle1.getSize().getHeight(), 0.1);

        assertEquals(30, rectangle2.getPosition().getX(), 0.1);
        assertEquals(30, rectangle2.getPosition().getX(), 0.1);
        assertEquals(20, rectangle2.getSize().getWidth(), 0.1);
        assertEquals(20, rectangle2.getSize().getHeight(), 0.1);
    }

    @Test
    public void rotateShouldRotateChildAsWell() {
        EntityGroup entityGroup = new EntityGroup();

        Rectangle rectangle = new Rectangle(10, 0);
        rectangle.setWidth(1);
        rectangle.setHeight(1);
        entityGroup.addChild(rectangle);

        entityGroup.rotate(90);

        assertEquals(10, rectangle.getPosition().getX(), 0.1);
        assertEquals(0, rectangle.getPosition().getY(), 0.1);
        assertEquals(90, rectangle.getRotation(), 0.1);
    }

    @Test
    public void rotateShouldRotateChildrenAsWell() {
        EntityGroup entityGroup = new EntityGroup();

        Rectangle rectangle1 = new Rectangle(10, 0);
        rectangle1.setWidth(1);
        rectangle1.setHeight(1);
        entityGroup.addChild(rectangle1);

        Rectangle rectangle2 = new Rectangle(0, 0);
        rectangle2.setWidth(1);
        rectangle2.setHeight(1);
        entityGroup.addChild(rectangle2);

        entityGroup.rotate(90);

        assertEquals(5, rectangle1.getPosition().getX(), 0.1);
        assertEquals(-5, rectangle1.getPosition().getY(), 0.1);
        assertEquals(90, rectangle1.getRotation(), 0.1);

        assertEquals(5, rectangle2.getPosition().getX(), 0.1);
        assertEquals(5, rectangle2.getPosition().getY(), 0.1);
        assertEquals(90, rectangle2.getRotation(), 0.1);
        assertEquals(90, entityGroup.getRotation(), 0.1);
    }

    @Test
    public void setRotationShouldRotateChildrenAsWell() {
        EntityGroup entityGroup = new EntityGroup();

        Rectangle rectangle1 = new Rectangle(10, 0);
        rectangle1.setWidth(1);
        rectangle1.setHeight(1);
        entityGroup.addChild(rectangle1);

        Rectangle rectangle2 = new Rectangle(0, 0);
        rectangle2.setWidth(1);
        rectangle2.setHeight(1);
        entityGroup.addChild(rectangle2);

        entityGroup.setRotation(90);

        assertEquals(5, rectangle1.getPosition().getX(), 0.1);
        assertEquals(-5, rectangle1.getPosition().getY(), 0.1);
        assertEquals(90, rectangle1.getRotation(), 0.1);

        assertEquals(5, rectangle2.getPosition().getX(), 0.1);
        assertEquals(5, rectangle2.getPosition().getY(), 0.1);
        assertEquals(90, rectangle2.getRotation(), 0.1);
        assertEquals(90, entityGroup.getRotation(), 0.1);
    }
}
