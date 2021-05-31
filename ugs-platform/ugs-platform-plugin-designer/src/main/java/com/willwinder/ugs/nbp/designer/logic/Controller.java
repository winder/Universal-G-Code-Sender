package com.willwinder.ugs.nbp.designer.logic;

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.logic.actions.AddAction;
import com.willwinder.ugs.nbp.designer.logic.actions.UndoManager;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

public class Controller {

    private final SelectionManager selectionManager;
    private Drawing drawing;
    private Tool tool;
    private Settings settings = new Settings();
    private Set<ControllerListener> listeners = new HashSet<>();
    private UndoManager undoManager;


    public Controller(SelectionManager selectionManager, UndoManager undoManager) {
        this.undoManager = undoManager;
        this.tool = Tool.SELECT;
        this.selectionManager = selectionManager;
        this.drawing = new Drawing(selectionManager);
        this.undoManager.addListener(() -> this.drawing.repaint());
    }

    public void addEntity(Entity s) {
        AddAction add = new AddAction(this, s);
        add.execute();
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
        drawing = new Drawing(selectionManager);
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
}
