package com.willwinder.ugs.nbp.designer.entities.controls;

import com.willwinder.ugs.nbp.designer.entities.AbstractEntity;
import com.willwinder.ugs.nbp.designer.entities.EntityEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.logic.Controller;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.Shape;

public class GridControl extends AbstractEntity implements Control {

    private final Controller controller;

    public GridControl(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void render(Graphics2D graphics) {
        graphics.setStroke(new BasicStroke(0.1f));
        graphics.setColor(Color.GRAY);

        double gridSize = 10;
        double width = (int)controller.getSettings().getStockSize().getWidth();
        double height = (int)controller.getSettings().getStockSize().getHeight();

        for (double x = 0; x <= width; x += gridSize) {
            graphics.drawLine((int) Math.round(x), 0, (int) Math.round(x), (int) height);
        }

        for (double y = 0; y <= height; y += gridSize) {
            graphics.drawLine(0, (int) Math.round(y), (int) width, (int) Math.round(y));
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
}
