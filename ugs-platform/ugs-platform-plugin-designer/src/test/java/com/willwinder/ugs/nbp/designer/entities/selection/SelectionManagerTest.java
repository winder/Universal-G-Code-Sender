package com.willwinder.ugs.nbp.designer.entities.selection;

import com.willwinder.ugs.nbp.designer.actions.SimpleUndoManager;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.entities.controls.MoveControl;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.model.Size;
import org.junit.Before;
import org.junit.Test;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.Collections;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

public class SelectionManagerTest {

    private SelectionManager target = new SelectionManager();

    @Before
    public void setUp() {

    }

    @Test
    public void moveShouldMoveAllEntites() {
        Rectangle firstEntity = new Rectangle();
        firstEntity.setPosition(new Point2D.Double(10, 20));
        firstEntity.setSize(new Size(10, 10));
        target.addSelection(firstEntity);

        Rectangle secondEntity = new Rectangle();
        secondEntity.setPosition(new Point2D.Double(20, 30));
        secondEntity.setSize(new Size(40, 40));
        target.addSelection(secondEntity);


        target.move(new Point2D.Double(10, 10));

        assertEquals(50, target.getSize().getWidth(), 0.1);
        assertEquals(50, target.getSize().getHeight(), 0.1);
        assertEquals(new Point2D.Double(20, 30), target.getPosition());
        assertEquals(new Point2D.Double(45, 55), target.getCenter());
        assertEquals(0, target.getRotation(), 0.1);

        assertEquals(new Point2D.Double(20, 30), firstEntity.getPosition());
        assertEquals(new Point2D.Double(30, 40), secondEntity.getPosition());
    }

    @Test
    public void setRotationShouldRotateAnEntityAroundItsCenter() {
        Rectangle rectangle = new Rectangle();
        rectangle.setPosition(new Point2D.Double(10, 20));
        rectangle.setSize(new Size(10, 10));
        target.addSelection(rectangle);
        assertEquals(new Point2D.Double(15, 25), target.getCenter());

        target.setRotation(90);

        assertEquals(new Point2D.Double(15, 25), target.getCenter());
        assertEquals(new Point2D.Double(10, 20), target.getPosition());
        assertEquals(90, target.getRotation(), 0.1);

        assertEquals(10, rectangle.getPosition().getX(), 0.1);
        assertEquals(20, rectangle.getPosition().getY(), 0.1);
        assertEquals(90, rectangle.getRotation(), 0.1);
    }

    @Test
    public void setRotationShouldRotateChildrenAsWell() {
        Rectangle rectangle1 = new Rectangle(10, 0);
        rectangle1.setWidth(1);
        rectangle1.setHeight(1);
        target.addSelection(rectangle1);

        Rectangle rectangle2 = new Rectangle(0, 0);
        rectangle2.setWidth(1);
        rectangle2.setHeight(1);
        target.addSelection(rectangle2);

        target.setRotation(90);

        assertEquals(90, rectangle1.getRotation(), 0.001);
        assertEquals(90, rectangle2.getRotation(), 0.001);
        assertEquals(new Point2D.Double(5, -5), rectangle1.getPosition());
        assertEquals(new Point2D.Double(5, 5), rectangle2.getPosition());
    }

    @Test
    public void addSelectionWithControlShouldNotBePossible() {
        Controller controller = mock(Controller.class);
        MoveControl control = new MoveControl(controller);

        target.addSelection(control);

        assertTrue(target.isEmpty());
    }

    @Test
    public void setSelectionWithControlShouldNotBePossible() {
        Controller controller = mock(Controller.class);
        MoveControl control = new MoveControl(controller);

        target.setSelection(Collections.singletonList(control));

        assertTrue(target.isEmpty());
    }
}
