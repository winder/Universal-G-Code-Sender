package com.willwinder.ugs.designer.actions;

import java.util.Stack;

/**
 * UndoManager is a simplistic reusable component to support an undo-redo
 * mechanism. UndoableActions can be added in the manager, which gives a
 * centered interface for performing their undo and redo actions.
 */
public class UndoManager {

	// Undo and redo stacks which contain the UndoableAction objects
	// When a new action is made it is put in the undo stack. When an operation
	// is undone, it is places in the redo stack.

	private Stack<DrawAction> undoStack;
	private Stack<DrawAction> redoStack;

	/**
	 * Constructs a empty Undo Manager.
	 */
	public UndoManager() {
		this.undoStack = new Stack<>();
		this.redoStack = new Stack<>();
	}

	/**
	 * Adds a new undoable action into this Undo Manager.
	 * 
	 * @param action
	 *            the UndoableAction to be added.
	 */
	public void addAction(DrawAction action) {
		this.redoStack.clear();
		this.undoStack.push(action);
		System.out.println(action);
	}

	/**
	 * Tests if an redo operation can be performed at this moment.
	 * 
	 * @return boolean value telling if a redo is possible to perform.
	 */

	public boolean canRedo() {
		return !this.redoStack.isEmpty();
	}

	/**
	 * Tests if an undo operation can be performed at this moment.
	 * 
	 * @return boolean value telling if an undo is possible to perform.
	 */
	public boolean canUndo() {
		return !this.undoStack.isEmpty();
	}

	/**
	 * Redoes one action from the redo stack. The redone action then is moved to
	 * the undo stack.
	 */
	public void redo() {
		DrawAction action = this.redoStack.pop();
		action.redo();
		this.undoStack.push(action);
	}

	/**
	 * Undoes one action from the undo stack. The undone action then is moved to
	 * the undo stack.
	 */
	public void undo() {
		DrawAction action = this.undoStack.pop();
		action.undo();
		this.redoStack.push(action);
	}
}
