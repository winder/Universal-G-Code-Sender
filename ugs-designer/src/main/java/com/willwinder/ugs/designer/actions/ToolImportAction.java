/*
    Copyright 2022-2026 Joacim Breiler

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
package com.willwinder.ugs.designer.actions;

import com.willwinder.ugs.designer.io.DesignFileImporter;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.utils.SvgIconLoader;
import com.willwinder.universalgcodesender.utils.SwingHelpers;
import com.willwinder.universalgcodesender.utils.ThreadHelper;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 * @author Joacim Breiler
 */
public class ToolImportAction extends AbstractDesignAction {
    private static final Logger LOGGER = Logger.getLogger(ToolImportAction.class.getName());

    public static final FileNameExtensionFilter[] FILE_NAME_EXTENSION_FILTERS = Stream.concat(
                    Stream.of(DesignFileImporter.ALL_SUPPORTED_FORMATS),
                    DesignFileImporter.FILE_FORMATS.stream())
            .map(format -> new FileNameExtensionFilter(format.description(), format.extensions().toArray(new String[0])))
            .toArray(FileNameExtensionFilter[]::new);

    public static final String SMALL_ICON_PATH = "img/import.svg";
    public static final String LARGE_ICON_PATH = "img/import24.svg";
    private final transient Controller controller;

    public ToolImportAction() {
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, SvgIconLoader.loadImageIcon(SMALL_ICON_PATH, SvgIconLoader.SIZE_SMALL).orElse(null));
        putValue(LARGE_ICON_KEY, SvgIconLoader.loadImageIcon(SMALL_ICON_PATH, SvgIconLoader.SIZE_MEDIUM).orElse(null));
        putValue("menuText", "Import file");
        putValue(NAME, "Import file");
        this.controller = ControllerFactory.getController();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileDialog.setAcceptAllFileFilterUsed(false);
        Arrays.asList(FILE_NAME_EXTENSION_FILTERS).forEach(fileDialog::addChoosableFileFilter);
        fileDialog.showOpenDialog(SwingHelpers.getRootFrame());

        BackendAPI backend = LookupService.lookup(BackendAPI.class);

        ThreadHelper.invokeLater(() -> {
            File f = fileDialog.getSelectedFile();
            if (f != null) {
                try {
                    DesignFileImporter.importFile(controller, backend.getSettings(), f);
                } catch (Exception exception) {
                    LOGGER.log(Level.SEVERE, "Could not import file " + f.getName(), exception);
                }
            }
        });
    }
}
