/*
    Copyright 2018 Will Winder

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

import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.helpers.ThemeColors;
import com.willwinder.universalgcodesender.uielements.panels.MachineStatusPanel;

import javax.swing.JTextField;
import java.awt.Color;
import java.awt.KeyboardFocusManager;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A text field for displaying and setting work coordinates.
 *
 * @author Joacim Breiler
 */
public class WorkCoordinateTextField extends JTextField implements KeyListener, FocusListener {
    private static final Logger logger = Logger.getLogger(MachineStatusPanel.class.getSimpleName());
    private final BackendAPI backend;
    private final Axis axis;

    public WorkCoordinateTextField(BackendAPI backend, Axis axis) {
        this.backend = backend;
        this.axis = axis;

        setText("0.000");

        setHorizontalAlignment(JTextField.RIGHT);
        setForeground(ThemeColors.LIGHT_BLUE);
        setDisabledTextColor(ThemeColors.LIGHT_BLUE_GREY);
        setBackground(null);
        setBorder(null);

        addFocusListener(this);
        addKeyListener(this);

        // TODO add I18N
        setToolTipText("<html><body>" +
                "<p><b>Displays and sets the work position<b/></p>" +
                "<p>Exact positions or simple expressions can be used where<br/>" +
                "the character <b>'#'</b> will be replaced with the current work position.</p>" +
                "</body></html>");
    }

    @Override
    public void keyTyped(KeyEvent event) {

    }

    @Override
    public void keyPressed(KeyEvent event) {

    }

    @Override
    public void keyReleased(KeyEvent event) {
        JTextField field = ((JTextField) event.getComponent());
        if (event.getKeyCode() == KeyEvent.VK_ENTER) {
            try {
                backend.setWorkPositionUsingExpression(axis, field.getText());
            } catch (Exception e) {
                logger.log(Level.INFO, "Couldn't set the work position", e);
            }
            KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
        } else if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
            KeyboardFocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
        }
    }

    @Override
    public void focusGained(FocusEvent event) {
        setBackground(Color.WHITE);
        setForeground(ThemeColors.VERY_DARK_GREY);
        setSelectionStart(0);
        setSelectionEnd(getText().length());
    }

    @Override
    public void focusLost(FocusEvent event) {
        setBackground(null);
        setForeground(ThemeColors.LIGHT_BLUE);
    }
}
