/*
    Copyright 2018 Will Winder

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
package com.willwinder.ugs.nbp.setupwizard;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

@ActionID(
        category = LocalizingService.CATEGORY_FILE,
        id = "com.willwinder.ugs.nbp.setupwizard.OpenSetupWizardAction")
@ActionRegistration(
        iconBase = OpenSetupWizardAction.ICON_BASE,
        displayName = "Setup wizard",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.MENU_MACHINE,
                position = 2010)
})
public class OpenSetupWizardAction extends AbstractAction {

    public static final String ICON_BASE = "icons/wizard.png";
    private BackendAPI backend;

    public OpenSetupWizardAction() {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue("menuText", "Setup wizard...");
        putValue(NAME, "Setup wizard");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        WizardStarter.openWizard(backend);
    }
}
