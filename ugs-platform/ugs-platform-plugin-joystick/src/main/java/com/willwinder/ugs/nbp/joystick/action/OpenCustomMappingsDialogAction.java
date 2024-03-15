/*
    Copyright 2024 Will Winder

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

import com.willwinder.ugs.nbp.joystick.CustomMappingsDialog;
import com.willwinder.ugs.nbp.joystick.service.JoystickService;
import com.willwinder.universalgcodesender.i18n.Localization;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.Component;
import java.awt.event.ActionEvent;

public class OpenCustomMappingsDialogAction extends AbstractAction {
    public static final String ICON_BASE = "com/willwinder/ugs/nbp/joystick/settings.svg";
    private final transient JoystickService joystickService;


    public OpenCustomMappingsDialogAction(JoystickService joystickService) {
        String title = Localization.getString("platform.plugin.joystick.customMappings");
        putValue(NAME, title);
        putValue("menuText", title);
        putValue(Action.SHORT_DESCRIPTION, title);
        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue(Action.SELECTED_KEY, false);
        this.joystickService = joystickService;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new CustomMappingsDialog((Component) e.getSource(), joystickService).setVisible(true);
    }
}
