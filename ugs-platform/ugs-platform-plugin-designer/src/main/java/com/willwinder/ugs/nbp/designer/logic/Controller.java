/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.designer.logic;

import com.google.common.collect.Sets;
import com.willwinder.ugs.nbp.designer.actions.AddAction;
import com.willwinder.ugs.nbp.designer.actions.UndoManager;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.ugs.nbp.designer.model.Settings;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * @author Joacim Breiler
 */
public class Controller {

    private final SelectionManager selectionManager;
    private final Settings settings = new Settings();
    private final Set<ControllerListener> listeners = Sets.newConcurrentHashSet();
    private final UndoManager undoManager;
    private Drawing drawing;
    private Tool tool;

    public Controller(SelectionManager selectionManager, UndoManager undoManager) {
        this.undoManager = undoManager;
        this.tool = Tool.SELECT;
        this.selectionManager = selectionManager;
        this.drawing = new Drawing(this);
        this.undoManager.addListener(() -> this.drawing.repaint());
    }

    public void addEntity(Entity s) {
        AddAction add = new AddAction(this, s);
        add.execute();
        undoManager.addAction(add);
    }

    public void addEntities(List<Entity> s) {
        AddAction add = new AddAction(this, s);
        add.execute();
        undoManager.addAction(add);
    }

    public Drawing getDrawing() {
        return drawing;
    }

    public SelectionManager getSelectionManager() {
        return selectionManager;
    }

    public Tool getTool() {
        return tool;
    }

    public void setTool(Tool t) {
        this.tool = t;
        notifyListeners(ControllerEventType.TOOL_SELECTED);
    }

    public void newDrawing() {
        drawing.clear();
        notifyListeners(ControllerEventType.NEW_DRAWING);
    }

    private void notifyListeners(ControllerEventType event) {
        new ArrayList<>(listeners)
                .forEach(l -> l.onControllerEvent(event));
    }

    public Settings getSettings() {
        return settings;
    }

    public void addListener(ControllerListener controllerListener) {
        listeners.add(controllerListener);
    }

    public void removeListener(ControllerListener controllerListener) {
        listeners.remove(controllerListener);
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }

    public void setDesign(Design design) {
        newDrawing();
        getDrawing().insertEntities(design.getEntities());
        settings.applySettings(design.getSettings());
        getDrawing().repaint();
        setTool(Tool.SELECT);
    }

    public void setCursor(Cursor cursor) {
        drawing.setCursor(cursor);
    }
}
