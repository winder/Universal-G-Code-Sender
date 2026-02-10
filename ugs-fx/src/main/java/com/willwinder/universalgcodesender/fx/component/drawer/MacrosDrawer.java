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
package com.willwinder.universalgcodesender.fx.component.drawer;

import com.willwinder.universalgcodesender.fx.actions.MacroAction;
import com.willwinder.universalgcodesender.fx.control.ActionButton;
import com.willwinder.universalgcodesender.fx.model.MacroAdapter;
import com.willwinder.universalgcodesender.fx.service.ActionRegistry;
import com.willwinder.universalgcodesender.fx.service.MacroRegistry;
import javafx.collections.ListChangeListener;
import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.layout.FlowPane;

public class MacrosDrawer extends Drawer {
    private final FlowPane flowPane;

    public MacrosDrawer() {
        flowPane = new FlowPane();
        flowPane.setHgap(10);
        flowPane.setVgap(10);
        flowPane.setPadding(new Insets(10));
        getChildren().add(flowPane);

        MacroRegistry.getInstance().getMacros().addListener((ListChangeListener<MacroAdapter>) c -> onUpdatedMacros());
        onUpdatedMacros();
    }

    private void onUpdatedMacros() {
        flowPane.getChildren().clear();
        ActionRegistry.getInstance().getAllActionsOfClass(MacroAction.class)
                .forEach(macroAction -> {
                    Button button = new ActionButton(macroAction, 24);
                    button.setPrefWidth(120);
                    button.setPrefHeight(38);
                    flowPane.getChildren().add(button);
                });
    }

    @Override
    public void setActive(boolean active) {

    }
}
