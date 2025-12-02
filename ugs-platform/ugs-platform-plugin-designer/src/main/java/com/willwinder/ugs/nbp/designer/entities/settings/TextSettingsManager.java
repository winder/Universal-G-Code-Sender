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
import com.willwinder.ugs.nbp.designer.entities.cuttable.Text;
import org.openide.util.lookup.ServiceProvider;

import java.util.ArrayList;
import java.util.List;

/**
 * Settings manager specialized for Text entities.
 * Handles text-specific settings like text content and font family.
 *
 * @author giro-dev
 */
@ServiceProvider(service = EntitySettingsManager.class, position = 3)
public class TextSettingsManager implements EntitySettingsManager {

    @Override
    public boolean canHandle(Entity entity) {
        return entity instanceof Text;
    }

    @Override
    public List<EntitySetting> getSupportedSettings(Entity entity) {
        if (!(entity instanceof Text)) {
            return List.of();
        }
        return getTextSettings();
    }

    @Override
    public boolean supportsSetting(EntitySetting setting) {
        return getTextSettings().contains(setting);
    }

    @Override
    public Object getSettingValue(EntitySetting setting, Entity entity) {
        if (!(entity instanceof Text text)) {
            return null;
        }

        return switch (setting) {
            case TEXT -> text.getText();
            case FONT_FAMILY -> text.getFontFamily();
            default -> null;
        };
    }

    @Override
    public void applySetting(EntitySetting setting, Object value, Entity entity) {
        if (!(entity instanceof Text text)) {
            return;
        }

        switch (setting) {
            case TEXT -> {
                if (value instanceof String textValue) {
                    text.setText(textValue);
                }
            }
            case FONT_FAMILY -> {
                if (value instanceof String font) {
                    text.setFontFamily(font);
                }
            }
        }
    }

    @Override
    public String getName() {
        return "TextSettingsManager";
    }

    /**
     * Gets all text-related settings that apply to Text entities
     */
    private static List<EntitySetting> getTextSettings() {
        List<EntitySetting> settings = new ArrayList<>();
        settings.add(EntitySetting.TEXT);
        settings.add(EntitySetting.FONT_FAMILY);
        return settings;
    }
}
