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

import javax.swing.filechooser.FileFilter;
import java.util.List;

/**
 * Can be implemented by a {@link org.openide.loaders.SaveAsCapable} cookie to contribute
 * selectable file formats to the "Save as..." file chooser presented by {@link SaveAsAction}.
 * <p>
 * When the currently active document exposes this interface the chooser will offer the given
 * filters and make sure the chosen file name ends with the extension of the selected filter
 * before {@link org.openide.loaders.SaveAsCapable#saveAs} is invoked. Documents that do not
 * implement it keep the plain, format-less chooser.
 *
 * @author Joacim Breiler
 */
public interface SaveAsFileFilterProvider {
    List<FileFilter> getSaveAsFileFilters();
}
