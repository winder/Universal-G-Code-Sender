package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.entities.selection.SelectionEvent;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionListener;
import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;

import javax.swing.SwingUtilities;

public abstract class JogMachineAbstractAction extends AbstractDesignAction implements SelectionListener, UGSEventListener {

    private final transient BackendAPI backend;

    protected JogMachineAbstractAction() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this);
        registerControllerListener();
        setEnabled(isEnabled());
    }

    private void registerControllerListener() {
        SelectionManager selectionManager = ControllerFactory.getController().getSelectionManager();
        selectionManager.addSelectionListener(this);
        setEnabled(isEnabled());
    }

    @Override
    public boolean isEnabled() {
        SelectionManager selectionManager = ControllerFactory.getController().getSelectionManager();
        boolean hasSelection = !selectionManager.getSelection().isEmpty();
        boolean isIdle = backend.getControllerState() == ControllerState.IDLE;
        return hasSelection && isIdle;
    }
    
    @Override
    public void onSelectionEvent(SelectionEvent selectionEvent) {
        setEnabled(isEnabled());
    }

    @Override
    public void UGSEvent(UGSEvent event) {
        if (event instanceof ControllerStateEvent) {
            SwingUtilities.invokeLater(() -> setEnabled(isEnabled()));
        }
    }
}
