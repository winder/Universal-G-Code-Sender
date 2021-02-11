package com.willwinder.ugs.nbp.designer.actions;

import org.openide.awt.UndoRedo;

import javax.swing.event.ChangeListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;

public class UndoManagerAdapter implements UndoRedo {

    private final UndoManager undoManager;

    public UndoManagerAdapter(UndoManager undoManager) {
        this.undoManager = undoManager;
    }

    @Override
    public boolean canUndo() {
        return undoManager.canUndo();
    }

    @Override
    public boolean canRedo() {
        return undoManager.canRedo();
    }

    @Override
    public void undo() throws CannotUndoException {
        undoManager.undo();
    }

    @Override
    public void redo() throws CannotRedoException {
        undoManager.redo();
    }

    @Override
    public void addChangeListener(ChangeListener changeListener) {

    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {

    }

    @Override
    public String getUndoPresentationName() {
        return undoManager.getUndoPresentationName();
    }

    @Override
    public String getRedoPresentationName() {
        return undoManager.getRedoPresentationName();
    }
}
