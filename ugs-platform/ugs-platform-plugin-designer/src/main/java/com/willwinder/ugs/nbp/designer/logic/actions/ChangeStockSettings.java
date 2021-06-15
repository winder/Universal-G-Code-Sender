package com.willwinder.ugs.nbp.designer.logic.actions;

import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.Size;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ChangeStockSettings extends AbstractAction implements UndoableAction {

    private final Size previousSize;
    private final Size newSize;
    private final double previousThickness;
    private final double newThickness;
    private final Controller controller;

    public ChangeStockSettings(Controller controller, double width, double height, double newThickness) {
        this.newSize = new Size(width, height);
        this.newThickness = newThickness;
        this.previousSize = controller.getSettings().getStockSize();
        this.previousThickness = controller.getSettings().getStockThickness();
        this.controller = controller;
        putValue("menuText", "Change stock settings");
        putValue(NAME, "Change stock settings");
    }
    @Override
    public void redo() {
        actionPerformed(null);
    }

    @Override
    public void undo() {
        this.controller.getSettings().setStockThickness(previousThickness);
        this.controller.getSettings().setStockSize(previousSize);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.controller.getSettings().setStockThickness(newThickness);
        this.controller.getSettings().setStockSize(newSize);
        this.controller.getDrawing().repaint();
    }
}
