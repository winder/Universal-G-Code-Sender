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
package com.willwinder.ugs.nbp.core.console;

import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.panels.CommandPanel;
import java.awt.BorderLayout;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.TopComponent;

/**
 * Top component which displays something.
 */
@TopComponent.Description(
        preferredID = "SerialConsoleTopComponent",
        //iconBase="SET/PATH/TO/ICON/HERE", 
        persistenceType = TopComponent.PERSISTENCE_ALWAYS
)
@TopComponent.Registration(mode = "output", openAtStartup = true)
@ActionID(category = LocalizingService.SerialConsoleCategory, id = LocalizingService.SerialConsoleActionId)
@ActionReference(path = LocalizingService.SerialConsoleWindowPath)
@TopComponent.OpenActionRegistration(
        displayName = "<Not localized:SerialConsoleTopComponent>",
        preferredID = "SerialConsoleTopComponent"
)
public final class SerialConsoleTopComponent extends TopComponent {

    private final CommandPanel commandPanel;
    private final BackendAPI backend;

    public SerialConsoleTopComponent() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        commandPanel = new CommandPanel(backend);
        this.setLayout(new BorderLayout());
        this.add(commandPanel, BorderLayout.CENTER);
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        commandPanel.requestFocus();
    }

    @Override
    public void componentOpened() {
        super.componentOpened();
        setName(LocalizingService.SerialConsoleTitle);
        setToolTipText(LocalizingService.SerialConsoleTooltip);

        backend.addUGSEventListener(commandPanel);
        backend.addMessageListener(commandPanel);
    }

    @Override
    protected void componentClosed() {
        super.componentClosed();
        backend.removeUGSEventListener(commandPanel);
        backend.removeMessageListener(commandPanel);
    }

    public void writeProperties(java.util.Properties p) {
    }

    public void readProperties(java.util.Properties p) {
    }
}