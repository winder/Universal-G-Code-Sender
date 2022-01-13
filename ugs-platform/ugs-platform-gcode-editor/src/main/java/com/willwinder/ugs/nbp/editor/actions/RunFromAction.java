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
package com.willwinder.ugs.nbp.editor.actions;

import com.willwinder.ugs.nbp.editor.GcodeDataObject;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.services.RunFromService;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.awt.ActionReferences;
import org.openide.awt.ActionRegistration;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.actions.CookieAction;

import javax.swing.*;
import java.awt.*;

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
                position = 1016)
})
public final class RunFromAction extends CookieAction implements UGSEventListener {

    public static final String NAME = LocalizingService.RunFromTitle;
    public static final String ICON_BASE = "icons/fast-forward.svg";
    private final transient  RunFromService runFromService;
    private final transient BackendAPI backend;

    public RunFromAction() {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        this.backend.addUGSEventListener(this);
        this.runFromService = CentralLookup.getDefault().lookup(RunFromService.class);
        setEnabled(isEnabled());
    }

    @Override
    public void UGSEvent(UGSEvent cse) {
        if (cse instanceof ControllerStateEvent) {
            EventQueue.invokeLater(() -> setEnabled(isEnabled()));
        }
    }

    @Override
    public boolean isEnabled() {
        return backend.getGcodeFile() != null && backend.isConnected() && !backend.isSendingFile() && super.isEnabled();
    }


    @Override
    public String getName() {
        return NAME;
    }

    @Override
    public HelpCtx getHelpCtx() {
        return null;
    }

    @Override
    protected void performAction(Node[] activatedNodes) {
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

    @Override
    protected int mode() {
        return CookieAction.MODE_ANY;
    }

    @Override
    protected Class<?>[] cookieClasses() {
        return new Class[]{GcodeDataObject.class};
    }

    @Override
    protected String iconResource() {
        return ICON_BASE;
    }
}
