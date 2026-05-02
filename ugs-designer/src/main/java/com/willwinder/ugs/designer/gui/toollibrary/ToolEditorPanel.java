/*
    Copyright 2026 Damian Nikodem

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
package com.willwinder.ugs.designer.gui.toollibrary;

import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.uielements.TextFieldWithUnit;
import net.miginfocom.swing.MigLayout;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.Dimension;
import java.util.function.Consumer;

/**
 * Editor panel for a single {@link ToolDefinition}. The panel is re-usable — call
 * {@link #setTool(ToolDefinition, boolean)} to bind a tool and {@link #getTool()} to read the
 * current edited values back. Any change fires the configured {@link #setChangeListener} so
 * callers can debounce-persist.
 *
 * Units:
 *   - Diameter is displayed in the tool's native unit (mm or inch). Toggling the unit selector
 *     converts the numeric value.
 *   - Feed / plunge / depth are displayed in the Settings preferred units and the label shows
 *     the unit abbreviation.
 */
public class ToolEditorPanel extends JPanel {
    private final UnitUtils.Units preferredUnits;
    private final Unit feedUnit;
    private final Unit depthUnit;

    private JTextField nameField;
    private EndmillShapeCombo shapeCombo;
    private JLabel angleLabel;
    private TextFieldWithUnit angleField;
    private JLabel diameterLabel;
    private TextFieldWithUnit diameterField;
    private JPanel diameterSlot;
    private JComboBox<UnitUtils.Units> diameterUnitCombo;
    private TextFieldWithUnit feedField;
    private TextFieldWithUnit plungeField;
    private TextFieldWithUnit depthField;
    private TextFieldWithUnit stepOverField;
    private TextFieldWithUnit spindleSpeedField;
    private JComboBox<String> spindleDirectionCombo;
    private JLabel errorLabel;

    private ToolDefinition current;
    private boolean readOnly;
    private boolean suppressEvents;
    private Consumer<ToolDefinition> changeListener = t -> {};

    public ToolEditorPanel(UnitUtils.Units preferredUnits) {
        this.preferredUnits = preferredUnits == null ? UnitUtils.Units.MM : preferredUnits;
        this.feedUnit = this.preferredUnits == UnitUtils.Units.INCH ? Unit.INCHES_PER_MINUTE : Unit.MM_PER_MINUTE;
        this.depthUnit = this.preferredUnits == UnitUtils.Units.INCH ? Unit.INCH : Unit.MM;
        initComponents();
    }

    public void setChangeListener(Consumer<ToolDefinition> listener) {
        this.changeListener = listener == null ? t -> {} : listener;
    }

    private void initComponents() {
        setLayout(new MigLayout("fillx, wrap 2", "[pref!][grow,fill]"));
        setPreferredSize(new Dimension(360, 420));
        setMinimumSize(new Dimension(360, 420));

        add(new JLabel("Name"));
        nameField = new JTextField();
        nameField.getDocument().addDocumentListener(simpleDocListener(this::fireChange));
        add(nameField, "growx");

        add(new JLabel("Shape"));
        shapeCombo = new EndmillShapeCombo();
        shapeCombo.addItemListener(e -> {
            if (suppressEvents) return;
            updateAngleVisibility();
            fireChange();
        });
        add(shapeCombo, "growx");

        angleLabel = new JLabel("V angle");
        add(angleLabel);
        angleField = new TextFieldWithUnit(Unit.DEGREE, 1, 60);
        angleField.addPropertyChangeListener("value", e -> { if (!suppressEvents) fireChange(); });
        add(angleField, "growx");

        add(new JSeparator(SwingConstants.HORIZONTAL), "spanx 2, growx, hmin 2");

        diameterLabel = new JLabel("Diameter");
        add(diameterLabel);
        JPanel diameterPanel = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        diameterSlot = new JPanel(new MigLayout("insets 0, fill"));
        diameterField = new TextFieldWithUnit(Unit.MM, 3, 0);
        diameterField.addPropertyChangeListener("value", e -> { if (!suppressEvents) fireChange(); });
        diameterSlot.add(diameterField, "grow");
        diameterPanel.add(diameterSlot, "growx");
        diameterUnitCombo = new JComboBox<>(new DefaultComboBoxModel<>(
                new UnitUtils.Units[]{UnitUtils.Units.MM, UnitUtils.Units.INCH}));
        diameterUnitCombo.addItemListener(e -> {
            if (suppressEvents) return;
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                convertDiameterToSelectedUnit();
                fireChange();
            }
        });
        diameterPanel.add(diameterUnitCombo);
        add(diameterPanel, "growx");

        add(new JSeparator(SwingConstants.HORIZONTAL), "spanx 2, growx, hmin 2");

        add(new JLabel("Feed speed (" + feedUnit.getAbbreviation() + ")"));
        feedField = new TextFieldWithUnit(feedUnit, 0, 0);
        feedField.addPropertyChangeListener("value", e -> { if (!suppressEvents) fireChange(); });
        add(feedField, "growx");

        add(new JLabel("Plunge speed (" + feedUnit.getAbbreviation() + ")"));
        plungeField = new TextFieldWithUnit(feedUnit, 0, 0);
        plungeField.addPropertyChangeListener("value", e -> { if (!suppressEvents) fireChange(); });
        add(plungeField, "growx");

        add(new JLabel("Depth per pass (" + depthUnit.getAbbreviation() + ")"));
        depthField = new TextFieldWithUnit(depthUnit, 3, 0);
        depthField.addPropertyChangeListener("value", e -> { if (!suppressEvents) fireChange(); });
        add(depthField, "growx");

        add(new JLabel("Step over"));
        stepOverField = new TextFieldWithUnit(Unit.PERCENT, 1, 0);
        stepOverField.addPropertyChangeListener("value", e -> { if (!suppressEvents) fireChange(); });
        add(stepOverField, "growx");

        add(new JSeparator(SwingConstants.HORIZONTAL), "spanx 2, growx, hmin 2");

        add(new JLabel("Max spindle speed"));
        spindleSpeedField = new TextFieldWithUnit(Unit.REVOLUTIONS_PER_MINUTE, 0, 0);
        spindleSpeedField.addPropertyChangeListener("value", e -> { if (!suppressEvents) fireChange(); });
        add(spindleSpeedField, "growx");

        add(new JLabel("Spindle command"));
        spindleDirectionCombo = new JComboBox<>(new DefaultComboBoxModel<>(new String[]{"M3", "M4", "M5"}));
        spindleDirectionCombo.addItemListener(e -> { if (!suppressEvents) fireChange(); });
        add(spindleDirectionCombo, "growx");

        errorLabel = new JLabel(" ");
        errorLabel.setForeground(java.awt.Color.RED.darker());
        add(errorLabel, "spanx 2, growx");
    }

    public void setTool(ToolDefinition tool, boolean readOnly) {
        this.current = tool;
        this.readOnly = readOnly;
        suppressEvents = true;
        try {
            boolean enabled = tool != null && !readOnly && !tool.isCustomSentinel();
            setFieldsEnabled(enabled);
            if (tool == null) {
                return;
            }
            nameField.setText(tool.getName() == null ? "" : tool.getName());
            shapeCombo.setSelectedItem(tool.getShape());
            if (tool.getVBitAngleDegrees() != null) {
                angleField.setDoubleValue(tool.getVBitAngleDegrees());
            } else {
                angleField.setDoubleValue(60);
            }
            rebuildDiameterField(tool.getDiameterUnit());
            diameterField.setDoubleValue(tool.getDiameter());
            diameterUnitCombo.setSelectedItem(tool.getDiameterUnit());
            feedField.setDoubleValue(convertFeedFromMmPerMin(tool.getFeedSpeed()));
            plungeField.setDoubleValue(convertFeedFromMmPerMin(tool.getPlungeSpeed()));
            depthField.setDoubleValue(convertDepthFromMm(tool.getDepthPerPass()));
            stepOverField.setDoubleValue(tool.getStepOverPercent());
            spindleSpeedField.setDoubleValue(tool.getMaxSpindleSpeed());
            spindleDirectionCombo.setSelectedItem(tool.getSpindleDirection());
            updateAngleVisibility();
            validateAndReportError();
        } finally {
            suppressEvents = false;
        }
    }

    private void setFieldsEnabled(boolean enabled) {
        nameField.setEnabled(enabled);
        shapeCombo.setEnabled(enabled);
        angleField.setEnabled(enabled);
        diameterField.setEnabled(enabled);
        diameterUnitCombo.setEnabled(enabled);
        feedField.setEnabled(enabled);
        plungeField.setEnabled(enabled);
        depthField.setEnabled(enabled);
        stepOverField.setEnabled(enabled);
        spindleSpeedField.setEnabled(enabled);
        spindleDirectionCombo.setEnabled(enabled);
    }

    private void rebuildDiameterField(UnitUtils.Units unit) {
        Unit fieldUnit = unit == UnitUtils.Units.INCH ? Unit.INCH : Unit.MM;
        int decimals = unit == UnitUtils.Units.INCH ? 4 : 3;
        diameterSlot.removeAll();
        diameterField = new TextFieldWithUnit(fieldUnit, decimals, 0);
        diameterField.addPropertyChangeListener("value", e -> { if (!suppressEvents) fireChange(); });
        diameterSlot.add(diameterField, "grow");
        diameterSlot.revalidate();
        diameterSlot.repaint();
    }

    private void convertDiameterToSelectedUnit() {
        UnitUtils.Units newUnit = (UnitUtils.Units) diameterUnitCombo.getSelectedItem();
        if (newUnit == null || current == null || newUnit == current.getDiameterUnit()) {
            return;
        }
        double currentValue = diameterField.getDoubleValue();
        double converted = currentValue * UnitUtils.scaleUnits(current.getDiameterUnit(), newUnit);
        current.setDiameterUnit(newUnit);
        current.setDiameter(converted);
        suppressEvents = true;
        try {
            rebuildDiameterField(newUnit);
            diameterField.setDoubleValue(converted);
        } finally {
            suppressEvents = false;
        }
    }

    private void updateAngleVisibility() {
        EndmillShape shape = shapeCombo.getSelectedShape();
        boolean show = shape == EndmillShape.V_BIT;
        angleLabel.setVisible(show);
        angleField.setVisible(show);
    }

    private double convertFeedFromMmPerMin(int mmPerMin) {
        if (preferredUnits == UnitUtils.Units.INCH) {
            return mmPerMin * UnitUtils.scaleUnits(UnitUtils.Units.MM, UnitUtils.Units.INCH);
        }
        return mmPerMin;
    }

    private int convertFeedToMmPerMin(double displayed) {
        if (preferredUnits == UnitUtils.Units.INCH) {
            return (int) Math.round(displayed * UnitUtils.scaleUnits(UnitUtils.Units.INCH, UnitUtils.Units.MM));
        }
        return (int) Math.round(displayed);
    }

    private double convertDepthFromMm(double mm) {
        if (preferredUnits == UnitUtils.Units.INCH) {
            return mm * UnitUtils.scaleUnits(UnitUtils.Units.MM, UnitUtils.Units.INCH);
        }
        return mm;
    }

    private double convertDepthToMm(double displayed) {
        if (preferredUnits == UnitUtils.Units.INCH) {
            return displayed * UnitUtils.scaleUnits(UnitUtils.Units.INCH, UnitUtils.Units.MM);
        }
        return displayed;
    }

    public ToolDefinition getTool() {
        if (current == null) return null;
        ToolDefinition edited = new ToolDefinition(current);
        edited.setName(nameField.getText());
        edited.setShape(shapeCombo.getSelectedShape());
        edited.setVBitAngleDegrees(edited.getShape() == EndmillShape.V_BIT
                ? angleField.getDoubleValue() : null);
        edited.setDiameter(diameterField.getDoubleValue());
        edited.setDiameterUnit((UnitUtils.Units) diameterUnitCombo.getSelectedItem());
        edited.setFeedSpeed(convertFeedToMmPerMin(feedField.getDoubleValue()));
        edited.setPlungeSpeed(convertFeedToMmPerMin(plungeField.getDoubleValue()));
        edited.setDepthPerPass(convertDepthToMm(depthField.getDoubleValue()));
        edited.setStepOverPercent(clampFraction(stepOverField.getDoubleValue()));
        edited.setMaxSpindleSpeed((int) Math.round(spindleSpeedField.getDoubleValue()));
        edited.setSpindleDirection((String) spindleDirectionCombo.getSelectedItem());
        return edited;
    }

    private double clampFraction(double value) {
        if (value <= 0) return 0.01;
        if (value > 1) return 1;
        return value;
    }

    private void fireChange() {
        if (current == null) return;
        current = getTool();
        validateAndReportError();
        changeListener.accept(new ToolDefinition(current));
    }

    private void validateAndReportError() {
        String error = "";
        if (current != null && current.getShape() == EndmillShape.V_BIT) {
            double angle = angleField.getDoubleValue();
            if (angle < 1 || angle > 179) {
                error = "V-bit angle must be between 1° and 179°.";
            }
        }
        if (current != null && current.getDiameter() <= 0) {
            error = "Diameter must be greater than zero.";
        }
        errorLabel.setText(error.isEmpty() ? " " : error);
    }

    private DocumentListener simpleDocListener(Runnable onChange) {
        return new DocumentListener() {
            @Override public void insertUpdate(DocumentEvent e) { if (!suppressEvents) onChange.run(); }
            @Override public void removeUpdate(DocumentEvent e) { if (!suppressEvents) onChange.run(); }
            @Override public void changedUpdate(DocumentEvent e) { if (!suppressEvents) onChange.run(); }
        };
    }
}
