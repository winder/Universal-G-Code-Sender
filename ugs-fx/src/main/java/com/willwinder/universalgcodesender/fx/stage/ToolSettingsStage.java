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
package com.willwinder.universalgcodesender.fx.stage;

import com.willwinder.ugs.designer.actions.ChangeToolSettingsAction;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.model.CoolantMode;
import com.willwinder.ugs.designer.model.PenMode;
import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import com.willwinder.universalgcodesender.fx.component.toollibrary.EndmillShapeComboBox;
import com.willwinder.universalgcodesender.fx.component.toollibrary.ToolShapeIcons;
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.universalgcodesender.fx.component.BorderedTitledPane;
import com.willwinder.universalgcodesender.fx.component.ButtonBox;
import com.willwinder.universalgcodesender.fx.component.SettingsRow;
import com.willwinder.universalgcodesender.fx.control.SwitchButton;
import com.willwinder.universalgcodesender.fx.control.UnitTextField;
import com.willwinder.universalgcodesender.fx.service.WorkspaceManager;
import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Control;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A basic tool settings dialog for the designer, mirroring the fields of the Swing
 * {@code ToolSettingsPanel} (without the tool library). Edits are applied to the designer
 * {@link Settings} as a single undoable {@link ChangeToolSettingsAction} when confirmed.
 */
public class ToolSettingsStage extends Stage {
    private final Controller controller;
    private static final String DEVIATION_STYLE = "-fx-control-inner-background: #FFF3CD;";
    private final Label selectedToolLabel = new Label();
    private final EndmillShapeComboBox toolShape = new EndmillShapeComboBox();
    private final SwitchButton useToolChanges = new SwitchButton();
    private final SettingsRow vBitAngleRow;
    /**
     * The library tool the design is bound to, or null for a custom tool. Fields that deviate from
     * it are highlighted, and it is stored with the design on apply.
     */
    private ToolDefinition librarySnapshot;

    private final UnitTextField toolDiameter;
    private final UnitTextField stepOver;
    private final UnitTextField vBitAngle;
    private final UnitTextField feedSpeed;
    private final UnitTextField plungeSpeed;
    private final UnitTextField depthPerPass;
    private final UnitTextField safeHeight;
    private final UnitTextField tabHeight;
    private final UnitTextField tabLength;
    private final SwitchButton detectMaxSpindleSpeed;
    private final UnitTextField maxSpindleSpeed;
    private final UnitTextField laserDiameter;
    private final ComboBox<String> spindleDirection;
    private final ComboBox<CoolantMode> coolantMode;
    private final UnitTextField flatnessPrecision;
    private final SwitchButton arcFitting;
    private final UnitTextField penWidth;
    private final ComboBox<PenMode> penMode;
    private final UnitTextField penDownDepth;
    private final UnitTextField penDownSpindleSpeed;
    private final UnitTextField penUpSpindleSpeed;
    private final TextField penDownCommand;
    private final TextField penUpCommand;
    private final SettingsRow penDownDepthRow;
    private final SettingsRow penDownSpindleSpeedRow;
    private final SettingsRow penUpSpindleSpeedRow;
    private final SettingsRow penDownCommandRow;
    private final SettingsRow penUpCommandRow;

    public ToolSettingsStage(Window owner, Controller controller) {
        this.controller = controller;
        initModality(Modality.APPLICATION_MODAL);
        initOwner(owner);
        setTitle("Tool settings");

        Settings settings = controller.getSettings();
        librarySnapshot = settings.getCurrentToolSnapshot() == null ? null : new ToolDefinition(settings.getCurrentToolSnapshot());
        toolShape.setValue(settings.getToolShape());
        useToolChanges.selectedProperty().set(settings.getUseToolChanges());
        toolDiameter = numericField(Unit.MM, settings.getToolDiameter());
        stepOver = numericField(Unit.PERCENT, settings.getToolStepOver());
        vBitAngle = numericField(Unit.DEGREE, settings.getVBitAngle());
        feedSpeed = numericField(Unit.MM_PER_MINUTE, settings.getFeedSpeed());
        plungeSpeed = numericField(Unit.MM_PER_MINUTE, settings.getPlungeSpeed());
        depthPerPass = numericField(Unit.MM, settings.getDepthPerPass());
        safeHeight = numericField(Unit.MM, settings.getSafeHeight());
        tabHeight = numericField(Unit.MM, settings.getTabHeight());
        tabLength = numericField(Unit.MM, settings.getTabLength());
        detectMaxSpindleSpeed = new SwitchButton();
        detectMaxSpindleSpeed.selectedProperty().set(settings.getDetectMaxSpindleSpeed());
        maxSpindleSpeed = numericField(Unit.REVOLUTIONS_PER_MINUTE, settings.getMaxSpindleSpeed());
        laserDiameter = numericField(Unit.MM, settings.getLaserDiameter());
        spindleDirection = new ComboBox<>(FXCollections.observableArrayList("M3", "M4", "M5"));
        spindleDirection.setValue(settings.getSpindleDirection());
        spindleDirection.setMaxWidth(Double.MAX_VALUE);
        coolantMode = new ComboBox<>(FXCollections.observableArrayList(CoolantMode.values()));
        coolantMode.setValue(settings.getCoolantMode());
        coolantMode.setMaxWidth(Double.MAX_VALUE);
        flatnessPrecision = numericField(Unit.MM, settings.getFlatnessPrecision());
        arcFitting = new SwitchButton();
        arcFitting.selectedProperty().set(settings.getArcFitting());
        penWidth = numericField(Unit.MM, settings.getPenWidth());
        penMode = new ComboBox<>(FXCollections.observableArrayList(PenMode.values()));
        penMode.setValue(settings.getPenMode());
        penMode.setMaxWidth(Double.MAX_VALUE);
        penDownDepth = numericField(Unit.MM, settings.getPenDownDepth());
        penDownSpindleSpeed = numericField(Unit.REVOLUTIONS_PER_MINUTE, settings.getPenDownSpindleSpeed());
        penUpSpindleSpeed = numericField(Unit.REVOLUTIONS_PER_MINUTE, settings.getPenUpSpindleSpeed());
        penDownCommand = new TextField(settings.getPenDownCommand());
        penUpCommand = new TextField(settings.getPenUpCommand());
        penDownDepthRow = new SettingsRow("Pen down depth", penDownDepth);
        penDownSpindleSpeedRow = new SettingsRow("Pen down speed", penDownSpindleSpeed);
        penUpSpindleSpeedRow = new SettingsRow("Pen up speed", penUpSpindleSpeed);
        penDownCommandRow = new SettingsRow("Pen down command", penDownCommand);
        penUpCommandRow = new SettingsRow("Pen up command", penUpCommand);
        penMode.valueProperty().addListener((observable, oldValue, newValue) -> updatePenRowVisibility());
        updatePenRowVisibility();
        vBitAngleRow = new SettingsRow("V-bit angle", vBitAngle);
        toolShape.valueProperty().addListener((observable, oldValue, newValue) -> updateVBitAngleVisibility());
        updateVBitAngleVisibility();
        installDeviationHighlighting();
        updateSelectedToolLabel();
        setScene(createScene());
        setWidth(520);
        // Tall enough for the whole form on a big screen, but never taller than the screen,
        // where the last sections and the buttons would end up out of reach
        setHeight(Math.min(860, Screen.getPrimary().getVisualBounds().getHeight() - 80));
        setResizable(true);

        setOnShowing(event -> centerOnOwner());
    }

    private Scene createScene() {
        Button selectToolButton = new Button("Select tool");
        selectToolButton.setOnAction(e -> onPickFromLibrary());
        selectedToolLabel.setMinWidth(120);
        selectedToolLabel.setMaxWidth(220);
        HBox libraryToolBox = new HBox(6, selectedToolLabel, selectToolButton);
        libraryToolBox.setAlignment(Pos.CENTER_LEFT);

        VBox form = new VBox(32,
                new BorderedTitledPane("Tool and Spindle", new VBox(10,
                        new SettingsRow("Name", "The tool from the tool library this design is cut with. Fields edited away from the library tool are highlighted.", libraryToolBox),
                        new SettingsRow("Tool change", "Writes an M6 tool change with the library tool's slot number at the start of the program. Needs a tool with a slot number.", useToolChanges),
                        new SettingsRow("Shape", toolShape),
                        vBitAngleRow,
                        new Separator(),
                        new SettingsRow("Tool diameter", toolDiameter),
                        new Separator(),
                        new SettingsRow("Feed speed", feedSpeed),
                        new SettingsRow("Plunge speed", plungeSpeed),
                        new SettingsRow("Depth per pass", depthPerPass),
                        new SettingsRow("Tool step over", stepOver),
                        new Separator(),
                        new SettingsRow("Detect max spindle speed", detectMaxSpindleSpeed),
                        new SettingsRow("Max spindle speed", maxSpindleSpeed),
                        new SettingsRow("Spindle start command", spindleDirection),
                        new SettingsRow("Coolant", coolantMode))
        ),
        new BorderedTitledPane("Cutting", new VBox(10,
                new SettingsRow("Safe height", safeHeight),
                new SettingsRow("Tab height", "How much material a tab leaves below the bottom of the cut, holding the shape in the stock.", tabHeight),
                new SettingsRow("Tab length", "How long a tab is along the tool path. Shapes too small for tabs this long get shorter ones.", tabLength))
        ),
        new BorderedTitledPane("Plotter", new VBox(10,
                new SettingsRow("Pen width", "The width of the line the pen draws. Fills are kept half of this inside the shape.", penWidth),
                new SettingsRow("Pen up/down", "How a plotter puts its pen down on the paper and lifts it again.", penMode),
                penDownDepthRow,
                penDownSpindleSpeedRow,
                penUpSpindleSpeedRow,
                penDownCommandRow,
                penUpCommandRow)),
                new BorderedTitledPane("Laser", new VBox(10,
                        new SettingsRow("Laser diameter", laserDiameter))),
                new BorderedTitledPane("Tool path", new VBox(10,
                        new SettingsRow("Curve precision", flatnessPrecision),
                        new SettingsRow("Generate arcs", arcFitting))));
        form.setPadding(new Insets(16));

        ScrollPane scroll = new ScrollPane(form);
        scroll.setFitToWidth(true);

        Button cancelButton = new Button("Cancel");
        cancelButton.setOnAction(e -> close());
        Button applyButton = new Button("Apply");
        applyButton.setOnAction(e -> onApply());

        ButtonBox buttonBox = new ButtonBox();
        ButtonBox.setButtonData(cancelButton, ButtonBox.ButtonData.CANCEL_CLOSE);
        ButtonBox.setButtonData(applyButton, ButtonBox.ButtonData.OK_DONE);
        buttonBox.getButtons().addAll(cancelButton, applyButton);

        BorderPane root = new BorderPane();
        root.setCenter(scroll);
        root.setBottom(buttonBox);

        Scene scene = new Scene(root);
        scene.getStylesheets().add(Objects.requireNonNull(getClass().getResource("/styles/root.css")).toExternalForm());
        return scene;
    }

    private void onApply() {
        Settings settings = new Settings();
        settings.applySettings(controller.getSettings());
        settings.setToolDiameter(toolDiameter.getValue());
        settings.setToolShape(toolShape.getSelectedShape());
        settings.setToolStepOver(stepOver.getValue());
        settings.setVBitAngle(vBitAngle.getValue());
        settings.setUseToolChanges(useToolChanges.selectedProperty().get());
        if (librarySnapshot != null) {
            settings.setCurrentToolId(librarySnapshot.getId());
            settings.setCurrentToolSnapshot(new ToolDefinition(librarySnapshot));
            settings.setToolNumber(librarySnapshot.getToolNumber());
        } else {
            settings.setCurrentToolId(null);
            settings.setCurrentToolSnapshot(null);
            settings.setToolNumber(ToolDefinition.UNASSIGNED_TOOL_NUMBER);
        }
        settings.setFeedSpeed((int) Math.round(feedSpeed.getValue()));
        settings.setPlungeSpeed((int) Math.round(plungeSpeed.getValue()));
        settings.setDepthPerPass(depthPerPass.getValue());
        settings.setSafeHeight(safeHeight.getValue());
        settings.setTabHeight(tabHeight.getValue());
        settings.setTabLength(tabLength.getValue());
        settings.setDetectMaxSpindleSpeed(detectMaxSpindleSpeed.selectedProperty().get());
        settings.setMaxSpindleSpeed((int) Math.round(maxSpindleSpeed.getValue()));
        settings.setLaserDiameter(laserDiameter.getValue());
        settings.setSpindleDirection(spindleDirection.getValue());
        settings.setCoolantMode(coolantMode.getValue());
        settings.setPenWidth(penWidth.getValue());
        settings.setPenMode(penMode.getValue());
        settings.setPenDownDepth(penDownDepth.getValue());
        settings.setPenDownSpindleSpeed((int) Math.round(penDownSpindleSpeed.getValue()));
        settings.setPenUpSpindleSpeed((int) Math.round(penUpSpindleSpeed.getValue()));
        settings.setPenDownCommand(penDownCommand.getText());
        settings.setPenUpCommand(penUpCommand.getText());
        settings.setFlatnessPrecision(flatnessPrecision.getValue());
        settings.setArcFitting(arcFitting.selectedProperty().get());

        ChangeToolSettingsAction action = new ChangeToolSettingsAction(controller, settings);
        action.actionPerformed(null);
        controller.getUndoManager().addAction(action);

        // The settings are stored in the design file, so they need to be saved with it
        WorkspaceManager.getInstance().markActiveWorkspaceDirty(true);
        close();
    }

    /**
     * Only the fields that the selected way of moving the pen actually uses are shown. The values
     * of the other ways are kept in the hidden fields, so switching back and forth does not lose a
     * machine setup that has already been dialed in.
     */
    private void updatePenRowVisibility() {
        PenMode selected = penMode.getValue();
        setRowVisible(penDownDepthRow, selected == PenMode.Z_AXIS);
        setRowVisible(penDownSpindleSpeedRow, selected == PenMode.SPINDLE_SPEED);
        setRowVisible(penUpSpindleSpeedRow, selected == PenMode.SPINDLE_SPEED);
        setRowVisible(penDownCommandRow, selected == PenMode.CUSTOM_COMMAND);
        setRowVisible(penUpCommandRow, selected == PenMode.CUSTOM_COMMAND);
    }

    private void updateVBitAngleVisibility() {
        setRowVisible(vBitAngleRow, toolShape.getSelectedShape() == EndmillShape.V_BIT);
    }

    private void onPickFromLibrary() {
        Optional<ToolDefinition> picked = ToolLibraryStage.pick(this, controller.getSettings().getPreferredUnits(),
                librarySnapshot == null ? null : librarySnapshot.getId());
        picked.ifPresent(this::selectTool);
    }

    /**
     * Binds the design to a library tool and copies its values into the fields. The library's
     * "custom" entry unbinds the design instead and leaves the fields as they are.
     */
    private void selectTool(ToolDefinition tool) {
        if (tool == null || tool.isCustomSentinel()) {
            librarySnapshot = null;
        } else {
            librarySnapshot = new ToolDefinition(tool);
            toolDiameter.setValue(tool.getDiameterInMm());
            toolShape.setValue(tool.getShape());
            if (tool.getVBitAngleDegrees() != null) {
                vBitAngle.setValue(tool.getVBitAngleDegrees());
            }
            feedSpeed.setValue(tool.getFeedSpeed());
            plungeSpeed.setValue(tool.getPlungeSpeed());
            depthPerPass.setValue(tool.getDepthPerPass());
            stepOver.setValue(tool.getStepOverPercent());
            maxSpindleSpeed.setValue(tool.getMaxSpindleSpeed());
            spindleDirection.setValue(tool.getSpindleDirection());
        }
        updateSelectedToolLabel();
        refreshDeviationHighlighting();
    }

    private void updateSelectedToolLabel() {
        if (librarySnapshot == null || librarySnapshot.getName() == null) {
            selectedToolLabel.setText("— Custom —");
            selectedToolLabel.setGraphic(null);
        } else {
            selectedToolLabel.setText(ToolShapeIcons.describe(librarySnapshot));
            selectedToolLabel.setGraphic(ToolShapeIcons.icon(librarySnapshot.getShape()));
        }
    }

    /**
     * Highlights fields whose value differs from the bound library tool, so it is visible that the
     * design no longer cuts with exactly that tool.
     */
    private void installDeviationHighlighting() {
        highlightDeviation(toolDiameter, () -> librarySnapshot == null ? null : librarySnapshot.getDiameterInMm());
        highlightDeviation(stepOver, () -> librarySnapshot == null ? null : librarySnapshot.getStepOverPercent());
        highlightDeviation(vBitAngle, () -> librarySnapshot == null ? null : librarySnapshot.getVBitAngleDegrees());
        highlightDeviation(feedSpeed, () -> librarySnapshot == null ? null : (double) librarySnapshot.getFeedSpeed());
        highlightDeviation(plungeSpeed, () -> librarySnapshot == null ? null : (double) librarySnapshot.getPlungeSpeed());
        highlightDeviation(depthPerPass, () -> librarySnapshot == null ? null : librarySnapshot.getDepthPerPass());
        highlightDeviation(maxSpindleSpeed, () -> librarySnapshot == null ? null : (double) librarySnapshot.getMaxSpindleSpeed());
        highlightDeviation(toolShape, () -> librarySnapshot == null ? null : librarySnapshot.getShape());
        highlightDeviation(spindleDirection, () -> librarySnapshot == null ? null : librarySnapshot.getSpindleDirection());
    }

    private final java.util.List<Runnable> deviationUpdates = new java.util.ArrayList<>();

    private void highlightDeviation(UnitTextField field, Supplier<Double> reference) {
        Runnable update = () -> {
            Double expected = reference.get();
            paintDeviation(field, expected != null && Math.abs(field.getValue() - expected) > 1e-6);
        };
        field.textProperty().addListener((observable, was, text) -> update.run());
        deviationUpdates.add(update);
        update.run();
    }

    private <T> void highlightDeviation(ComboBox<T> combo, Supplier<T> reference) {
        Runnable update = () -> {
            T expected = reference.get();
            paintDeviation(combo, expected != null && !Objects.equals(expected, combo.getValue()));
        };
        combo.valueProperty().addListener((observable, was, value) -> update.run());
        deviationUpdates.add(update);
        update.run();
    }

    private void refreshDeviationHighlighting() {
        deviationUpdates.forEach(Runnable::run);
    }

    private static void paintDeviation(Control control, boolean deviates) {
        control.setStyle(deviates ? DEVIATION_STYLE : "");
    }

    private static void setRowVisible(SettingsRow row, boolean visible) {
        row.setVisible(visible);
        row.setManaged(visible);
    }

    private void centerOnOwner() {
        Window owner = getOwner();
        if (owner != null) {
            setX(owner.getX() + owner.getWidth() / 2 - getWidth() / 2);
            setY(owner.getY() + owner.getHeight() / 2 - getHeight() / 2);
        }
    }

    private static UnitTextField numericField(Unit unit, double value) {
        UnitTextField field = new UnitTextField(new UnitValue(unit, value), unit);
        field.setMaxWidth(Double.MAX_VALUE);
        return field;
    }
}
