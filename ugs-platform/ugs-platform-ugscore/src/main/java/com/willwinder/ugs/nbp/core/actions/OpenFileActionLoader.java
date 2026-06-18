/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.ugs.nbp.core.actions;

import com.willwinder.universalgcodesender.services.FileLoader;

import javax.swing.SwingUtilities;
import java.io.File;

/**
 * A {@link FileLoader} for the platform edition that opens files through the {@link OpenFileAction},
 * so that opening a file from e.g. the pendant behaves the same as opening it from the menu. The
 * action is run on the event dispatch thread since it interacts with the Swing UI.
 *
 * @author Joacim Breiler
 */
public class OpenFileActionLoader implements FileLoader {

    @Override
    public void openFile(File file) {
        SwingUtilities.invokeLater(() -> new OpenFileAction(file).actionPerformed(null));
    }
}
