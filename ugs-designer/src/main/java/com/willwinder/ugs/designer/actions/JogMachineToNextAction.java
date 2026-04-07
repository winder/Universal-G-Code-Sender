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
package com.willwinder.ugs.designer.actions;

import com.willwinder.universalgcodesender.utils.SvgIconLoader;

import java.awt.event.ActionEvent;

/**
 * An action that will jog the machine to the center of the next object in the list
 *
 * @author Joacim Breiler
 */
public class JogMachineToNextAction extends JogMachineToCenterAction {
    public static final String SMALL_ICON_PATH = "img/jog-to.svg";
    public static final String LARGE_ICON_PATH = "img/jog-to24.svg";

    public JogMachineToNextAction() {
        super();
        putValue("menuText", "Jog machine to next");
        putValue(NAME, "Jog machine to next");
        putValue(SHORT_DESCRIPTION, "Jog machine to next");
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, SvgIconLoader.loadImageIcon(SMALL_ICON_PATH, SvgIconLoader.SIZE_SMALL).orElse(null));
        putValue(LARGE_ICON_KEY, SvgIconLoader.loadImageIcon(SMALL_ICON_PATH, SvgIconLoader.SIZE_MEDIUM).orElse(null));
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        new SelectNextAction().actionPerformed(e);
        super.actionPerformed(e);
    }
}
