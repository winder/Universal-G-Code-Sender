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
package com.willwinder.universalgcodesender.fx.control;

import com.willwinder.universalgcodesender.fx.actions.Action;
import com.willwinder.universalgcodesender.fx.helper.ShortcutConverter;
import com.willwinder.universalgcodesender.fx.service.ShortcutService;
import javafx.collections.MapChangeListener;
import javafx.scene.control.Tooltip;
import javafx.util.Duration;

import java.util.Optional;

/**
 * A tooltip for an action button that names the action and its keyboard shortcut, as
 * "Draw rectangle (R)". The text follows the title and the shortcut as they change.
 */
public final class ActionTooltip {
    private static final Duration SHOW_DELAY = Duration.millis(100);

    private ActionTooltip() {
    }

    public static Tooltip create(Action action) {
        Tooltip tooltip = new Tooltip();
        tooltip.setShowDelay(SHOW_DELAY);
        Runnable refresh = () -> tooltip.setText(text(action.getTitle(), ShortcutService.getShortcut(action.getId())));
        refresh.run();
        action.titleProperty().addListener((_, _, _) -> refresh.run());
        ShortcutService.getShortcuts().addListener((MapChangeListener<String, String>) change -> {
            if (action.getId().equals(change.getKey())) {
                refresh.run();
            }
        });
        return tooltip;
    }

    static String text(String title, Optional<String> shortcut) {
        return shortcut
                .map(ShortcutConverter::toDisplayString)
                .filter(display -> !display.isEmpty())
                .map(display -> title + " (" + display + ")")
                .orElse(title);
    }
}
