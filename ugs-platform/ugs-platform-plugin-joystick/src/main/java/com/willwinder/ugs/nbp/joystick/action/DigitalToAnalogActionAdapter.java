/*
    Copyright 2020 Will Winder

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
package com.willwinder.ugs.nbp.joystick.action;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * An action that wraps a digital action as an analog action.
 */
public class DigitalToAnalogActionAdapter extends AbstractAction implements AnalogAction {

    private final Action action;

    private float currentValue;
    private boolean changed;

    public DigitalToAnalogActionAdapter(Action action) {
        this.action = action;
        this.putValue(NAME, action.getValue(NAME));
    }

    @Override
    public void setValue(float value) {
        if (value <= 0) {
            value = 0;
        } else {
            value = 1;
        }

        if (currentValue != value) {
            changed = true;
            currentValue = value;
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (changed && currentValue > 0) {
            changed = false;
            action.actionPerformed(e);
        }
    }
}
