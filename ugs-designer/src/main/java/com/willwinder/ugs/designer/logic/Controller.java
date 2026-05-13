/*
    Copyright 2021-2023 Will Winder

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
package com.willwinder.ugs.designer.logic;

import com.google.common.collect.Sets;
import com.willwinder.ugs.designer.actions.AddAction;
import com.willwinder.ugs.designer.actions.UndoManager;
import com.willwinder.ugs.designer.entities.entities.Entity;
import com.willwinder.ugs.designer.entities.entities.selection.SelectionManager;
import com.willwinder.ugs.designer.gui.Drawing;
import com.willwinder.ugs.designer.io.ugsd.UgsDesignReader;
import com.willwinder.ugs.designer.io.ugsd.UgsDesignWriter;
import com.willwinder.ugs.designer.model.Design;
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import com.willwinder.universalgcodesender.services.LookupService;

import javax.swing.SwingUtilities;
import java.awt.Cursor;
import java.awt.Window;
import java.io.File;
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
    private final Drawing drawing;
    private Tool tool;

    public Controller(SelectionManager selectionManager, UndoManager undoManager) {
        this.undoManager = undoManager;
        this.selectionManager = selectionManager;
        this.drawing = new Drawing(this);
        this.undoManager.addListener(this.drawing::repaint);

        setTool(Tool.SELECT);
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
        undoManager.clear();
        drawing.clear();
        notifyListeners(ControllerEventType.NEW_DRAWING);
    }

    private void notifyListeners(ControllerEventType event) {
        listeners.forEach(l -> l.onControllerEvent(event));
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
        getDrawing().repaint();
        setTool(Tool.SELECT);
    }

    public void setCursor(Cursor cursor) {
        drawing.setCursor(cursor);
    }

    public void loadFile(File file) {
        UgsDesignReader reader = new UgsDesignReader();
        Design design = reader.read(file).orElse(new Design());
        setDesign(design);
        reconcileToolOnLoad(design.getToolSnapshot());
    }

    private void reconcileToolOnLoad(ToolDefinition projectTool) {
        if (projectTool == null) {
            return;
        }
        ToolLibraryService library = LookupService.lookupOptional(ToolLibraryService.class).orElse(null);
        if (library == null) {
            // Headless mode or no library — apply the project's tool as a raw snapshot.
            applyToolSnapshot(projectTool);
            return;
        }
        Window parent = SwingUtilities.getWindowAncestor(drawing);
        ToolLibrarySyncService sync = new ToolLibrarySyncService(library);
        ToolDefinition resolved = sync.resolveOnLoad(parent, projectTool);
        if (resolved != null) {
            applyToolSnapshot(resolved);
        }
    }

    private void applyToolSnapshot(ToolDefinition tool) {
        settings.applySettings(tool.applyToSettings(settings));
    }

    public void saveFile(File file) {
        UgsDesignWriter writer = new UgsDesignWriter();
        writer.write(file, this);
    }
}
