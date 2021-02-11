package com.willwinder.ugs.nbp.designer.actions;

public interface UndoableAction {
    /**
     * This method redoes an action performed and recorded in an UndoManager.
     */
    void redo();

    /**
     * This method undoes an action performed and recorded in an UndoManager.
     */
    void undo();
}
