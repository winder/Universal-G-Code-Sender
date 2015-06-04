/*
    Copywrite 2015 Will Winder

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
package com.willwinder.ugs.nbp.connection;

import com.willwinder.universalgcodesender.utils.CommUtils;

/**
 *
 * @author wwinder
 */
public class PortComboBox extends ComboWithPreferences {

    @Override
    Class getPreferenceClass() {
        return ConnectionProperty.class;
    }

    @Override
    String getPreferenceName() {
        return "address";
    }

    @Override
    String getDefaultValue() {
        return "";
    }
    
    public PortComboBox() {
        super();
    }
    
    @Override
    protected void initComboBox() {
        // Add serial ports
        String[] portList = CommUtils.getSerialPortList();
        if (portList.length < 1) {
            this.addItem("<no ports>");
            //MainWindow.displayErrorDialog(Localization.getString("mainWindow.error.noSerialPort"));
        } else {
            for (String port : portList) {
                this.addItem(port);
            }

            this.setSelectedIndex(0);
        }
    }
}
