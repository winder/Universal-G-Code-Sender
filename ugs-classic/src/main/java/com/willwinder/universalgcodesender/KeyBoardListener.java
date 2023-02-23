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
package com.willwinder.universalgcodesender;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.jog.JogPanel;

import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;

/**
 * Listens to global keyboard events and triggers shortcut actions
 */
public class KeyBoardListener implements KeyEventDispatcher {

    private static KeyBoardListener instance;
    private final JogPanel jogPanel;
    private final BackendAPI backend;

    private KeyBoardListener(BackendAPI backend, JogPanel jogPanel) {
        this.jogPanel = jogPanel;
        this.backend = backend;
    }

    public static void registerListener(BackendAPI backend, JogPanel jogPanel) {
        if (instance != null) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().removeKeyEventDispatcher(instance);
        }

        instance = new KeyBoardListener(backend, jogPanel);
        KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(instance);
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent e) {
        // Prevent stealing key events from certain types of components
        if (!jogPanel.isKeyboardMovementEnabled() ||
                e.getSource() instanceof JTextField ||
                e.getSource() instanceof JComboBox ||
                SwingUtilities.getWindowAncestor(e.getComponent()) instanceof JDialog) {
            return false;
        }

        if (e.getID() != KeyEvent.KEY_PRESSED) {
            return false;
        }

        switch (e.getKeyCode()) {
            case KeyEvent.VK_RIGHT:
            case KeyEvent.VK_KP_RIGHT:
            case KeyEvent.VK_NUMPAD6:
                jogPanel.xPlusButtonActionPerformed();
                e.consume();
                return true;
            case KeyEvent.VK_LEFT:
            case KeyEvent.VK_KP_LEFT:
            case KeyEvent.VK_NUMPAD4:
                jogPanel.xMinusButtonActionPerformed();
                e.consume();
                return true;
            case KeyEvent.VK_UP:
            case KeyEvent.VK_KP_UP:
            case KeyEvent.VK_NUMPAD8:
                jogPanel.yPlusButtonActionPerformed();
                e.consume();
                return true;
            case KeyEvent.VK_DOWN:
            case KeyEvent.VK_KP_DOWN:
            case KeyEvent.VK_NUMPAD2:
                jogPanel.yMinusButtonActionPerformed();
                e.consume();
                return true;
            case KeyEvent.VK_PAGE_UP:
            case KeyEvent.VK_NUMPAD9:
                jogPanel.zPlusButtonActionPerformed();
                e.consume();
                return true;
            case KeyEvent.VK_PAGE_DOWN:
            case KeyEvent.VK_NUMPAD3:
                jogPanel.zMinusButtonActionPerformed();
                e.consume();
                return true;
            case KeyEvent.VK_ADD:
                jogPanel.increaseStepActionPerformed();
                e.consume();
                return true;
            case KeyEvent.VK_SUBTRACT:
                jogPanel.decreaseStepActionPerformed();
                e.consume();
                return true;
            case KeyEvent.VK_DIVIDE:
                jogPanel.divideStepActionPerformed();
                e.consume();
                return true;
            case KeyEvent.VK_MULTIPLY:
                jogPanel.multiplyStepActionPerformed();
                e.consume();
                return true;
            case KeyEvent.VK_INSERT:
            case KeyEvent.VK_NUMPAD0:
                try {
                    backend.resetCoordinatesToZero();
                    e.consume();
                } catch (Exception ex) {
                    // Never mind
                }
                return true;
            default:
                break;
        }

        return false;
    }
}
