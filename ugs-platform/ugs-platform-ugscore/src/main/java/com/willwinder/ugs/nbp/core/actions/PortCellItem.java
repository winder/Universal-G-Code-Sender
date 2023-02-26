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

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.BorderLayout;

/**
 * Renders a connection device
 *
 * @author Joacim Breiler
 */
public class PortCellItem extends JPanel {
    private static final Border DEFAULT_BORDER = new EmptyBorder(5, 5, 5, 5);

    PortCellItem(JList<? extends IConnectionDevice> list, IConnectionDevice device, boolean isSelected) {
        super(new BorderLayout());
        setOpaque(true);
        setBorder(DEFAULT_BORDER);
        populatePortInfo(device);
        setComponentOrientation(list.getComponentOrientation());

        if (isSelected) {
            setBackground(list.getSelectionBackground());
            setForeground(list.getSelectionForeground());
        } else {
            setBackground(list.getBackground());
            setForeground(list.getForeground());
        }

        setEnabled(list.isEnabled());
        setFont(list.getFont());
    }

    private void populatePortInfo(IConnectionDevice device) {
        if (!device.getDescription().isPresent()) {
            setTitle(device.getAddress());
        } else {
            setTitleAndDescription(device);
        }
    }

    private void setTitle(String title) {
        JLabel addressLabel = new JLabel(title);
        add(addressLabel, BorderLayout.CENTER);
    }

    private void setTitleAndDescription(IConnectionDevice device) {
        setTitle(device.getDescription().orElse(device.getAddress()));
        JLabel addressLabel = new JLabel(device.getAddress());
        addressLabel.setFont(addressLabel.getFont().deriveFont(8f));
        add(addressLabel, BorderLayout.SOUTH);
    }
}
