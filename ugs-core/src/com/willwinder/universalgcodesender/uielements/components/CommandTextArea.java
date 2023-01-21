/*
    Copyright 2016-2023 Will Winder

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
package com.willwinder.universalgcodesender.uielements.components;

import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.utils.CommandHistory;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import org.apache.commons.lang3.StringUtils;

import javax.swing.JTextField;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import static com.willwinder.universalgcodesender.utils.GUIHelpers.displayErrorDialog;

/**
 * @author wwinder
 */
public class CommandTextArea extends JTextField implements KeyEventDispatcher, UGSEventListener {
    private final transient CommandHistory commandHistory = new CommandHistory();
    // This is needed for unit testing.
    protected boolean focusNotNeeded = false;
    private transient BackendAPI backend;
    /**
     * A variable that indicates if the focus should be regained to this component when
     * its state changes from enabled with focus -> disabled -> enabled.
     */
    private boolean regainFocus = false;

    public CommandTextArea() {
        this(null);
    }

    public CommandTextArea(BackendAPI backend) {
        init(backend);

        // Make it possible to send multiple lines
        getDocument().putProperty("filterNewlines", Boolean.FALSE);
    }

    public final void init(BackendAPI backend) {
        this.backend = backend;
        if (backend != null) {
            this.backend.addUGSEventListener(this);
            this.setEnabled(backend.isConnected());
        }

        addActionListener(this::action);
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
                .addKeyEventDispatcher(this);
    }

    /**
     * Detect connection events to enable/disable command text area.
     */
    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof ControllerStateEvent) {
            ControllerState state = backend.getControllerState();
            boolean isIdle = (state == ControllerState.IDLE || state == ControllerState.ALARM || state == ControllerState.CHECK);
            if (!isIdle && isEnabled()) {
                regainFocus = hasFocus();
                setEnabled(false);
            } else if (isIdle && !isEnabled()) {
                setEnabled(true);
                if (regainFocus) {
                    regainFocus = false;
                    requestFocusInWindow();
                }
            }
        }
    }

    public void action(ActionEvent evt) {
        final String commands = getText().replaceAll("(\\r\\n|\\n\\r|\\r)", "\n");
        GUIHelpers.invokeLater(() -> {
            try {
                sendCommands(StringUtils.split(commands, "\n"));
            } catch (Exception ex) {
                displayErrorDialog(ex.getMessage());
            }
        });

        setText("");
        commandHistory.add(commands);
    }

    private void sendCommands(String[] commands) throws Exception {
        if (commands.length == 0) {
            backend.sendGcodeCommand("");
        } else {
            for (String command : commands) {
                backend.sendGcodeCommand(command);
            }
        }
    }

    private boolean isArrowKey(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP:
            case KeyEvent.VK_DOWN:
                return true;
            default:
                return false;
        }
    }

    /**
     * The up/down keyboard events cycle through previous commands.
     */
    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        if (e.getID() == KeyEvent.KEY_PRESSED
                && (this.hasFocus() || focusNotNeeded)
                && isArrowKey(e)) {

            if (e.getKeyCode() == KeyEvent.VK_UP) {
                setText(commandHistory.previous());
            } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                setText(commandHistory.next());
            }

            return true;
        }
        return false;
    }
}
