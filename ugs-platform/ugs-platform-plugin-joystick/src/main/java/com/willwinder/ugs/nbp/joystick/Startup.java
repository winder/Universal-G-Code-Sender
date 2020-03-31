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

import com.willwinder.ugs.nbp.joystick.service.JoystickService;
import com.willwinder.ugs.nbp.joystick.service.JoystickServiceImpl;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import org.openide.modules.OnStart;

/**
 * Initializes, starts and adds the JoystickService to the central lookup when UGS starts.
 */
@OnStart
public class Startup implements Runnable {
    @Override
    public void run() {
        JoystickService joystickService = CentralLookup.getDefault().lookup(JoystickService.class);
        if (joystickService == null) {
            joystickService = new JoystickServiceImpl();
            if (Settings.isActive()) {
                joystickService.initialize();
            }
            CentralLookup.getDefault().add(joystickService);
        }
    }
}
