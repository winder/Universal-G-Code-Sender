package com.willwinder.ugs.nbp.designer.entities.controls;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Text;
import com.willwinder.ugs.nbp.designer.gui.Colors;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.logic.Controller;

import java.awt.*;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.util.List;

public class EditTextControl extends AbstractControl {

    private final Controller controller;
    private long previousTime = 0;

    public EditTextControl(Controller controller) {
        super(controller.getSelectionManager());
        this.controller = controller;
        this.previousTime = System.currentTimeMillis();
    }

    @Override
    public boolean isWithin(Point2D point) {
        return isTextEntitySelected();
    }

    private boolean isTextEntitySelected() {
        boolean isTextEntity = false;
        List<Entity> selection = controller.getSelectionManager().getSelection();
        if (!selection.isEmpty()) {
            Entity entity = selection.get(0);
            isTextEntity = entity instanceof Text;
        }
        return isTextEntity;
    }

    @Override
    public void render(Graphics2D graphics, Drawing drawing) {
        if (!isTextEntitySelected()) {
            return;
        }

        if (previousTime + 600 < System.currentTimeMillis()) {
            graphics.setColor(Colors.CURSOR);
            graphics.setStroke(new BasicStroke(Double.valueOf(1 / drawing.getScale()).floatValue()));
            Rectangle2D bounds = getRelativeShape().getBounds2D();
            Line2D line = new Line2D.Double(bounds.getX() + bounds.getWidth(), bounds.getY(), bounds.getX() + bounds.getWidth(), bounds.getY() + bounds.getHeight());
            graphics.draw(getSelectionManager().getTransform().createTransformedShape(line));

        }

        if (previousTime + 1200 < System.currentTimeMillis()) {
            previousTime = System.currentTimeMillis();
        }
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {

    }
}
