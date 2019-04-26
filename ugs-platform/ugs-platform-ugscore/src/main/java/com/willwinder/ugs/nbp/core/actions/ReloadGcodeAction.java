/*
    Copywrite 2015-2018 Will Winder

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
import java.util.logging.Logger;

@ActionID(
        category = LocalizingService.ReloadGcodeCategory,
        id = LocalizingService.ReloadGcodeActionId)
@ActionRegistration(
        iconBase = ReloadGcodeAction.ICON_BASE,
        displayName = "resources.MessagesBundle#" + LocalizingService.ReloadGcodeTitleKey,
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.ReloadGcodeWindowPath,
                position = 11),
        @ActionReference(
                path = "Toolbars/File",
                position = 11),
        @ActionReference(
                path = "Shortcuts",
                name = "M-R")
})
public final class ReloadGcodeAction extends AbstractAction implements UGSEventListener {

    public static final String ICON_BASE = "resources/icons/reload.png";
    private static final Logger logger = Logger.getLogger(ReloadGcodeAction.class.getName());

    private BackendAPI backend;

    public ReloadGcodeAction() {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        this.backend.addUGSEventListener(this);

        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue("menuText", LocalizingService.ReloadGcodeTitle);
        putValue(NAME, LocalizingService.ReloadGcodeTitle);

        // set initial state to disabled, because otherwise the enabled state in the toolbar does not get correctly
        // set after the first enable
        setEnabled(isEnabled());
    }

    @Override
    public boolean isEnabled() {
        return backend != null && backend.getGcodeFile() != null && backend.getGcodeFile().exists();
    }

    @Override
    public void UGSEvent(UGSEvent cse) {
        if (cse.isFileChangeEvent()) {
            java.awt.EventQueue.invokeLater(() -> setEnabled(isEnabled()));
        }
    }


    @Override
    public void actionPerformed(ActionEvent e) {
        if (backend.getGcodeFile() != null && backend.getGcodeFile().exists()) {
            GUIHelpers.openGcodeFile(backend.getGcodeFile(), backend);
        }
    }
}
