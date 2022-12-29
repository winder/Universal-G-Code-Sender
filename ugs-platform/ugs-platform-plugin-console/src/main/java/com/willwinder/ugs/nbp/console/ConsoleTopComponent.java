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
package com.willwinder.ugs.nbp.console;

import com.willwinder.ugs.nbp.console.actions.EnableDisableVerboseAction;
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
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionReference;
import org.openide.windows.FoldHandle;
import org.openide.windows.IOColorPrint;
import org.openide.windows.IOColors;
import org.openide.windows.IOContainer;
import org.openide.windows.IOFolding;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;
import org.openide.windows.TopComponent;

import javax.swing.Action;
import javax.swing.text.JTextComponent;
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
public final class ConsoleTopComponent extends TopComponent implements UGSEventListener, MessageListener {

    /**
     * The IO name that will be used for writing log messages
     */
    public static final String IO_CHANNEL_NAME = "Console";

    /**
     * Actions to be shown in the popup menu
     */
    private static final Action[] ACTIONS = {
            new EnableDisableVerboseAction()
    };

    private final transient BackendAPI backend;
    private transient InputOutput io;
    private transient ConsoleProvider consoleProvider;

    public ConsoleTopComponent() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        setLayout(new BorderLayout());
        backend.addUGSEventListener(this);
        backend.addMessageListener(this);
        setName(Localization.getString("platform.plugin.console.title"));
        setToolTipText(LocalizingService.LocationStatusTooltip);
    }

    @Override
    public void componentOpened() {
        consoleProvider = new ConsoleProvider(this);
        IOContainer ioContainer = IOContainer.create(consoleProvider);

        io = IOProvider.getDefault().getIO(IO_CHANNEL_NAME, ACTIONS, ioContainer);
        io.select();

        JTextComponent textView = consoleProvider.getTextComponent();
        textView.addKeyListener(new ConsoleKeyListener(textView, this::sendCommand));
    }

    @Override
    protected void componentActivated() {
        consoleProvider.requestActive();
    }

    private void sendCommand(String command) {
        try {
            for (String commandRow : StringUtils.split(command, "\n")) {
                backend.sendGcodeCommand(false, commandRow);
            }
        } catch (Exception e) {
            try {
                IOColorPrint.print(io, "Error while sending command\n", IOColors.getColor(io, IOColors.OutputType.LOG_FAILURE));
                FoldHandle foldHandle = IOFolding.startFold(io, false);
                IOColorPrint.print(io, e.getMessage() + "\n", IOColors.getColor(io, IOColors.OutputType.LOG_DEBUG));
                foldHandle.silentFinish();
            } catch (IOException ex) {
                // Ignore any errors
            }
        }
    }

    @Override
    public void componentClosed() {
        io.closeInputOutput();
    }

    @Override
    public void onMessage(MessageType messageType, String message) {
        if (messageType == MessageType.VERBOSE && !backend.getSettings().isVerboseOutputEnabled()) {
            return;
        }

        Color color = IOColors.getColor(io, IOColors.OutputType.OUTPUT);
        if (messageType == MessageType.ERROR) {
            color = IOColors.getColor(io, IOColors.OutputType.LOG_FAILURE);
        } else if (messageType == MessageType.VERBOSE) {
            color = IOColors.getColor(io, IOColors.OutputType.LOG_DEBUG);
        }

        try {
            IOColorPrint.print(io, message, color);
        } catch (IOException e) {
            // Ignore any errors
        }
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof ControllerStateEvent) {
            ControllerStateEvent controllerStateEvent = (ControllerStateEvent) evt;
            io.setInputVisible(controllerStateEvent.getState() == ControllerState.IDLE);
        }
    }
}
