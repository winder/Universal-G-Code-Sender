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
import com.willwinder.ugs.nbp.designer.entities.cuttable.Raster;
import org.openide.util.lookup.ServiceProvider;

import java.util.ArrayList;
import java.util.List;

@ServiceProvider(service = EntitySettingsManager.class, position = 5)
public class RasterSettingsManager implements EntitySettingsManager {

    @Override
    public boolean canHandle(Entity entity) {
        return entity instanceof Raster;
    }

    @Override
    public List<EntitySetting> getSupportedSettings(Entity entity) {
        if (!(entity instanceof Raster)) {
            return List.of();
        }
        return getRasterSettings();
    }

    @Override
    public boolean supportsSetting(EntitySetting setting) {
        return getRasterSettings().contains(setting);
    }

    @Override
    public Object getSettingValue(EntitySetting setting, Entity entity) {
        if (!(entity instanceof Raster raster)) {
            return null;
        }

        return switch (setting) {
            case RASTER_BRIGHTNESS -> raster.getBrightness();
            case RASTER_CONTRAST -> raster.getContrast();
            case RASTER_GAMMA -> raster.getGamma();
            case RASTER_INVERT -> raster.isInvert();
            case RASTER_LEVELS -> raster.getLevels();
            default -> null;
        };
    }

    @Override
    public void applySetting(EntitySetting setting, Object value, Entity entity) {
        if (!(entity instanceof Raster raster)) {
            return;
        }

        switch (setting) {
            case RASTER_BRIGHTNESS -> {
                if (value instanceof Double doubleValue) {
                    raster.setBrightness(doubleValue);
                }
            }
            case RASTER_CONTRAST -> {
                if (value instanceof Double doubleValue) {
                    raster.setContrast(doubleValue);
                }
            }
            case RASTER_GAMMA -> {
                if (value instanceof Double doubleValue) {
                    raster.setGamma(doubleValue);
                }
            }
            case RASTER_LEVELS -> {
                if (value instanceof Integer intValue) {
                    raster.setLevels(intValue);
                }
            }
            case RASTER_INVERT -> {
                if (value instanceof Boolean booleanValue) {
                    raster.setInvert(booleanValue);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "RasterSettingsManager";
    }

    private static List<EntitySetting> getRasterSettings() {
        List<EntitySetting> settings = new ArrayList<>();
        settings.add(EntitySetting.RASTER_BRIGHTNESS);
        settings.add(EntitySetting.RASTER_CONTRAST);
        settings.add(EntitySetting.RASTER_GAMMA);
        settings.add(EntitySetting.RASTER_LEVELS);
        settings.add(EntitySetting.RASTER_INVERT);
        return settings;
    }
}
