/*
    Copywrite 2015-2016 Will Winder

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
package com.willwinder.ugs.nbp.core.connection;

/**
 *
 * @author wwinder
 */
public class BaudComboBox extends ComboWithPreferences {
    @Override
    Class getPreferenceClass() {
        return ConnectionProperty.class;
    }

    @Override
    String getPreferenceName() {
        return "baud";
    }

    @Override
    String getDefaultValue() {
        return "115200";
    }
    
    @Override
    protected void initComboBox() {
        String[] values = { "2400", "4800", "9600", "19200", "38400", "57600", "115200" };
        for (String value : values) this.addItem(value);
    }
}
