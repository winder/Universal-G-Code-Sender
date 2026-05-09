/*
    Copyright 2026 Damian Nikodem

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

import com.willwinder.ugs.nbp.lib.Mode;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.loaders.DataObject;
import org.openide.util.ImageUtilities;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Closes the currently open editor/program file.
 *
 * @author Damian Nikodem
 */
@ActionID(
        category = LocalizingService.CATEGORY_FILE,
        id = "com.willwinder.ugs.nbp.core.actions.CloseFileAction")
@ActionRegistration(
        displayName = "Close",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.MENU_FILE,
                position = 250)
})
public class CloseFileAction extends AbstractAction {
    public static final String ICON_BASE = "resources/icons/close.svg";
    private static final String WELCOME_PAGE_CLASS = "com.willwinder.ugp.welcome.WelcomePageTopComponent";
    private static final String GCODE_MIME_TYPE = "text/xgcode";
    private static final String DESIGN_MIME_TYPE = "application/x-ugs";

    public CloseFileAction() {
        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue(NAME, "Close");
        putValue("noIconInMenu", Boolean.FALSE);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        TopComponent activated = TopComponent.getRegistry().getActivated();
        if (activated != null && WELCOME_PAGE_CLASS.equals(activated.getClass().getName()) && activated.close()) {
            return;
        }

        TopComponent selectedEditor = getSelectedEditorTopComponent();
        if (selectedEditor != null && isCloseableDocument(selectedEditor)) {
            selectedEditor.close();
        }
    }

    private boolean isCloseableDocument(TopComponent topComponent) {
        if (WELCOME_PAGE_CLASS.equals(topComponent.getClass().getName())) {
            return true;
        }

        DataObject dataObject = topComponent.getLookup().lookup(DataObject.class);
        if (dataObject == null) {
            return false;
        }

        String mimeType = dataObject.getPrimaryFile().getMIMEType();
        return GCODE_MIME_TYPE.equals(mimeType) || DESIGN_MIME_TYPE.equals(mimeType);
    }

    private TopComponent getSelectedEditorTopComponent() {
        TopComponent primaryEditor = getSelectedTopComponentForMode(Mode.EDITOR_PRIMARY);
        if (primaryEditor != null && isCloseableDocument(primaryEditor)) {
            return primaryEditor;
        }

        TopComponent secondaryEditor = getSelectedTopComponentForMode(Mode.EDITOR_SECONDARY);
        if (secondaryEditor != null && isCloseableDocument(secondaryEditor)) {
            return secondaryEditor;
        }

        return null;
    }

    private TopComponent getSelectedTopComponentForMode(String modeName) {
        org.openide.windows.Mode mode = WindowManager.getDefault().findMode(modeName);
        if (mode == null) {
            return null;
        }

        return mode.getSelectedTopComponent();
    }
}
