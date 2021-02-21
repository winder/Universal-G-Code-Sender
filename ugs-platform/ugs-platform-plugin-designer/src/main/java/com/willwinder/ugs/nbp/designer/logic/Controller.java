package com.willwinder.ugs.nbp.designer.logic;

import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.gui.entities.AbstractEntity;
import com.willwinder.ugs.nbp.designer.gui.entities.Entity;
import com.willwinder.ugs.nbp.designer.logic.actions.AddAction;
import com.willwinder.ugs.nbp.designer.logic.actions.UndoManager;
import com.willwinder.ugs.nbp.designer.logic.selection.SelectionManager;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;

import java.util.HashSet;
import java.util.Set;

public class Controller {

    private final SelectionManager selectionManager;
    private final UndoManager undoManager;
    private Drawing drawing;
    private Tool tool;
    private Settings settings = new Settings();
    private Set<ControllerListener> listeners = new HashSet<>();


    public Controller() {

        tool = Tool.SELECT;
        selectionManager = CentralLookup.getDefault().lookup(SelectionManager.class);
        drawing = new Drawing();

        // Refresh the drawing when something has been undone or redone
        undoManager = CentralLookup.getDefault().lookup(UndoManager.class);
        undoManager.addListener(() -> getDrawing().repaint());
    }

    public void addEntity(Entity s) {
        AddAction add = new AddAction(drawing, s);
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
        drawing = new Drawing();
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


    public void setScale(double scale) {
        drawing.setScale(scale);
        drawing.repaint();
    }

    public UndoManager getUndoManager() {
        return undoManager;
    }
}
