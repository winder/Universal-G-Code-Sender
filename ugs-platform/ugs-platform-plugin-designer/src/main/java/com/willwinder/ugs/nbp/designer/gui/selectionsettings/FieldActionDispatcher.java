package com.willwinder.ugs.nbp.designer.gui.selectionsettings;

import com.willwinder.ugs.nbp.designer.actions.ChangeEntitySettingsAction;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import com.willwinder.ugs.nbp.designer.entities.EntitySettingsManager;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.logic.Controller;

import java.util.List;
import java.util.logging.Logger;

/**
 * This class is responsible for listening to field events to create and execute undoable actions.
 * Every change to a textfield will update the entity using this dispatcher.
 * Now uses the new EntitySettingsManager for improved maintainability and proper undo support.
 *
 * @author Joacim Breiler
 */
public class FieldActionDispatcher implements FieldEventListener {
    private static final Logger LOGGER = Logger.getLogger(FieldActionDispatcher.class.getSimpleName());

    private final Controller controller;
    private final SelectionSettingsModel model;

    public FieldActionDispatcher(SelectionSettingsModel model, Controller controller) {
        this.model = model;
        this.controller = controller;
    }

    @Override
    public void onFieldUpdate(EntitySetting entitySetting, Object object) {
        if (model.get(entitySetting).equals(object)) {
            return;
        }

        SelectionManager selectionManager = controller.getSelectionManager();
        Group selection = selectionManager.getSelectionGroup();
        if (selection.getChildren().isEmpty()) {
            return;
        }

        List<Entity> entities = selection.getChildren();

        // Check if the setting is applicable to the selected entities
        List<EntitySetting> applicableSettings = EntitySettingsManager.getApplicableSettings(entities);
        if (!applicableSettings.contains(entitySetting)) {
            LOGGER.warning(() -> "Setting " + entitySetting + " is not applicable to the selected entities");
            return;
        }

        // Create and execute the undoable action
        ChangeEntitySettingsAction action = new ChangeEntitySettingsAction(entities, entitySetting, object);
        action.redo();
        controller.getUndoManager().addAction(action);

        LOGGER.fine(() -> "Applied setting " + entitySetting + " with value " + object + " to " + entities.size() + " entities");
    }
}
