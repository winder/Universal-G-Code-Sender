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

import com.willwinder.ugs.nbp.core.actions.OpenFileAction;
import com.willwinder.ugs.nbp.designer.io.ugsd.UgsDesignWriter;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import org.openide.filesystems.FileObject;
import org.openide.loaders.SaveAsCapable;
import org.openide.nodes.Node;

import java.io.File;

/**
 * Provides support for saving the file as...
 *
 * @author Joacim Breiler
 */
public class UgsSaveAsCookie implements SaveAsCapable, Node.Cookie {
    @Override
    public void saveAs(FileObject folder, String name) {
        File file = new File(folder.getPath(), name);
        UgsDesignWriter writer = new UgsDesignWriter();
        writer.write(file, ControllerFactory.getController());
        new OpenFileAction(file).actionPerformed(null);
    }
}
