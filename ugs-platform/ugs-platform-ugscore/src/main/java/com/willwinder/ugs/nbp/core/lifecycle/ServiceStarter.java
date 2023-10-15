/*
    Copyright 2016-2021 Will Winder

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

import com.willwinder.ugs.nbp.core.control.MacroService;
import com.willwinder.ugs.nbp.core.services.*;
import com.willwinder.ugs.nbp.core.statusline.SendStatusLineService;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.Version;
import org.openide.modules.OnStart;
import org.openide.util.Lookup;
import org.openide.windows.WindowManager;

import java.util.logging.Logger;

/**
 * @author wwinder
 */
@OnStart
public class ServiceStarter implements Runnable {
    private static final Logger logger = Logger.getLogger(ServiceStarter.class.getName());

    @Override
    public void run() {
        logger.info("Starting UGS " + Version.getVersionString());
        logger.info("Loading LocalizingService...");
        Lookup.getDefault().lookup(LocalizingService.class);
        logger.info("Loading JogService...");
        Lookup.getDefault().lookup(JogActionService.class);
        logger.info("Loading OverrideActionService...");
        Lookup.getDefault().lookup(OverrideActionService.class);
        logger.info("Loading MacroService...");
        Lookup.getDefault().lookup(MacroService.class);
        logger.info("Loading SendStatusLineService...");
        Lookup.getDefault().lookup(SendStatusLineService.class);
        logger.info("Loading SettingsChangedNotificationService...");
        Lookup.getDefault().lookup(SettingsChangedNotificationService.class);
        logger.info("Loading WindowTitleUpdaterService...");
        Lookup.getDefault().lookup(WindowTitleUpdaterService.class);
        logger.info("Loading PendantService...");
        Lookup.getDefault().lookup(PendantService.class);
        logger.info("Loading ConsoleNotificationService...");
        Lookup.getDefault().lookup(ConsoleNotificationService.class);
        logger.info("Loading FileFilterService...");
        Lookup.getDefault().lookup(FileFilterService.class);
        logger.info("Services loaded!");

        logger.info("Setting UGP version title.");
        Settings settings = CentralLookup.getDefault().lookup(Settings.class);
        setupVersionInformation(settings);
    }

    private void setupVersionInformation(Settings settings) {
        // Only change the window title when all the UI components are fully loaded.
        WindowManager.getDefault().invokeWhenUIReady(() -> Utils.checkNightlyBuild(settings));
    }
}
