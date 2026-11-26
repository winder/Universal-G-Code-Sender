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
package com.willwinder.ugs.platform.surfacescanner.actions;

import com.willwinder.ugs.platform.surfacescanner.SurfaceScanner;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.utils.AutoLevelSettings;
import com.willwinder.universalgcodesender.utils.Settings;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

import static com.willwinder.ugs.platform.surfacescanner.Utils.getRoundPosition;

public class UpdateMinMaxFromGcode extends AbstractAction implements UGSEventListener {

    private final BackendAPI backend;
    private final SurfaceScanner surfaceScanner;

    public UpdateMinMaxFromGcode(SurfaceScanner surfaceScanner) {
        this.surfaceScanner = surfaceScanner;
        this.backend = LookupService.lookup(BackendAPI.class);
        this.backend.addUGSEventListener(this);

        String title = Localization.getString("autoleveler.panel.use-file");
        putValue(NAME, title);
        putValue("menuText", title);
        putValue(Action.SHORT_DESCRIPTION, title);
    }

    @Override
    public boolean isEnabled() {
        return backend != null && backend.getProcessedGcodeFile() != null && ((backend.isConnected() && backend.isIdle()) || !backend.isConnected());
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (!isEnabled() ) {
            return;
        }

        Settings.FileStats fs = backend.getSettings().getFileStats();
        Position min = fs.minCoordinate.getPositionIn(backend.getSettings().getPreferredUnits());
        Position max = fs.maxCoordinate.getPositionIn(backend.getSettings().getPreferredUnits());

        // The scan region follows the file. Sampled points stay until the user clears them.
        AutoLevelSettings autoLevelSettings = backend.getSettings().getAutoLevelSettings();
        autoLevelSettings.setMin(getRoundPosition(min));
        autoLevelSettings.setMax(getRoundPosition(max));
        surfaceScanner.update(
                new Position(autoLevelSettings.getMinX(), autoLevelSettings.getMinY(), autoLevelSettings.getMinZ(), Units.MM),
                new Position(autoLevelSettings.getMaxX(), autoLevelSettings.getMaxY(), autoLevelSettings.getMaxZ(), Units.MM));
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof ControllerStatusEvent || evt instanceof FileStateEvent) {
            setEnabled(isEnabled());
        }
    }
}
