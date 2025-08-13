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

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.platform.surfacescanner.SurfaceScanner;
import static com.willwinder.ugs.platform.surfacescanner.Utils.getRoundPosition;
import static com.willwinder.ugs.platform.surfacescanner.Utils.removeProbeData;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.utils.Settings;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

public class UpdateMinMaxFromGcode extends AbstractAction implements UGSEventListener {

    private final BackendAPI backend;
    private final SurfaceScanner surfaceScanner;

    public UpdateMinMaxFromGcode(SurfaceScanner surfaceScanner) {
        this.surfaceScanner = surfaceScanner;
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);
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

        if (!removeProbeData(surfaceScanner)) {
            return;
        }

        Settings.FileStats fs = backend.getSettings().getFileStats();
        Position min = fs.minCoordinate.getPositionIn(backend.getSettings().getPreferredUnits());
        Position max = fs.maxCoordinate.getPositionIn(backend.getSettings().getPreferredUnits());

        // We need to round the positions
        backend.getSettings().getAutoLevelSettings().setMin(getRoundPosition(min));
        backend.getSettings().getAutoLevelSettings().setMax(getRoundPosition(max));

        // Reset the scanner
        surfaceScanner.reset();
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof ControllerStatusEvent || evt instanceof FileStateEvent) {
            setEnabled(isEnabled());
        }
    }
}
