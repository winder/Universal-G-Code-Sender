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
package com.willwinder.ugs.designer.actions;

import static com.willwinder.ugs.designer.utils.StitchPathUtils.stitchEntities;
import com.willwinder.ugs.designer.entities.entities.Entity;
import com.willwinder.ugs.designer.entities.entities.selection.SelectionEvent;
import com.willwinder.ugs.designer.entities.entities.selection.SelectionListener;
import com.willwinder.ugs.designer.entities.entities.selection.SelectionManager;
import com.willwinder.ugs.designer.gui.Drawing;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.universalgcodesender.utils.SvgIconLoader;

import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * An action for stitching together several small path (such as lines) into one continuous path
 *
 * @author Joacim Breiler
 */
public class StitchAction extends AbstractDesignAction implements SelectionListener {
    public static final String SMALL_ICON_PATH = "img/stitch.svg";
    public static final String LARGE_ICON_PATH = "img/stitch24.svg";
    private final transient Controller controller;

    public StitchAction() {
        putValue("menuText", "Stitch");
        putValue(NAME, "Stitch");
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SHORT_DESCRIPTION, "Stitch multiple lines into one shape");
        putValue(SMALL_ICON, SvgIconLoader.loadImageIcon(SMALL_ICON_PATH, SvgIconLoader.SIZE_SMALL).orElse(null));
        putValue(LARGE_ICON_KEY, SvgIconLoader.loadImageIcon(SMALL_ICON_PATH, SvgIconLoader.SIZE_MEDIUM).orElse(null));

        this.controller = ControllerFactory.getController();
        SelectionManager selectionManager = controller.getSelectionManager();
        selectionManager.addSelectionListener(this);
        onSelectionEvent(new SelectionEvent());
    }

    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        boolean hasMultipleOpenShapes = controller.getSelectionManager()
                .getSelection()
                .stream()
                .anyMatch(e -> !e.isClosedShape()) || controller.getSelectionManager().getSelection().size() > 1;
        setEnabled(hasMultipleOpenShapes);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        List<Entity> selection = controller.getSelectionManager().getSelection();

        List<? extends Entity> result = stitchEntities(selection);
        UndoableStichAction undoableStichAction = new UndoableStichAction(controller.getDrawing(), result, selection);
        controller.getUndoManager().addAction(undoableStichAction);
        undoableStichAction.execute();
    }

    /**
     * Stores the state of the stitching as an undoable action
     */
    static class UndoableStichAction implements UndoableAction, DrawAction {
        private final List<Entity> newEntities;
        private final List<Entity> oldEntites;
        private final Drawing drawing;

        public UndoableStichAction(Drawing drawing, List<? extends Entity> newEntities, List<? extends Entity> oldEntites) {
            this.drawing = drawing;
            this.newEntities = new ArrayList<>(newEntities);
            this.oldEntites = new ArrayList<>(oldEntites);
        }

        @Override
        public void redo() {
            execute();
        }

        @Override
        public void undo() {
            drawing.insertEntities(oldEntites);
            drawing.removeEntities(newEntities);
            drawing.repaint();
        }

        @Override
        public void execute() {
            drawing.insertEntities(newEntities);
            drawing.removeEntities(oldEntites);
            drawing.repaint();
        }

        @Override
        public String toString() {
            return "stitch " + (newEntities.size() > 1 ? "entities" : "entity");
        }
    }

}
