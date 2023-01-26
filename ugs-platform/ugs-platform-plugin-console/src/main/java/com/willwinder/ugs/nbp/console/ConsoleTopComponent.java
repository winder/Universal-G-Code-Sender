/*
    Copyright 2022-2023 Will Winder

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
package com.willwinder.ugs.nbp.console;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.MessageListener;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.uielements.components.CommandTextArea;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.IOColorPrint;
import org.openide.windows.IOColors;
import org.openide.windows.InputOutput;
import org.openide.windows.TopComponent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;

/**
 * A window that displays a console log for UGS
 *
 * @author Joacim Breiler
 */
@TopComponent.Description(
        preferredID = "ConsoleTopComponent"
)
@TopComponent.Registration(
        mode = "output",
        openAtStartup = true
)
@ActionID(
        category = LocalizingService.CATEGORY_WINDOW,
        id = "com.willwinder.ugs.nbp.console.ConsoleTopComponent"
)
@ActionReference(
        path = LocalizingService.MENU_WINDOW
)
@TopComponent.OpenActionRegistration(
        displayName = "resources.MessagesBundle#platform.plugin.console.title",
        preferredID = "ConsoleTopComponent"
)
public final class ConsoleTopComponent extends TopComponent {

    private CommandTextArea commandTextArea;
    private ConsolePanel consolePanel;

    public ConsoleTopComponent() {
        setLayout(new BorderLayout());
        setName(Localization.getString("platform.plugin.console.title"));
        setToolTipText(LocalizingService.LocationStatusTooltip);
    }

    @Override
    public void componentOpened() {
        removeAll();
        BackendAPI backendAPI = CentralLookup.getDefault().lookup(BackendAPI.class);
        consolePanel = new ConsolePanel(backendAPI);
        add(consolePanel, BorderLayout.CENTER);
        commandTextArea = new CommandTextArea(backendAPI);
        add(commandTextArea, BorderLayout.SOUTH);
    }

    @Override
    protected void componentActivated() {
        super.componentActivated();
        commandTextArea.requestFocus();
    }

    @Override
    public void componentClosed() {
        consolePanel.close();
    }
}
