package com.willwinder.ugs.nbp.designer.actions;

public interface UndoManager {
    void addAction(DrawAction action);

    boolean canRedo();

    boolean canUndo();

    void redo();

    void undo();
}
