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
package com.willwinder.universalgcodesender.fx.component.toollibrary;

import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import com.willwinder.universalgcodesender.fx.component.SettingsRow;
import com.willwinder.universalgcodesender.fx.control.UnitTextField;
import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.UnitValue;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TextField;
import javafx.scene.control.Tooltip;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntPredicate;

/**
 * Edits one tool of the tool library. Every change is reported to the change listener as a copy of
 * the tool as soon as it is made, so the dialog can write it to the library at once. Feed rates and
 * depths are shown in the preferred units and stored in millimeters; the diameter keeps the unit the
 * tool was defined in.
 */
public class ToolEditorPane extends VBox {
    private static final int MAX_TOOL_NUMBER = 9999;
    private static final double DEFAULT_V_BIT_ANGLE = 60;

    private final Unit feedUnit;
    private final Unit depthUnit;
    private final TextField nameField = new TextField();
    private final Spinner<Integer> toolNumberSpinner = new Spinner<>();
    private final EndmillShapeComboBox shapeCombo = new EndmillShapeComboBox();
    private final UnitTextField angleField;
    private final SettingsRow angleRow;
    private final UnitTextField diameterField;
    private final ComboBox<UnitUtils.Units> diameterUnitCombo;
    private final UnitTextField feedField;
    private final UnitTextField plungeField;
    private final UnitTextField depthField;
    private final UnitTextField stepOverField;
    private final UnitTextField spindleSpeedField;
    private final ComboBox<String> spindleDirectionCombo;
    private final Label errorLabel = new Label(" ");
    private final List<Region> editable;
    private ToolDefinition current;
    private boolean suppressEvents;
    private Consumer<ToolDefinition> changeListener = tool -> {
    };
    private IntPredicate occupiedToolNumber = number -> false;

    public ToolEditorPane(UnitUtils.Units preferredUnits) {
        super(4);
        UnitUtils.Units preferredUnits1 = preferredUnits == null ? UnitUtils.Units.MM : preferredUnits;
        this.feedUnit = preferredUnits1 == UnitUtils.Units.INCH ? Unit.INCHES_PER_MINUTE : Unit.MM_PER_MINUTE;
        this.depthUnit = preferredUnits1 == UnitUtils.Units.INCH ? Unit.INCH : Unit.MM;
        setPadding(new Insets(16));

        nameField.setMaxWidth(Double.MAX_VALUE);
        nameField.setOnAction(event -> fireChange());
        nameField.focusedProperty().addListener((observable, was, focused) -> {
            if (!focused) {
                fireChange();
            }
        });

        // The spinner buttons step past numbers other tools hold; typing a held number still takes it over
        toolNumberSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(
                ToolDefinition.UNASSIGNED_TOOL_NUMBER, MAX_TOOL_NUMBER, ToolDefinition.UNASSIGNED_TOOL_NUMBER) {
            @Override
            public void increment(int steps) {
                setValue(ToolNumbers.nextFree(getValue(), steps, occupiedToolNumber, MAX_TOOL_NUMBER));
            }

            @Override
            public void decrement(int steps) {
                setValue(ToolNumbers.nextFree(getValue(), -steps, occupiedToolNumber, MAX_TOOL_NUMBER));
            }
        });
        toolNumberSpinner.setEditable(true);
        toolNumberSpinner.setMaxWidth(Double.MAX_VALUE);
        toolNumberSpinner.setTooltip(new Tooltip("The tool slot used in a tool change, for example \"M6 T2\". 0 means none."));
        toolNumberSpinner.valueProperty().addListener((observable, was, value) -> fireChange());
        toolNumberSpinner.getEditor().setOnAction(event -> commitSpinner());
        toolNumberSpinner.getEditor().focusedProperty().addListener((observable, was, focused) -> {
            if (!focused) {
                commitSpinner();
            }
        });

        shapeCombo.valueProperty().addListener((observable, was, shape) -> {
            updateAngleVisibility();
            fireChange();
        });

        angleField = field(Unit.DEGREE);
        angleRow = new SettingsRow("V-bit angle", angleField);

        diameterField = field(Unit.MM);
        diameterUnitCombo = new ComboBox<>(FXCollections.observableArrayList(UnitUtils.Units.MM, UnitUtils.Units.INCH));
        diameterUnitCombo.setValue(UnitUtils.Units.MM);
        diameterUnitCombo.valueProperty().addListener((observable, was, unit) -> {
            if (suppressEvents || unit == null) {
                return;
            }
            // The field converts its value when its unit changes, so the tool keeps its size
            diameterField.setUnit(unit == UnitUtils.Units.INCH ? Unit.INCH : Unit.MM);
            fireChange();
        });
        // Room for the value and the unit side by side, so neither "INCH" nor the number is clipped
        diameterField.setPrefWidth(150);
        diameterUnitCombo.setMinWidth(100);
        diameterUnitCombo.setPrefWidth(100);
        HBox diameterBox = new HBox(6, diameterField, diameterUnitCombo);
        diameterBox.setMaxWidth(Double.MAX_VALUE);
        HBox.setHgrow(diameterField, Priority.ALWAYS);

        feedField = field(feedUnit);
        plungeField = field(feedUnit);
        depthField = field(depthUnit);
        stepOverField = field(Unit.PERCENT);
        spindleSpeedField = field(Unit.REVOLUTIONS_PER_MINUTE);
        spindleDirectionCombo = new ComboBox<>(FXCollections.observableArrayList("M3", "M4", "M5"));
        spindleDirectionCombo.setValue("M3");
        spindleDirectionCombo.setMaxWidth(Double.MAX_VALUE);
        spindleDirectionCombo.valueProperty().addListener((observable, was, value) -> fireChange());

        errorLabel.setStyle("-fx-text-fill: #b00020;");
        errorLabel.setWrapText(true);

        editable = List.of(nameField, toolNumberSpinner, shapeCombo, angleField, diameterField, diameterUnitCombo,
                feedField, plungeField, depthField, stepOverField, spindleSpeedField, spindleDirectionCombo);

        getChildren().addAll(
                new SettingsRow("Name", nameField),
                new SettingsRow("Tool number", toolNumberSpinner),
                new SettingsRow("Shape", shapeCombo),
                angleRow,
                new Separator(),
                new SettingsRow("Diameter", diameterBox),
                new Separator(),
                new SettingsRow("Feed speed", feedField),
                new SettingsRow("Plunge speed", plungeField),
                new SettingsRow("Depth per pass", depthField),
                new SettingsRow("Step over", stepOverField),
                new Separator(),
                new SettingsRow("Max spindle speed", spindleSpeedField),
                new SettingsRow("Spindle command", spindleDirectionCombo),
                errorLabel);
        updateAngleVisibility();
        setTool(null, true);
    }

    /**
     * Tells the editor which tool numbers other tools hold, so the spinner buttons step past them.
     */
    public void setOccupiedToolNumbers(IntPredicate occupied) {
        this.occupiedToolNumber = occupied == null ? number -> false : occupied;
    }

    public void setChangeListener(Consumer<ToolDefinition> listener) {
        this.changeListener = listener == null ? tool -> {
        } : listener;
    }

    /**
     * Shows a tool, or clears the editor when {@code tool} is null. Built in "custom" sentinels
     * and read only views cannot be edited.
     */
    public void setTool(ToolDefinition tool, boolean readOnly) {
        this.current = tool;
        suppressEvents = true;
        try {
            boolean enabled = tool != null && !readOnly && !tool.isCustomSentinel();
            editable.forEach(control -> control.setDisable(!enabled));
            if (tool == null) {
                nameField.clear();
                errorLabel.setText(" ");
                return;
            }
            nameField.setText(tool.getName() == null ? "" : tool.getName());
            toolNumberSpinner.getValueFactory().setValue(clampToolNumber(tool.getToolNumber()));
            shapeCombo.setValue(tool.getShape());
            angleField.setValue(tool.getVBitAngleDegrees() == null ? DEFAULT_V_BIT_ANGLE : tool.getVBitAngleDegrees());
            diameterUnitCombo.setValue(tool.getDiameterUnit());
            diameterField.setUnit(tool.getDiameterUnit() == UnitUtils.Units.INCH ? Unit.INCH : Unit.MM);
            diameterField.setValue(tool.getDiameter());
            feedField.setValue(convert(tool.getFeedSpeed(), Unit.MM_PER_MINUTE, feedUnit));
            plungeField.setValue(convert(tool.getPlungeSpeed(), Unit.MM_PER_MINUTE, feedUnit));
            depthField.setValue(convert(tool.getDepthPerPass(), Unit.MM, depthUnit));
            stepOverField.setValue(tool.getStepOverPercent());
            spindleSpeedField.setValue(tool.getMaxSpindleSpeed());
            spindleDirectionCombo.setValue(tool.getSpindleDirection());
            updateAngleVisibility();
            validate();
        } finally {
            suppressEvents = false;
        }
    }

    /**
     * The tool as currently edited, or null when nothing is shown.
     */
    public ToolDefinition getTool() {
        if (current == null) {
            return null;
        }
        ToolDefinition edited = new ToolDefinition(current);
        edited.setName(nameField.getText());
        edited.setToolNumber(toolNumberSpinner.getValue() == null ? ToolDefinition.UNASSIGNED_TOOL_NUMBER : toolNumberSpinner.getValue());
        edited.setShape(shapeCombo.getSelectedShape());
        edited.setVBitAngleDegrees(edited.getShape() == EndmillShape.V_BIT ? angleField.getValue() : null);
        edited.setDiameter(diameterField.getValue());
        edited.setDiameterUnit(diameterUnitCombo.getValue() == null ? UnitUtils.Units.MM : diameterUnitCombo.getValue());
        edited.setFeedSpeed((int) Math.round(convert(feedField.getValue(), feedUnit, Unit.MM_PER_MINUTE)));
        edited.setPlungeSpeed((int) Math.round(convert(plungeField.getValue(), feedUnit, Unit.MM_PER_MINUTE)));
        edited.setDepthPerPass(convert(depthField.getValue(), depthUnit, Unit.MM));
        edited.setStepOverPercent(clampFraction(stepOverField.getValue()));
        edited.setMaxSpindleSpeed((int) Math.round(spindleSpeedField.getValue()));
        edited.setSpindleDirection(spindleDirectionCombo.getValue());
        return edited;
    }

    private void fireChange() {
        if (suppressEvents || current == null) {
            return;
        }
        current = getTool();
        if (validate()) {
            changeListener.accept(new ToolDefinition(current));
        }
    }

    /**
     * Reports what is wrong with the tool, if anything, and says whether it may be saved.
     */
    private boolean validate() {
        String error = "";
        if (current != null && current.getShape() == EndmillShape.V_BIT) {
            double angle = angleField.getValue();
            if (angle < 1 || angle > 179) {
                error = "V-bit angle must be between 1° and 179°.";
            }
        }
        if (current != null && current.getDiameter() <= 0) {
            error = "Diameter must be greater than zero.";
        }
        errorLabel.setText(error.isEmpty() ? " " : error);
        return error.isEmpty();
    }

    private void commitSpinner() {
        try {
            int typed = Integer.parseInt(toolNumberSpinner.getEditor().getText().trim());
            toolNumberSpinner.getValueFactory().setValue(clampToolNumber(typed));
        } catch (NumberFormatException e) {
            toolNumberSpinner.getEditor().setText(String.valueOf(toolNumberSpinner.getValue()));
        }
    }

    private void updateAngleVisibility() {
        boolean show = shapeCombo.getSelectedShape() == EndmillShape.V_BIT;
        angleRow.setVisible(show);
        angleRow.setManaged(show);
    }

    private UnitTextField field(Unit unit) {
        UnitTextField field = new UnitTextField(new UnitValue(unit, 0), unit);
        field.setMaxWidth(Double.MAX_VALUE);
        field.textProperty().addListener((observable, was, text) -> fireChange());
        return field;
    }

    private static int clampToolNumber(int toolNumber) {
        return Math.min(Math.max(toolNumber, ToolDefinition.UNASSIGNED_TOOL_NUMBER), MAX_TOOL_NUMBER);
    }

    private static double clampFraction(double value) {
        if (value <= 0) {
            return 0.01;
        }
        return Math.min(value, 1);
    }

    private static double convert(double value, Unit from, Unit to) {
        return from == to ? value : new UnitValue(from, value).convertTo(to).doubleValue();
    }
}
