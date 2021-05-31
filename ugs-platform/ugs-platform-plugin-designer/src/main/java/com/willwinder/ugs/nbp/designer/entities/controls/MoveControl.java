package com.willwinder.ugs.nbp.designer.entities.controls;

import com.willwinder.ugs.nbp.designer.gui.Colors;
import com.willwinder.ugs.nbp.designer.gui.MouseEntityEvent;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.logic.actions.MoveAction;
import com.willwinder.ugs.nbp.designer.logic.actions.UndoManager;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;

import java.awt.*;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;

public class MoveControl extends AbstractControl {
    private Point2D startOffset = new Point2D.Double();
    private Point2D startPositon = new Point2D.Double();

    public MoveControl(SelectionManager selectionManager) {
        super(selectionManager);
    }

    @Override
    public void setSize(Dimension s) {
    }

    @Override
    public boolean isWithin(Point2D point) {
        return getSelectionManager().isWithin(point);
    }

    @Override
    public void render(Graphics2D graphics) {
        graphics.setStroke(new BasicStroke(1f));
        graphics.setColor(Colors.CONTROL_BORDER);

        // Highlight the model
        graphics.draw(getShape());

        // Draw the bounds
        graphics.setStroke(new BasicStroke(1f, 0, 0, 1, new float[]{2, 2}, 0));
        graphics.draw(getSelectionManager().getTransform().createTransformedShape(getRelativeShape().getBounds()));
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        if (entityEvent instanceof MouseEntityEvent && entityEvent.getTarget() == this) {
            MouseEntityEvent mouseShapeEvent = (MouseEntityEvent) entityEvent;
            Point2D mousePosition = mouseShapeEvent.getCurrentMousePosition();

            Entity target = getSelectionManager();
            if (mouseShapeEvent.getType() == EventType.MOUSE_PRESSED) {
                startPositon = target.getPosition();
                startOffset = new Point2D.Double(mousePosition.getX() - target.getPosition().getX(), mousePosition.getY() - target.getPosition().getY());
            } else if (mouseShapeEvent.getType() == EventType.MOUSE_DRAGGED) {
                Point2D deltaMovement = new Point2D.Double(mousePosition.getX() - target.getPosition().getX() - startOffset.getX(), mousePosition.getY() - target.getPosition().getY() - startOffset.getY());
                target.move(deltaMovement);
            } else if (mouseShapeEvent.getType() == EventType.MOUSE_RELEASED) {
                Point2D deltaMovement = new Point2D.Double(mousePosition.getX() - target.getPosition().getX() - startOffset.getX(), mousePosition.getY() - target.getPosition().getY() - startOffset.getY());
                target.move(deltaMovement);
                Point2D deltaMovementTotal = new Point2D.Double(mousePosition.getX() - startPositon.getX() - startOffset.getX(), mousePosition.getY() - startPositon.getY() - startOffset.getY());
                addUndoAction(deltaMovementTotal, target);
            }
        }
    }

    private void addUndoAction(Point2D deltaMovement, Entity target) {
        UndoManager undoManager = CentralLookup.getDefault().lookup(UndoManager.class);
        if (undoManager != null) {
            List<Entity> entityList = new ArrayList<>();
            if (target instanceof SelectionManager) {
                entityList.addAll(((SelectionManager) target).getSelection());
            } else {
                entityList.add(target);
            }
            undoManager.addAction(new MoveAction(entityList, deltaMovement));
        }
    }
}
