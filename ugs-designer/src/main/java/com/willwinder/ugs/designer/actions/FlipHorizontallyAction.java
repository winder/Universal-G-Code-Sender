package com.willwinder.ugs.designer.actions;

import com.willwinder.ugs.designer.entities.entities.Entity;
import com.willwinder.ugs.designer.entities.entities.EntityGroup;
import com.willwinder.ugs.designer.entities.entities.selection.SelectionEvent;
import com.willwinder.ugs.designer.entities.entities.selection.SelectionListener;
import com.willwinder.ugs.designer.entities.entities.selection.SelectionManager;
import com.willwinder.ugs.designer.gui.Drawing;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.universalgcodesender.utils.SvgIconLoader;

import java.awt.event.ActionEvent;
import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.util.List;

public class FlipHorizontallyAction extends AbstractDesignAction implements SelectionListener {
    public static final String SMALL_ICON_PATH = "img/flip-horizontal.svg";
    private final transient Controller controller;

    public FlipHorizontallyAction() {
        putValue("menuText", "Flip horizontally");
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, SvgIconLoader.loadImageIcon(SMALL_ICON_PATH, SvgIconLoader.SIZE_SMALL).orElse(null));
        putValue(LARGE_ICON_KEY, SvgIconLoader.loadImageIcon(SMALL_ICON_PATH, SvgIconLoader.SIZE_MEDIUM).orElse(null));

        putValue(NAME, "Flip horizontally");

        this.controller = ControllerFactory.getController();

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
            UndoableFlipHorizontallyAction undoableAction = new UndoableFlipHorizontallyAction(controller.getDrawing(), entityGroup);
            controller.getUndoManager().addAction(undoableAction);
            undoableAction.execute();
        }
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        SelectionManager selectionManager = controller.getSelectionManager();
        setEnabled(!selectionManager.getSelection().isEmpty());
    }

    private record UndoableFlipHorizontallyAction(Drawing drawing,
                                                  Entity entity) implements UndoableAction, DrawAction {

        @Override
            public void redo() {
                Point2D position = entity.getPosition();
                entity.applyTransform(AffineTransform.getScaleInstance(-1d, 1d));
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
                return "flip horizontally";
            }
        }
}
