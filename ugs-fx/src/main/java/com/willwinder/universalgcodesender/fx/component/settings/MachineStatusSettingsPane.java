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

import com.willwinder.universalgcodesender.fx.component.SettingsRow;
import com.willwinder.universalgcodesender.fx.control.SwitchButton;
import com.willwinder.universalgcodesender.fx.settings.Settings;
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.text.Font;

public class MachineStatusSettingsPane extends VBox {

    public MachineStatusSettingsPane() {
        setSpacing(20);
        addTitleSection();
        addShowMachinePositionSection();
    }

    private void addTitleSection() {
        Label title = new Label(Localization.getString("settings.machineStatus"));
        title.setFont(Font.font(20));
        getChildren().add(title);
    }

    private void addShowMachinePositionSection() {
        SwitchButton showMachinePositionToggle = new SwitchButton();
        showMachinePositionToggle.selectedProperty().bindBidirectional(Settings.getInstance().showMachinePositionProperty());
        getChildren().add(new SettingsRow(Localization.getString("settings.showMachinePosition"), showMachinePositionToggle));
    }
}