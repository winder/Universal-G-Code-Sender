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

import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Text;

import java.awt.*;

/**
 * Model for text-specific properties (text content, font family)
 * Extends CuttableSettingsModel to include both entity and cuttable properties.
 */
public class TextSettingsModel extends CuttableSettingsModel {

    private String text = "";
    private String fontFamily = Font.SANS_SERIF;

    public String getText() {
        return text;
    }

    public void setText(String text) {
        if (!this.text.equals(text)) {
            this.text = text;
            notifyListeners(EntitySetting.TEXT);
        }
    }

    public String getFontFamily() {
        return fontFamily;
    }

    public void setFontFamily(String fontFamily) {
        if (!this.fontFamily.equals(fontFamily)) {
            this.fontFamily = fontFamily;
            notifyListeners(EntitySetting.FONT_FAMILY);
        }
    }

    @Override
    public void reset() {
        super.reset();
        setText("");
        setFontFamily(Font.SANS_SERIF);
    }

    @Override
    public void updateFromGroup(Group selectionGroup) {
        super.updateFromGroup(selectionGroup); // Update entity and cuttable properties

        if (selectionGroup.getSettings().contains(EntitySetting.TEXT)) {
            Text textEntity = (Text) selectionGroup.getChildren().get(0);
            setText(textEntity.getText());
        }

        if (selectionGroup.getSettings().contains(EntitySetting.FONT_FAMILY)) {
            Text textEntity = (Text) selectionGroup.getChildren().get(0);
            setFontFamily(textEntity.getFontFamily());
        }
    }

    public Object getValueFor(EntitySetting setting) {
        return switch (setting) {
            case TEXT -> getText();
            case FONT_FAMILY -> getFontFamily();
            default -> super.getValueFor(setting);
        };
    }

    public void updateValueFor(EntitySetting setting, Object newValue) {
        switch (setting) {
            case TEXT -> setText((String) newValue);
            case FONT_FAMILY -> setFontFamily((String) newValue);
            default -> super.updateValueFor(setting, newValue);
        }
    }
}
