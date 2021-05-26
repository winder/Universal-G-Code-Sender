package com.willwinder.ugs.nbp.designer.gui.controls;

import com.willwinder.ugs.nbp.designer.gui.MouseEntityEvent;
import com.willwinder.ugs.nbp.designer.gui.entities.EventType;
import com.willwinder.ugs.nbp.designer.gui.entities.Group;
import com.willwinder.ugs.nbp.designer.gui.entities.Rectangle;
import com.willwinder.ugs.nbp.designer.logic.selection.SelectionManager;
import org.junit.Test;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

import static org.junit.Assert.*;

public class MoveControlTest {

    @Test
    public void moveShouldMoveWhenDragged() {
        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        rectangle.move(new Point2D.Double(10, 10));

        SelectionManager selectionManager = new SelectionManager();

        MoveControl target = new MoveControl(rectangle, selectionManager);
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_PRESSED, new Point2D.Double(10, 10), new Point2D.Double(10, 10)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(12, 12)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));

        assertEquals(11, rectangle.getPosition().getX(), 0.1);
        assertEquals(11, rectangle.getPosition().getY(), 0.1);
    }

    @Test
    public void moveShouldMoveWhenDraggedAndReleasedTarget() {
        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        rectangle.move(new Point2D.Double(10, 10));

        SelectionManager selectionManager = new SelectionManager();

        MoveControl target = new MoveControl(rectangle, selectionManager);
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_PRESSED, new Point2D.Double(10, 10), new Point2D.Double(10, 10)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(12, 12)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_RELEASED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));

        assertEquals(11, rectangle.getPosition().getX(), 0.1);
        assertEquals(11, rectangle.getPosition().getY(), 0.1);
    }

    @Test
    public void moveEntityShouldUseGlobalTransform() {
        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        rectangle.move(new Point2D.Double(10, 10));

        Group group = new Group();
        group.addChild(rectangle);
        group.move(new Point2D.Double(10, 10));

        SelectionManager selectionManager = new SelectionManager();

        MoveControl target = new MoveControl(rectangle, selectionManager);
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_PRESSED, new Point2D.Double(10, 10), new Point2D.Double(10, 10)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(12, 12)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_RELEASED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));

        assertEquals(21, rectangle.getPosition().getX(), 0.1);
        assertEquals(21, rectangle.getPosition().getY(), 0.1);
    }

    @Test
    public void moveEntityShouldWorkOnScaledEntities() {
        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        rectangle.applyTransform(AffineTransform.getScaleInstance(2, 2));

        SelectionManager selectionManager = new SelectionManager();

        MoveControl target = new MoveControl(rectangle, selectionManager);
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_PRESSED, new Point2D.Double(10, 10), new Point2D.Double(10, 10)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(12, 12)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_RELEASED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));

        assertEquals(1, rectangle.getPosition().getX(), 0.1);
        assertEquals(1, rectangle.getPosition().getY(), 0.1);
    }

    @Test
    public void moveShouldMoveMultipleSelectedEntitiesFromSelectionManager() {
        Rectangle rectangle1 = new Rectangle();
        rectangle1.setWidth(10);
        rectangle1.setHeight(10);
        rectangle1.move(new Point2D.Double(10, 10));

        Rectangle rectangle2 = new Rectangle();
        rectangle2.setWidth(10);
        rectangle2.setHeight(10);

        SelectionManager selectionManager = new SelectionManager();
        selectionManager.addSelection(rectangle1);
        selectionManager.addSelection(rectangle2);

        assertEquals(0, selectionManager.getPosition().getX(), 0.1);
        assertEquals(0, selectionManager.getPosition().getY(), 0.1);

        MoveControl target = new MoveControl(selectionManager, selectionManager);
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_PRESSED, new Point2D.Double(10, 10), new Point2D.Double(10, 10)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(12, 12)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_RELEASED, new Point2D.Double(10, 10), new Point2D.Double(20, 20)));

        assertEquals(10, selectionManager.getPosition().getX(), 0.1);
        assertEquals(10, selectionManager.getPosition().getY(), 0.1);

        assertEquals(20, rectangle1.getPosition().getX(), 0.1);
        assertEquals(20, rectangle1.getPosition().getY(), 0.1);

        assertEquals(10, rectangle2.getPosition().getX(), 0.1);
        assertEquals(10, rectangle2.getPosition().getY(), 0.1);
    }

    @Test
    public void moveShouldMoveEntitiesWhenParentIsScaled() {
        Group group = new Group();
        group.applyTransform(AffineTransform.getScaleInstance(0.1, 0.1));

        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        group.addChild(rectangle);

        SelectionManager selectionManager = new SelectionManager();
        selectionManager.addSelection(rectangle);

        assertEquals(0, selectionManager.getPosition().getX(), 0.1);
        assertEquals(0, selectionManager.getPosition().getY(), 0.1);

        MoveControl target = new MoveControl(selectionManager, selectionManager);
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_PRESSED, new Point2D.Double(10, 10), new Point2D.Double(10, 10)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(12, 12)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_RELEASED, new Point2D.Double(10, 10), new Point2D.Double(20, 20)));

        assertEquals(10, rectangle.getPosition().getX(), 0.1);
        assertEquals(10, rectangle.getPosition().getY(), 0.1);
    }
}