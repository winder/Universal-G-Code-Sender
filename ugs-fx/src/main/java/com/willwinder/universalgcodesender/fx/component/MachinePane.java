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

import com.willwinder.universalgcodesender.fx.component.dro.MachineStatusPane;
import com.willwinder.universalgcodesender.fx.component.jog.JogPane;
import com.willwinder.universalgcodesender.fx.helper.SplitPaneDividerPersistence;
import com.willwinder.universalgcodesender.fx.settings.Settings;
import javafx.application.Platform;
import javafx.geometry.Orientation;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.Border;

/**
 * The machine status and the jogging controls, stacked in a vertical split whose divider
 * position is persisted.
 */
public class MachinePane extends SplitPane {
    private boolean dividerPersisted;

    public MachinePane() {
        MachineStatusPane machineStatusPane = new MachineStatusPane();
        getItems().addAll(machineStatusPane, new JogPane());
        setOrientation(Orientation.VERTICAL);

        // The divider can only be positioned once the pane is in a scene, and the persistence
        // keeps following the divider afterwards even while the pane is collapsed away.
        sceneProperty().addListener((observable, oldScene, newScene) -> {
            if (newScene != null && !dividerPersisted) {
                dividerPersisted = true;
                Platform.runLater(() -> SplitPaneDividerPersistence.install(
                        this, machineStatusPane, Settings.getInstance().windowDividerLeftProperty()));
            }
        });
    }
}
