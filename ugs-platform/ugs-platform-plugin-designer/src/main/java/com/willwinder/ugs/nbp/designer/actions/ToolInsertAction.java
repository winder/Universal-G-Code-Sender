/*
    Copyright 2021 Will Winder

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

import com.willwinder.ugs.nbp.designer.io.dxf.DxfReader;
import com.willwinder.ugs.nbp.designer.io.svg.SvgReader;
import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.Tool;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Optional;

/**
 * @author Joacim Breiler
 */
public final class ToolInsertAction extends AbstractAction {

    public static final String SMALL_ICON_PATH = "img/import.svg";
    public static final String LARGE_ICON_PATH = "img/import24.svg";
    private final Controller controller;

    public ToolInsertAction(Controller controller) {
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
        putValue("menuText", "Insert");
        putValue(NAME, "Insert");
        this.controller = controller;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileDialog.addChoosableFileFilter(new FileNameExtensionFilter(".svg", "svg"));
        fileDialog.addChoosableFileFilter(new FileNameExtensionFilter(".dxf", "dxf"));
        fileDialog.showOpenDialog(null);

        ThreadHelper.invokeLater(() -> {
            File f = fileDialog.getSelectedFile();
            if (f != null) {

                Optional<Design> optionalDesign = Optional.empty();
                if (StringUtils.endsWithIgnoreCase(f.getName(), ".svg")) {
                    SvgReader svgReader = new SvgReader();
                    optionalDesign = svgReader.read(f);
                } else if (StringUtils.endsWithIgnoreCase(f.getName(), ".dxf")) {
                    DxfReader reader = new DxfReader();
                    optionalDesign = reader.read(f);
                }

                if (optionalDesign.isPresent()) {
                    Design design = optionalDesign.get();

                    design.getEntities().forEach(entity -> {
                        controller.addEntity(entity);
                        controller.getSelectionManager().addSelection(entity);
                    });

                    controller.getDrawing().repaint();
                    controller.setTool(Tool.SELECT);
                } else {
                    throw new RuntimeException("Could not open: " + f.getName());
                }
            }
        });
    }
}
