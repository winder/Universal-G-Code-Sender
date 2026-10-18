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

import com.willwinder.ugs.designer.actions.ToggleHidden;
import com.willwinder.ugs.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.designer.entities.selection.SelectionEvent;
import com.willwinder.universalgcodesender.i18n.Localization;
import javafx.application.Platform;

/**
 * Hides the selected entities, or shows them again when the first selected one is hidden. The
 * title and icon follow the selection so the menu reads as "Hide" or "Show" for what it will do.
 */
public class DesignToggleHiddenAction extends AbstractDesignOperationAction {
    public static final String ICON_HIDE = "icons/eye-slash.svg";
    public static final String ICON_SHOW = "icons/eye.svg";

    private static final String TITLE_HIDE = Localization.getString("platform.designer.hide");
    private static final String TITLE_SHOW = Localization.getString("platform.designer.show");

    public DesignToggleHiddenAction() {
        super(new ToggleHidden(), TITLE_HIDE, ICON_HIDE);
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        super.onSelectionEvent(selectionEvent);
        boolean willShow = controller.getSelectionManager().getSelection().stream()
                .filter(Cuttable.class::isInstance)
                .map(Cuttable.class::cast)
                .findFirst()
                .map(Cuttable::isHidden)
                .orElse(false);
        Platform.runLater(() -> {
            titleProperty().set(willShow ? TITLE_SHOW : TITLE_HIDE);
            labelProperty().set(willShow ? TITLE_SHOW : TITLE_HIDE);
            iconProperty().set(willShow ? ICON_SHOW : ICON_HIDE);
        });
    }
}
