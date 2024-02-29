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
package com.willwinder.ugs.nbp.core.lifecycle;

import java.util.logging.Logger;
import javax.swing.Action;
import org.openide.awt.Actions;
import org.openide.windows.OnShowing;

/**
 * Makes the application enter full screen if the {@link #setUseFullScreen(boolean)} was set to true
 * before the application was started
 *
 * @author Joacim Breiler
 */
@OnShowing
public class FullScreenOptionProcessor implements Runnable {
    private static final Logger LOGGER = Logger.getLogger(StartupOptionProcessor.class.getName());

    private static boolean useFullScreen = false;

    @Override
    public void run() {
        if (useFullScreen) {
            Action action = Actions.forID("Window", "org.netbeans.core.windows.actions.ToggleFullScreenAction");
            if (action == null) {
                LOGGER.info("Could not find the ToggleFullScreenAction to enter full screen");
                return;
            }
            action.actionPerformed(null);
        }
    }

    public static void setUseFullScreen(boolean useFullscreen) {
        FullScreenOptionProcessor.useFullScreen = useFullscreen;
    }
}