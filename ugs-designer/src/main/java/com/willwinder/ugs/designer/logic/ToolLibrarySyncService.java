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
package com.willwinder.ugs.designer.logic;

import com.willwinder.ugs.designer.gui.toollibrary.ToolConflictDialog;
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;

import java.awt.Window;
import java.util.Optional;

/**
 * Reconciles a project-embedded {@link ToolDefinition} against the user's Tool Library at load
 * time. Four cases, each maps to a deterministic outcome the caller applies to the session's
 * {@link Settings}:
 *
 *   - no project tool   → NO_OP
 *   - matching library  → APPLY_PROJECT_TOOL (implicit — already the same)
 *   - differing library → prompt user
 *   - unknown id        → prompt user
 */
public class ToolLibrarySyncService {

    public enum Outcome {
        NO_OP,
        APPLY_PROJECT_TOOL,
        APPLY_LIBRARY_TOOL,
        LIBRARY_UPDATED_FROM_PROJECT,
        IMPORTED_TO_LIBRARY
    }

    public record Result(Outcome outcome, ToolDefinition toolToApply) {}

    private final ToolLibraryService library;

    public ToolLibrarySyncService(ToolLibraryService library) {
        this.library = library;
    }

    /**
     * Pure resolution — returns what should happen without performing any UI prompts. Used by
     * tests and by {@link #resolveOnLoad(Window, ToolDefinition)} after the user picks a path.
     */
    public Result resolveDeterministic(ToolDefinition projectTool) {
        if (projectTool == null || projectTool.getId() == null) {
            return new Result(Outcome.NO_OP, null);
        }
        Optional<ToolDefinition> existing = library.getById(projectTool.getId());
        if (existing.isEmpty()) {
            return new Result(Outcome.APPLY_PROJECT_TOOL, projectTool);
        }
        ToolDefinition libraryTool = existing.get();
        if (libraryTool.matchesValues(projectTool)) {
            return new Result(Outcome.NO_OP, libraryTool);
        }
        // Conflict — caller must prompt.
        return new Result(Outcome.APPLY_PROJECT_TOOL, projectTool);
    }

    /**
     * Interactive resolution. Returns the tool that should become the session's active tool, or
     * {@code null} if no change is needed. Side-effect: may mutate the library (import/update).
     */
    public ToolDefinition resolveOnLoad(Window parent, ToolDefinition projectTool) {
        if (projectTool == null || projectTool.getId() == null) {
            return null;
        }
        Optional<ToolDefinition> existing = library.getById(projectTool.getId());
        if (existing.isEmpty()) {
            ToolConflictDialog.Resolution choice = ToolConflictDialog.promptForMissing(parent, projectTool);
            return switch (choice) {
                case IMPORT_TO_LIBRARY -> library.addTool(cleanForImport(projectTool));
                case USE_PROJECT_ONLY -> projectTool;
                default -> null;
            };
        }
        ToolDefinition libraryTool = existing.get();
        if (libraryTool.matchesValues(projectTool)) {
            return libraryTool;
        }
        ToolConflictDialog.Resolution choice = ToolConflictDialog.promptForMismatch(parent, libraryTool, projectTool);
        return switch (choice) {
            case USE_PROJECT_FOR_SESSION -> projectTool;
            case UPDATE_LIBRARY_FROM_PROJECT -> {
                ToolDefinition merged = new ToolDefinition(projectTool);
                merged.setId(libraryTool.getId());
                merged.setBuiltIn(libraryTool.isBuiltIn());
                merged.setCustomSentinel(libraryTool.isCustomSentinel());
                yield library.updateTool(merged);
            }
            case KEEP_LIBRARY -> libraryTool;
            default -> null;
        };
    }

    private ToolDefinition cleanForImport(ToolDefinition projectTool) {
        ToolDefinition copy = new ToolDefinition(projectTool);
        copy.setBuiltIn(false);
        copy.setCustomSentinel(false);
        return copy;
    }
}
