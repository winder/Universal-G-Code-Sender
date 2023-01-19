/*
    Copyright 2023 Will Winder

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
package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

import java.awt.event.ActionEvent;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author Joacim Breiler
 */
@ActionID(
        category = LocalizingService.CATEGORY_DESIGNER,
        id = "ToggleHidden")
@ActionRegistration(
        iconBase = ToggleHidden.SMALL_ICON_PATH,
        displayName = "Hidden",
        lazy = false)
public class ToggleHidden extends AbstractDesignAction implements SelectionListener {
    public static final String SMALL_ICON_PATH = "img/eye.svg";
    public static final String LARGE_ICON_PATH = "img/eye24.svg";
    public static final String SMALL_ICON_HIDDEN_PATH = "img/eyeoff.svg";
    public static final String LARGE_ICON_HIDDEN_PATH = "img/eyeoff24.svg";
    public static final String PROPERTY_MENU_TEXT = "menuText";

    public ToggleHidden() {
        putValue(PROPERTY_MENU_TEXT, "Toggle hidden");
        putValue(NAME, "Toggle hidden");
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));

        ControllerFactory.getSelectionManager().addSelectionListener(this);
        ControllerFactory.getController().addListener(e -> onSelectionEvent(null));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Boolean isHidden = getCuttableStream()
                .findFirst()
                .map(Cuttable::isHidden)
                .orElse(true);

        UndoableShowHideAction action = new UndoableShowHideAction(getCuttableStream().collect(Collectors.toList()), !isHidden);
        action.redo();
        ControllerFactory.getUndoManager().addAction(action);
    }

    private Stream<Cuttable> getCuttableStream() {
        SelectionManager selectionManager = ControllerFactory.getSelectionManager();
        return selectionManager.getSelection().stream().filter(Cuttable.class::isInstance).map(Cuttable.class::cast);
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        SelectionManager selectionManager = ControllerFactory.getSelectionManager();
        setEnabled(!selectionManager.getSelection().isEmpty());

        boolean allIsHidden = getCuttableStream().allMatch(Cuttable::isHidden);
        if (allIsHidden) {
            putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
            putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
            putValue(PROPERTY_MENU_TEXT, "Show");
            putValue(NAME, "Show");
        } else {
            putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_HIDDEN_PATH, false));
            putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_HIDDEN_PATH, false));
            putValue(PROPERTY_MENU_TEXT, "Hide");
            putValue(NAME, "Hide");
        }
    }

    private static class UndoableShowHideAction implements UndoableAction {
        private final List<Cuttable> entities;
        private final boolean setAsHidden;

        public UndoableShowHideAction(List<Cuttable> entities, boolean setAsHidden) {
            this.entities = entities;
            this.setAsHidden = setAsHidden;
        }

        @Override
        public void redo() {
            entities.forEach(entity -> entity.setHidden(setAsHidden));
            triggerSelectionEvent();
        }

        @Override
        public void undo() {
            entities.forEach(entity -> entity.setHidden(!setAsHidden));
            triggerSelectionEvent();
        }

        private void triggerSelectionEvent() {
            SelectionManager selectionManager = ControllerFactory.getSelectionManager();
            List<Entity> selection = selectionManager.getSelection();
            selectionManager.clearSelection();
            selectionManager.setSelection(selection);
        }

        @Override
        public String toString() {
            if (setAsHidden) {
                return "hide entities";
            } else {
                return "show entities";
            }
        }
    }
}
