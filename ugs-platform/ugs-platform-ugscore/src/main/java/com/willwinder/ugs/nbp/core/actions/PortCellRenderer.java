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
package com.willwinder.ugs.nbp.core.actions;

import com.willwinder.universalgcodesender.connection.IConnectionDevice;
import com.willwinder.universalgcodesender.i18n.Localization;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.ListCellRenderer;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;
import java.awt.Component;
import java.io.Serializable;

/**
 * Renders a connection device in a list cell
 *
 * @author Joacim Breiler
 */
public class PortCellRenderer implements ListCellRenderer<IConnectionDevice>, Serializable {
    private static final Border DEFAULT_BORDER = new EmptyBorder(5, 5, 5, 5);

    public Component getListCellRendererComponent(
            JList<? extends IConnectionDevice> list,
            IConnectionDevice value,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        if (list.getComponentCount() == 0) {
            return new JLabel(Localization.getString("mainWindow.error.noSerialPort"));
        }

        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(true);
        panel.setBorder(DEFAULT_BORDER);
        populatePortInfo(value, panel);
        panel.setComponentOrientation(list.getComponentOrientation());

        if (isSelected) {
            panel.setBackground(list.getSelectionBackground());
            panel.setForeground(list.getSelectionForeground());
        } else {
            panel.setBackground(list.getBackground());
            panel.setForeground(list.getForeground());
        }

        panel.setEnabled(list.isEnabled());
        panel.setFont(list.getFont());
        return panel;
    }

    private void populatePortInfo(IConnectionDevice value, JPanel panel) {
        if (!value.getDescription().isPresent()) {
            JLabel addressLabel = new JLabel(value.getAddress());
            panel.add(addressLabel, BorderLayout.CENTER);
        } else {
            JLabel descriptionLabel = new JLabel(value.getDescription().get());
            panel.add(descriptionLabel, BorderLayout.CENTER);
            JLabel addressLabel = new JLabel(value.getAddress());
            addressLabel.setFont(addressLabel.getFont().deriveFont(8f));
            panel.add(addressLabel, BorderLayout.SOUTH);
        }
    }
}
