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
package com.willwinder.ugs.nbp.joystick.service;

import com.willwinder.ugs.nbp.core.statusline.SeparatorPanel;
import com.willwinder.ugs.nbp.joystick.ui.JoystickStatusLine;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import org.openide.awt.StatusLineElementProvider;
import org.openide.util.lookup.ServiceProvider;

import java.awt.*;

/**
 * Registers a status line for displaying the state of the connected controller
 *
 * @author Joacim Breiler
 */
@ServiceProvider(service = StatusLineElementProvider.class, position=-1)
public class JoystickStatusLineService implements StatusLineElementProvider {

    @Override
    public Component getStatusLineElement() {
        JoystickStatusLine jogStatusLine = new JoystickStatusLine(CentralLookup.getDefault().lookup(JoystickService.class));
        return new SeparatorPanel(jogStatusLine);
    }
}