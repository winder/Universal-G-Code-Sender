/*
    Copyright 2022 Will Winder

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
package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.io.c2d.C2dReader;
import com.willwinder.ugs.nbp.designer.io.dxf.DxfReader;
import com.willwinder.ugs.nbp.designer.io.eagle.EaglePnpReader;
import com.willwinder.ugs.nbp.designer.io.svg.SvgReader;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import com.willwinder.ugs.nbp.designer.logic.Tool;
import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.ugs.nbp.lib.lookup.CentralLookup;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.apache.commons.lang3.StringUtils;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Optional;

/**
 * @author Joacim Breiler
 */
@ActionID(
        category = LocalizingService.CATEGORY_DESIGNER,
        id = "ToolImportAction")
@ActionRegistration(
        iconBase = ToolImportAction.SMALL_ICON_PATH,
        displayName = "Import file",
        lazy = false)
public final class ToolImportAction extends AbstractDesignAction {

    public static final String SMALL_ICON_PATH = "img/import.svg";
    public static final String LARGE_ICON_PATH = "img/import24.svg";
    private final transient Controller controller;

    public ToolImportAction() {
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
        putValue("menuText", "Import file");
        putValue(NAME, "Import file");
        this.controller = ControllerFactory.getController();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileDialog.addChoosableFileFilter(new FileNameExtensionFilter("Scalable Vector Graphics (.svg)", "svg"));
        fileDialog.addChoosableFileFilter(new FileNameExtensionFilter("Autodesk CAD (.dxf)", "dxf"));
        fileDialog.addChoosableFileFilter(new FileNameExtensionFilter("Carbide Create (.c2d)", "c2d"));
        fileDialog.addChoosableFileFilter(new FileNameExtensionFilter("Eagle (.mnt, .mnb)", "mnt", "mnb"));
        fileDialog.showOpenDialog(null);

        BackendAPI backend = CentralLookup.getDefault().lookup(BackendAPI.class);

        ThreadHelper.invokeLater(() -> {
            File f = fileDialog.getSelectedFile();
            if (f != null) {

                Optional<Design> optionalDesign = Optional.empty();
                if (StringUtils.endsWithIgnoreCase(f.getName(), ".svg")) {
                    SvgReader svgReader = new SvgReader();
                    optionalDesign = svgReader.read(f);
                } else if (StringUtils.endsWithIgnoreCase(f.getName(), ".dxf")) {
                    DxfReader reader = new DxfReader(backend.getSettings());
                    optionalDesign = reader.read(f);
                } else if (StringUtils.endsWithIgnoreCase(f.getName(), ".c2d")) {
                    C2dReader reader = new C2dReader();
                    optionalDesign = reader.read(f);
                } else if (StringUtils.endsWithIgnoreCase(f.getName(), ".mnt") || StringUtils.endsWithIgnoreCase(f.getName(), ".mnb")) {
                    EaglePnpReader reader = new EaglePnpReader();
                    optionalDesign = reader.read(f);
                }

                if (optionalDesign.isPresent()) {
                    Design design = optionalDesign.get();
                    controller.setTool(Tool.SELECT);
                    controller.addEntities(design.getEntities());
                    controller.getSelectionManager().addSelection(design.getEntities());
                    controller.getDrawing().repaint();
                } else {
                    throw new RuntimeException("Could not open: " + f.getName());
                }
            }
        });
    }
}
