/*
    Copywrite 2016 Will Winder

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

import com.willwinder.ugs.nbp.lookup.CentralLookup;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.FirmwareUtils;
import org.openide.util.Lookup;

/**
 *
 * @author wwinder
 */
public class FirmwareComboBox extends ComboWithPreferences {
    @Override
    Class getPreferenceClass() {
        return ConnectionProperty.class;
    }

    @Override
    String getPreferenceName() {
        return "firmware";
    }

    @Override
    String getDefaultValue() {
        return "GRBL";
    }
    
    @Override
    protected void initComboBox() {
        FirmwareUtils.getFirmwareList().stream().forEach((value) -> {
            this.addItem(value);
        });
    }
}
