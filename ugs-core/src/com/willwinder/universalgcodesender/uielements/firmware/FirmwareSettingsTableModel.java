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
package com.willwinder.universalgcodesender.uielements.firmware;

import com.willwinder.universalgcodesender.firmware.FirmwareSetting;
import com.willwinder.universalgcodesender.i18n.Localization;

import javax.swing.table.AbstractTableModel;
import java.util.ArrayList;
import java.util.List;

/**
 * A table model for handling firmware settings
 *
 * @author Joacim Breiler
 */
public class FirmwareSettingsTableModel extends AbstractTableModel {
    private final List<FirmwareSetting> settings;

    public FirmwareSettingsTableModel(List<FirmwareSetting> settings) {
        this.settings = settings;
    }

    public List<FirmwareSetting> getSettings() {
        return new ArrayList<>(settings);
    }

    @Override
    public int getRowCount() {
        return settings.size();
    }

    @Override
    public int getColumnCount() {
        return 3;
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return columnIndex == 1;
    }

    @Override
    public String getColumnName(int columnIndex) {
        switch (columnIndex) {
            case 0:
                return Localization.getString("setting");
            case 1:
                return Localization.getString("value");
            case 2:
                return Localization.getString("description");
            default:
                return null;
        }
    }

    @Override
    public Object getValueAt(int rowIndex, int columnIndex) {
        if (getRowCount() > settings.size()) {
            return null;
        }

        FirmwareSetting setting = this.settings.get(rowIndex);
        if (setting == null) {
            return null;
        }

        switch (columnIndex) {
            case 0:
                return setting.getKey();
            case 1:
                return setting.getValue();
            case 2:
                return setting.getShortDescription();
            default:
                return null;
        }
    }

    @Override
    public void setValueAt(Object value, int rowIndex, int columnIndex) {
        if (columnIndex == 1) {
            FirmwareSetting oldSetting = settings.get(rowIndex);
            FirmwareSetting setting = new FirmwareSetting(oldSetting.getKey(), value.toString(), oldSetting.getUnits(), oldSetting.getDescription(), oldSetting.getShortDescription());
            settings.set(rowIndex, setting);
        }
    }

    public void updateSetting(FirmwareSetting setting) {
        this.settings.removeIf(c -> c.getKey().equals(setting.getKey()));
        this.settings.add(setting);
        this.fireTableDataChanged();
    }
}
