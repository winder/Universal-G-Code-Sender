package com.willwinder.ugs.nbp.designer.logic.actions;

import java.util.HashSet;
import java.util.Set;
import java.util.Stack;

/**
 * UndoManager is a simplistic reusable component to support an undo-redo
 * mechanism. UndoableActions can be added in the manager, which gives a
 * centered interface for performing their undo and redo actions.
 *
 * @author Alex Lagerstedt
 */
public class SimpleUndoManager implements UndoManager {

    // Undo and redo stacks which contain the UndoableAction objects
    // When a new action is made it is put in the undo stack. When an operation
    // is undone, it is places in the redo stack.

    private Stack<UndoableAction> undoStack;
    private Stack<UndoableAction> redoStack;
    private Set<UndoManagerListener> listeners;

    /**
     * Constructs a empty Undo Manager.
     */
    public SimpleUndoManager() {
        undoStack = new Stack<>();
        redoStack = new Stack<>();
        listeners = new HashSet<>();
    }

    /**
     * Adds a new undoable action into this Undo Manager.
     *
     * @param action the UndoableAction to be added.
     */
    @Override
    public void addAction(UndoableAction action) {
        redoStack.clear();
        undoStack.push(action);
        listeners.forEach(UndoManagerListener::onChanged);
    }

    /**
     * Tests if an redo operation can be performed at this moment.
     *
     * @return boolean value telling if a redo is possible to perform.
     */
    @Override
    public boolean canRedo() {
        return !redoStack.isEmpty();
    }

    /**
     * Tests if an undo operation can be performed at this moment.
     *
     * @return boolean value telling if an undo is possible to perform.
     */
    @Override
    public boolean canUndo() {
        return !undoStack.isEmpty();
    }

    /**
     * Redoes one action from the redo stack. The redone action then is moved to
     * the undo stack.
     */
    @Override
    public void redo() {
        if (canRedo()) {
            UndoableAction action = redoStack.pop();
            action.redo();
            undoStack.push(action);
            listeners.forEach(UndoManagerListener::onChanged);
        }
    }

    /**
     * Undoes one action from the undo stack. The undone action then is moved to
     * the undo stack.
     */
    @Override
    public void undo() {
        if (canUndo()) {
            UndoableAction action = undoStack.pop();
            action.undo();
            redoStack.push(action);
            listeners.forEach(UndoManagerListener::onChanged);
        }
    }

    @Override
    public String getUndoPresentationName() {
        if (!canUndo()) {
            return "";
        }
        return undoStack.peek().toString();
    }

    @Override
    public String getRedoPresentationName() {
        if (!canRedo()) {
            return "";
        }
        return redoStack.peek().toString();
    }

    @Override
    public void addListener(UndoManagerListener undoListener) {
        listeners.add(undoListener);
    }

    @Override
    public void clear() {
        undoStack.clear();
        redoStack.clear();
        listeners.forEach(UndoManagerListener::onChanged);
    }
}
