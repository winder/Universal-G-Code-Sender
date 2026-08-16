/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.designer.model;

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;

import java.util.Collections;
import java.util.List;

/**
 * @author Joacim Breiler
 */
public class Design {
    private List<Entity> entities = Collections.emptyList();
    private ToolDefinition toolSnapshot;
    private Settings settings;

    public List<Entity> getEntities() {
        return entities;
    }

    public void setEntities(List<Entity> entities) {
        this.entities = entities;
    }

    public ToolDefinition getToolSnapshot() {
        return toolSnapshot;
    }

    public void setToolSnapshot(ToolDefinition toolSnapshot) {
        this.toolSnapshot = toolSnapshot;
    }

    /**
     * Returns the settings stored together with the design, or {@code null} for designs saved
     * before settings were part of the file format.
     */
    public Settings getSettings() {
        return settings;
    }

    public void setSettings(Settings settings) {
        this.settings = settings;
    }
}
