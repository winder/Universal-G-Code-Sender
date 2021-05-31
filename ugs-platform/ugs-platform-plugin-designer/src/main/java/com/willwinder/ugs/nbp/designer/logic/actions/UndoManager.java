package com.willwinder.ugs.nbp.designer.logic.actions;

import com.willwinder.ugs.nbp.designer.DesignerTopComponent;

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
