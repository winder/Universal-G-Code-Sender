package com.willwinder.ugs.nbp.designer.actions;

public interface UndoManager {
    void addAction(UndoableAction action);

    boolean canRedo();

    boolean canUndo();

    void redo();

    void undo();

    String getUndoPresentationName();

    String getRedoPresentationName();

    void addListener(UndoManagerListener undoListener);

    void clear();

    void removeListener(UndoManagerListener undoListener);
}
