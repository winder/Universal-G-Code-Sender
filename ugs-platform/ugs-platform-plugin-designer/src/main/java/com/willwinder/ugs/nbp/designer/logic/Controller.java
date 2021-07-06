package com.willwinder.ugs.nbp.designer.logic;

import com.willwinder.ugs.nbp.designer.actions.AddAction;
import com.willwinder.ugs.nbp.designer.actions.UndoManager;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.ugs.nbp.designer.model.Settings;

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
        this.drawing = new Drawing(this);
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
        drawing = new Drawing(this);
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
        design.getEntities().forEach(getDrawing()::insertEntity);

        // We want to update per new setting to ensure that event listeners will be notified
        if (design.getSettings() != null) {
            settings.setStockSize(design.getSettings().getStockSize());
            settings.setDepthPerPass(design.getSettings().getDepthPerPass());
            settings.setFeedSpeed(design.getSettings().getFeedSpeed());
            settings.setPlungeSpeed(design.getSettings().getPlungeSpeed());
            settings.setStockThickness(design.getSettings().getStockThickness());
            settings.setToolDiameter(design.getSettings().getToolDiameter());
            settings.setToolStepOver(design.getSettings().getToolStepOver());
            settings.setPreferredUnits(design.getSettings().getPreferredUnits());
            settings.setSafeHeight(design.getSettings().getSafeHeight());
        }

        getDrawing().repaint();
    }
}
