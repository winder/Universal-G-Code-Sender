/*
    Copyright 2026 Damian Nikodem

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
package com.willwinder.ugs.designer.model.toollibrary;

public final class ToolLibraryMigrator {
    private ToolLibraryMigrator() {
    }

    /**
     * Normalises a loaded library to the current schema. Day-one behaviour: accept anything at or
     * below {@link ToolLibraryFile#CURRENT_SCHEMA_VERSION}, stamp the version forward, and return.
     * Future schema changes (rename/remove/add fields) hook their branches in here.
     */
    public static ToolLibraryFile migrate(ToolLibraryFile file) {
        if (file == null) {
            return new ToolLibraryFile();
        }
        if (file.getSchemaVersion() > ToolLibraryFile.CURRENT_SCHEMA_VERSION) {
            // Best-effort load of a newer schema — return as-is; the service refuses to overwrite.
            return file;
        }
        file.setSchemaVersion(ToolLibraryFile.CURRENT_SCHEMA_VERSION);
        return file;
    }
}
