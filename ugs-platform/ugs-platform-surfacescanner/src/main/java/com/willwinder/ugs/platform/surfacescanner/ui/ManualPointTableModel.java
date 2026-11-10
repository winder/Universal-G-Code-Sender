/*
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
package com.willwinder.ugs.platform.surfacescanner.ui;

import com.willwinder.ugs.platform.surfacescanner.SurfaceScanner;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.Position;

import javax.swing.table.AbstractTableModel;
import java.util.List;

/**
 * Displays the manually captured surface points.
 */
public class ManualPointTableModel extends AbstractTableModel {
    private final SurfaceScanner surfaceScanner;

    public ManualPointTableModel(SurfaceScanner surfaceScanner) {
        this.surfaceScanner = surfaceScanner;
        surfaceScanner.addListener(this::fireTableDataChanged);
    }

    @Override
    public int getRowCount() {
        return surfaceScanner.getScannedPoints().size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public String getColumnName(int column) {
        return switch (column) {
            case 0 -> Localization.getString("machineStatus.pin.x");
            case 1 -> Localization.getString("machineStatus.pin.y");
            default -> Localization.getString("machineStatus.pin.z");
        };
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        List<Position> points = surfaceScanner.getScannedPoints();
        Position point = points.get(rowIndex);
        return switch (columnIndex) {
            case 0 -> point.getX();
            case 1 -> point.getY();
            default -> point.getZ();
        };
    }
}
