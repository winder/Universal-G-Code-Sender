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
package com.willwinder.ugs.designer.actions;

import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.universalgcodesender.utils.SvgIconLoader;

import java.awt.event.ActionEvent;

/**
 * Selects a tool of the designer.
 */
public abstract class AbstractToolAction extends AbstractDesignAction {
    private final transient Controller controller;
    private final Tool tool;

    protected AbstractToolAction(Tool tool, String iconPath, String name, String description) {
        this.tool = tool;
        putValue("iconBase", iconPath);
        putValue(SMALL_ICON, SvgIconLoader.loadImageIcon(iconPath, SvgIconLoader.SIZE_SMALL).orElse(null));
        putValue(LARGE_ICON_KEY, SvgIconLoader.loadImageIcon(iconPath, SvgIconLoader.SIZE_MEDIUM).orElse(null));
        putValue("menuText", name);
        putValue(NAME, name);
        putValue(SHORT_DESCRIPTION, description);
        this.controller = ControllerFactory.getController();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        controller.setTool(tool);
    }
}
