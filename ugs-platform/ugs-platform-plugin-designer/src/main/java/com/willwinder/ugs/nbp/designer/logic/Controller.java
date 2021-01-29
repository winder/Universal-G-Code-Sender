package com.willwinder.ugs.nbp.designer.logic;

import com.willwinder.ugs.nbp.designer.actions.*;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.logic.events.ControllerEventType;
import com.willwinder.ugs.nbp.designer.logic.events.ControllerListener;
import com.willwinder.ugs.nbp.designer.selection.SelectionManager;

import java.util.ArrayList;
import java.util.List;

public class Controller {

	private Drawing drawing;
	private UndoManager undoManager;
	private Tool tool;
	private Settings settings = new Settings();
    private List<ControllerListener> listeners = new ArrayList<>();
    private final SelectionManager selectionManager = new SelectionManager();


    public Controller() {
		drawing = null;
		undoManager = new SimpleUndoManager();
		tool = Tool.LINE;
		drawing = new Drawing();
	}

	public void addShape(Entity s) {
		DrawAction add = new AddAction(drawing, s);
		add.execute();
		undoManager.addAction(add);
	}

	public void deleteSelectedShapes() {
		if (!getSelectionManager().getShapes().isEmpty()) {
			DrawAction del = new DeleteAction(drawing, getSelectionManager().getShapes());
			del.execute();
			undoManager.addAction(del);
			drawing.repaint();
			getSelectionManager().empty();
		}
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

	public void newDrawing() {
		drawing = new Drawing();
		notifyListeners(ControllerEventType.NEW_DRAWING);
	}

    private void notifyListeners(ControllerEventType event) {
    	listeners.forEach(l -> l.onControllerEvent(event));
    }

	public void redo() {
		if (this.undoManager.canRedo()) {
			this.undoManager.redo();
		}
		drawing.repaint();
	}

	public void selectAll() {
        selectionManager.empty();
		for (Entity sh : drawing) {
            selectionManager.add(sh);
		}
		drawing.repaint();

	}

	public void setTool(Tool t) {
		this.tool = t;
	}

	public void undo() {
		if (this.undoManager.canUndo()) {
			this.undoManager.undo();
		}
		drawing.repaint();
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
}
