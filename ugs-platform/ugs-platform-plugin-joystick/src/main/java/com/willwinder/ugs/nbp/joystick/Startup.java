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
import org.openide.cookies.InstanceCookie;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.modules.OnStart;
import org.openide.util.actions.SystemAction;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Initializes, starts and adds the JoystickService to the central lookup when UGS starts.
 */
@OnStart
public class Startup implements Runnable {
    @Override
    public void run() {
        JoystickService joystickService = CentralLookup.getDefault().lookup(JoystickService.class);
        if (joystickService != null) {
            return;
        }

        joystickService = new JoystickServiceImpl();
        if (Settings.isActive()) {
            joystickService.initialize();
        }
        CentralLookup.getDefault().add(joystickService);

    }
}
