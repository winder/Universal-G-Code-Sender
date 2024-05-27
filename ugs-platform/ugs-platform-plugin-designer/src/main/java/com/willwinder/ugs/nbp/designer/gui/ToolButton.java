/*
    Copyright 2024 Will Winder

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
package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.entities.cuttable.CutType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.model.UnitUtils;

/**
 * A button that displays the currently used tool in the design
 *
 * @author Joacim Breiler
 */
public class ToolButton extends PanelButton {
    private final transient Controller controller;

    public ToolButton(Controller controller) {
        super("", "");
        this.controller = controller;
        controller.getSettings().addListener(this::updateText);
        controller.getDrawing().addListener(e -> updateText());
        controller.getDrawing().getRootEntity().addListener(e -> updateText());
        updateText();
    }

    private static boolean isMillOperation(Cuttable c) {
        return c.getCutType() == CutType.ON_PATH || c.getCutType() == CutType.CENTER_DRILL || c.getCutType() == CutType.INSIDE_PATH || c.getCutType() == CutType.OUTSIDE_PATH || c.getCutType() == CutType.POCKET;
    }

    private static boolean isLaserOperation(Cuttable c) {
        return c.getCutType() == CutType.LASER_FILL || c.getCutType() == CutType.LASER_ON_PATH;
    }

    private void updateText() {
        setTitle("Tool");
        boolean hasLaserOperations = controller.getDrawing().getEntities().stream()
                .filter(Cuttable.class::isInstance)
                .map(e -> (Cuttable) e)
                .anyMatch(ToolButton::isLaserOperation);

        boolean hasMillOperations = controller.getDrawing().getEntities().stream()
                .filter(Cuttable.class::isInstance)
                .map(e -> (Cuttable) e)
                .anyMatch(ToolButton::isMillOperation);


        boolean hasMixedOperations = hasLaserOperations && hasMillOperations;

        if (hasMixedOperations) {
            setText("Mixed");
        } else if (hasLaserOperations) {
            setText("Laser");
        } else {
            setText(getMillToolDescription());
        }
    }

    public String getMillToolDescription() {
        double scale = UnitUtils.scaleUnits(UnitUtils.Units.MM, controller.getSettings().getPreferredUnits());
        return Utils.formatter.format(controller.getSettings().getToolDiameter() * scale) + " " + controller.getSettings().getPreferredUnits().abbreviation;
    }
}
