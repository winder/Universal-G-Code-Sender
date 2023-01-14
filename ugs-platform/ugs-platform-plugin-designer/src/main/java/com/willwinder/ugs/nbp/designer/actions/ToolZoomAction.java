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
import org.openide.awt.StatusDisplayer;
import org.openide.util.ImageUtilities;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Joacim Breiler
 */
@ActionID(
        category = LocalizingService.CATEGORY_DESIGNER,
        id = "ToolZoomAction")
@ActionRegistration(
        iconBase = ToolZoomAction.ICON_SMALL_PATH,
        displayName = "Zoom",
        lazy = false)
public class ToolZoomAction extends AbstractAction {
    public static final String ICON_SMALL_PATH = "img/zoom.svg";
    private static final String ICON_LARGE_PATH = "img/zoom24.svg";

    public ToolZoomAction() {
        putValue("iconBase", ICON_SMALL_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(ICON_SMALL_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(ICON_LARGE_PATH, false));
        putValue("menuText", "Zoom");
        putValue(NAME, "Zoom");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        StatusDisplayer.getDefault().setStatusText("Click to Zoom in, SHIFT + Click to Zoom out");
        ControllerFactory.getController().setTool(Tool.ZOOM);
    }
}
