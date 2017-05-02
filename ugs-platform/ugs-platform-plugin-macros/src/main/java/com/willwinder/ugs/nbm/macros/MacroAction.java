package com.willwinder.ugs.nbm.macros;

import com.willwinder.universalgcodesender.MacroHelper;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.types.Macro;
import org.openide.util.Exceptions;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class MacroAction extends AbstractAction {
        private BackendAPI backend;
        private Macro macro;

        public MacroAction(BackendAPI backend, Macro macro) {
            this.backend = backend;
            this.macro = macro;
            putValue(NAME, macro.getName());
            putValue("menuText", macro.getName());
            putValue("displayName", macro.getName());
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            try {
                if (macro != null && macro.getGcode() != null) {
                    MacroHelper.executeCustomGcode(macro.getGcode(), backend);
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        @Override
        public boolean isEnabled() {
            return backend.isConnected() && backend.isIdle();
        }
    }