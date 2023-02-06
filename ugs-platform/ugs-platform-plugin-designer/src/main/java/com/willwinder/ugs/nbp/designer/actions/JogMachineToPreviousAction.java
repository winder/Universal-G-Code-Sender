/*
    Copyright 2023 Will Winder

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
package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

import java.awt.event.ActionEvent;

/**
 * An action that will jog the machine to the center of the previous object in the list
 *
 * @author Joacim Breiler
 */
@ActionID(
        category = LocalizingService.CATEGORY_DESIGNER,
        id = "com.willwinder.ugs.nbp.designer.actions.JogMachineToPreviousAction")
@ActionRegistration(
        iconBase = JogMachineToPreviousAction.SMALL_ICON_PATH,
        displayName = "Jog machine to previous",
        lazy = false)
public class JogMachineToPreviousAction extends JogMachineToCenterAction {
    public static final String SMALL_ICON_PATH = "img/jog-to.svg";
    public static final String LARGE_ICON_PATH = "img/jog-to24.svg";

    public JogMachineToPreviousAction() {
        super();
        putValue("menuText", "Jog machine to previous");
        putValue(NAME, "Jog machine to previous");
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new SelectPreviousAction().actionPerformed(e);
        super.actionPerformed(e);
    }
}
