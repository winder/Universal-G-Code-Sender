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
package com.willwinder.ugs.nbp.joystick;

import com.willwinder.ugs.nbp.lib.options.AbstractOptionPanelController;
import org.netbeans.spi.options.OptionsPanelController;

@OptionsPanelController.SubRegistration(
        location = "UGS",
        displayName = "Joystick",
        keywords = "Joystick",
        keywordsCategory = "UGS/Joystick"
)
@org.openide.util.NbBundle.Messages({"AdvancedOption_DisplayName_Joystick=Joystick", "AdvancedOption_Keywords_Joystick=joystick, gamepad"})
public class JoystickOptionsPanelController extends AbstractOptionPanelController<JoystickOptionsPanel> {
    @Override
    public JoystickOptionsPanel getPanel() {
        if (panel == null) {
            panel = new JoystickOptionsPanel(this);
        }
        return panel;
    }
}

