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
package com.willwinder.ugs.nbm.workflow;

import org.openide.util.ImageUtilities;

import javax.swing.table.DefaultTableCellRenderer;
import java.io.File;

/**
 * A cell renderer that renders a {@link File}.
 *
 * @author Joacim Breiler
 */
public class FileTableCellRenderer extends DefaultTableCellRenderer {
    @Override
    protected void setValue(Object value) {
        if (value instanceof File) {
            File file = (File) value;
            super.setValue(file.getName());
            super.setToolTipText(file.getAbsolutePath());
            super.setIcon(ImageUtilities.loadImageIcon("/com/willwinder/ugs/nbm/workflow/icons/new.svg", false));
        }
    }
}
