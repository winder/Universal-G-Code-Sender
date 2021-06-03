package com.willwinder.ugs.nbp.designer.logic.actions;


import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.logic.Controller;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * AddAction implements a single undoable action where an entity is added to a
 * Drawing.
 */
public class AddAction extends AbstractAction implements DrawAction, UndoableAction {

    private final transient Controller controller;
    private final transient Entity entity;

    /**
     * Creates an AddAction that adds the given Entity to the given Drawing.
     *
     * @param controller the controller for the drawing.
     * @param entity   the shape to be added.
     */
    public AddAction(Controller controller, Entity entity) {
        this.controller = controller;
        this.entity = entity;
    }

    public void execute() {
        controller.getDrawing().insertEntity(entity);
    }

    public void redo() {
        this.execute();
    }

    public void undo() {
        controller.getDrawing().removeEntity(entity);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        controller.getUndoManager().addAction(this);
        execute();
    }

    @Override
    public String toString() {
        return "add entity";
    }
}
