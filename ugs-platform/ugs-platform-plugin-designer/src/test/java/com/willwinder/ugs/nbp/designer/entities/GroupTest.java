package com.willwinder.ugs.nbp.designer.entities;

import org.junit.Test;

import java.awt.geom.Point2D;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class GroupTest {

    @Test
    public void getChildrenAtShouldReturnEntitesWithinPoint() {
        Group group = new Group();
        group.setPosition(10, 10);

        Rectangle rectangle = new Rectangle(0, 0);
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        group.addChild(rectangle);

        List<Entity> childrenAt = group.getChildrenAt(new Point2D.Double(11, 11));
        assertEquals(1, childrenAt.size());
        assertEquals(rectangle, childrenAt.get(0));

        childrenAt = group.getChildrenAt(new Point2D.Double(9, 9));
        assertEquals(0, childrenAt.size());
    }
}
