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
package com.willwinder.ugs.nbp.designer.gui.clipart;

import com.jidesoft.popup.JidePopup;

import javax.swing.JLabel;
import java.awt.Component;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

/**
 * A tooltip that will display information about the clipart that
 * is represented in a {@link ClipartButton}.
 *
 * @author Joacim Breiler
 */
public class ClipartTooltip implements MouseListener {

    private final JidePopup popup;

    public ClipartTooltip() {
        popup = new JidePopup();
        popup.setKeepPreviousSize(false);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        // Not used
    }

    @Override
    public void mousePressed(MouseEvent e) {
        // Not used
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        // Not used
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        if (e.getSource() instanceof ClipartButton) {
            ClipartButton clipartButton = (ClipartButton) e.getSource();
            Clipart clipart = clipartButton.getClipart();
            popup.getContentPane().removeAll();
            popup.getContentPane().add(new JLabel(getClipartText(clipart)));
            popup.showPopup((Component) e.getSource());
        }
    }

    private String getClipartText(Clipart clipart) {
        return "<html>" +
                "Name: <b>" + clipart.getName() + "</b><br>" +
                "License: " + clipart.getSource().getLicense() + "<br>" +
                "Source: " + clipart.getSource().getName() + "<br>" +
                "Credits: " + clipart.getSource().getCredits() + "<br>" +
                "URL: <a href=\"" + clipart.getSource().getUrl() + "\">" + clipart.getSource().getUrl() + "</a>" +
                "</html>";
    }

    @Override
    public void mouseExited(MouseEvent e) {
        popup.hidePopup();
    }
}
