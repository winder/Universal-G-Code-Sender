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
package com.willwinder.universalgcodesender.fx.component.settings;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.fx.component.SettingsRow;
import com.willwinder.universalgcodesender.fx.control.SwitchButton;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class TerminalSettingsPane extends VBox {

    private final BackendAPI backend;

    public TerminalSettingsPane() {
        setSpacing(20);

        backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        addTitleSection();
        addVerboseLoggingSection();
    }

    private void addVerboseLoggingSection() {
        SwitchButton switchButton = new SwitchButton();
        switchButton.setSelected(backend.getSettings().isVerboseOutputEnabled());
        switchButton.selectedProperty().addListener((observable, oldValue, newValue) -> backend.getSettings().setVerboseOutputEnabled(newValue));
        getChildren().add(new SettingsRow(Localization.getString("mainWindow.swing.showVerboseOutputCheckBox"), switchButton));
    }

    private void addTitleSection() {
        Label title = new Label(Localization.getString("settings.terminal"));
        title.setFont(Font.font(20));
        getChildren().add(title);
    }

}