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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Resolves tools for one program, using what the program itself says about its tools.
 *
 * <p>The designer records the cutter a program was posted for as a {@code ; Tool: ...} comment,
 * with or without a preceding {@code M6 T} word. A {@code T} word that matches a library slot wins.
 * Otherwise the most recent tool comment before the segment is used: first by looking the
 * description up as a library tool name, then by reading the diameter and shape out of the
 * description. Programs without any of that fall through to the given fallback resolver.
 */
public final class ProgramToolResolver implements ToolResolver {
    private static final Pattern TOOL_COMMENT = Pattern.compile("[;(]\\s*Tool:\\s*([^;()]+?)\\s*\\)?\\s*$", Pattern.CASE_INSENSITIVE);

    /**
     * A tool description found in the program, in force from {@code commandNumber} onwards.
     */
    public record ToolComment(int commandNumber, String description) {
    }

    private final List<ToolComment> comments;
    private final LibraryToolResolver library;
    private final ToolLibraryService libraryService;
    private final Map<String, Optional<ResolvedTool>> described = new ConcurrentHashMap<>();
    private final Map<Integer, Optional<ResolvedTool>> slots = new ConcurrentHashMap<>();

    public ProgramToolResolver(List<ToolComment> comments, ToolLibraryService libraryService, LibraryToolResolver library) {
        this.comments = List.copyOf(comments);
        this.libraryService = libraryService;
        this.library = library;
    }

    /**
     * Reads the tool comments out of a program file. Commands are numbered from zero in file order,
     * the same way the visualizer parser numbers the segments it produces from a plain file.
     */
    public static ProgramToolResolver forProgram(Path file, ToolLibraryService libraryService, LibraryToolResolver library) throws IOException {
        String text = Files.readString(file);
        return new ProgramToolResolver(findToolComments(text.lines().toList()), libraryService, library);
    }

    public static List<ToolComment> findToolComments(List<String> lines) {
        List<ToolComment> result = new ArrayList<>();
        for (int i = 0; i < lines.size(); i++) {
            Matcher matcher = TOOL_COMMENT.matcher(lines.get(i));
            if (matcher.find()) {
                result.add(new ToolComment(i, matcher.group(1).trim()));
            }
        }
        return result;
    }

    public List<ToolComment> comments() {
        return comments;
    }

    @Override
    public ResolvedTool describe(CutSegment cut) {
        Optional<ResolvedTool> slot = slots.computeIfAbsent(cut.toolNumber(), library::byToolNumber);
        if (slot.isPresent()) {
            return slot.get();
        }
        ToolComment comment = commentInForce(cut.commandNumber());
        if (comment != null) {
            Optional<ResolvedTool> tool = described.computeIfAbsent(comment.description(), this::describe);
            if (tool.isPresent()) {
                return tool.get().withRequestedToolNumber(cut.toolNumber());
            }
        }
        return library.describe(cut);
    }

    private ToolComment commentInForce(int commandNumber) {
        ToolComment result = null;
        for (ToolComment comment : comments) {
            if (comment.commandNumber() > commandNumber) {
                break;
            }
            result = comment;
        }
        return result;
    }

    private Optional<ResolvedTool> describe(String description) {
        Optional<ResolvedTool> named = libraryService.getTools().stream()
                .filter(tool -> tool.getName() != null && tool.getName().trim().equalsIgnoreCase(description))
                .filter(tool -> tool.getDiameter() > 0)
                .findFirst()
                .map(tool -> ResolvedTool.ofDefinition(tool, ResolvedTool.Source.PROGRAM_COMMENT_LIBRARY));
        if (named.isPresent()) {
            return named;
        }
        return ToolDescriptionParser.parse(description)
                .map(profile -> ResolvedTool.of(profile, ResolvedTool.Source.PROGRAM_COMMENT, description));
    }
}
