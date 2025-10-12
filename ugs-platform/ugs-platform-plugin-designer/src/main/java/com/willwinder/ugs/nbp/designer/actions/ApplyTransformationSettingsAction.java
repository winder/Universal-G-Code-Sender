/*
    Copyright 2024 Albert Giró

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
import com.willwinder.ugs.nbp.designer.entities.TransformationSettingsHandler;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

/**
 * @author giro-dev
 */
public class ApplyTransformationSettingsAction implements UndoableAction {
    private final List<Entity> entities;
    private final TransformationSettingsHandler newSettings;
    private final Map<Entity, TransformationSettingsHandler> previousSettings;

    public ApplyTransformationSettingsAction(List<Entity> entities, TransformationSettingsHandler newSettings) {
        this.entities = entities;
        this.newSettings = newSettings.copy();
        this.previousSettings = new HashMap<>();
        
        // Store the current state of all entities
        for (Entity entity : entities) {
            this.previousSettings.put(entity, TransformationSettingsHandler.fromEntity(entity));
        }
    }

    @Override
    public void redo() {
        for (Entity entity : entities) {
            newSettings.applyToEntity(entity);
        }
    }

    @Override
    public void undo() {
        for (Entity entity : entities) {
            TransformationSettingsHandler previousSetting = previousSettings.get(entity);
            if (previousSetting != null) {
                previousSetting.applyToEntity(entity);
            }
        }
    }

    @Override
    public String toString() {
        return "Apply transformation settings";
    }
}
