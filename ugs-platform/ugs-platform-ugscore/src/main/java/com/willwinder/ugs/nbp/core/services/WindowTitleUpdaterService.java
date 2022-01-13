/*
    Copyright 2018 Will Winder

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
package com.willwinder.ugs.nbp.core.services;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.utils.Version;
import org.openide.util.lookup.ServiceProvider;
import org.openide.windows.WindowManager;

/**
 * A service that will listen for file load events and change the window title.
 *
 * @author Joacim Breiler
 */
@ServiceProvider(service = WindowTitleUpdaterService.class)
public class WindowTitleUpdaterService {

    private final BackendAPI backend;

    public WindowTitleUpdaterService() {
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this::onEvent);
        updateTitle();
        updateVersion();
    }

    private void updateVersion() {
        // Sets the version string in the about dialog
        System.setProperty("netbeans.buildnumber", String.valueOf(Version.getBuildDateAsNumber()));
    }

    private void onEvent(UGSEvent event) {
        if (event instanceof FileStateEvent) {
            updateTitle();
        }
    }

    private void updateTitle() {
        WindowManager.getDefault().invokeWhenUIReady(() -> {

            String title = Localization.getString("platform-title")
                    + " (" + Localization.getString("version")
                    + " " + Version.getVersionString() + ")";

            if (backend.getGcodeFile() != null) {
                title = backend.getGcodeFile().getName() + " - " + title;
            }

            WindowManager.getDefault().getMainWindow().setTitle(title);
        });
    }
}
