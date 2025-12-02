package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.util.List;

public class MoveActionTest {

    @Test
    public void testMove() {
        Rectangle rectangle1 = new Rectangle();
        rectangle1.setPosition(new Point2D.Double(10, 10));

        Rectangle rectangle2 = new Rectangle();
        rectangle2.setPosition(new Point2D.Double(0, 0));

        MoveAction moveAction = new MoveAction(List.of(rectangle1, rectangle2), new Point2D.Double(10, 5));
        moveAction.redo();

        assertEquals(new Point2D.Double(20, 15), rectangle1.getPosition());
        assertEquals(new Point2D.Double(10, 5), rectangle2.getPosition());

        moveAction.undo();

        assertEquals(new Point2D.Double(10, 10), rectangle1.getPosition());
        assertEquals(new Point2D.Double(0, 0), rectangle2.getPosition());
    }
}