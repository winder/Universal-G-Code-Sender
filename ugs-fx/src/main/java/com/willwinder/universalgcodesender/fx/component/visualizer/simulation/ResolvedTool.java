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

import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;

import java.util.Optional;

/**
 * A tool profile together with where it came from, so the user can be told which tool a program
 * is simulated with and why.
 *
 * @param profile             the cutting profile
 * @param source              how the tool was decided
 * @param label               a short name for the tool, such as {@code T2 · 1/4" Upcut} or {@code 6mm Ball}
 * @param definition          the library tool behind the profile, when there is one
 * @param requestedToolNumber the tool number the program selected with a T word, or
 *                            {@link ToolDefinition#UNASSIGNED_TOOL_NUMBER} when it selected none.
 *                            Kept even when no library tool has that number, so the user can see
 *                            which number to assign.
 */
public record ResolvedTool(ToolProfile profile, Source source, String label, Optional<ToolDefinition> definition,
                           int requestedToolNumber) {

    public enum Source {
        PROGRAM_SLOT("Selected by the program's T word, matched to the tool library"),
        PROGRAM_COMMENT_LIBRARY("Named in the program's tool comment, matched to the tool library"),
        PROGRAM_COMMENT("Read from the program's tool comment"),
        DESIGN("The tool the design is set up with"),
        DEFAULT_TOOL("Not selected by the program, using the default tool from the visualizer settings"),
        LIBRARY_FIRST("Not selected by the program, using the first tool in the tool library"),
        BUILT_IN("Not selected by the program and the tool library is empty, using a 1/8\" flat endmill"),
        FIXED("Fixed tool");

        private final String description;

        Source(String description) {
            this.description = description;
        }

        public String description() {
            return description;
        }

        /**
         * True when the program itself did not say which tool to use.
         */
        public boolean isFallback() {
            return this == DEFAULT_TOOL || this == LIBRARY_FIRST || this == BUILT_IN;
        }
    }

    public static ResolvedTool of(ToolProfile profile, Source source, String label) {
        return new ResolvedTool(profile, source, label, Optional.empty(), ToolDefinition.UNASSIGNED_TOOL_NUMBER);
    }

    public static ResolvedTool ofDefinition(ToolDefinition tool, Source source) {
        return new ResolvedTool(ToolProfile.fromDefinition(tool), source, describe(tool), Optional.of(tool), tool.getToolNumber());
    }

    public ResolvedTool withRequestedToolNumber(int toolNumber) {
        return new ResolvedTool(profile, source, label, definition, toolNumber);
    }

    /**
     * True when the program selected a tool number that no library tool holds, so the simulation
     * is using some other tool in its place.
     */
    public boolean isRequestedToolNumberUnassigned() {
        return requestedToolNumber > ToolDefinition.UNASSIGNED_TOOL_NUMBER
                && definition.map(tool -> tool.getToolNumber() != requestedToolNumber).orElse(true);
    }

    /**
     * Why this tool is used, mentioning the requested tool number when the program asked for one
     * that the library does not have.
     */
    public String explanation() {
        if (isRequestedToolNumberUnassigned()) {
            String replacement = switch (source) {
                case DEFAULT_TOOL -> "the default tool from the visualizer settings";
                case LIBRARY_FIRST -> "the first tool in the tool library";
                case BUILT_IN -> "a 1/8\" flat endmill, since the tool library is empty";
                case PROGRAM_COMMENT, PROGRAM_COMMENT_LIBRARY -> "the tool named in the program's tool comment";
                default -> "another tool";
            };
            return "The program selects T" + requestedToolNumber + ", but no library tool has that number. Using "
                    + replacement + ". Assign T" + requestedToolNumber + " to a library tool to simulate with it.";
        }
        return source.description();
    }

    /**
     * A tool's slot and name, such as {@code T2 · 6mm Upcut}.
     */
    public static String describe(ToolDefinition tool) {
        String name = tool.getName() == null || tool.getName().isBlank() ? tool.getId() : tool.getName();
        return tool.hasToolNumber() ? "T" + tool.getToolNumber() + " · " + name : name;
    }
}
