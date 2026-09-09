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
package com.willwinder.universalgcodesender.fx.service;

import com.willwinder.universalgcodesender.fx.component.visualizer.simulation.DesignTool;
import com.willwinder.universalgcodesender.fx.component.visualizer.simulation.LibraryToolResolver;
import com.willwinder.universalgcodesender.fx.component.visualizer.simulation.ToolResolver;
import com.willwinder.universalgcodesender.fx.model.UgsdWorkspaceContext;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

/**
 * Decides how the tools of a program are resolved: from the design settings when the program is
 * the export of the open design, otherwise from its {@code T} words and the tool library. Shared
 * by the stock simulation and the tool listing so both tell the same story.
 */
public final class ProgramToolResolution {
    private ProgramToolResolution() {
    }

    /**
     * Opens the tool library, which reads a file, so call it off the JavaFX thread.
     *
     * @param defaultToolId the library tool to fall back to when the program selects none, or empty
     */
    public static ToolResolver forProgram(File file, String defaultToolId) {
        Optional<ToolResolver> designTool = designToolFor(file);
        return designTool.orElseGet(() -> new LibraryToolResolver(ToolLibraryProvider.getInstance(), defaultToolId));
    }

    /**
     * When the program is the export of the open design, the tool comes straight from the design
     * settings rather than from the program text.
     */
    public static Optional<ToolResolver> designToolFor(File file) {
        return WorkspaceManager.getInstance().getActiveWorkspace()
                .filter(UgsdWorkspaceContext.class::isInstance)
                .map(UgsdWorkspaceContext.class::cast)
                .filter(workspace -> workspace.getGcodeFile().map(exported -> isSameFile(exported, file)).orElse(false))
                .flatMap(UgsdWorkspaceContext::getController)
                .map(controller -> DesignTool.resolver(controller.getSettings()));
    }

    static boolean isSameFile(File a, File b) {
        try {
            return a.getCanonicalFile().equals(b.getCanonicalFile());
        } catch (IOException e) {
            return a.getAbsoluteFile().equals(b.getAbsoluteFile());
        }
    }
}
