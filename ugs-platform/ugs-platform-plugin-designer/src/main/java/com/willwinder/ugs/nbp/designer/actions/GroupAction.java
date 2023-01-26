package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntityGroup;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.gui.DrawingEvent;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

import java.awt.event.ActionEvent;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@ActionID(
        id = "com.willwinder.ugs.nbp.designer.actions.GroupAction",
        category = "Edit")
@ActionRegistration(
        iconBase = GroupAction.SMALL_ICON_PATH,
        displayName = "Group entities",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = "Shortcuts",
                name = "D-G")
})
public class GroupAction extends AbstractDesignAction implements SelectionListener {
    public static final String SMALL_ICON_PATH = "img/folder.svg";
    private static final String LARGE_ICON_PATH = "img/folder24.svg";

    public GroupAction() {
        putValue("menuText", "Group entities");
        putValue(NAME, "Group entities");
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));

        registerSelectionListener();
    }

    private void registerSelectionListener() {
        SelectionManager selectionManager = ControllerFactory.getController().getSelectionManager();
        selectionManager.addSelectionListener(this);
        setEnabled(!selectionManager.getChildren().isEmpty());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Controller controller = ControllerFactory.getController();
        UndoableGroupAction action = new UndoableGroupAction(controller.getSelectionManager().getSelection());
        controller.getUndoManager().addAction(action);
        action.redo();
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        SelectionManager selectionManager = ControllerFactory.getSelectionManager();
        setEnabled(!selectionManager.getSelection().isEmpty());
    }

    private static class UndoableGroupAction implements UndoableAction {
        private final List<Entity> entities;
        private Group group;

        public UndoableGroupAction(List<Entity> entities) {
            this.entities = entities;
        }

        @Override
        public void redo() {
            Controller controller = ControllerFactory.getController();
            Optional<EntityGroup> parent = controller.getDrawing().getRootEntity().findParentFor(entities.get(0));

            controller.getSelectionManager().clearSelection();
            controller.getDrawing().removeEntities(entities);

            group = new Group();
            group.addAll(entities);
            if (parent.isPresent()) {
                parent.get().addChild(group);
            } else {
                controller.getDrawing().insertEntity(group);
            }
            controller.getDrawing().notifyListeners(DrawingEvent.ENTITY_ADDED);
            controller.getSelectionManager().setSelection(Collections.singletonList(group));
        }

        @Override
        public void undo() {
            Controller controller = ControllerFactory.getController();
            Optional<EntityGroup> parent = controller.getDrawing().getRootEntity().findParentFor(group);
            controller.getSelectionManager().clearSelection();
            controller.getDrawing().removeEntity(group);

            if (parent.isPresent()) {
                parent.get().addAll(entities);
            } else {
                controller.getDrawing().insertEntities(entities);
            }
            controller.getDrawing().notifyListeners(DrawingEvent.ENTITY_ADDED);
            controller.getSelectionManager().setSelection(entities);
        }

        @Override
        public String toString() {
            return "group entities";
        }
    }
}
