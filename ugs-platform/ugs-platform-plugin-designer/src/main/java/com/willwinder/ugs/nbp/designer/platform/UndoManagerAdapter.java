package com.willwinder.ugs.nbp.designer.platform;

import com.google.common.collect.Sets;
import com.willwinder.ugs.nbp.designer.actions.UndoManager;
import org.openide.awt.UndoRedo;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.undo.CannotRedoException;
import javax.swing.undo.CannotUndoException;
import java.util.Set;

public class
UndoManagerAdapter implements UndoRedo {

    private final UndoManager undoManager;
    private final Set<ChangeListener> listeners = Sets.newConcurrentHashSet();

    public UndoManagerAdapter(UndoManager undoManager) {
        this.undoManager = undoManager;

        undoManager.addListener(() ->
                listeners.forEach(l ->
                        SwingUtilities.invokeLater(() -> {
                            l.stateChanged(new ChangeEvent(this));
                        })));
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
        listeners.add(changeListener);
    }

    @Override
    public void removeChangeListener(ChangeListener changeListener) {
        listeners.remove(changeListener);
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
