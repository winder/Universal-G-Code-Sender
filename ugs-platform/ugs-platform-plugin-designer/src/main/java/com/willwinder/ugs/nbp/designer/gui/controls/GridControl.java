package com.willwinder.ugs.nbp.designer.gui.controls;

import com.willwinder.ugs.nbp.designer.gui.entities.AbstractEntity;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.gui.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.logic.selection.SelectionManager;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

public class GridControl extends AbstractEntity implements Control {

    private final Drawing drawing;

    public GridControl(Drawing drawing) {
        this.drawing = drawing;
    }

    @Override
    public void render(Graphics2D graphics) {
        graphics.setStroke(new BasicStroke(0.1f));
        graphics.setColor(Color.GRAY);

        double gridSize = drawing.getScale() * 10;
        for (double x = 0; x < drawing.getWidth(); x += gridSize) {
            graphics.drawLine((int) Math.round(x), 0, (int) Math.round(x), drawing.getHeight());
        }

        for (double y = 0; y < drawing.getHeight(); y += gridSize) {
            graphics.drawLine(0, (int) Math.round(y), drawing.getWidth(), (int) Math.round(y));
        }
    }

    @Override
    public void setSize(Dimension s) {

    }

    @Override
    public Shape getShape() {
        return new Rectangle();
    }

    @Override
    public Shape getRelativeShape() {
        return new Rectangle();
    }

    @Override
    public SelectionManager getSelectionManager() {
        return null;
    }

    @Override
    public void onEvent(EntityEvent entityEvent) {

    }

    @Override
    public AbstractEntity getTarget() {
        return null;
    }
}
