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

import com.willwinder.universalgcodesender.fx.stage.AboutStage;
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.event.ActionEvent;
import javafx.scene.Node;
import javafx.stage.Window;

public class OpenAboutAction extends BaseAction {

    private static final String ICON_BASE = "icons/info.svg";

    public OpenAboutAction() {
        super(Localization.getString("actions.openAbout"), Localization.getString("actions.openAbout"), Localization.getString("actions.category.help"), ICON_BASE);
        setMenuVisible(true);
        // A high order keeps the help menu as the last top level menu
        setMenuOrder(1000);
    }

    @Override
    public void handleAction(ActionEvent event) {
        Window window = event.getSource() instanceof Node node ? node.getScene().getWindow() : null;
        AboutStage aboutStage = new AboutStage(window);
        aboutStage.show();
    }
}
