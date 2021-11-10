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

import com.willwinder.ugs.nbp.designer.gcode.SimpleGcodeRouter;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.util.logging.Logger;

/**
 * @author Joacim Breiler
 */
public class ExportGcodeAction extends AbstractAction {
    private static final Logger LOGGER = Logger.getLogger(ExportGcodeAction.class.getSimpleName());
    private static final String SMALL_ICON_PATH = "img/export.svg";
    private static final String LARGE_ICON_PATH = "img/export24.svg";
    private final Controller controller;

    public ExportGcodeAction(Controller controller) {
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
        putValue("menuText", "Export Gcode");
        putValue(NAME, "Export Gcode");

        this.controller = controller;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        SimpleGcodeRouter simpleGcodeRouter = new SimpleGcodeRouter();
        simpleGcodeRouter.setSafeHeight(controller.getSettings().getSafeHeight());
        simpleGcodeRouter.setFeedSpeed(controller.getSettings().getFeedSpeed());
        simpleGcodeRouter.setPlungeSpeed(controller.getSettings().getPlungeSpeed());
        String gcode = simpleGcodeRouter.toGcode(controller.getDrawing().getEntities());
        LOGGER.info(gcode);
    }
}
