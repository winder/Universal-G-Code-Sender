/*
 * This class is mainly to satiet netbeans and its automatic code generation.
 */

/*
    Copywrite 2013 Will Winder

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
package com.willwinder.universalgcodesender.uielements;

import com.willwinder.universalgcodesender.i18n.Localization;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author wwinder
 */
public class GcodeTableModel extends DefaultTableModel {
    private final Class[] types = new Class [] {
        java.lang.String.class, java.lang.Boolean.class, java.lang.Boolean.class, java.lang.String.class
    };
    private final boolean[] canEdit = new boolean [] {
        false, false, false, false
    };
    
    public GcodeTableModel() {
        super(new Object [][] {

            },
            new String [] {
                Localization.getString("gcodeTable.command"),
                Localization.getString("gcodeTable.sent"),
                Localization.getString("gcodeTable.done"),
                Localization.getString("gcodeTable.response")
            }
        );
    }      

    @Override
    public Class getColumnClass(int columnIndex) {
        return types [columnIndex];
    }

    @Override
    public boolean isCellEditable(int rowIndex, int columnIndex) {
        return canEdit [columnIndex];
    }

}
