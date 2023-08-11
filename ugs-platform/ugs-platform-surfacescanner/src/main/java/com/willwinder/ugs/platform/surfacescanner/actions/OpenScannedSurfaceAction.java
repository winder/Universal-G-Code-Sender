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
import com.willwinder.ugs.platform.surfacescanner.io.XyzFileFilter;
import com.willwinder.ugs.platform.surfacescanner.SurfaceScanner;
import com.willwinder.ugs.platform.surfacescanner.io.XyzSurfaceReader;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStatusEvent;
import com.willwinder.universalgcodesender.utils.AutoLevelSettings;
import com.willwinder.universalgcodesender.utils.MathUtils;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Optional;

import static com.willwinder.ugs.platform.surfacescanner.Utils.*;

public class OpenScannedSurfaceAction extends AbstractAction implements UGSEventListener {
    public static final String ICON_BASE = "com/willwinder/ugs/platform/surfacescanner/icons/open.svg";

    private final SurfaceScanner surfaceScanner;
    private final BackendAPI backend;

    public OpenScannedSurfaceAction(SurfaceScanner surfaceScanner) {
        this.backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        this.backend.addUGSEventListener(this);
        this.surfaceScanner = surfaceScanner;

        String title = Localization.getString("autoleveler.panel.open");
        putValue(NAME, title);
        putValue("menuText", title);
        putValue(Action.SHORT_DESCRIPTION, title);
        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));
    }

    private static Optional<File> chooseOpenFile() {
        fileChooser.setFileFilter(new XyzFileFilter());
        int result = fileChooser.showOpenDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            return Optional.empty();
        }

        File selectedFile = fileChooser.getSelectedFile();
        return Optional.of(selectedFile);
    }

    @Override
    public boolean isEnabled() {
        return (backend.isConnected() && backend.isIdle()) || !backend.isConnected();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (surfaceScanner.isValid() && !shouldEraseProbedData()) {
            return;
        }

        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        Optional<File> file = chooseOpenFile();
        if (!file.isPresent()) {
            return;
        }

        try {
            XyzSurfaceReader surfaceReader = new XyzSurfaceReader();
            List<Position> positions = surfaceReader.read(Files.newInputStream(file.get().toPath()));

            updateSettings(backend.getSettings().getAutoLevelSettings(), positions);
            updatePoints(positions);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof ControllerStatusEvent) {
            setEnabled(isEnabled());
        }
    }

    private void updatePoints(List<Position> positions) {
        Optional<Position> nextProbePoint = surfaceScanner.getNextProbePoint();
        while (nextProbePoint.isPresent()) {
            Position probePoint = nextProbePoint.get();
            Position probedPosition = positions.stream()
                    .filter(p -> MathUtils.isEqual(p.getX(), probePoint.getX(), 0.01) && MathUtils.isEqual(p.getY(), probePoint.getY(), 0.01))
                    .findFirst()
                    .orElseThrow(() -> new IllegalStateException("The supplied file is missing height data for grid point: " + probePoint));

            surfaceScanner.probeEvent(probedPosition);
            nextProbePoint = surfaceScanner.getNextProbePoint();
        }
    }

    private void updateSettings(AutoLevelSettings autoLevelSettings, List<Position> positions) {
        Position minPosition = getMinPosition(positions);
        Position maxPosition = getMaxPosition(positions);
        autoLevelSettings.setMin(minPosition);
        autoLevelSettings.setMax(maxPosition);

        double stepResolution = positions.get(1).getY() - positions.get(0).getY();
        autoLevelSettings.setStepResolution(stepResolution);

        surfaceScanner.update(minPosition, maxPosition);
        surfaceScanner.reset();
    }
}
