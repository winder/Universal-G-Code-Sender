/*
    Copyright 2020-2023 Will Winder

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
package com.willwinder.ugs.nbp.dro.panels;

import com.willwinder.universalgcodesender.model.Axis;

import javax.swing.JComponent;

public interface AxisPanelListener {
    /**
     * When the reset click button is pressed
     *
     * @param component - the button component being pressed
     * @param axis      - the axis that should be reset
     */
    void onResetClick(JComponent component, Axis axis);

    /**
     * When the work position is being clicked
     *
     * @param component - the label being clicked
     * @param axis      - the axis that the label is showing
     */
    void onWorkPositionClick(JComponent component, Axis axis);
}
