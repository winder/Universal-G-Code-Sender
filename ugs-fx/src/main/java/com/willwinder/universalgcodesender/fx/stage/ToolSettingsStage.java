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
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.universalgcodesender.fx.component.ButtonBox;
import com.willwinder.universalgcodesender.fx.component.SettingsRow;
import com.willwinder.universalgcodesender.fx.control.SwitchButton;
import com.willwinder.universalgcodesender.fx.control.UnitTextField;
import com.willwinder.universalgcodesender.fx.service.WorkspaceManager;
import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

import java.util.Objects;

/**
 * A basic tool settings dialog for the designer, mirroring the fields of the Swing
 * {@code ToolSettingsPanel} (without the tool library). Edits are applied to the designer
 * {@link Settings} as a single undoable {@link ChangeToolSettingsAction} when confirmed.
 */
public class ToolSettingsStage extends Stage {
    private final Controller controller;

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

        setScene(createScene());
        setWidth(380);
        setHeight(800);
        setResizable(true);

        setOnShowing(event -> centerOnOwner());
    }

    private Scene createScene() {
        VBox form = new VBox(4,
                new SettingsRow("Tool diameter", toolDiameter),
                new SettingsRow("Tool step over", stepOver),
                new SettingsRow("V-bit angle", vBitAngle),
                new Separator(),
                new SettingsRow("Default feed speed", feedSpeed),
                new SettingsRow("Plunge speed", plungeSpeed),
                new SettingsRow("Depth per pass", depthPerPass),
                new SettingsRow("Safe height", safeHeight),
                new Separator(),
                new SettingsRow("Tab height", "How much material a tab leaves below the bottom of the cut, holding the shape in the stock.", tabHeight),
                new SettingsRow("Tab length", "How long a tab is along the tool path. Shapes too small for tabs this long get shorter ones.", tabLength),
                new Separator(),
                new SettingsRow("Detect max spindle speed", detectMaxSpindleSpeed),
                new SettingsRow("Max spindle speed", maxSpindleSpeed),
                new SettingsRow("Spindle start command", spindleDirection),
                new SettingsRow("Coolant", coolantMode),
                new Separator(),
                new SettingsRow("Pen width", "The width of the line the pen draws. Fills are kept half of this inside the shape.", penWidth),
                new SettingsRow("Pen up/down", "How a plotter puts its pen down on the paper and lifts it again.", penMode),
                penDownDepthRow,
                penDownSpindleSpeedRow,
                penUpSpindleSpeedRow,
                penDownCommandRow,
                penUpCommandRow,
                new Separator(),
                new SettingsRow("Laser diameter", laserDiameter),
                new SettingsRow("Curve precision", flatnessPrecision),
                new SettingsRow("Generate arcs", arcFitting));
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
        settings.setToolStepOver(stepOver.getValue());
        settings.setVBitAngle(vBitAngle.getValue());
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
