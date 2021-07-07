package com.willwinder.ugs.nbp.designer.gui.entities;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import org.junit.Test;

import java.awt.geom.AffineTransform;
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
    public void movingChildrenShouldIgnoreParentLocation() {
        EntityGroup entityGroup = new EntityGroup();
        entityGroup.move(new Point2D.Double(10, 10));

        Rectangle rectangle = new Rectangle(10, 10);
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        entityGroup.addChild(rectangle);

        rectangle.move(new Point2D.Double(-5, -5));
        rectangle.move(new Point2D.Double(-5, -5));

        assertEquals(0, rectangle.getPosition().getX(), 0.1);
        assertEquals(0, rectangle.getPosition().getY(), 0.1);
    }

    @Test
    public void scalingGroupShouldScaleChildren() {
        EntityGroup entityGroup = new EntityGroup();
        Rectangle rectangle = new Rectangle(10, 10);
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        entityGroup.addChild(rectangle);

        entityGroup.applyTransform(AffineTransform.getScaleInstance(2, 2));

        assertEquals(10, entityGroup.getPosition().getX(), 0.1);
        assertEquals(10, entityGroup.getPosition().getY(), 0.1);
        assertEquals(20, entityGroup.getSize().getWidth(), 0.1);
        assertEquals(20, entityGroup.getSize().getHeight(), 0.1);


        assertEquals(10, rectangle.getPosition().getX(), 0.1);
        assertEquals(10, rectangle.getPosition().getX(), 0.1);
        assertEquals(20, rectangle.getSize().getWidth(), 0.1);
        assertEquals(20, rectangle.getSize().getHeight(), 0.1);
    }


    //@Test
    public void rotateShouldRotateChildrenAsWell() {
        EntityGroup entityGroup = new EntityGroup();
        entityGroup.move(new Point2D.Double(10, 0));

        Rectangle rectangle = new Rectangle(10, 0);
        rectangle.setWidth(0);
        rectangle.setHeight(0);
        entityGroup.addChild(rectangle);

        entityGroup.rotate(90);

        assertEquals(0, rectangle.getPosition().getX(), 0.1);
        assertEquals(10, rectangle.getPosition().getY(), 0.1);
        assertEquals(90, rectangle.getRotation(), 0.1);
    }
}
