package com.willwinder.ugs.nbp.designer.entities;

import com.willwinder.ugs.nbp.designer.actions.SimpleUndoManager;
import com.willwinder.ugs.nbp.designer.entities.controls.MoveControl;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.gui.MouseEntityEvent;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;

@Ignore("Need to figure out how to run these tests on build server")
public class MoveControlTest {

    @Test
    public void moveShouldMoveWhenDragged() {
        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        rectangle.move(new Point2D.Double(10, 10));

        SelectionManager selectionManager = new SelectionManager();
        selectionManager.addSelection(rectangle);

        Controller controller = new Controller(selectionManager, new SimpleUndoManager());
        MoveControl target = new MoveControl(controller);
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_PRESSED, new Point2D.Double(10, 10), new Point2D.Double(10, 10)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(12, 12)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));

        Assert.assertEquals(11, rectangle.getPosition().getX(), 0.1);
        Assert.assertEquals(11, rectangle.getPosition().getY(), 0.1);
    }

    @Test
    public void moveShouldMoveWhenDraggedAndReleasedTarget() {
        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        rectangle.move(new Point2D.Double(10, 10));

        SelectionManager selectionManager = new SelectionManager();
        selectionManager.addSelection(rectangle);

        Controller controller = new Controller(selectionManager, new SimpleUndoManager());
        MoveControl target = new MoveControl(controller);
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_PRESSED, new Point2D.Double(10, 10), new Point2D.Double(10, 10)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(12, 12)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_RELEASED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));

        Assert.assertEquals(11, rectangle.getPosition().getX(), 0.1);
        Assert.assertEquals(11, rectangle.getPosition().getY(), 0.1);
    }

    @Test
    public void moveEntityShouldUseGlobalTransform() {
        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        rectangle.move(new Point2D.Double(10, 10));

        EntityGroup entityGroup = new EntityGroup();
        entityGroup.addChild(rectangle);
        entityGroup.move(new Point2D.Double(10, 10));

        SelectionManager selectionManager = new SelectionManager();
        selectionManager.addSelection(rectangle);

        Controller controller = new Controller(selectionManager, new SimpleUndoManager());
        MoveControl target = new MoveControl(controller);
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_PRESSED, new Point2D.Double(10, 10), new Point2D.Double(10, 10)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(12, 12)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_RELEASED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));

        Assert.assertEquals(21, rectangle.getPosition().getX(), 0.1);
        Assert.assertEquals(21, rectangle.getPosition().getY(), 0.1);
    }

    @Test
    public void moveEntityShouldWorkOnScaledEntities() {
        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        rectangle.applyTransform(AffineTransform.getScaleInstance(2, 2));

        SelectionManager selectionManager = new SelectionManager();
        selectionManager.addSelection(rectangle);

        Controller controller = new Controller(selectionManager, new SimpleUndoManager());
        MoveControl target = new MoveControl(controller);
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_PRESSED, new Point2D.Double(10, 10), new Point2D.Double(10, 10)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(12, 12)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_RELEASED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));

        Assert.assertEquals(1, rectangle.getPosition().getX(), 0.1);
        Assert.assertEquals(1, rectangle.getPosition().getY(), 0.1);
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

        Assert.assertEquals(0, selectionManager.getPosition().getX(), 0.1);
        Assert.assertEquals(0, selectionManager.getPosition().getY(), 0.1);

        Controller controller = new Controller(selectionManager, new SimpleUndoManager());
        MoveControl target = new MoveControl(controller);
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_PRESSED, new Point2D.Double(10, 10), new Point2D.Double(10, 10)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(12, 12)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(20, 20)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_RELEASED, new Point2D.Double(10, 10), new Point2D.Double(20, 20)));

        Assert.assertEquals(10, selectionManager.getPosition().getX(), 0.1);
        Assert.assertEquals(10, selectionManager.getPosition().getY(), 0.1);

        Assert.assertEquals(20, rectangle1.getPosition().getX(), 0.1);
        Assert.assertEquals(20, rectangle1.getPosition().getY(), 0.1);

        Assert.assertEquals(10, rectangle2.getPosition().getX(), 0.1);
        Assert.assertEquals(10, rectangle2.getPosition().getY(), 0.1);
    }

    @Test
    public void moveShouldMoveEntitiesWhenParentIsScaled() {
        EntityGroup entityGroup = new EntityGroup();
        entityGroup.applyTransform(AffineTransform.getScaleInstance(0.1, 0.1));

        Rectangle rectangle = new Rectangle();
        rectangle.setWidth(10);
        rectangle.setHeight(10);
        entityGroup.addChild(rectangle);

        SelectionManager selectionManager = new SelectionManager();
        selectionManager.addSelection(rectangle);

        Assert.assertEquals(0, selectionManager.getPosition().getX(), 0.1);
        Assert.assertEquals(0, selectionManager.getPosition().getY(), 0.1);

        Controller controller = new Controller(selectionManager, new SimpleUndoManager());
        MoveControl target = new MoveControl(controller);
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_PRESSED, new Point2D.Double(10, 10), new Point2D.Double(10, 10)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(11, 11)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(12, 12)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_DRAGGED, new Point2D.Double(10, 10), new Point2D.Double(20, 20)));
        target.onEvent(new MouseEntityEvent(target, EventType.MOUSE_RELEASED, new Point2D.Double(10, 10), new Point2D.Double(20, 20)));

        Assert.assertEquals(10, rectangle.getPosition().getX(), 0.1);
        Assert.assertEquals(10, rectangle.getPosition().getY(), 0.1);
    }
}