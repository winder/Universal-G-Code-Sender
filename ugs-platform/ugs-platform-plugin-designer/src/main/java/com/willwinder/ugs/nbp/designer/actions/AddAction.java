package com.willwinder.ugs.nbp.designer.actions;


import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.gui.Drawing;

/**
 * AddAction implements a single undoable action where a Shape is added to a
 * Drawing.
 */
public class AddAction implements DrawAction {

	private Drawing drawing;
    private Entity shape;

	/**
	 * Creates an AddAction that adds the given Shape to the given Drawing.
	 * 
	 * @param drawing
	 *            the drawing into which the shape should be added.
	 * @param shape
	 *            the shape to be added.
	 */
	public AddAction(Drawing drawing, Entity shape) {
		this.drawing = drawing;
		this.shape = shape;
	}

	public void execute() {
		drawing.insertShape(shape);
	}

	public void redo() {
		this.execute();
	}

	public void undo() {
		drawing.removeShape(shape);
	}

}
