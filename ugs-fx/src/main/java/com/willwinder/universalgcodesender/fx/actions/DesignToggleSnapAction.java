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
package com.willwinder.universalgcodesender.fx.actions;

import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.event.ActionEvent;

/**
 * Turns snapping to the grid on and off in the designer. The grid spacing is set in the
 * design section of the visualizer settings.
 */
public class DesignToggleSnapAction extends BaseAction {
    public static final String ICON = "icons/snap.svg";

    public DesignToggleSnapAction() {
        super(Localization.getString("platform.designer.toggleSnapping"), Localization.getString("platform.designer.toggleSnapping"),
                Localization.getString("actions.category.designer"), ICON);
        selectedProperty().bindBidirectional(VisualizerSettings.getInstance().designSnapEnabledProperty());
    }

    @Override
    public void handleAction(ActionEvent event) {
        VisualizerSettings settings = VisualizerSettings.getInstance();
        settings.designSnapEnabledProperty().set(!settings.designSnapEnabledProperty().get());
    }
}
