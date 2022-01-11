package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;

public class FlipVerticallyAction extends AbstractAction implements SelectionListener {
    private static final String SMALL_ICON_PATH = "img/flip-vertical.svg";
    private static final String LARGE_ICON_PATH = "img/flip-vertical24.svg";
    private final transient Controller controller;

    public FlipVerticallyAction(Controller controller) {
        putValue("menuText", "Flip vertically");
        putValue(NAME, "Flip vertically");
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));

        this.controller = controller;

        SelectionManager selectionManager = controller.getSelectionManager();
        selectionManager.addSelectionListener(this);
        setEnabled(!selectionManager.getSelection().isEmpty());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SelectionManager selectionManager = controller.getSelectionManager();
        if (!selectionManager.getSelection().isEmpty()) {
            List<Entity> entities = selectionManager.getSelection();

            EntityGroup entityGroup = new EntityGroup();
            entityGroup.addAll(entities);
            UndoableFlipVerticallyAction undoableAction = new UndoableFlipVerticallyAction(controller.getDrawing(), entityGroup);
            controller.getUndoManager().addAction(undoableAction);
            undoableAction.execute();
        }
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        SelectionManager selectionManager = controller.getSelectionManager();
        setEnabled(!selectionManager.getSelection().isEmpty());
    }

    private static class UndoableFlipVerticallyAction implements UndoableAction, DrawAction {

        private final Entity entity;
        private final Drawing drawing;

        public UndoableFlipVerticallyAction(Drawing drawing, Entity entity) {
            this.drawing = drawing;
            this.entity = entity;
        }

        @Override
        public void redo() {
            Point2D position = entity.getPosition();
            entity.applyTransform(AffineTransform.getScaleInstance(1d, -1d));
            entity.setPosition(position);
            drawing.repaint();
        }

        @Override
        public void undo() {
            redo();
        }

        @Override
        public void execute() {
            redo();
        }

        @Override
        public String toString() {
            return "flip vertically";
        }
    }
}
