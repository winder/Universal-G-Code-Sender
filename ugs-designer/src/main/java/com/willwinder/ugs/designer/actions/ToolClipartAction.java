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
package com.willwinder.ugs.designer.actions;

import com.willwinder.ugs.designer.gui.clipart.Clipart;
import com.willwinder.ugs.designer.gui.clipart.InsertClipartDialog;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.ugs.designer.logic.Tool;
import com.willwinder.universalgcodesender.utils.SvgIconLoader;
import com.willwinder.universalgcodesender.utils.ThreadHelper;

import java.awt.event.ActionEvent;
import java.util.Optional;

/**
 * @author Joacim Breiler
 */
public class ToolClipartAction extends AbstractDesignAction {

    public static final String SMALL_ICON_PATH = "img/clipart.svg";
    public static final String LARGE_ICON_PATH = "img/clipart24.svg";

    public ToolClipartAction() {
        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, SvgIconLoader.loadImageIcon(SMALL_ICON_PATH, SvgIconLoader.SIZE_SMALL).orElse(null));
        putValue(LARGE_ICON_KEY, SvgIconLoader.loadImageIcon(SMALL_ICON_PATH, SvgIconLoader.SIZE_MEDIUM).orElse(null));
        putValue("menuText", "Insert clipart");
        putValue(NAME, "Insert clipart");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        InsertClipartDialog dialog = new InsertClipartDialog();
        ThreadHelper.invokeLater(() -> {
            Optional<Clipart> optionalClipart = dialog.showDialog();
            Controller controller = ControllerFactory.getController();
            if (optionalClipart.isPresent()) {
                AddAction addAction = new AddAction(controller, optionalClipart.get().getCuttable());
                addAction.actionPerformed(e);
                controller.getSelectionManager().addSelection(optionalClipart.get().getCuttable());
                controller.getDrawing().repaint();
                controller.setTool(Tool.SELECT);
            }
        });
    }
}
