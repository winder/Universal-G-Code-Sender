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
package com.willwinder.universalgcodesender.fx.component;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.OverrunStyle;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import org.apache.commons.lang3.StringUtils;

public class SettingsRow extends HBox {
    public SettingsRow(String title, Region... controls) {
        this(title, "", controls);
    }

    public SettingsRow(String title, String tooltip, Region... controls) {
        super(10);
        setMinHeight(36);
        setAlignment(Pos.CENTER_LEFT);

        Label label = new Label(title);
        label.setWrapText(true);
        label.setMinHeight(Region.USE_PREF_SIZE);
        label.setPrefHeight(Region.USE_COMPUTED_SIZE);
        label.setMaxHeight(Double.MAX_VALUE);
        label.setAlignment(Pos.CENTER_LEFT);
        label.setMinWidth(220);
        label.setPrefWidth(220);
        label.setMaxWidth(220);
        label.setTextOverrun(OverrunStyle.CLIP);

        HBox controlBox = new HBox(10, controls);
        controlBox.setMinHeight(Region.USE_COMPUTED_SIZE);
        controlBox.setPrefHeight(Region.USE_COMPUTED_SIZE);
        controlBox.setMaxHeight(Region.USE_PREF_SIZE);
        controlBox.setMinWidth(Region.USE_COMPUTED_SIZE);
        controlBox.setPrefWidth(Region.USE_COMPUTED_SIZE);
        controlBox.setMaxWidth(Region.USE_PREF_SIZE);
        getChildren().addAll(label, controlBox);

        if (StringUtils.isNotBlank(tooltip)) {
            InfoTooltip infoTooltip = new InfoTooltip(tooltip);
            HBox.setMargin(infoTooltip, new Insets(0, 4, 0, 4));
            getChildren().add(infoTooltip);
        }
    }
}