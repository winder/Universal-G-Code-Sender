/*
    Copyright 2025 Albert Giró Quer

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
package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import com.willwinder.ugs.nbp.designer.entities.settings.EntitySettingsManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * An undoable action for changing entity settings through the settings panel.
 * This action captures the previous values and can restore them on undo.
 * Uses the service-based EntitySettingsServiceManager for improved extensibility.
 *
 * @author giro-dev
 */
public class ChangeEntitySettingsAction implements UndoableAction {
    private final List<Entity> entities;
    private final EntitySetting setting;
    private final Object newValue;
    private final Map<Entity, Object> previousValues;
    private final EntitySettingsManager settingsManager;

    public ChangeEntitySettingsAction(List<Entity> entities, EntitySetting setting, Object newValue, EntitySettingsManager entitySettingsManager) {
        this.entities = entities;
        this.setting = setting;
        this.newValue = newValue;
        this.previousValues = new HashMap<>();
        this.settingsManager = entitySettingsManager;
        // Store the current values for all entities
        for (Entity entity : entities) {
            Object currentValue = settingsManager.getSettingValue(setting, entity);
            previousValues.put(entity, currentValue);
        }
    }

    @Override
    public void redo() {
        settingsManager.applySetting(setting, newValue, entities);
    }

    @Override
    public void undo() {
        for (Entity entity : entities) {
            Object previousValue = previousValues.get(entity);
            if (previousValue != null) {
                settingsManager.applySetting(setting, previousValue, entity);
            }
        }
    }

    @Override
    public String toString() {
        return "Change " + setting.getLabel();
    }
}
