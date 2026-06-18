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

import com.willwinder.universalgcodesender.model.BackendAPI;

import java.io.File;

/**
 * The default {@link FileLoader} which loads the gcode file directly into the {@link BackendAPI}.
 * This is used when no other loader has been registered.
 *
 * @author Joacim Breiler
 */
public class BackendFileLoader implements FileLoader {
    private final BackendAPI backend;

    public BackendFileLoader(BackendAPI backend) {
        this.backend = backend;
    }

    @Override
    public void openFile(File file) throws Exception {
        backend.setGcodeFile(file);
    }
}
