/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import com.willwinder.ugs.nbp.designer.logic.Tool;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

import java.awt.event.ActionEvent;

/**
 * @author Joacim Breiler
 */
@ActionID(
        category = LocalizingService.CATEGORY_DESIGNER,
        id = "ToolDrawCircleAction")
@ActionRegistration(
        iconBase = ToolDrawCircleAction.ICON_SMALL_PATH,
        displayName = "Draw ellipse",
        lazy = false)
public class ToolDrawCircleAction extends AbstractDesignAction {
    public static final String ICON_SMALL_PATH = "img/circle.svg";
    public static final String ICON_LARGE_PATH = "img/circle24.svg";

    public ToolDrawCircleAction() {
        putValue("iconBase", ICON_SMALL_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_SMALL_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(ICON_LARGE_PATH, false));
        putValue("menuText", "Draw ellipse");
        putValue(NAME, "Draw ellipse");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        Controller controller = ControllerFactory.getController();
        controller.setTool(Tool.CIRCLE);
    }
}
