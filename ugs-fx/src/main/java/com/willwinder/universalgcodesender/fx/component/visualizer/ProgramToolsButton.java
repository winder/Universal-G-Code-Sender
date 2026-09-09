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

import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import com.willwinder.universalgcodesender.fx.component.toollibrary.ToolShapeIcons;
import com.willwinder.universalgcodesender.fx.component.visualizer.simulation.ProgramTools.ProgramTool;
import com.willwinder.universalgcodesender.fx.component.visualizer.simulation.ResolvedTool;
import com.willwinder.universalgcodesender.fx.component.visualizer.simulation.ToolProfile;
import com.willwinder.universalgcodesender.fx.helper.SvgLoader;
import com.willwinder.universalgcodesender.fx.model.UgsdWorkspaceContext;
import com.willwinder.universalgcodesender.fx.model.WorkspaceContext;
import com.willwinder.universalgcodesender.fx.service.ProgramToolsService;
import com.willwinder.universalgcodesender.fx.service.ToolLibraryProvider;
import com.willwinder.universalgcodesender.fx.service.WorkspaceManager;
import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings;
import com.willwinder.universalgcodesender.fx.stage.ToolLibraryStage;
import com.willwinder.universalgcodesender.model.UnitUtils;
import javafx.application.Platform;
import javafx.geometry.Bounds;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Tooltip;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.util.Duration;

import java.util.List;
import java.util.Optional;

/**
 * Shows which tools the loaded program is simulated with. A single tool is named on the button,
 * more are counted, and a program that does not say which tool it uses is marked, since the
 * simulation then falls back to a default tool. Clicking lists every tool with where it came from
 * and lets the user choose the default tool. Shown for program workspaces; a design shows its own
 * tool button instead.
 */
public class ProgramToolsButton extends Button {
    private static final int ICON_SIZE = 32;

    private final ProgramToolsService service = ProgramToolsService.getInstance();
    private Popup popup;

    public ProgramToolsButton() {
        getStyleClass().add("tool-button");
        Tooltip tooltip = new Tooltip("Tools used by the program");
        tooltip.setShowDelay(Duration.millis(100));
        setTooltip(tooltip);
        setOnAction(event -> togglePopup());
        service.toolsProperty().addListener((observable, was, tools) -> updateText(tools));
        bindProgramVisibility();
        updateText(service.getTools());
    }

    private void updateText(List<ProgramTool> tools) {
        boolean fallback = tools.stream().anyMatch(tool -> tool.tool().source().isFallback());
        if (tools.isEmpty()) {
            setText("No tool");
        } else if (tools.size() == 1) {
            ResolvedTool only = tools.getFirst().tool();
            setText(only.isRequestedToolNumberUnassigned() ? "T" + only.requestedToolNumber() + " not assigned" : only.label());
        } else {
            long unassigned = tools.stream().filter(tool -> tool.tool().isRequestedToolNumberUnassigned()).count();
            setText(unassigned > 0 ? tools.size() + " tools, " + unassigned + " not assigned" : tools.size() + " tools");
        }
        setGraphic(SvgLoader.loadImageIcon(fallback ? "icons/toolbox.svg" : "icons/tool.svg", ICON_SIZE).orElse(null));
        getTooltip().setText(fallback
                ? "The program selects a tool the library does not have, or none at all, so another tool is used for the simulation. Click to see which and to assign it."
                : "Tools used by the program");
    }

    private void togglePopup() {
        if (popup != null && popup.isShowing()) {
            popup.hide();
            return;
        }
        popup = createPopup(service.getTools());
        Bounds bounds = localToScreen(getBoundsInLocal());
        if (bounds == null || getScene() == null) {
            return;
        }
        popup.show(getScene().getWindow(), bounds.getMinX(), bounds.getMinY());
        // Sits above the button, which is at the bottom of the visualizer
        popup.setY(bounds.getMinY() - popup.getHeight() - 6);
    }

    private Popup createPopup(List<ProgramTool> tools) {
        Popup result = new Popup();
        result.setAutoHide(true);
        result.setAutoFix(true);
        result.setHideOnEscape(true);

        VBox bubble = new VBox(8);
        bubble.setPadding(new Insets(12));
        bubble.setStyle("-fx-background-color: white; -fx-background-radius: 6; -fx-border-color: #c8c8c8; "
                + "-fx-border-radius: 6; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.25), 12, 0, 0, 3);");
        bubble.setMinWidth(320);
        bubble.setMaxWidth(460);

        Label title = new Label(tools.isEmpty() ? "The program has no cutting moves" : "Tools used by the program");
        title.setStyle("-fx-font-weight: bold;");
        bubble.getChildren().add(title);
        for (ProgramTool tool : tools) {
            bubble.getChildren().add(row(tool, result));
        }

        bubble.getChildren().add(new Separator());
        Label defaultLabel = new Label("Default tool: " + describeDefaultTool());
        defaultLabel.setWrapText(true);
        defaultLabel.setStyle("-fx-opacity: 0.8;");
        Button chooseDefault = new Button("Choose tool…");
        chooseDefault.setTooltip(new Tooltip("Choose the tool the simulation uses when the program does not select one. The tool library can be edited there too."));
        chooseDefault.setOnAction(event -> {
            result.hide();
            chooseDefaultTool();
        });
        bubble.getChildren().addAll(defaultLabel, chooseDefault);

        result.getContent().add(bubble);
        return result;
    }

    private HBox row(ProgramTool tool, Popup popup) {
        ResolvedTool resolved = tool.tool();
        Label name = new Label(resolved.label());
        name.setStyle("-fx-font-weight: bold;");
        HBox heading = new HBox(8, name);
        heading.setAlignment(Pos.CENTER_LEFT);
        if (resolved.isRequestedToolNumberUnassigned()) {
            Label requested = new Label("T" + resolved.requestedToolNumber() + " in program");
            requested.setStyle("-fx-font-size: 0.85em; -fx-font-weight: bold; -fx-text-fill: #7a4b00; -fx-border-color: #e0a800; "
                    + "-fx-border-radius: 3; -fx-padding: 0 4 0 4;");
            heading.getChildren().add(requested);
        }
        Label source = new Label(resolved.explanation() + " " + tool.cuts() + (tool.cuts() == 1 ? " cut." : " cuts."));
        source.setWrapText(true);
        source.setStyle("-fx-font-size: 0.9em; -fx-opacity: 0.75;");
        VBox text = new VBox(2, heading, source);
        HBox.setHgrow(text, Priority.ALWAYS);
        if (resolved.isRequestedToolNumberUnassigned()) {
            Button assign = new Button("Assign T" + resolved.requestedToolNumber() + "…");
            assign.setTooltip(new Tooltip("Pick the library tool the program means by T" + resolved.requestedToolNumber()
                    + ". It gets that tool number, taking it over from any other tool."));
            assign.setOnAction(event -> {
                popup.hide();
                assignToolNumber(resolved.requestedToolNumber());
            });
            text.getChildren().add(assign);
        }
        ImageView icon = ToolShapeIcons.icon(shapeOf(resolved));
        HBox row = new HBox(10, icon, text);
        row.setAlignment(Pos.TOP_LEFT);
        if (resolved.source().isFallback() || resolved.isRequestedToolNumberUnassigned()) {
            row.setStyle("-fx-background-color: #FFF3CD; -fx-background-radius: 4; -fx-padding: 6;");
        }
        return row;
    }

    /**
     * Lets the user pick the library tool the program means by a tool number and gives it that
     * number. The library change re-resolves the program's tools and reruns the simulation.
     */
    private void assignToolNumber(int toolNumber) {
        Optional<ToolDefinition> picked = ToolLibraryStage.pick(getScene().getWindow(), UnitUtils.Units.MM, null);
        picked.filter(tool -> !tool.isCustomSentinel()).ifPresent(tool -> {
            ToolDefinition assigned = new ToolDefinition(tool);
            assigned.setToolNumber(toolNumber);
            ToolLibraryProvider.getInstance().updateTool(assigned);
        });
    }

    private static EndmillShape shapeOf(ResolvedTool tool) {
        return tool.definition().map(ToolDefinition::getShape).orElseGet(() -> switch (tool.profile().kind()) {
            case BALL -> EndmillShape.BALL;
            case V_BIT -> EndmillShape.V_BIT;
            default -> EndmillShape.UPCUT;
        });
    }

    private static String describeDefaultTool() {
        String id = VisualizerSettings.getInstance().stockDefaultToolIdProperty().get();
        if (id == null || id.isBlank()) {
            return "first tool in the library";
        }
        return ToolLibraryProvider.getInstance().getById(id)
                .map(ResolvedTool::describe)
                .orElse("missing from the library, using the first tool instead");
    }

    private void chooseDefaultTool() {
        String current = VisualizerSettings.getInstance().stockDefaultToolIdProperty().get();
        Optional<ToolDefinition> picked = ToolLibraryStage.pick(getScene().getWindow(), UnitUtils.Units.MM,
                current == null || current.isBlank() ? null : current);
        picked.ifPresent(tool -> VisualizerSettings.getInstance().stockDefaultToolIdProperty()
                .set(tool.isCustomSentinel() ? "" : tool.getId()));
    }

    private void bindProgramVisibility() {
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
                // Visibility only depends on the workspace type
            }
        });
    }

    private void updateVisibility(WorkspaceContext workspace) {
        boolean isProgram = workspace != null && !(workspace instanceof UgsdWorkspaceContext);
        setVisible(isProgram);
        setManaged(isProgram);
    }
}
