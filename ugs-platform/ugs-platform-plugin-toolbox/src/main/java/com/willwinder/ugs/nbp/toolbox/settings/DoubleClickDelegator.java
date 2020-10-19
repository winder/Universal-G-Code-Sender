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
package com.willwinder.ugs.nbp.toolbox.settings;

import com.willwinder.ugs.nbp.lib.services.ActionReference;

import javax.swing.JList;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.function.Consumer;

/**
 * Listens to double click events and delegates those events.
 */
public class DoubleClickDelegator implements MouseListener {

    private final Consumer<ActionReference> delegate;

    DoubleClickDelegator(Consumer<ActionReference> delegate) {
        this.delegate = delegate;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void mouseClicked(MouseEvent e) {
        JList<ActionReference> list = (JList<ActionReference>) e.getSource();
        if (e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1) {
            ActionReference actionReference = list.getSelectedValue();
            this.delegate.accept(actionReference);
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }
}
