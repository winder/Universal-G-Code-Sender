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
package com.willwinder.ugs.nbp.filebrowser.actions;

import com.willwinder.ugs.nbp.filebrowser.FileBrowserDialog;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.CapabilitiesConstants;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import javax.swing.SwingUtilities;
import java.awt.Component;
import java.awt.event.ActionEvent;

@ActionID(
        category = LocalizingService.CATEGORY_MACHINE,
        id = "OpenFileBrowserAction")
@ActionRegistration(
        iconBase = "img/new.svg",
        displayName = "File browser",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.MENU_MACHINE,
                position = 2005),
})
public final class OpenFileBrowserAction extends AbstractAction implements UGSEventListener {

    public static final String SMALL_ICON_PATH = "icons/foldertree.svg";
    public static final String LARGE_ICON_PATH = "icons/foldertree32.svg";
    private BackendAPI backend;

    public OpenFileBrowserAction() {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
        putValue("menuText", "File browser");
        putValue(NAME, "File browser");

        setEnabled(isEnabled());
        backend.addUGSEventListener(this);
    }

    @Override
    public boolean isEnabled() {
        ControllerState state = backend.getControllerState();
        return backend.isConnected() &&
                (state == ControllerState.IDLE || state == ControllerState.ALARM) &&
                backend.getController().getCapabilities().hasCapability(CapabilitiesConstants.FILE_SYSTEM);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        BackendAPI backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
        FileBrowserDialog dialog = new FileBrowserDialog(backendAPI.getController().getFileService());
        dialog.setLocationRelativeTo(SwingUtilities.getWindowAncestor((Component) e.getSource()));
        ThreadHelper.invokeLater(dialog::showDialog);
    }

    @Override
    public void UGSEvent(UGSEvent event) {
        if (event instanceof ControllerStateEvent) {
            boolean enabled = isEnabled();
            SwingUtilities.invokeLater(() -> setEnabled(enabled));
        }
    }
}
