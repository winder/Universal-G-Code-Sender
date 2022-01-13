package com.willwinder.ugs.nbp.core.control;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.MacroHelper;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.types.Macro;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import org.openide.util.Exceptions;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.Serializable;

public class MacroAction extends AbstractAction implements Serializable {
    private transient BackendAPI backend;
    private Macro macro;

    /**
     * Empty constructor to be used for serialization
     */
    public MacroAction() {
    }

    public MacroAction(Macro macro) {
        this.macro = macro;
        this.backend = getBackend();
    }

    private BackendAPI getBackend() {
        if (backend == null) {
            backend = CentralLookup.getDefault().lookup(BackendAPI.class);
            backend.addUGSEventListener(this::onEvent);
        }
        return backend;
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof ControllerStateEvent) {
           setEnabled(isEnabled());
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (macro != null && macro.getGcode() != null) {
            EventQueue.invokeLater(() -> {
                try {
                    MacroHelper.executeCustomGcode(macro.getGcode(), getBackend());
                } catch (Exception ex) {
                    GUIHelpers.displayErrorDialog(ex.getMessage());
                    Exceptions.printStackTrace(ex);
                }
            });
        }
    }

    @Override
    public boolean isEnabled() {
        return getBackend().getControllerState() == ControllerState.IDLE;
    }
}