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
package com.willwinder.universalgcodesender.uielements.components;

import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashSet;

/**
 * A popup editor that will be positioned near the parent component
 *
 * @author Joacim Breiler
 */
public class PopupEditor extends JDialog {
    private final TextField textField;
    private final HashSet<PopupEditorListener> listeners = new HashSet<>();

    /**
     * Constructor
     *
     * @param parent the parent component that this popup editor will be
     * @param title  the title to be displayed in the popup dialog
     * @param text   the text to be displayed in the editor window
     */
    public PopupEditor(JComponent parent, String title, String text) {
        super(SwingUtilities.getWindowAncestor(parent), title, Dialog.ModalityType.MODELESS);
        setResizable(false);
        JPanel panel = new JPanel(new MigLayout("fill, wrap 2, inset 2, gap 6, wmin 200, hmin 48", "[70%][30%]"));
        textField = new TextField(text);
        textField.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 16));
        textField.addKeyListener(new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {
            }

            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                    dispose();
                } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    notifyListenersAndDispose();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        });
        JButton button = new JButton("Set");
        button.setMinimumSize(new Dimension(10, 10));
        button.addActionListener((event) -> notifyListenersAndDispose());

        panel.add(textField, "grow");
        panel.add(button, "grow");
        add(panel);
        pack();
        setLocationRelativeTo(parent);
    }

    private void notifyListenersAndDispose() {
        listeners.forEach(listener -> listener.onValue(textField.getText()));
        dispose();
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            textField.requestFocusInWindow();
        }
    }

    public void addPopupListener(PopupEditorListener listener) {
        this.listeners.add(listener);
    }

    @Override
    public void dispose() {
        this.listeners.clear();
        super.dispose();
    }

    public interface PopupEditorListener {
        void onValue(String value);
    }
}
