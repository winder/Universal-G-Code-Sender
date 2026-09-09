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
package com.willwinder.universalgcodesender.fx.component.visualizer.simulation;

import com.willwinder.ugs.designer.logic.ToolLibraryService;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Resolves {@code T} words against the designer tool library. A tool number that matches a
 * library tool uses that tool. Anything else, including programs that never select a tool, falls
 * back to the configured default tool, then to the library tool with the lowest tool number, and
 * finally to {@link ToolProfile#DEFAULT}.
 */
public final class LibraryToolResolver implements ToolResolver {
    private final ToolLibraryService library;
    private final String defaultToolId;
    private final Map<Integer, ResolvedTool> cache = new ConcurrentHashMap<>();

    /**
     * @param defaultToolId the id of the library tool to use when the program gives no usable tool
     *                      number, or null or empty to pick one from the library
     */
    public LibraryToolResolver(ToolLibraryService library, String defaultToolId) {
        this.library = library;
        this.defaultToolId = defaultToolId == null ? "" : defaultToolId;
    }

    @Override
    public ResolvedTool describe(CutSegment cut) {
        return cache.computeIfAbsent(cut.toolNumber(), this::resolveToolNumber);
    }

    /**
     * The library tool in the given slot, if there is one.
     */
    public Optional<ResolvedTool> byToolNumber(int toolNumber) {
        if (toolNumber <= ToolDefinition.UNASSIGNED_TOOL_NUMBER) {
            return Optional.empty();
        }
        return library.getByToolNumber(toolNumber)
                .filter(LibraryToolResolver::hasDiameter)
                .map(tool -> ResolvedTool.ofDefinition(tool, ResolvedTool.Source.PROGRAM_SLOT));
    }

    private ResolvedTool resolveToolNumber(int toolNumber) {
        Optional<ResolvedTool> tool = byToolNumber(toolNumber);
        if (tool.isEmpty() && !defaultToolId.isBlank()) {
            tool = library.getById(defaultToolId)
                    .filter(LibraryToolResolver::hasDiameter)
                    .map(definition -> ResolvedTool.ofDefinition(definition, ResolvedTool.Source.DEFAULT_TOOL));
        }
        if (tool.isEmpty()) {
            tool = library.getTools().stream()
                    .filter(LibraryToolResolver::hasDiameter)
                    .min(Comparator.comparingInt(LibraryToolResolver::sortableToolNumber))
                    .map(definition -> ResolvedTool.ofDefinition(definition, ResolvedTool.Source.LIBRARY_FIRST));
        }
        return tool.orElse(ResolvedTool.of(ToolProfile.DEFAULT, ResolvedTool.Source.BUILT_IN, ToolProfile.DEFAULT.toString()))
                .withRequestedToolNumber(toolNumber);
    }

    private static boolean hasDiameter(ToolDefinition tool) {
        return tool.getDiameter() > 0;
    }

    /**
     * Sorts tools with a slot first, in slot order, and tools without one last.
     */
    private static int sortableToolNumber(ToolDefinition tool) {
        return tool.hasToolNumber() ? tool.getToolNumber() : Integer.MAX_VALUE;
    }
}
