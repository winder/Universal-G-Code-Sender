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
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.universalgcodesender.fx.component.ButtonBox;
import com.willwinder.universalgcodesender.fx.component.SettingsRow;
import com.willwinder.universalgcodesender.fx.control.SwitchButton;
import com.willwinder.universalgcodesender.fx.control.UnitTextField;
import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.Window;

/**
 * A basic tool settings dialog for the designer, mirroring the fields of the Swing
 * {@code ToolSettingsPanel} (without the tool library). Edits are applied to the designer
 * {@link Settings} as a single undoable {@link ChangeToolSettingsAction} when confirmed.
 */
public class ToolSettingsStage extends Stage {
    private final Controller controller;

    private final UnitTextField toolDiameter;
    private final UnitTextField stepOver;
    private final UnitTextField feedSpeed;
    private final UnitTextField plungeSpeed;
    private final UnitTextField depthPerPass;
    private final UnitTextField safeHeight;
    private final SwitchButton detectMaxSpindleSpeed;
    private final UnitTextField maxSpindleSpeed;
    private final UnitTextField laserDiameter;
    private final ComboBox<String> spindleDirection;
    private final UnitTextField flatnessPrecision;

    public ToolSettingsStage(Window owner, Controller controller) {
        this.controller = controller;
        initModality(Modality.APPLICATION_MODAL);
        initOwner(owner);
        setTitle("Tool settings");

        Settings settings = controller.getSettings();
        toolDiameter = numericField(Unit.MM, settings.getToolDiameter());
        stepOver = numericField(Unit.PERCENT, settings.getToolStepOver());
        feedSpeed = numericField(Unit.MM_PER_MINUTE, settings.getFeedSpeed());
        plungeSpeed = numericField(Unit.MM_PER_MINUTE, settings.getPlungeSpeed());
        depthPerPass = numericField(Unit.MM, settings.getDepthPerPass());
        safeHeight = numericField(Unit.MM, settings.getSafeHeight());
        detectMaxSpindleSpeed = new SwitchButton();
        detectMaxSpindleSpeed.selectedProperty().set(settings.getDetectMaxSpindleSpeed());
        maxSpindleSpeed = numericField(Unit.REVOLUTIONS_PER_MINUTE, settings.getMaxSpindleSpeed());
        laserDiameter = numericField(Unit.MM, settings.getLaserDiameter());
        spindleDirection = new ComboBox<>(FXCollections.observableArrayList("M3", "M4", "M5"));
        spindleDirection.setValue(settings.getSpindleDirection());
        spindleDirection.setMaxWidth(Double.MAX_VALUE);
        flatnessPrecision = numericField(Unit.MM, settings.getFlatnessPrecision());

        setScene(createScene());
        setWidth(380);
        setHeight(580);
        setResizable(true);

        setOnShowing(event -> centerOnOwner());
    }

    private Scene createScene() {
        VBox form = new VBox(4,
                new SettingsRow("Tool diameter", toolDiameter),
                new SettingsRow("Tool step over", stepOver),
                new Separator(),
                new SettingsRow("Default feed speed", feedSpeed),
                new SettingsRow("Plunge speed", plungeSpeed),
                new SettingsRow("Depth per pass", depthPerPass),
                new SettingsRow("Safe height", safeHeight),
                new Separator(),
                new SettingsRow("Detect max spindle speed", detectMaxSpindleSpeed),
                new SettingsRow("Max spindle speed", maxSpindleSpeed),
                new SettingsRow("Spindle start command", spindleDirection),
                new Separator(),
                new SettingsRow("Laser diameter", laserDiameter),
                new SettingsRow("Arc precision", flatnessPrecision));
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
        scene.getStylesheets().add(getClass().getResource("/styles/root.css").toExternalForm());
        return scene;
    }

    private void onApply() {
        Settings settings = new Settings();
        settings.applySettings(controller.getSettings());
        settings.setToolDiameter(toolDiameter.getValue());
        settings.setToolStepOver(stepOver.getValue());
        settings.setFeedSpeed((int) Math.round(feedSpeed.getValue()));
        settings.setPlungeSpeed((int) Math.round(plungeSpeed.getValue()));
        settings.setDepthPerPass(depthPerPass.getValue());
        settings.setSafeHeight(safeHeight.getValue());
        settings.setDetectMaxSpindleSpeed(detectMaxSpindleSpeed.selectedProperty().get());
        settings.setMaxSpindleSpeed((int) Math.round(maxSpindleSpeed.getValue()));
        settings.setLaserDiameter(laserDiameter.getValue());
        settings.setSpindleDirection(spindleDirection.getValue());
        settings.setFlatnessPrecision(flatnessPrecision.getValue());

        ChangeToolSettingsAction action = new ChangeToolSettingsAction(controller, settings);
        action.actionPerformed(null);
        controller.getUndoManager().addAction(action);
        close();
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
