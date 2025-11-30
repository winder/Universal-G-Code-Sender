/*
    Copyright 2024 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.nbp.designer.gui.selectionsettings;

import com.willwinder.ugs.nbp.designer.actions.ChangeEntitySettingsAction;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.entities.settings.EntitySettingsManager;
import com.willwinder.ugs.nbp.designer.gui.selectionsettings.models.EntitySettingsModelListener;
import com.willwinder.ugs.nbp.designer.gui.selectionsettings.models.SettingsModelFactory;
import com.willwinder.ugs.nbp.designer.gui.selectionsettings.models.TransformSettingsModel;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import org.openide.util.Lookup;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Logger;

/**
 * Layered field action dispatcher that uses the new layered model architecture.
 * Replaces the old SelectionSettingsModel-based FieldActionDispatcher.
 *
 * @author Joacim Breiler
 */
public class LayeredFieldActionDispatcher implements FieldEventListener, EntitySettingsModelListener {
    private static final Logger LOGGER = Logger.getLogger(LayeredFieldActionDispatcher.class.getSimpleName());

    private final Controller controller;
    private TransformSettingsModel currentModel;
    private final List<EntitySettingsManager> settingsManagers = new ArrayList<>();

    public LayeredFieldActionDispatcher(Controller controller) {
        this.controller = controller;
        this.settingsManagers.addAll(Lookup.getDefault().lookupAll(EntitySettingsManager.class));
    }

    /**
     * Updates the current model based on the selection
     */
    public void updateModel(Group selectionGroup) {
        // Remove listener from old model
        if (currentModel != null) {
            currentModel.removeListener(this);
        }

        // Create new model for current selection
        currentModel = SettingsModelFactory.createModelForSelection(selectionGroup);

        // Add listener to new model
        currentModel.addListener(this);
    }

    public TransformSettingsModel getCurrentModel() {
        return currentModel;
    }

    @Override
    public void onFieldUpdate(EntitySetting entitySetting, Object newValue) {
        if (currentModel == null) {
            return;
        }

        // Check if the value actually changed
        try {
            Object currentValue = getCurrentValueFromModel(entitySetting);
            if (currentValue != null && currentValue.equals(newValue)) {
                return;
            }
        } catch (Exception e) {
            LOGGER.warning("Could not get current value for setting " + entitySetting + ": " + e.getMessage());
        }

        SelectionManager selectionManager = controller.getSelectionManager();
        Group selection = selectionManager.getSelectionGroup();
        if (selection.getChildren().isEmpty()) {
            return;
        }

        List<Entity> entities = selection.getChildren();

        // Find appropriate settings manager
        Optional<EntitySettingsManager> manager = settingsManagers.stream()
                .filter(settingsManager -> settingsManager.canHandle(entities))
                .findFirst();

        if (manager.isEmpty()) {
            LOGGER.warning(() -> "Setting " + entitySetting + " is not applicable to the selected entities");
            return;
        }

        // Create and execute undoable action
        ChangeEntitySettingsAction action = new ChangeEntitySettingsAction(entities, entitySetting, newValue, manager.get());
        action.redo();
        controller.getUndoManager().addAction(action);

        // Update the model to reflect the change
        updateModelValue(entitySetting, newValue);

        LOGGER.fine(() -> "Applied setting " + entitySetting + " with value " + newValue + " to " + entities.size() + " entities");
    }

    @Override
    public void onModelUpdate(EntitySetting setting) {
        // Model was updated externally, refresh the UI if needed
        // This allows for model-driven UI updates
        LOGGER.fine(() -> "Model updated for setting: " + setting);
    }

    private Object getCurrentValueFromModel(EntitySetting setting) {
        if (currentModel == null) return null;
        return currentModel.getValueFor(setting);
    }

    private void updateModelValue(EntitySetting setting, Object newValue) {
        if (currentModel == null) return;
        currentModel.updateValueFor(setting, newValue);

    }

    public void release() {
        if (currentModel != null) {
            currentModel.removeListener(this);
            currentModel = null;
        }
    }
}
