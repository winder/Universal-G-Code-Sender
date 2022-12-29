/*
    Copyright 2022 Will Winder

    This file is part of Universal Gcode Sender (UGS).

    UGS is free software: you can redistribute it and/or modify
    it under the terms of the GNU General Public License as published by
    the Free Software Foundation, either version 3 of the License, or
    (at your option) any later version.

    UGS is distributed in the hope that it will be useful,
    but WITHOUT ANY WARRANTY; without even the implied warranty of
    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
    GNU General Public License for more details.

    You should have received a copy of the GNU General Public License
    along with UGS.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.nbp.console.actions;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.SettingChangedEvent;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * @author Joacim Breiler
 */
public class EnableDisableVerboseAction extends AbstractAction implements UGSEventListener {

    private final transient BackendAPI backend;

    public EnableDisableVerboseAction() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this);
        setEnabled(true);
        updateName();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        backend.getSettings().setVerboseOutputEnabled(!backend.getSettings().isVerboseOutputEnabled());
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof SettingChangedEvent) {
            updateName();
        }
    }

    private void updateName() {
        if (backend.getSettings().isVerboseOutputEnabled()) {
            putValue(NAME, Localization.getString("platform.plugin.console.action.verbose.disable"));
        } else {
            putValue(NAME, Localization.getString("platform.plugin.console.action.verbose.enable"));
        }
    }
}
