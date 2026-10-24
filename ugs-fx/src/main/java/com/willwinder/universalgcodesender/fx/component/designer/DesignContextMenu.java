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
package com.willwinder.universalgcodesender.fx.component.designer;

import com.willwinder.universalgcodesender.fx.actions.Action;
import com.willwinder.universalgcodesender.fx.actions.DesignBreakApartAction;
import com.willwinder.universalgcodesender.fx.actions.DesignCopyAction;
import com.willwinder.universalgcodesender.fx.actions.DesignDeleteAction;
import com.willwinder.universalgcodesender.fx.actions.DesignFlipHorizontalAction;
import com.willwinder.universalgcodesender.fx.actions.DesignFlipVerticalAction;
import com.willwinder.universalgcodesender.fx.actions.DesignGroupAction;
import com.willwinder.universalgcodesender.fx.actions.DesignIntersectionAction;
import com.willwinder.universalgcodesender.fx.actions.DesignOffsetAction;
import com.willwinder.universalgcodesender.fx.actions.DesignPasteAction;
import com.willwinder.universalgcodesender.fx.actions.DesignSubtractAction;
import com.willwinder.universalgcodesender.fx.actions.DesignToggleHiddenAction;
import com.willwinder.universalgcodesender.fx.actions.DesignUnionAction;
import com.willwinder.universalgcodesender.fx.control.ActionMenuItem;
import com.willwinder.universalgcodesender.fx.service.ActionRegistry;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SeparatorMenuItem;

import java.util.List;
import java.util.Optional;

/**
 * The right click menu over the design, built from the registered design actions so it stays
 * in step with the toolbar and the shortcuts.
 */
public final class DesignContextMenu {

    private DesignContextMenu() {
    }

    public static ContextMenu create() {
        ContextMenu menu = new ContextMenu();
        addSection(menu, List.of(DesignCopyAction.class, DesignPasteAction.class, DesignDeleteAction.class));
        addSection(menu, List.of(DesignGroupAction.class, DesignBreakApartAction.class));
        addSection(menu, List.of(DesignUnionAction.class, DesignSubtractAction.class, DesignIntersectionAction.class, DesignOffsetAction.class));
        addSection(menu, List.of(DesignFlipHorizontalAction.class, DesignFlipVerticalAction.class));
        addSection(menu, List.of(DesignToggleHiddenAction.class));
        return menu;
    }

    private static void addSection(ContextMenu menu, List<Class<? extends Action>> actionClasses) {
        List<MenuItem> items = actionClasses.stream()
                .map(actionClass -> ActionRegistry.getInstance().getAction(actionClass.getCanonicalName()))
                .flatMap(Optional::stream)
                .map(action -> (MenuItem) new ActionMenuItem(action))
                .toList();
        if (items.isEmpty()) {
            return;
        }
        if (!menu.getItems().isEmpty()) {
            menu.getItems().add(new SeparatorMenuItem());
        }
        menu.getItems().addAll(items);
    }
}
