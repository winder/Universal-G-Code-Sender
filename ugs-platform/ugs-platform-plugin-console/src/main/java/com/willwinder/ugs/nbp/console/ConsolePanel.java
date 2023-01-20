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
package com.willwinder.ugs.nbp.console;

import com.willwinder.ugs.nbp.console.actions.EnableDisableVerboseAction;
import com.willwinder.universalgcodesender.listeners.MessageListener;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.openide.windows.IOColorPrint;
import org.openide.windows.IOColors;
import org.openide.windows.IOContainer;
import org.openide.windows.IOProvider;
import org.openide.windows.InputOutput;

import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.text.JTextComponent;
import java.awt.BorderLayout;
import java.awt.Color;
import java.io.IOException;

/**
 * A panel that loads a netbeans console and dispatches UGS messages to it
 *
 * @author Joacim Breiler
 */
public class ConsolePanel extends JPanel implements MessageListener {
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

    private final transient InputOutput io;
    private final transient BackendAPI backend;

    public ConsolePanel(BackendAPI backend) {
        super(new BorderLayout());

        ConsoleProvider consoleProvider = new ConsoleProvider(this);
        IOContainer ioContainer = IOContainer.create(consoleProvider);

        io = IOProvider.getDefault().getIO(IO_CHANNEL_NAME, ACTIONS, ioContainer);
        io.select();
        io.setInputVisible(false);

        // After the IO has been loaded, get the text component and disable some properties
        JTextComponent textView = consoleProvider.getTextComponent();
        textView.setEditable(false);

        this.backend = backend;
        backend.addMessageListener(this);
    }

    public void close() {
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
}
