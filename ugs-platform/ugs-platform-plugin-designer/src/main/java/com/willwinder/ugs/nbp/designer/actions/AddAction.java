package com.willwinder.ugs.nbp.designer.actions;


import com.willwinder.ugs.nbp.designer.entities.AbstractEntity;
import com.willwinder.ugs.nbp.designer.gui.Drawing;

/**
 * AddAction implements a single undoable action where an entity is added to a
 * Drawing.
 */
public class AddAction implements DrawAction, UndoableAction {

	private Drawing drawing;
    private AbstractEntity shape;

	/**
	 * Creates an AddAction that adds the given Entity to the given Drawing.
	 * 
	 * @param drawing
	 *            the drawing into which the shape should be added.
	 * @param shape
	 *            the shape to be added.
	 */
	public AddAction(Drawing drawing, AbstractEntity shape) {
		this.drawing = drawing;
		this.shape = shape;
	}

	public void execute() {
		drawing.insertEntity(shape);
	}

	public void redo() {
		this.execute();
	}

	public void undo() {
		drawing.removeEntity(shape);
	}

}
