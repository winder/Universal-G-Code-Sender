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
import javax.swing.ListCellRenderer;
import java.awt.Component;
import java.io.Serializable;

/**
 * Renders a connection device in a list cell
 *
 * @author Joacim Breiler
 */
public class PortCellRenderer implements ListCellRenderer<IConnectionDevice>, Serializable {

    public Component getListCellRendererComponent(
            JList<? extends IConnectionDevice> list,
            IConnectionDevice device,
            int index,
            boolean isSelected,
            boolean cellHasFocus) {

        if (list.getComponentCount() == 0) {
            return new JLabel(Localization.getString("mainWindow.error.noSerialPort"));
        }

        return new PortCellItem(list, device, isSelected);
    }


}
