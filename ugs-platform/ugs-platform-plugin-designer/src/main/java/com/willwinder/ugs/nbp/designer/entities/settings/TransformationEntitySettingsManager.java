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

import com.willwinder.ugs.nbp.designer.entities.Anchor;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import org.openide.util.lookup.ServiceProvider;

import java.awt.geom.Point2D;
import java.util.List;

/**
 * Base settings manager for handling common Entity transformation settings.
 * This manager handles position, size, rotation, and basic transformation properties.
 *
 * @author giro-dev
 */
@ServiceProvider(service = EntitySettingsManager.class, position = 1)
public class TransformationEntitySettingsManager implements EntitySettingsManager {

    @Override
    public boolean canHandle(Entity entity) {
        return entity != null; // Can handle any entity for basic transformation settings
    }

    @Override
    public List<EntitySetting> getSupportedSettings(Entity entity) {
        return EntitySetting.TRANSFORMATION_SETTINGS;
    }

    @Override
    public boolean supportsSetting(EntitySetting setting) {
        return EntitySetting.TRANSFORMATION_SETTINGS.contains(setting);
    }

    @Override
    public Object getSettingValue(EntitySetting setting, Entity entity) {
        return switch (setting) {
            case POSITION_X -> entity.getPosition().getX();
            case POSITION_Y -> entity.getPosition().getY();
            case WIDTH -> entity.getSize().getWidth();
            case HEIGHT -> entity.getSize().getHeight();
            case ROTATION -> entity.getRotation();
            case LOCK_RATIO -> false;
            default -> null;
        };
    }

    @Override
    public void applySetting(EntitySetting setting, Object value, Entity entity) {
        switch (setting) {
            case POSITION_X -> {
                if (value instanceof Double x) {
                    entity.setPosition(new Point2D.Double(x, entity.getPosition().getY()));
                }
            }
            case POSITION_Y -> {
                if (value instanceof Double y) {
                    entity.setPosition(new Point2D.Double(entity.getPosition().getX(), y));
                }
            }
            case WIDTH -> {
                if (value instanceof Double width) {
                    entity.setWidth(width);
                }
            }
            case HEIGHT -> {
                if (value instanceof Double height) {
                    entity.setHeight(height);
                }
            }
            case ROTATION -> {
                if (value instanceof Double rotation) {
                    entity.setRotation(rotation);
                }
            }
            case ANCHOR -> {
                if (value instanceof Anchor anchor) {
                    entity.setPivotPoint(anchor);
                }
            }
            case LOCK_RATIO -> {
                if (value instanceof Boolean lockRatio) {
                    entity.setLockRatio(lockRatio);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "BaseEntitySettingsManager";
    }

}
