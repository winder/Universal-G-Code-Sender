/*
    Copywrite 2016 Will Winder

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
package com.willwinder.universalgcodesender.uielements;

import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import static com.willwinder.universalgcodesender.utils.GUIHelpers.displayErrorDialog;
import java.awt.Color;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JTextField;

/**
 *
 * @author wwinder
 */
public class CommandTextArea extends JTextField implements KeyEventDispatcher, UGSEventListener {
    private BackendAPI backend;
    List<String> commandHistory = new ArrayList<>();
    int commandNum = -1;

    // This is needed for unit testing.
    protected boolean focusNotNeeded;

    public CommandTextArea() {
        this(null);
    }

    public CommandTextArea(BackendAPI backend) {
        init(backend);
    }

    public final void init(BackendAPI backend) {
        this.backend = backend;
        if (backend != null) {
            this.backend.addUGSEventListener(this);
            this.setEnabled(backend.isConnected());
        }

        this.addActionListener((ActionEvent evt) -> action(evt));
        KeyboardFocusManager.getCurrentKeyboardFocusManager()
            .addKeyEventDispatcher(this);
    }

    /**
     * Detect connection events to enable/disable command text area.
     */
    @Override
    public void UGSEvent(com.willwinder.universalgcodesender.model.UGSEvent evt) {
        this.setEnabled(backend.isConnected());
    }

    public void action(ActionEvent evt) {
        final String str = getText().replaceAll("(\\r\\n|\\n\\r|\\r|\\n)", "");
        GUIHelpers.invokeLater(() -> {
            try {
                backend.sendGcodeCommand(str);
            } catch (Exception ex) {
                displayErrorDialog(ex.getMessage());
            }
        });
        setText("");
        this.commandHistory.add(str);
        this.commandNum = -1;
    }
    
    private boolean isArrowKey(KeyEvent e) {
        switch (e.getKeyCode()) {
                case KeyEvent.VK_UP:
                case KeyEvent.VK_DOWN:
                case KeyEvent.VK_LEFT:
                case KeyEvent.VK_RIGHT:
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
                && commandHistory.size() > 0
                && (this.hasFocus() || focusNotNeeded)
                && isArrowKey(e)) {
            boolean pressed = false;
            
            if (e.getKeyCode() == KeyEvent.VK_UP) {
                pressed = true;
                commandNum++;
            }
            else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
                pressed = true;
                commandNum--;
            }

            if (pressed) {
                if (commandNum < 0) {
                    commandNum = -1;
                    setText("");
                    java.awt.Toolkit.getDefaultToolkit().beep();
                    return true;
                } else if (commandNum > (commandHistory.size()-1)) {
                    commandNum = commandHistory.size()-1;
                    java.awt.Toolkit.getDefaultToolkit().beep();
                }

                // Get index from end.
                int index = commandHistory.size() - 1 - commandNum;
                String text = this.commandHistory.get(index);
                setText(text);
            }

            return true;
        }
        return false;
    }
}
