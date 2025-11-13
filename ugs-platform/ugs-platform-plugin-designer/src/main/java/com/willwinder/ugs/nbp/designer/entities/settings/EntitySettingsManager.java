/*
    Copyright 2024 Albert Giro Quer

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
package com.willwinder.ugs.nbp.designer.entities.settings;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;

import java.util.List;

/**
 * Interface for entity settings managers that handle specific types of entities and their settings.
 * This interface enables a plugin-like architecture where different entity types can have
 * their own specialized settings managers without modifying existing code.
 *
 * @author giro-dev
 */
public interface EntitySettingsManager {

    default boolean canHandle(List<Entity> entities) {
        return entities.stream()
                .allMatch(this::canHandle);
    }

    /**
     * Checks if this settings manager can handle the given entity type.
     *
     * @param entity the entity to check
     * @return true if this manager can handle the entity type
     */
    boolean canHandle(Entity entity);


    /**
     * Gets all settings that this manager provides for the given entity.
     *
     * @param entity the entity to get settings for
     * @return list of supported settings
     */
    List<EntitySetting> getSupportedSettings(Entity entity);

    /**
     * Checks if this manager supports the given setting.
     *
     * @param setting the setting to check
     * @return true if this manager supports the setting
     */
    boolean supportsSetting(EntitySetting setting);

    /**
     * Gets the current value of a setting from the entity.
     *
     * @param setting the setting to get
     * @param entity  the entity to get the setting from
     * @return the current value, or null if not applicable
     */
    Object getSettingValue(EntitySetting setting, Entity entity);

    /**
     * Applies a setting value to the entity.
     *
     * @param setting the setting to apply
     * @param value   the value to set
     * @param entity  the entity to apply the setting to
     */
    void applySetting(EntitySetting setting, Object value, Entity entity);

    /**
     * Gets a human-readable name for this settings manager.
     * Used for debugging and logging purposes.
     *
     * @return the name of this settings manager
     */
    String getName();

    default void applySetting(EntitySetting setting, Object newValue, List<Entity> entities) {
        for (Entity entity : entities) {
            applySetting(setting, newValue, entity);
        }
    }
}
