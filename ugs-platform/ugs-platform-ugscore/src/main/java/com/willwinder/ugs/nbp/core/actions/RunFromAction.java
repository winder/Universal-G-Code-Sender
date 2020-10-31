/*
    Copyright 2020 Will Winder


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
import com.willwinder.universalgcodesender.services.RunFromService;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import java.awt.event.ActionEvent;

@ActionID(
        category = LocalizingService.RunFromCategory,
        id = LocalizingService.RunFromActionId)
@ActionRegistration(
        iconBase = RunFromAction.ICON_BASE,
        displayName = "Run from...",
        lazy = false)
@ActionReferences({
        @ActionReference(
                path = LocalizingService.RunFromWindowPath,
                position = 1017)
})
public final class RunFromAction extends AbstractAction implements UGSEventListener {

    public static final String ICON_BASE = "resources/icons/fast-forward.svg";
    private final RunFromService runFromService;

    private BackendAPI backend;

    public RunFromAction() {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        this.backend.addUGSEventListener(this);
        this.runFromService = CentralLookup.getDefault().lookup(RunFromService.class);


        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue("menuText", LocalizingService.RunFromTitle);
        putValue(NAME, LocalizingService.RunFromTitle);
        setEnabled(isEnabled());
    }

    @Override
    public void UGSEvent(UGSEvent cse) {
        if (cse.isStateChangeEvent()) {
            java.awt.EventQueue.invokeLater(() -> setEnabled(isEnabled()));
        }
    }

    @Override
    public boolean isEnabled() {
        return backend.getGcodeFile() != null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled()) {
            return;
        }

        try {
            Integer result = Integer.parseInt(
                    JOptionPane.showInputDialog(
                            new JFrame(),
                            "Enter a line number to start from.",
                            "Run From Action",
                            JOptionPane.QUESTION_MESSAGE
                    ));

            runFromService.runFromLine(result);
        } catch (Exception ex) {
            GUIHelpers.displayErrorDialog(ex.getLocalizedMessage());
        }
    }
}
