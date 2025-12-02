/*
    Copyright 2024-2025 Will Winder

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

import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.settings.EntitySettingsManager;
import com.willwinder.ugs.nbp.designer.gui.selectionsettings.models.EntitySettingsModelListener;
import com.willwinder.ugs.nbp.designer.gui.selectionsettings.models.SettingsModelFactory;
import com.willwinder.ugs.nbp.designer.gui.selectionsettings.models.TransformSettingsModel;
import org.openide.util.Lookup;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/**
 * Layered field action dispatcher that uses the new layered model architecture.
 * Replaces the old SelectionSettingsModel-based FieldActionDispatcher.
 *
 * @author Joacim Breiler
 */
public class LayeredFieldActionDispatcher implements EntitySettingsModelListener {
    private static final Logger LOGGER = Logger.getLogger(LayeredFieldActionDispatcher.class.getSimpleName());

    private TransformSettingsModel currentModel;
    private final List<EntitySettingsManager> settingsManagers = new ArrayList<>();

    public LayeredFieldActionDispatcher() {
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
