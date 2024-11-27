/*
    Copyright 2021-2024 Will Winder

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
import static com.willwinder.universalgcodesender.model.Axis.B;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;

@ActionID(
        category = LocalizingService.ResetBZeroCategory,
        id = LocalizingService.ResetBZeroActionId)
@ActionRegistration(
        iconBase = ResetBCoordinateToZeroAction.ICON_BASE,
        displayName = "resources/MessagesBundle#" + LocalizingService.ResetBZeroTitleKey,
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.ResetBZeroWindowPath,
                position = 1031)
})
public final class ResetBCoordinateToZeroAction extends AbstractAction implements UGSEventListener {

    public static final String ICON_BASE = "resources/icons/resetzero_b.svg";
    public static final String LARGE_ICON_PATH = "resources/icons/resetzero_b24.svg";

    private final BackendAPI backend;

    public ResetBCoordinateToZeroAction() {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        this.backend.addUGSEventListener(this);

        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
        putValue("menuText", LocalizingService.ResetBZeroTitle);
        putValue(NAME, LocalizingService.ResetBZeroTitle);
        putValue(SHORT_DESCRIPTION, LocalizingService.ResetBZeroTitle);
        setEnabled(isEnabled());
    }

    @Override
    public void UGSEvent(UGSEvent cse) {
        if (cse instanceof ControllerStateEvent) {
            java.awt.EventQueue.invokeLater(() -> setEnabled(isEnabled()));
        }
    }

    @Override
    public boolean isEnabled() {
        return backend.isIdle() &&
                backend.getControllerState() == ControllerState.IDLE;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            backend.resetCoordinateToZero(B);
        } catch (Exception ex) {
            GUIHelpers.displayErrorDialog(ex.getLocalizedMessage());
        }
    }
}
