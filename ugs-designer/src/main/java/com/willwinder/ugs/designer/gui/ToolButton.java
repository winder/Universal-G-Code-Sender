/*
    Copyright 2024-2026 Joacim Breiler

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
package com.willwinder.ugs.designer.gui;

import com.willwinder.ugs.designer.entities.cuttable.CutType;
import com.willwinder.ugs.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.designer.gui.toollibrary.ToolShapeIcon;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.utils.SvgIconLoader;

import javax.swing.Icon;
import java.util.EnumMap;
import java.util.Map;

/**
 * A button that displays the currently used tool in the design
 *
 * @author Joacim Breiler
 */
public class ToolButton extends PanelButton {
    static final String LASER_ICON_PATH = "img/tool-laser.svg";
    private static final Icon LASER_ICON = SvgIconLoader
            .loadImageIcon(LASER_ICON_PATH, SvgIconLoader.SIZE_MEDIUM).orElse(null);

    private final transient Controller controller;
    private final transient Map<EndmillShape, Icon> millIcons = new EnumMap<>(EndmillShape.class);

    public ToolButton(Controller controller) {
        super("", "");
        this.controller = controller;
        controller.getSettings().addListener(this::updateText);
        controller.getModel().getRootEntity().addListener(e -> updateText());
        updateText();
    }

    private static boolean isMillOperation(Cuttable c) {
        return c.getCutType() == CutType.ON_PATH || c.getCutType() == CutType.CENTER_DRILL || c.getCutType() == CutType.INSIDE_PATH || c.getCutType() == CutType.OUTSIDE_PATH || c.getCutType() == CutType.POCKET || c.getCutType() == CutType.VCARVE;
    }

    private static boolean isLaserOperation(Cuttable c) {
        return c.getCutType() == CutType.LASER_FILL || c.getCutType() == CutType.LASER_ON_PATH || c.getCutType() == CutType.LASER_RASTER;
    }

    private static boolean isPlotterOperation(Cuttable c) {
        return c.getCutType() == CutType.PLOTTER_ON_PATH || c.getCutType() == CutType.PLOTTER_FILL;
    }

    private void updateText() {
        setTitle("Tool");
        boolean hasLaserOperations = controller.getModel().getEntities().stream()
                .filter(Cuttable.class::isInstance)
                .map(e -> (Cuttable) e)
                .anyMatch(ToolButton::isLaserOperation);

        boolean hasMillOperations = controller.getModel().getEntities().stream()
                .filter(Cuttable.class::isInstance)
                .map(e -> (Cuttable) e)
                .anyMatch(ToolButton::isMillOperation);

        boolean hasPlotterOperations = controller.getModel().getEntities().stream()
                .filter(Cuttable.class::isInstance)
                .map(e -> (Cuttable) e)
                .anyMatch(ToolButton::isPlotterOperation);

        boolean hasMixedOperations = (hasLaserOperations ? 1 : 0) + (hasMillOperations ? 1 : 0) + (hasPlotterOperations ? 1 : 0) > 1;

        if (hasMixedOperations) {
            setText("Mixed");
            setIcon(getMillIcon());
        } else if (hasLaserOperations) {
            setText("Laser");
            setIcon(LASER_ICON);
        } else if (hasPlotterOperations) {
            setText("Pen");
            setIcon(getMillIcon());
        } else {
            setText(getMillToolDescription());
            setIcon(getMillIcon());
        }
    }

    private Icon getMillIcon() {
        return millIcons.computeIfAbsent(controller.getSettings().getToolShape(),
                shape -> new ToolShapeIcon(shape, SvgIconLoader.SIZE_MEDIUM));
    }

    public String getMillToolDescription() {
        double scale = UnitUtils.scaleUnits(UnitUtils.Units.MM, controller.getSettings().getPreferredUnits());
        return Utils.formatter.format(controller.getSettings().getToolDiameter() * scale) + " " + controller.getSettings().getPreferredUnits().abbreviation;
    }
}
