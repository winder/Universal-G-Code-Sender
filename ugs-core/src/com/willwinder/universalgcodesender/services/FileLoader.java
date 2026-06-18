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
package com.willwinder.universalgcodesender.services;

import java.io.File;

/**
 * Opens a file into the application.
 *
 * <p>This abstracts <em>how</em> a file is opened so that different editions can plug in their own
 * behaviour. The JavaFX edition opens the file as a workspace, the platform edition opens it through
 * its {@code OpenFileAction}, and when no loader has been registered the file is loaded directly into
 * the {@link com.willwinder.universalgcodesender.model.BackendAPI} by {@link BackendFileLoader}.
 *
 * <p>Register an implementation through {@link LookupService#register(Object)} during application
 * startup. Callers that want to open a file should resolve the registered loader, for example:
 *
 * <pre>{@code
 * FileLoader loader = LookupService.lookupOptional(FileLoader.class)
 *         .orElseGet(() -> new BackendFileLoader(backend));
 * loader.openFile(file);
 * }</pre>
 *
 * @author Joacim Breiler
 */
public interface FileLoader {

    /**
     * Opens the given file.
     *
     * @param file the file to open
     * @throws Exception if the file could not be opened
     */
    void openFile(File file) throws Exception;
}
