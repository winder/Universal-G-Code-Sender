/*
    Copyright 2023 Will Winder

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
package com.willwinder.ugs.platform.probe.actions;

import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.ugs.platform.probe.ProbeTopComponent;
import com.willwinder.universalgcodesender.i18n.Localization;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.windows.WindowManager;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;

/**
 * Opens the probe module and highlights the settings tab.
 *
 * @author Joacim Breiler
 */
@ActionID(
        category = LocalizingService.CATEGORY_MACHINE,
        id = "com.willwinder.ugs.platform.probe.actions.OpenProbeSettingsAction")
@ActionRegistration(
        displayName = "Settings",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.MENU_MACHINE_PROBE,
                position = 50,
                separatorBefore = 49)})
public class OpenProbeSettingsAction extends AbstractAction {

    public OpenProbeSettingsAction() {
        putValue("menuText", Localization.getString("probe.action.settings"));
        putValue(NAME, Localization.getString("probe.action.settings"));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ProbeTopComponent outputWindow = (ProbeTopComponent) WindowManager.getDefault().findTopComponent(ProbeTopComponent.preferredId);
        outputWindow.open();
        outputWindow.requestActive();
        outputWindow.selectSettingsTab();
    }
}
