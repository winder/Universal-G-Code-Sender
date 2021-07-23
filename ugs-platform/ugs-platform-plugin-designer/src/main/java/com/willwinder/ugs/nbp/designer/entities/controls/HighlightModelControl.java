package com.willwinder.ugs.nbp.designer.entities.controls;

import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.gui.Colors;
import com.willwinder.ugs.nbp.designer.model.Size;

import java.awt.*;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;

public class HighlightModelControl extends AbstractControl {
    protected HighlightModelControl(SelectionManager selectionManager) {
        super(selectionManager);
    }

    @Override
    public void render(Graphics2D graphics) {
        // Draw the bounds
        graphics.setColor(Colors.CONTROL_BORDER);
        graphics.setStroke(new BasicStroke(0.8f));
        Rectangle2D bounds = getRelativeShape().getBounds2D();
        bounds.setFrame(bounds.getX() - ResizeControl.MARGIN, bounds.getY() - ResizeControl.MARGIN, bounds.getWidth() + (ResizeControl.MARGIN * 2), bounds.getHeight() + (ResizeControl.MARGIN * 2));
        graphics.draw(getSelectionManager().getTransform().createTransformedShape(bounds));
    }

    @Override
    public boolean isWithin(Point2D point) {
        return false;
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {

    }
}
