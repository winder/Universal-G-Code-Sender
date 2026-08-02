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
package com.willwinder.ugs.designer.gui.toollibrary;

import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;

public final class ToolLabels {
    private ToolLabels() {
    }

    /**
     * The name a tool is listed under, prefixed with its slot when it has one so the user can see
     * at a glance which tools their machine can call by number.
     */
    public static String describe(ToolDefinition tool) {
        if (tool == null) {
            return "";
        }
        String name = tool.getName() == null ? tool.getId() : tool.getName();
        return tool.hasToolNumber() ? "T" + tool.getToolNumber() + " · " + name : name;
    }
}
