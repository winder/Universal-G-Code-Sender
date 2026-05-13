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
package com.willwinder.ugs.designer.gui.toollibrary;

import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;

import javax.swing.JOptionPane;
import java.awt.Window;

public final class ToolConflictDialog {

    public enum Resolution {
        USE_PROJECT_FOR_SESSION,
        UPDATE_LIBRARY_FROM_PROJECT,
        KEEP_LIBRARY,
        IMPORT_TO_LIBRARY,
        USE_PROJECT_ONLY,
        CANCELLED
    }

    private ToolConflictDialog() {
    }

    /**
     * Prompts the user for how to reconcile a project-embedded tool that differs from the
     * matching entry in the user's library.
     */
    public static Resolution promptForMismatch(Window parent, ToolDefinition libraryTool, ToolDefinition projectTool) {
        String message = "The tool \"" + projectTool.getName() + "\" in this project differs from the one in your Tool Library.\n\n"
                + "How would you like to continue?";
        Object[] options = {"Use project tool (session only)", "Update library", "Keep library tool", "Cancel"};
        int choice = JOptionPane.showOptionDialog(parent, message, "Tool Library mismatch",
                JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE, null, options, options[0]);
        return switch (choice) {
            case 0 -> Resolution.USE_PROJECT_FOR_SESSION;
            case 1 -> Resolution.UPDATE_LIBRARY_FROM_PROJECT;
            case 2 -> Resolution.KEEP_LIBRARY;
            default -> Resolution.CANCELLED;
        };
    }

    /**
     * Prompts when the project references a tool id that is no longer in the library.
     */
    public static Resolution promptForMissing(Window parent, ToolDefinition projectTool) {
        String message = "The tool \"" + projectTool.getName() + "\" used by this project is not in your Tool Library.\n\n"
                + "How would you like to continue?";
        Object[] options = {"Import to library", "Use for this session only", "Cancel"};
        int choice = JOptionPane.showOptionDialog(parent, message, "Unknown Tool",
                JOptionPane.DEFAULT_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
        return switch (choice) {
            case 0 -> Resolution.IMPORT_TO_LIBRARY;
            case 1 -> Resolution.USE_PROJECT_ONLY;
            default -> Resolution.CANCELLED;
        };
    }
}
