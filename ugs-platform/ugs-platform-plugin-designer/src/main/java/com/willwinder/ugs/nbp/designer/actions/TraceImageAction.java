/*
    Copyright 2022 Will Winder

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

import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.gui.imagetracer.ImageTracerDialog;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.logic.ControllerFactory;
import com.willwinder.ugs.nbp.designer.logic.Tool;
import com.willwinder.ugs.nbp.lib.services.LocalizingService;
import com.willwinder.universalgcodesender.utils.ThreadHelper;
import org.openide.awt.ActionID;
import org.openide.awt.ActionRegistration;
import org.openide.util.ImageUtilities;

import java.awt.event.ActionEvent;
import java.util.List;

/**
 * @author Joacim Breiler
 */
@ActionID(
        category = LocalizingService.CATEGORY_DESIGNER,
        id = "TraceImageAction")
@ActionRegistration(
        iconBase = TraceImageAction.SMALL_ICON_PATH,
        displayName = "Trace image",
        lazy = false)
public class TraceImageAction extends AbstractDesignAction {

    public static final String SMALL_ICON_PATH = "img/trace.svg";
    public static final String LARGE_ICON_PATH = "img/trace24.svg";
    private final transient Controller controller;

    public TraceImageAction() {
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));
        putValue("menuText", "Trace image");
        putValue(NAME, "Trace Image");
        this.controller = ControllerFactory.getController();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        ImageTracerDialog dialog = new ImageTracerDialog();
        ThreadHelper.invokeLater(() -> {
            List<Entity> entities = dialog.showDialog();

            if (entities != null && !entities.isEmpty()) {
                AddAction addAction = new AddAction(controller, entities);
                addAction.actionPerformed(e);
                controller.getSelectionManager().addSelection(entities);
                controller.getDrawing().repaint();
                controller.setTool(Tool.SELECT);
            }
        });
    }
}
