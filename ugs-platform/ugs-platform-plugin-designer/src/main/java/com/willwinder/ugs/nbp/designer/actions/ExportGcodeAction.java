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

import com.willwinder.ugs.nbp.designer.io.DesignWriter;
import com.willwinder.ugs.nbp.designer.io.gcode.GcodeDesignWriter;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import com.willwinder.universalgcodesender.utils.SwingHelpers;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.openide.util.ImageUtilities;

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.Optional;

/**
 * @author Joacim Breiler
 */
public class ExportGcodeAction extends AbstractDesignAction {
    public static final String SMALL_ICON_PATH = "img/export.svg";
    public static final String LARGE_ICON_PATH = "img/export24.svg";
    public static String lastDirectory = "";

    public ExportGcodeAction() {
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
        putValue("menuText", "Export Gcode");
        putValue(NAME, "Export Gcode");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Optional<File> fileOptional = SwingHelpers.createFile(lastDirectory);
        if (fileOptional.isPresent()) {
            Controller controller = ControllerFactory.getController();
            File file = new File(getFilePath(fileOptional.get()));
            lastDirectory = file.getAbsolutePath();
            DesignWriter designWriter = new GcodeDesignWriter();
            designWriter.write(file, controller);
        }
    }

    private static String getFilePath(File file) {
        String path = file.getAbsolutePath();

        // Add file extension if missing
        String extension = FilenameUtils.getExtension(file.getName());
        if (StringUtils.isEmpty(extension)) {
            path = path + ".gcode";
        }
        return path;
    }
}
