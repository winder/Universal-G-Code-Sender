/*
    Copyright 2022 Will Winder

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
package com.willwinder.ugs.nbp.filebrowser;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import java.awt.Font;
import java.util.ArrayList;
import java.util.List;

public class FileTable extends JTable {
    public FileTable(FileTableModel tableModel) {
        super(tableModel);
        setAutoCreateRowSorter(true);
        setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(getModel());
        setRowSorter(sorter);
        List<RowSorter.SortKey> sortKeys = new ArrayList<>();
        sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
        sorter.setSortKeys(sortKeys);
        sorter.sort();

        JTableHeader tableHeader = getTableHeader();
        tableHeader.setFont(tableHeader.getFont().deriveFont(Font.BOLD));
        tableHeader.getColumnModel().getColumn(0).setMinWidth(200);

        tableHeader.getColumnModel().getColumn(1).setMaxWidth(200);
        tableHeader.getColumnModel().getColumn(1).setCellRenderer(new FileSizeCellRenderer());
        tableHeader.setReorderingAllowed(true);
    }
}
