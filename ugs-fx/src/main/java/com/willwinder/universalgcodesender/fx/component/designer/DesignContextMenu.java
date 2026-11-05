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
import com.willwinder.universalgcodesender.fx.actions.DesignAddNodesAction;
import com.willwinder.universalgcodesender.fx.actions.DesignBreakApartAction;
import com.willwinder.universalgcodesender.fx.actions.DesignBreakPathAction;
import com.willwinder.universalgcodesender.fx.actions.DesignCopyAction;
import com.willwinder.universalgcodesender.fx.actions.DesignDeleteAction;
import com.willwinder.universalgcodesender.fx.actions.DesignFlipHorizontalAction;
import com.willwinder.universalgcodesender.fx.actions.DesignFlipVerticalAction;
import com.willwinder.universalgcodesender.fx.actions.DesignGroupAction;
import com.willwinder.universalgcodesender.fx.actions.DesignIntersectionAction;
import com.willwinder.universalgcodesender.fx.actions.DesignJoinPathAction;
import com.willwinder.universalgcodesender.fx.actions.DesignMakeCurveAction;
import com.willwinder.universalgcodesender.fx.actions.DesignMakeLineAction;
import com.willwinder.universalgcodesender.fx.actions.DesignNestAction;
import com.willwinder.universalgcodesender.fx.actions.DesignOffsetAction;
import com.willwinder.universalgcodesender.fx.actions.DesignPasteAction;
import com.willwinder.universalgcodesender.fx.actions.DesignSubtractAction;
import com.willwinder.universalgcodesender.fx.actions.DesignToggleHiddenAction;
import com.willwinder.universalgcodesender.fx.actions.DesignUnionAction;
import com.willwinder.universalgcodesender.fx.component.designer.editor.VertexEditing;
import com.willwinder.universalgcodesender.fx.control.ActionMenuItem;
import com.willwinder.universalgcodesender.fx.service.ActionRegistry;
import javafx.beans.binding.BooleanExpression;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.value.ObservableBooleanValue;
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
        Sections sections = new Sections(menu);
        sections.add(List.of(DesignBreakPathAction.class, DesignJoinPathAction.class), VertexEditing.nodeSelectedProperty());
        sections.add(List.of(DesignAddNodesAction.class, DesignMakeCurveAction.class, DesignMakeLineAction.class), VertexEditing.nodeSelectedProperty());
        sections.add(List.of(DesignCopyAction.class, DesignPasteAction.class, DesignDeleteAction.class));
        sections.add(List.of(DesignGroupAction.class, DesignBreakApartAction.class));
        sections.add(List.of(DesignUnionAction.class, DesignSubtractAction.class, DesignIntersectionAction.class, DesignOffsetAction.class, DesignNestAction.class));
        sections.add(List.of(DesignFlipHorizontalAction.class, DesignFlipVerticalAction.class));
        sections.add(List.of(DesignToggleHiddenAction.class));
        return menu;
    }

    /**
     * Adds sections of actions separated by separators. A section can be hidden, and a
     * separator is only shown between two shown sections, so hidden sections leave no stray
     * separators behind.
     */
    private static final class Sections {
        private static final ObservableBooleanValue ALWAYS = new SimpleBooleanProperty(true);

        private final ContextMenu menu;
        // Whether any of the sections added so far is shown, null before the first one
        private BooleanExpression anyShown;

        private Sections(ContextMenu menu) {
            this.menu = menu;
        }

        private void add(List<Class<? extends Action>> actionClasses) {
            add(actionClasses, ALWAYS);
        }

        /**
         * @param visible when the section is shown
         */
        private void add(List<Class<? extends Action>> actionClasses, ObservableBooleanValue visible) {
            List<MenuItem> items = actionClasses.stream()
                    .map(actionClass -> ActionRegistry.getInstance().getAction(actionClass.getCanonicalName()))
                    .flatMap(Optional::stream)
                    .map(action -> (MenuItem) new ActionMenuItem(action))
                    .toList();
            if (items.isEmpty()) {
                return;
            }
            BooleanExpression shown = BooleanExpression.booleanExpression(visible);
            if (anyShown != null) {
                SeparatorMenuItem separator = new SeparatorMenuItem();
                separator.visibleProperty().bind(anyShown.and(shown));
                menu.getItems().add(separator);
            }
            if (visible != ALWAYS) {
                items.forEach(item -> item.visibleProperty().bind(shown));
            }
            menu.getItems().addAll(items);
            anyShown = anyShown == null ? shown : anyShown.or(shown);
        }
    }
}
