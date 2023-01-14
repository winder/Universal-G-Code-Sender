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

import com.willwinder.ugs.nbp.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.nbp.designer.io.DesignReader;
import com.willwinder.ugs.nbp.designer.io.c2d.C2dReader;
import com.willwinder.ugs.nbp.designer.io.svg.SvgReader;
import com.willwinder.ugs.nbp.designer.io.ugsd.UgsDesignReader;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import com.willwinder.ugs.nbp.designer.model.Design;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.openide.util.ImageUtilities;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Optional;

public class OpenAction extends AbstractDesignAction {
    public static final FileFilter DESIGN_FILE_FILTER = new FileNameExtensionFilter("UGS Design (ugsd)", "ugsd");
    private static final String ICON_SMALL_PATH = "img/open.svg";
    private static final String ICON_LARGE_PATH = "img/open24.svg";
    private static final FileFilter SVG_FILE_FILTER = new FileNameExtensionFilter("Scalable Vector Graphics (svg)", "svg");
    private static final FileFilter C2D_FILE_FILTER = new FileNameExtensionFilter("Carbide Create (c2d)", "c2d");
    private final JFileChooser fileChooser;

    public OpenAction() {
        putValue("iconBase", ICON_SMALL_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_SMALL_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(ICON_LARGE_PATH, false));
        putValue("menuText", "Open");
        putValue(NAME, "Open");

        fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.addChoosableFileFilter(DESIGN_FILE_FILTER);
        fileChooser.addChoosableFileFilter(SVG_FILE_FILTER);
        fileChooser.addChoosableFileFilter(C2D_FILE_FILTER);
        fileChooser.setFileFilter(DESIGN_FILE_FILTER);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        UndoManager undoManager = ControllerFactory.getUndoManager();
        undoManager.clear();

        SelectionManager selectionManager = ControllerFactory.getSelectionManager();
        selectionManager.clearSelection();

        Controller controller = ControllerFactory.getController();
        fileChooser.showOpenDialog(null);

        ThreadHelper.invokeLater(() -> {
            DesignReader designReader = new UgsDesignReader();
            if (fileChooser.getFileFilter() == SVG_FILE_FILTER) {
                designReader = new SvgReader();
            } else if (fileChooser.getFileFilter() == C2D_FILE_FILTER) {
                designReader = new C2dReader();
            }

            File selectedFile = fileChooser.getSelectedFile();
            Optional<Design> optional = designReader.read(selectedFile);
            if (optional.isPresent()) {
                controller.setDesign(optional.get());
            } else {
                throw new RuntimeException("Could not open svg: " + selectedFile.getName());
            }
        });
    }
}
