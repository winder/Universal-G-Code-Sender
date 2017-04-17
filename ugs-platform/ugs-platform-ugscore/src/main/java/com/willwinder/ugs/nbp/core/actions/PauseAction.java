/*
    Copywrite 2015-2016 Will Winder

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
import com.willwinder.universalgcodesender.i18n.Localization;
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
        category = "Machine",
        id = "com.willwinder.ugs.nbp.core.actions.PauseAction")
@ActionRegistration(
        iconBase = PauseAction.ICON_BASE,
        displayName = "#" + PauseAction.TITLE_LOCALIZATION_KEY,
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = "Menu/Machine",
                position = 1015),
        @ActionReference(
                path = "Toolbars/StartPauseStop",
                position = 1015)
})
public final class PauseAction extends AbstractAction implements UGSEventListener {

    // Icons: http://www.customicondesign.com/free-icons/flatastic-icon-set/
    public static final String ICON_BASE = "resources/icons/pause.png";
    public static final String TITLE_LOCALIZATION_KEY = "mainWindow.swing.pauseButton";
    private BackendAPI backend;

    public PauseAction() {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        this.backend.addUGSEventListener(this);

        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue("menuText", Localization.getString(TITLE_LOCALIZATION_KEY));
        putValue(NAME, Localization.getString(TITLE_LOCALIZATION_KEY));
    }

    @Override
    public void UGSEvent(UGSEvent cse) {
        java.awt.EventQueue.invokeLater(() -> setEnabled(isEnabled()));
    }

    @Override
    public boolean isEnabled() {
        return backend != null && backend.canPause();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            backend.pauseResume();
        } catch (Exception ex) {
            GUIHelpers.displayErrorDialog(ex.getLocalizedMessage());
        }
    }
}
