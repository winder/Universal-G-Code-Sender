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

import com.willwinder.ugs.platform.surfacescanner.io.XyzFileFilter;
import com.willwinder.ugs.platform.surfacescanner.SurfaceScanner;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.Position;
import org.apache.commons.io.IOUtils;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Optional;

import static com.willwinder.ugs.platform.surfacescanner.Utils.fileChooser;

public class SaveScannedSurfaceAction extends AbstractAction {
    public static final String ICON_BASE = "com/willwinder/ugs/platform/surfacescanner/icons/save.svg";

    private final SurfaceScanner surfaceScanner;

    public SaveScannedSurfaceAction(SurfaceScanner surfaceScanner) {
        this.surfaceScanner = surfaceScanner;
        String title = Localization.getString("autoleveler.panel.save");
        putValue(NAME, title);
        putValue("menuText", title);
        putValue(Action.SHORT_DESCRIPTION, title);
        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));

        setEnabled(isEnabled());
        this.surfaceScanner.addListener(() -> setEnabled(isEnabled()));
    }

    private static Optional<File> chooseSaveFile() {
        fileChooser.setFileFilter(new XyzFileFilter());
        int result = fileChooser.showSaveDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            return Optional.empty();
        }

        File selectedFile = fileChooser.getSelectedFile();
        if (!selectedFile.getName().endsWith(".xyz")) {
            selectedFile = new File(selectedFile.getAbsolutePath() + ".xyz");
        }
        return Optional.of(selectedFile);
    }

    @Override
    public boolean isEnabled() {
        return surfaceScanner.isValid();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Optional<File> selectedFile = chooseSaveFile();
        if (!selectedFile.isPresent()) {
            return;
        }

        Position[][] grid = surfaceScanner.getProbePositionGrid();

        StringBuilder output = new StringBuilder();
        for (Position[] row : grid) {
            for (Position cell : row) {
                output.append(cell.getX()).append(" ").append(cell.getY()).append(" ").append(cell.getZ()).append("\n");
            }
        }
        try {
            IOUtils.write(output, Files.newOutputStream(selectedFile.get().toPath()), Charset.defaultCharset());
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
}
