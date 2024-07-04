package com.willwinder.ugs.nbp.designer.entities.controls;

import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.gui.Colors;
import com.willwinder.ugs.nbp.designer.gui.Drawing;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class HighlightModelControl extends AbstractControl {
    public HighlightModelControl(SelectionManager selectionManager) {
        super(selectionManager);
    }

    @Override
    public void render(Graphics2D graphics, Drawing drawing) {
        if (getSelectionManager().getSelection().isEmpty()) {
            return;
        }

        // Draw the bounds
        graphics.setColor(Colors.CONTROL_BORDER);
        graphics.setStroke(new BasicStroke((float) (0.8f / drawing.getScale())));
        Rectangle2D bounds = getRelativeShape().getBounds2D();
        bounds.setFrame(bounds.getX() , bounds.getY() , bounds.getWidth(), bounds.getHeight());
        graphics.draw(getSelectionManager().getTransform().createTransformedShape(bounds));
    }

    @Override
    public boolean isWithin(Point2D point) {
        return false;
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {
        // Not applicable
    }
}
