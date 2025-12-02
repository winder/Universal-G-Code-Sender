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
package com.willwinder.ugs.nbp.designer.gui.selectionsettings.models;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Text;

/**
 * Factory for creating appropriate settings models based on entity types in selection.
 * Uses the most specific model that can handle all selected entities.
 */
public class SettingsModelFactory {

    /**
     * Creates the most appropriate settings model for the given selection.
     * Returns the most specific model that can handle all entities in the selection.
     */
    public static TransformSettingsModel createModelForSelection(Group selectionGroup) {

        TransformSettingsModel model = selectionGroup.getAllChildren().stream()
                .map(SettingsModelFactory::getModelTypeForEntity)
                .reduce(SettingsModelFactory::getMoreSpecificModel)
                .orElse(new TransformSettingsModel());

        model.updateFromGroup(selectionGroup);
        return model;
    }

    private static TransformSettingsModel getMoreSpecificModel(TransformSettingsModel model1, TransformSettingsModel model2) {
        return model1 instanceof TextSettingsModel || model2 instanceof TextSettingsModel
                ? new TextSettingsModel()
                : model1 instanceof CuttableSettingsModel || model2 instanceof CuttableSettingsModel
                ? new CuttableSettingsModel()
                : new TransformSettingsModel();
    }


    /**
     * Gets the appropriate model type for a specific entity
     */
    public static TransformSettingsModel getModelTypeForEntity(Entity entity) {
        if (entity instanceof Text) {
            return new TextSettingsModel();
        } else if (entity instanceof Cuttable) {
            return new CuttableSettingsModel();
        } else {
            return new TransformSettingsModel();
        }
    }
}
