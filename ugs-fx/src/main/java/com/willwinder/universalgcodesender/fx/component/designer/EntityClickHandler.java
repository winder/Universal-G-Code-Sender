package com.willwinder.universalgcodesender.fx.component.designer;

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.EventType;
import com.willwinder.ugs.designer.entities.controls.MoveControl;
import com.willwinder.ugs.designer.gui.MouseEntityEvent;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.universalgcodesender.fx.component.visualizer.DragHandler;
import com.willwinder.universalgcodesender.fx.component.visualizer.PickHandler;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * Combined pick + drag handler attached to an entity's scene node. Single click
 * selects the entity (or toggles when multi-selecting); a primary-button drag
 * starts a move via {@link MoveControl}.
 */
public class EntityClickHandler implements PickHandler, DragHandler {
    private final Controller controller;
    private final Entity entity;
    private final Runnable onMoved;

    public EntityClickHandler(Controller controller, Entity entity, Runnable onMoved) {
        this.controller = controller;
        this.entity = entity;
        this.onMoved = onMoved;
    }

    @Override
    public void onPicked(boolean multiSelect) {
        if (multiSelect) {
            controller.getSelectionManager().toggleSelection(entity);
        } else {
            controller.getSelectionManager().setSelection(List.of(entity));
        }
    }

    @Override
    public void onDragStart(double x, double y) {
        forwardToMoveControl(EventType.MOUSE_PRESSED, x, y, x, y);
    }

    @Override
    public void onDrag(double sx, double sy, double cx, double cy) {
        forwardToMoveControl(EventType.MOUSE_DRAGGED, sx, sy, cx, cy);
        onMoved.run();
    }

    @Override
    public void onDragEnd(double sx, double sy, double cx, double cy) {
        forwardToMoveControl(EventType.MOUSE_RELEASED, sx, sy, cx, cy);
        onMoved.run();
    }

    private void forwardToMoveControl(EventType type, double sx, double sy, double cx, double cy) {
        controller.getDrawing().getControls().stream()
                .filter(MoveControl.class::isInstance)
                .findFirst()
                .ifPresent(control -> control.onEvent(new MouseEntityEvent(control, type,
                        new Point2D.Double(sx, sy), new Point2D.Double(cx, cy))));
    }
}
