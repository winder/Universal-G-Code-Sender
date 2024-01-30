/*
    Copyright 2021-2024 Will Winder

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
package com.willwinder.ugs.nbp.core.services;

import com.willwinder.universalgcodesender.uielements.components.GcodeFileTypeFilter;
import com.willwinder.universalgcodesender.uielements.helpers.FilenameFilterAdapter;
import org.apache.commons.io.filefilter.OrFileFilter;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.filechooser.FileFilter;
import java.io.FilenameFilter;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A file filter service that keeps track of all file types that we are allowed to open.
 *
 * @see com.willwinder.ugs.nbp.core.actions.OpenAction
 * @author Joacim Breiler
 */
@ServiceProvider(service = FileFilterService.class)
public class FileFilterService {

    public final Set<FileFilter> fileFilters = new HashSet<>();

    public FileFilterService() {
        fileFilters.add(new GcodeFileTypeFilter());
    }

    public void registerFileFilter(FileFilter fileFilter) {
        fileFilters.add(fileFilter);
    }

    public FilenameFilter getFilenameFilters() {
        return new OrFileFilter(fileFilters.stream()
                .map(FilenameFilterAdapter::new)
                .collect(Collectors.toList()));
    }
}
