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
package com.willwinder.ugs.nbp.designer.platform;

import com.willwinder.ugs.designer.io.svg.SvgWriter;
import com.willwinder.ugs.designer.io.ugsd.UgsDesignWriter;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.ugs.nbp.core.actions.OpenFileAction;
import com.willwinder.ugs.nbp.core.actions.SaveAsFileFilterProvider;

import org.openide.filesystems.FileObject;
import org.openide.loaders.SaveAsCapable;
import org.openide.nodes.Node;

import java.io.File;
import java.util.List;
import javax.swing.filechooser.FileFilter;
import javax.swing.filechooser.FileNameExtensionFilter;
import org.apache.commons.lang3.StringUtils;

import static com.willwinder.ugs.designer.actions.SaveAction.UGSD_EXT;

/**
 * Provides support for saving the file as... in either the UGS design or SVG format.
 *
 * @author Joacim Breiler
 */
public class UgsSaveAsCookie implements SaveAsCapable, SaveAsFileFilterProvider, Node.Cookie {

    @Override
    public List<FileFilter> getSaveAsFileFilters() {
        return List.of(
                new FileNameExtensionFilter("UGS Design (ugsd)", "ugsd"),
                new FileNameExtensionFilter("Scalable Vector Graphics (svg)", "svg"));
    }

    @Override
    public void saveAs(FileObject folder, String name) {
        if (StringUtils.endsWithIgnoreCase(name, SvgWriter.SVG_EXTENSION)) {
            File file = new File(folder.getPath(), name);
            new SvgWriter().write(file, ControllerFactory.getController());
            return;
        }

        File file = new File(folder.getPath(), StringUtils.appendIfMissing(name, UGSD_EXT));
        new UgsDesignWriter().write(file, ControllerFactory.getController());
        new OpenFileAction(file).actionPerformed(null);
    }
}
