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
package com.willwinder.universalgcodesender.fx.component.toollibrary;

import com.willwinder.ugs.designer.gui.toollibrary.ToolShapeIcon;
import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import javafx.scene.image.ImageView;

/**
 * Icons and labels for tools, shared by the tool library dialog and the tool settings.
 */
public final class ToolShapeIcons {
    public static final int LIST_ICON_SIZE = 16;

    private ToolShapeIcons() {
    }

    /**
     * The endmill icon for a shape, sized for a list or combo box row.
     */
    public static ImageView icon(EndmillShape shape) {
        return SvgLoader.loadImageIcon(ToolShapeIcon.getIconPath(shape), LIST_ICON_SIZE).orElse(null);
    }

    /**
     * How a tool is referred to in the settings: its slot and name, such as {@code T2 · 6mm Upcut}.
     */
    public static String describe(ToolDefinition tool) {
        if (tool == null) {
            return "";
        }
        String name = tool.getName() == null ? tool.getId() : tool.getName();
        return tool.hasToolNumber() ? "T" + tool.getToolNumber() + " · " + name : name;
    }
}
