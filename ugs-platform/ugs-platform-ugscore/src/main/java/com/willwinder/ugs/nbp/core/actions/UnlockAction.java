/*
    Copyright 2015-2018 Will Winder


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
package com.willwinder.ugs.nbp.core.actions;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;

@ActionID(
        category = LocalizingService.UnlockCategory,
        id = LocalizingService.UnlockActionId)
@ActionRegistration(
        iconBase = UnlockAction.ICON_BASE,
        displayName = "resources.MessagesBundle#" + LocalizingService.UnlockTitleKey,
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = "Toolbars/Machine Actions",
                position = 981),
        @ActionReference(
                path = LocalizingService.UnlockWindowPath,
                position = 1020)
})
public final class UnlockAction extends AbstractAction implements UGSEventListener {

    public static final String ICON_BASE = "resources/icons/lock.svg";

    private BackendAPI backend;

    public UnlockAction() {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        this.backend.addUGSEventListener(this);
        this.backend.addControllerStateListener(this::UGSEvent);

        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue("menuText", LocalizingService.UnlockTitle);
        putValue(NAME, LocalizingService.UnlockTitle);
        setEnabled(isEnabled());
    }

    @Override
    public void UGSEvent(UGSEvent cse) {
        if (cse.isControllerStatusEvent()) {
            java.awt.EventQueue.invokeLater(() -> setEnabled(isEnabled()));
        }

        if (cse.isStateChangeEvent()) {
            java.awt.EventQueue.invokeLater(() -> setEnabled(isEnabled()));
        }
    }

    @Override
    public boolean isEnabled() {
        return backend.isIdle() &&
                backend.getController() != null &&
                backend.getController().getControllerStatus() != null &&
                backend.getController().getControllerStatus().getState() == ControllerState.ALARM;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            backend.killAlarmLock();
        } catch (Exception ex) {
            GUIHelpers.displayErrorDialog(ex.getLocalizedMessage());
        }
    }
}
