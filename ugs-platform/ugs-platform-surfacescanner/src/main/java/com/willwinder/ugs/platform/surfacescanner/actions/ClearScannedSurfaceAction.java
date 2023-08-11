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
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * An action for clearing the scanned surface
 *
 * @author Joacim Breiler
 */
public class ClearScannedSurfaceAction extends AbstractAction implements UGSEventListener {

    public static final String ICON_BASE = "com/willwinder/ugs/platform/surfacescanner/icons/delete.svg";
    private final SurfaceScanner surfaceScanner;
    private final BackendAPI backend;

    public ClearScannedSurfaceAction(SurfaceScanner surfaceScanner) {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        this.backend.addUGSEventListener(this);

        this.surfaceScanner = surfaceScanner;
        this.surfaceScanner.addListener(() -> setEnabled(isEnabled()));

        String title = Localization.getString("autoleveler.panel.clear");
        putValue(NAME, title);
        putValue("menuText", title);
        putValue(Action.SHORT_DESCRIPTION, title);
        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
        putValue(Action.SELECTED_KEY, false);
    }

    @Override
    public boolean isEnabled() {
        return ((backend.isConnected() && backend.isIdle()) || !backend.isConnected()) && surfaceScanner.isValid();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        surfaceScanner.reset();
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof ControllerStatusEvent) {
            setEnabled(isEnabled());
        }
    }
}
