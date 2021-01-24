package com.willwinder.ugs.nbp.designer.actions;


import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.gui.Drawing;
import com.willwinder.ugs.nbp.designer.logic.selection.SelectionManager;

/**
 * DeleteAction implements a single undoable action where all Shapes in a given
 * Selection are added to a Drawing.
 */
public class DeleteAction implements DrawAction {

	Drawing d;
	SelectionManager selection;

	int position;

	/**
	 * Creates an DeleteAction that removes all shapes in the given Selection
	 * from the given Drawing.
	 * 
	 * @param drawing
	 *            the drawing into which the shape should be added.
	 * @param selection
	 *            the shape to be added.
	 */
	public DeleteAction(Drawing drawing, SelectionManager selection) {
		// The selection need to be hard-copied because the selection behind the
		// reference will change while editing the drawing.
		this.selection = selection;
		this.d = drawing;
	}

	public void execute() {
		for (Entity s : selection.getShapes()) {
			d.removeShape(s);
		}
		selection.empty();
	}

	public void redo() {
		execute();
	}

	public void undo() {
		for (Entity s : selection.getShapes()) {
			d.insertShape(s);
		}
	}

}
