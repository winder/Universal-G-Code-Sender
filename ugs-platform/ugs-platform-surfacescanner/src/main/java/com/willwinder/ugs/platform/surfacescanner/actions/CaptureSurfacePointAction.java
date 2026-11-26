/*
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
import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

/**
 * Records the current work coordinate as a manually sampled surface point.
 * Touch plate thickness is subtracted from Z, as it is for an automatic probe.
 */
public class CaptureSurfacePointAction extends AbstractAction implements UGSEventListener {
    public static final String ICON_BASE = "com/willwinder/ugs/platform/surfacescanner/icons/capture.svg";
    private final SurfaceScanner surfaceScanner;
    private final BackendAPI backend;

    public CaptureSurfacePointAction(SurfaceScanner surfaceScanner) {
        this.surfaceScanner = surfaceScanner;
        this.backend = LookupService.lookup(BackendAPI.class);
        this.backend.addUGSEventListener(this);

        String title = Localization.getString("autoleveler.panel.capture-point");
        putValue(NAME, title);
        putValue("menuText", title);
        putValue(SHORT_DESCRIPTION, Localization.getString("autoleveler.panel.capture-point.tooltip"));
        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        setEnabled(isEnabled());
    }

    @Override
    public boolean isEnabled() {
        return backend != null && backend.isConnected() && backend.isIdle();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Position position = backend.getWorkPosition();
        try {
            if (position == null || position.getUnits() == null || position.getUnits() == Units.UNKNOWN) {
                throw new IllegalArgumentException("A known work position is required.");
            }
            surfaceScanner.captureManualPoint(position);
        } catch (IllegalArgumentException ex) {
            GUIHelpers.displayErrorDialog(Localization.getString("autoleveler.panel.capture-invalid"));
        }
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof ControllerStatusEvent) {
            setEnabled(isEnabled());
        }
    }
}
