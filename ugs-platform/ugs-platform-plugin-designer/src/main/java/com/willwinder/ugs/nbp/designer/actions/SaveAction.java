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

import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.openide.util.ImageUtilities;

import javax.swing.JFileChooser;
import java.awt.event.ActionEvent;
import java.io.File;

/**
 * @author Joacim Breiler
 */
public class SaveAction extends AbstractDesignAction {
    private static final String ICON_SMALL_PATH = "img/save.svg";
    private static final String ICON_LARGE_PATH = "img/save24.svg";
    private final transient Controller controller;

    public SaveAction(Controller controller) {
        putValue("iconBase", ICON_SMALL_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_SMALL_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(ICON_LARGE_PATH, false));
        putValue("menuText", "Save");
        putValue(NAME, "Save");
        this.controller = controller;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        controller.getSelectionManager().clearSelection();

        JFileChooser fileDialog = new JFileChooser();
        fileDialog.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileDialog.setDialogType(JFileChooser.CUSTOM_DIALOG);
        fileDialog.addChoosableFileFilter(OpenAction.DESIGN_FILE_FILTER);

        fileDialog.setSelectedFile(new File("out.ugsd"));
        fileDialog.showSaveDialog(null);

        File f = fileDialog.getSelectedFile();
        if (f != null) {
            ThreadHelper.invokeLater(() -> controller.saveFile(f));
        }
    }
}
