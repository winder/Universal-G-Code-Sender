package com.willwinder.ugs.nbp.designer.gui.controls;

import com.willwinder.ugs.nbp.designer.gui.entities.Entity;
import com.willwinder.ugs.nbp.designer.gui.Colors;
import com.willwinder.ugs.nbp.designer.gui.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.gui.entities.EventType;
import com.willwinder.ugs.nbp.designer.gui.MouseEntityEvent;
import com.willwinder.ugs.nbp.designer.logic.selection.SelectionManager;

import java.awt.BasicStroke;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.geom.Point2D;
import java.util.logging.Logger;

public class MoveControl extends AbstractControl {
    private static final Logger LOGGER = Logger.getLogger(MoveControl.class.getSimpleName());
    private Point2D startOffset = new Point2D.Double();

    public MoveControl(Entity target, SelectionManager selectionManager) {
        super(target, selectionManager);
    }

    @Override
    public void setSize(Dimension s) {
    }

    @Override
    public boolean isWithin(Point2D point) {
        return getTarget().isWithin(point);
    }

    @Override
    public void render(Graphics2D g) {
        g.setStroke(new BasicStroke(1f));
        g.setColor(Colors.CONTROL_BORDER);

        // Highlight the model
        g.draw(getShape());

        // Draw the bounds
        g.setStroke(new BasicStroke(1f, 0, 0, 1, new float[]{2, 2}, 0));
        g.draw(getTarget().getGlobalTransform().createTransformedShape(getRelativeShape().getBounds()));
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        if (entityEvent instanceof MouseEntityEvent && entityEvent.getTarget() == this) {
            MouseEntityEvent mouseShapeEvent = (MouseEntityEvent) entityEvent;
            Point2D mousePosition = mouseShapeEvent.getCurrentMousePosition();

            Entity target = getTarget();
            Point2D deltaMovement = new Point2D.Double(mousePosition.getX() - target.getPosition().getX() - startOffset.getX(), mousePosition.getY() - target.getPosition().getY() - startOffset.getY());

            if (mouseShapeEvent.getType() == EventType.MOUSE_PRESSED) {
                startOffset = new Point2D.Double(mousePosition.getX() - target.getPosition().getX(), mousePosition.getY() - target.getPosition().getY());
            } else if (mouseShapeEvent.getType() == EventType.MOUSE_DRAGGED) {
                target.move(deltaMovement);
            } else if (mouseShapeEvent.getType() == EventType.MOUSE_RELEASED) {
                LOGGER.info("Stopped moving " + target.getPosition());
            }

        }
    }
}
