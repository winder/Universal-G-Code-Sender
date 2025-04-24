/*
Copyright 2023-2024 Will Winder

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
package com.willwinder.ugs.nbp.core.ui;

import com.willwinder.universalgcodesender.connection.IConnectionDevice;
import net.miginfocom.swing.MigLayout;
import org.openide.util.ImageUtilities;

import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import java.awt.Component;
import java.awt.Font;

/**
 * Renders a connection device
 *
 * @author Joacim Breiler
 */
public class PortCellItem extends JPanel {
    private static final Border DEFAULT_BORDER = new EmptyBorder(5, 5, 5, 5);

    PortCellItem(JList<? extends IConnectionDevice> list, IConnectionDevice device, boolean isSelected) {
        super(new MigLayout());
        setOpaque(true);
        setBorder(DEFAULT_BORDER);
        populatePortInfo(device);
        setComponentOrientation(list.getComponentOrientation());

        for(Component component : getComponents()) {
            if (isSelected) {
                setBackground(list.getSelectionBackground());
                component.setBackground(list.getSelectionBackground());
                component.setForeground(list.getSelectionForeground());
            } else {
                setBackground(list.getBackground());
                component.setBackground(list.getBackground());
                component.setForeground(list.getForeground());
            }
        }
        setEnabled(list.isEnabled());
    }

    private void populatePortInfo(IConnectionDevice device) {
        add(new JLabel(ImageUtilities.loadImageIcon("resources/icons/device24.svg", false)), "spany");

        JLabel label = new JLabel(device.getAddress());
        Font f = label.getFont();
        label.setFont(f.deriveFont(Font.BOLD));
        add(label, "wrap, growx, align left");

        device.getDescription().ifPresent(description -> add(new JLabel(description), "wrap, growx, align left"));
        device.getManufacturer().ifPresent(manufacturer -> add(new JLabel(manufacturer), "wrap, growx, align left"));
    }
}