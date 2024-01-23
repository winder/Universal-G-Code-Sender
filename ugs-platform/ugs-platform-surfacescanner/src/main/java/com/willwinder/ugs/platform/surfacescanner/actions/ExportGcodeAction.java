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
package com.willwinder.ugs.platform.surfacescanner.actions;

import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.platform.surfacescanner.SurfaceScanner;
import static com.willwinder.ugs.platform.surfacescanner.Utils.createCommandProcessor;
import static com.willwinder.ugs.platform.surfacescanner.Utils.fileChooser;
import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.processors.CommandProcessorList;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserUtils;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.FileStateEvent;
import com.willwinder.universalgcodesender.uielements.components.GcodeFileTypeFilter;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.SimpleGcodeStreamWriter;
import org.apache.commons.io.FilenameUtils;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JFileChooser;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An action that will export the currently loaded gcode with the applied surface scan
 *
 * @author Joacim Breiler
 */
public class ExportGcodeAction extends AbstractAction implements UGSEventListener {
    public static final String ICON_BASE = "com/willwinder/ugs/platform/surfacescanner/icons/export.svg";
    private static final Logger LOGGER = Logger.getLogger(ExportGcodeAction.class.getSimpleName());
    private final transient SurfaceScanner surfaceScanner;
    private final transient BackendAPI backend;

    public ExportGcodeAction(SurfaceScanner surfaceScanner) {
        this.surfaceScanner = surfaceScanner;
        backend = CentralLookup.getDefault().lookup(BackendAPI.class);
        backend.addUGSEventListener(this);

        String title = Localization.getString("autoleveler.panel.export");
        putValue(NAME, title);
        putValue("menuText", title);
        putValue(Action.SHORT_DESCRIPTION, Localization.getString("autoleveler.panel.export.tooltip"));
        putValue("iconBase", ICON_BASE);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_BASE, false));

        setEnabled(isEnabled());
        this.surfaceScanner.addListener(() -> setEnabled(isEnabled()));
    }

    private static Optional<File> chooseSaveFile(File file) {
        fileChooser.setFileFilter(new GcodeFileTypeFilter());
        fileChooser.setSelectedFile(file);
        int result = fileChooser.showSaveDialog(null);
        if (result != JFileChooser.APPROVE_OPTION) {
            return Optional.empty();
        }
        File selectedFile = fileChooser.getSelectedFile();
        if (!selectedFile.getName().endsWith(".gcode")) {
            selectedFile = new File(selectedFile.getAbsolutePath() + ".gcode");
        }
        return Optional.of(selectedFile);
    }

    private Optional<File> getExportFile() {
        File loadedFile = backend.getGcodeFile();
        File file = new File(loadedFile.getPath() + File.separator + FilenameUtils.removeExtension(loadedFile.getName()) + "-autoleveled.gcode");
        return chooseSaveFile(file);
    }

    @Override
    public boolean isEnabled() {
        return surfaceScanner.isValid() && backend.getGcodeFile() != null;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Optional<File> selectedFile = getExportFile();
        if (selectedFile.isEmpty()) {
            return;
        }

        CommandProcessorList commandProcessor = createCommandProcessor(backend.getSettings().getAutoLevelSettings(), surfaceScanner);
        File gcodeFile = backend.getGcodeFile();
        GcodeParser gcp = new GcodeParser();
        gcp.addCommandProcessor(commandProcessor);

        try (SimpleGcodeStreamWriter gcw = new SimpleGcodeStreamWriter(selectedFile.get())) {
            GcodeParserUtils.processAndExport(gcp, gcodeFile, gcw);
        } catch (IOException | GcodeParserException ex) {
            LOGGER.log(Level.SEVERE, "Could not export gcode", ex);
            GUIHelpers.displayErrorDialog("Could not export the loaded gcode: " + ex.getMessage());
        }
    }

    @Override
    public void UGSEvent(UGSEvent evt) {
        if (evt instanceof FileStateEvent) {
            setEnabled(isEnabled());
        }
    }
}
