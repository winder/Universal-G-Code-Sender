package com.willwinder.ugs.nbp.designer.actions;

/**
 * This interface depicts a single undoable drawing action.
 */
public interface DrawAction {

    /**
     * This method performs the original action.
     */
    void execute();

    /**
     * This method redoes an action performed and recorded in an UndoManager.
     */
    void redo();

    /**
     * This method undoes an action performed and recorded in an UndoManager.
     */
    void undo();

}
