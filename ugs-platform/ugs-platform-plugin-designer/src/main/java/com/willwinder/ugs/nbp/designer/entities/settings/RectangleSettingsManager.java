/*
    Copyright 2026 Joacim Breiler

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
import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import org.openide.util.lookup.ServiceProvider;

import java.util.List;


@ServiceProvider(service = EntitySettingsManager.class, position = 10)
public class RectangleSettingsManager implements EntitySettingsManager {

    @Override
    public boolean canHandle(Entity entity) {
        return entity instanceof Rectangle;
    }

    @Override
    public List<EntitySetting> getSupportedSettings(Entity entity) {
        return List.of(EntitySetting.CORNER_RADIUS);
    }

    @Override
    public boolean supportsSetting(EntitySetting setting) {
        return setting == EntitySetting.CORNER_RADIUS;
    }

    @Override
    public Object getSettingValue(EntitySetting setting, Entity entity) {
        return entity instanceof Rectangle rectangle ? rectangle.getCornerRadius() : null;
    }

    @Override
    public void applySetting(EntitySetting setting, Object value, Entity entity) {
        if (setting == EntitySetting.CORNER_RADIUS && value instanceof Double cornerRounding && entity instanceof Rectangle rectangle) {
            rectangle.setCornerRadius(cornerRounding);
        }
    }

    @Override
    public String getName() {
        return "RectangleSettingsManager";
    }
}
