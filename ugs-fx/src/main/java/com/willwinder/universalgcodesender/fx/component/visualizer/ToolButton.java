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
package com.willwinder.universalgcodesender.fx.component.visualizer;

import com.willwinder.ugs.designer.entities.cuttable.CutType;
import com.willwinder.ugs.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ControllerFactory;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import com.willwinder.universalgcodesender.fx.model.UgsdWorkspaceContext;
import com.willwinder.universalgcodesender.fx.model.WorkspaceContext;
import com.willwinder.universalgcodesender.fx.service.WorkspaceManager;
import com.willwinder.universalgcodesender.fx.stage.ToolSettingsStage;
import com.willwinder.universalgcodesender.model.UnitUtils;
import javafx.application.Platform;
import javafx.scene.control.Button;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.util.Duration;

/**
 * Overlay button in the {@link Visualizer} that shows the current designer tool (mill diameter, or
 * "Laser"/"Mixed") and opens the {@link ToolSettingsStage} when clicked. Only visible while a
 * design workspace is active. Mirrors the Swing {@code ToolButton}.
 */
public class ToolButton extends Button {
    private static final int ICON_SIZE = 32;
    private final transient Controller controller;

    public ToolButton() {
        this.controller = ControllerFactory.getController();
        getStyleClass().add("tool-button");

        Tooltip tooltip = new Tooltip("Tool settings");
        tooltip.setShowDelay(Duration.millis(100));
        setTooltip(tooltip);

        setOnAction(e -> openToolSettings());

        controller.getSettings().addListener(this::updateText);
        controller.getDrawing().getRootEntity().addListener(e -> updateText());
        bindDesignVisibility();
        updateText();
    }

    private void openToolSettings() {
        new ToolSettingsStage(getScene() == null ? null : getScene().getWindow(), controller).show();
    }

    private void updateText() {
        Platform.runLater(() -> {
            boolean hasLaser = controller.getDrawing().getEntities().stream()
                    .filter(Cuttable.class::isInstance)
                    .map(Cuttable.class::cast)
                    .anyMatch(ToolButton::isLaserOperation);
            boolean hasMill = controller.getDrawing().getEntities().stream()
                    .filter(Cuttable.class::isInstance)
                    .map(Cuttable.class::cast)
                    .anyMatch(ToolButton::isMillOperation);

            if (hasLaser && hasMill) {
                setText("Mixed");
                setGraphic(loadIcon("icons/tool.svg"));
            } else if (hasLaser) {
                setText("Laser");
                setGraphic(loadIcon("icons/laser.svg"));
            } else {
                setText(getMillToolDescription());
                setGraphic(loadIcon("icons/tool.svg"));
            }
        });
    }

    private static ImageView loadIcon(String icon) {
        return SvgLoader.loadImageIcon(icon, ICON_SIZE).orElse(null);
    }

    private String getMillToolDescription() {
        double scale = UnitUtils.scaleUnits(UnitUtils.Units.MM, controller.getSettings().getPreferredUnits());
        return Utils.formatter.format(controller.getSettings().getToolDiameter() * scale)
                + " " + controller.getSettings().getPreferredUnits().abbreviation;
    }

    private static boolean isMillOperation(Cuttable c) {
        return c.getCutType() == CutType.ON_PATH || c.getCutType() == CutType.CENTER_DRILL
                || c.getCutType() == CutType.INSIDE_PATH || c.getCutType() == CutType.OUTSIDE_PATH
                || c.getCutType() == CutType.POCKET;
    }

    private static boolean isLaserOperation(Cuttable c) {
        return c.getCutType() == CutType.LASER_FILL || c.getCutType() == CutType.LASER_ON_PATH
                || c.getCutType() == CutType.LASER_RASTER;
    }

    private void bindDesignVisibility() {
        WorkspaceManager workspaceManager = WorkspaceManager.getInstance();
        updateVisibility(workspaceManager.getActiveWorkspace().orElse(null));
        workspaceManager.addListener(new WorkspaceManager.WorkspaceListener() {
            @Override
            public void onWorkspaceOpened(WorkspaceContext workspace) {
                Platform.runLater(() -> updateVisibility(workspace));
            }

            @Override
            public void onWorkspaceClosed() {
                Platform.runLater(() -> updateVisibility(null));
            }

            @Override
            public void onWorkspaceDirtyStateChanged(WorkspaceContext workspace, boolean dirty) {
                // Visibility only depends on the workspace type, not its dirty state.
            }
        });
    }

    private void updateVisibility(WorkspaceContext workspace) {
        boolean isDesign = workspace instanceof UgsdWorkspaceContext;
        setVisible(isDesign);
        setManaged(isDesign);
    }
}
