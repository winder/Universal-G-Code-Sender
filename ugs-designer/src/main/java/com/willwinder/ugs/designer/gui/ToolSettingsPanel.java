/*
    Copyright 2021-2024 Will Winder
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
package com.willwinder.ugs.designer.gui;

import com.willwinder.ugs.designer.gui.toollibrary.DeviationHighlighter;
import com.willwinder.ugs.designer.gui.toollibrary.EndmillShapeCombo;
import com.willwinder.ugs.designer.gui.toollibrary.ToolLabels;
import com.willwinder.ugs.designer.gui.toollibrary.ToolLibraryDialog;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ToolLibraryService;
import com.willwinder.ugs.designer.model.CoolantMode;
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.uielements.TextFieldWithUnit;
import net.miginfocom.swing.MigLayout;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import java.awt.Dimension;
import java.text.ParseException;
import java.util.Optional;

/**
 * @author Joacim Breiler
 */
public class ToolSettingsPanel extends JPanel {
    public static final String TOOL_FIELD_CONSTRAINT = "grow, wrap";
    private final transient Controller controller;
    private final transient ToolLibraryService libraryService;

    private JButton pickFromLibraryButton;
    private JLabel selectedToolLabel;
    private EndmillShapeCombo endmillShape;
    private JPanel diameterSlot;
    private TextFieldWithUnit toolDiameter;
    private JComboBox<UnitUtils.Units> diameterUnitCombo;
    private boolean suppressEvents;
    private UnitUtils.Units diameterDisplayUnit = UnitUtils.Units.MM;
    private TextFieldWithUnit feedSpeed;
    private TextFieldWithUnit plungeSpeed;
    private TextFieldWithUnit depthPerPass;
    private TextFieldWithUnit stepOver;
    private JLabel vBitAngleLabel;
    private TextFieldWithUnit vBitAngle;
    private JTextField safeHeight;
    private JCheckBox detectMaxSpindleSpeed;
    private TextFieldWithUnit laserDiameter;
    private TextFieldWithUnit maxSpindleSpeed;
    private JComboBox<String> spindleDirection;
    private JComboBox<CoolantMode> coolantMode;
    private TextFieldWithUnit flatnessPrecision;
    private JCheckBox arcFitting;
    private JCheckBox useToolChanges;

    private transient ToolDefinition librarySnapshot;

    public ToolSettingsPanel(Controller controller) {
        this.controller = controller;
        this.libraryService = LookupService.lookupOptional(ToolLibraryService.class).orElse(null);
        initComponents();
        setMinimumSize(new Dimension(360, 500));
        setPreferredSize(new Dimension(360, 500));
        restoreLibraryBinding();
        attachDeviationHighlighters();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill, hidemode 3", "[pref!][grow,fill]"));

        pickFromLibraryButton = new JButton("Pick from Library…");
        pickFromLibraryButton.addActionListener(e -> onPickFromLibrary());
        add(pickFromLibraryButton, "spanx, growx, split 2");
        selectedToolLabel = new JLabel(" ");
        add(selectedToolLabel, "wrap, growx");

        add(new JSeparator(SwingConstants.HORIZONTAL), "spanx, grow, wrap, hmin 2");

        add(new JLabel("Tool shape"));
        endmillShape = new EndmillShapeCombo();
        endmillShape.setSelectedItem(controller.getSettings().getToolShape());
        endmillShape.addItemListener(e -> {
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                updateVBitAngleVisibility();
            }
        });
        add(endmillShape, TOOL_FIELD_CONSTRAINT);

        add(new JLabel("Tool diameter"));
        JPanel diameterRow = new JPanel(new MigLayout("insets 0, fillx", "[grow][]"));
        diameterSlot = new JPanel(new MigLayout("insets 0, fill"));
        diameterRow.add(diameterSlot, "growx");
        diameterUnitCombo = new JComboBox<>(new DefaultComboBoxModel<>(
                new UnitUtils.Units[]{UnitUtils.Units.MM, UnitUtils.Units.INCH}));
        diameterUnitCombo.addItemListener(e -> {
            if (suppressEvents) return;
            if (e.getStateChange() == java.awt.event.ItemEvent.SELECTED) {
                onDiameterUnitChanged();
            }
        });
        diameterRow.add(diameterUnitCombo);
        rebuildDiameterField(UnitUtils.Units.MM, controller.getSettings().getToolDiameter());
        add(diameterRow, TOOL_FIELD_CONSTRAINT);

        add(new JLabel("Tool step over"));
        stepOver = new TextFieldWithUnit(Unit.PERCENT, 2,
                controller.getSettings().getToolStepOver());
        add(stepOver, TOOL_FIELD_CONSTRAINT);

        vBitAngleLabel = new JLabel("V-bit angle");
        add(vBitAngleLabel);
        vBitAngle = new TextFieldWithUnit(Unit.DEGREE, 1, controller.getSettings().getVBitAngle());
        add(vBitAngle, TOOL_FIELD_CONSTRAINT);
        updateVBitAngleVisibility();

        add(new JLabel("Generate tool changes"));
        useToolChanges = new JCheckBox("", controller.getSettings().getUseToolChanges());
        useToolChanges.setToolTipText("Writes \"M6 T<n>\" for tools that have a tool number assigned.");
        add(useToolChanges, TOOL_FIELD_CONSTRAINT);

        add(new JSeparator(SwingConstants.HORIZONTAL), "spanx, grow, wrap, hmin 2");

        add(new JLabel("Default feed speed"));
        feedSpeed = new TextFieldWithUnit(Unit.MM_PER_MINUTE, 0, controller.getSettings().getFeedSpeed());
        add(feedSpeed, TOOL_FIELD_CONSTRAINT);

        add(new JLabel("Plunge speed"));
        plungeSpeed = new TextFieldWithUnit(Unit.MM_PER_MINUTE, 0, controller.getSettings().getPlungeSpeed());
        add(plungeSpeed, TOOL_FIELD_CONSTRAINT);

        add(new JLabel("Depth per pass"));
        depthPerPass = new TextFieldWithUnit(Unit.MM, 2, controller.getSettings().getDepthPerPass());
        add(depthPerPass, TOOL_FIELD_CONSTRAINT);

        add(new JLabel("Safe height"));
        safeHeight = new TextFieldWithUnit(Unit.MM, 2, controller.getSettings().getSafeHeight());
        add(safeHeight, TOOL_FIELD_CONSTRAINT);

        add(new JSeparator(SwingConstants.HORIZONTAL), "spanx, grow, wrap, hmin 2");

        add(new JLabel("Detect max spindle speed"));
        detectMaxSpindleSpeed = new JCheckBox("", controller.getSettings().getDetectMaxSpindleSpeed());
        add(detectMaxSpindleSpeed, TOOL_FIELD_CONSTRAINT);

        add(new JLabel("Max spindle speed"));
        maxSpindleSpeed = new TextFieldWithUnit(Unit.REVOLUTIONS_PER_MINUTE, 0, controller.getSettings().getMaxSpindleSpeed());
        add(maxSpindleSpeed, TOOL_FIELD_CONSTRAINT);

        add(new JSeparator(SwingConstants.HORIZONTAL), "spanx, grow, wrap, hmin 2");

        add(new JLabel("Laser diameter"));
        laserDiameter = new TextFieldWithUnit(Unit.MM, 3, controller.getSettings().getLaserDiameter());
        add(laserDiameter, TOOL_FIELD_CONSTRAINT);

        add(new JSeparator(SwingConstants.HORIZONTAL), "spanx, grow, wrap, hmin 2");

        add(new JLabel("Spindle Start Command"));
        spindleDirection = new JComboBox<>(new DefaultComboBoxModel<>(new String[]{"M3", "M4", "M5"}));
        spindleDirection.setSelectedItem(controller.getSettings().getSpindleDirection());
        add(spindleDirection, TOOL_FIELD_CONSTRAINT);

        add(new JLabel("Coolant"));
        coolantMode = new JComboBox<>(new DefaultComboBoxModel<>(CoolantMode.values()));
        coolantMode.setSelectedItem(controller.getSettings().getCoolantMode());
        coolantMode.setToolTipText("Turns the coolant on after the tool change and off at the end of the program.");
        add(coolantMode, TOOL_FIELD_CONSTRAINT);

        add(new JLabel("Curve precision"));
        flatnessPrecision = new TextFieldWithUnit(Unit.MM, 3, controller.getSettings().getFlatnessPrecision());
        add(flatnessPrecision, TOOL_FIELD_CONSTRAINT);

        add(new JLabel("Generate arcs"));
        arcFitting = new JCheckBox("", controller.getSettings().getArcFitting());
        add(arcFitting, TOOL_FIELD_CONSTRAINT);
    }

    /**
     * The angle only describes the geometry of a V-shaped bit, so it is hidden for every other
     * shape. The value is kept in the hidden field so it survives a temporary shape change.
     */
    private void updateVBitAngleVisibility() {
        boolean isVBit = endmillShape.getSelectedShape().requiresAngle();
        vBitAngleLabel.setVisible(isVBit);
        vBitAngle.setVisible(isVBit);
        revalidate();
        repaint();
    }

    private void rebuildDiameterField(UnitUtils.Units unit, double valueInFieldUnit) {
        diameterDisplayUnit = unit == null ? UnitUtils.Units.MM : unit;
        Unit fieldUnit = diameterDisplayUnit == UnitUtils.Units.INCH ? Unit.INCH : Unit.MM;
        int decimals = diameterDisplayUnit == UnitUtils.Units.INCH ? 4 : 3;
        diameterSlot.removeAll();
        toolDiameter = new TextFieldWithUnit(fieldUnit, decimals, valueInFieldUnit);
        diameterSlot.add(toolDiameter, "grow");
        diameterSlot.revalidate();
        diameterSlot.repaint();
        suppressEvents = true;
        try {
            diameterUnitCombo.setSelectedItem(diameterDisplayUnit);
        } finally {
            suppressEvents = false;
        }
    }

    private void onDiameterUnitChanged() {
        UnitUtils.Units newUnit = (UnitUtils.Units) diameterUnitCombo.getSelectedItem();
        if (newUnit == null || newUnit == diameterDisplayUnit) {
            return;
        }
        double currentValue = toolDiameter.getDoubleValue();
        double converted = currentValue * UnitUtils.scaleUnits(diameterDisplayUnit, newUnit);
        rebuildDiameterField(newUnit, converted);
        DeviationHighlighter.attachDouble(toolDiameter, () -> librarySnapshot == null ? null
                : valueInDisplayUnit(librarySnapshot.getDiameterInMm()));
    }

    /**
     * Restores which library tool the design is bound to, without touching any of the fields. The
     * values shown are always the ones stored in the design, so user edits to a library tool
     * survive reopening the dialog. The binding is only used for the tool name and for
     * highlighting fields that deviate from the library definition.
     */
    private void restoreLibraryBinding() {
        Settings settings = controller.getSettings();
        String activeId = settings.getCurrentToolId();
        if (activeId != null && libraryService != null) {
            Optional<ToolDefinition> tool = libraryService.getById(activeId);
            if (tool.isPresent() && !tool.get().isCustomSentinel()) {
                librarySnapshot = tool.get();
                updateSelectedToolLabel();
                return;
            }
        }
        ToolDefinition snapshot = settings.getCurrentToolSnapshot();
        if (snapshot != null && !snapshot.isCustomSentinel()) {
            librarySnapshot = new ToolDefinition(snapshot);
        }
        updateSelectedToolLabel();
    }

    private void attachDeviationHighlighters() {
        DeviationHighlighter.attachDouble(toolDiameter, () -> librarySnapshot == null ? null
                : valueInDisplayUnit(librarySnapshot.getDiameterInMm()));
        DeviationHighlighter.attachDouble(maxSpindleSpeed,
                () -> librarySnapshot == null ? null : (double) librarySnapshot.getMaxSpindleSpeed());
        DeviationHighlighter.attachDouble(flatnessPrecision,
                () -> librarySnapshot == null ? null : null);
        DeviationHighlighter.attachDouble(laserDiameter,
                () -> librarySnapshot == null ? null : null);

        DeviationHighlighter.attachDouble(feedSpeed,
                () -> librarySnapshot == null ? null : (double) librarySnapshot.getFeedSpeed());
        DeviationHighlighter.attachDouble(plungeSpeed,
                () -> librarySnapshot == null ? null : (double) librarySnapshot.getPlungeSpeed());
        DeviationHighlighter.attachDouble(depthPerPass,
                () -> librarySnapshot == null ? null : librarySnapshot.getDepthPerPass());
        DeviationHighlighter.attachDouble(stepOver,
                () -> librarySnapshot == null ? null : librarySnapshot.getStepOverPercent());
        DeviationHighlighter.attachDouble(vBitAngle,
                () -> librarySnapshot == null ? null : librarySnapshot.getVBitAngleDegrees());
        DeviationHighlighter.attachCombo(spindleDirection,
                () -> librarySnapshot == null ? null : librarySnapshot.getSpindleDirection());
        DeviationHighlighter.attachCombo(endmillShape,
                () -> librarySnapshot == null ? null : librarySnapshot.getShape());
    }

    private Double valueInDisplayUnit(double mm) {
        if (diameterDisplayUnit == UnitUtils.Units.INCH) {
            return mm * UnitUtils.scaleUnits(UnitUtils.Units.MM, UnitUtils.Units.INCH);
        }
        return mm;
    }

    private void onPickFromLibrary() {
        if (libraryService == null) {
            return;
        }
        Optional<ToolDefinition> picked = ToolLibraryDialog.pick(
                SwingUtilities.getWindowAncestor(this),
                controller.getSettings().getPreferredUnits(),
                librarySnapshot == null ? null : librarySnapshot.getId());
        picked.ifPresent(this::selectTool);
    }

    void selectTool(ToolDefinition tool) {
        if (tool == null || tool.isCustomSentinel()) {
            librarySnapshot = null;
            updateSelectedToolLabel();
        } else {
            librarySnapshot = tool;
            applyLibrarySnapshotToFields();
        }
    }

    private void applyLibrarySnapshotToFields() {
        if (librarySnapshot == null) return;
        rebuildDiameterField(librarySnapshot.getDiameterUnit(), librarySnapshot.getDiameter());
        // Re-attach highlighter to the new field
        DeviationHighlighter.attachDouble(toolDiameter, () -> librarySnapshot == null ? null
                : valueInDisplayUnit(librarySnapshot.getDiameterInMm()));
        try {
            feedSpeed.setDoubleValue(librarySnapshot.getFeedSpeed());
            plungeSpeed.setDoubleValue(librarySnapshot.getPlungeSpeed());
            depthPerPass.setDoubleValue(librarySnapshot.getDepthPerPass());
            stepOver.setDoubleValue(librarySnapshot.getStepOverPercent());
            if (librarySnapshot.getVBitAngleDegrees() != null) {
                vBitAngle.setDoubleValue(librarySnapshot.getVBitAngleDegrees());
            }
            maxSpindleSpeed.setDoubleValue(librarySnapshot.getMaxSpindleSpeed());
            spindleDirection.setSelectedItem(librarySnapshot.getSpindleDirection());
            endmillShape.setSelectedItem(librarySnapshot.getShape());
        } catch (RuntimeException ignored) {
            // Bad format — leave field as-is
        }
        updateSelectedToolLabel();
    }

    private void updateSelectedToolLabel() {
        if (librarySnapshot == null || librarySnapshot.getName() == null) {
            selectedToolLabel.setText("— Custom —");
        } else {
            selectedToolLabel.setText(ToolLabels.describe(librarySnapshot));
        }
    }

    public double getToolDiameter() {
        try {
            double displayed = Utils.formatter.parse(toolDiameter.getText()).doubleValue();
            return displayed * UnitUtils.scaleUnits(diameterDisplayUnit, UnitUtils.Units.MM);
        } catch (ParseException e) {
            return controller.getSettings().getToolDiameter();
        }
    }

    public double getStepOver() {
        return stepOver.getDoubleValue();
    }

    public double getVBitAngle() {
        return vBitAngle.getDoubleValue();
    }

    public double getDepthPerPass() {
        try {
            return Utils.formatter.parse(depthPerPass.getText()).doubleValue();
        } catch (ParseException e) {
            return controller.getSettings().getDepthPerPass();
        }
    }

    public int getFeedSpeed() {
        try {
            return Utils.formatter.parse(feedSpeed.getText()).intValue();
        } catch (ParseException e) {
            return controller.getSettings().getFeedSpeed();
        }
    }

    public int getPlungeSpeed() {
        try {
            return Utils.formatter.parse(plungeSpeed.getText()).intValue();
        } catch (ParseException e) {
            return controller.getSettings().getPlungeSpeed();
        }
    }

    public double getSafeHeight() {
        try {
            return Utils.formatter.parse(safeHeight.getText()).doubleValue();
        } catch (ParseException e) {
            return controller.getSettings().getSafeHeight();
        }
    }

    private double getLaserDiameter() {
        try {
            return Utils.formatter.parse(laserDiameter.getText()).doubleValue();
        } catch (ParseException e) {
            return controller.getSettings().getLaserDiameter();
        }
    }

    private double getMaxSpindleSpeed() {
        try {
            return Utils.formatter.parse(maxSpindleSpeed.getText()).doubleValue();
        } catch (ParseException e) {
            return controller.getSettings().getMaxSpindleSpeed();
        }
    }

    private boolean getDetectMaxSpindleSpeed() {
        return detectMaxSpindleSpeed.isSelected();
    }

    private String getSpindleDirection() {
        return (String) spindleDirection.getSelectedItem();
    }

    private double getFlatnessPrecision() {
        try {
            return Utils.formatter.parse(flatnessPrecision.getText()).doubleValue();
        } catch (ParseException e) {
            return controller.getSettings().getFlatnessPrecision();
        }
    }

    public EndmillShape getEndmillShape() {
        return endmillShape.getSelectedShape();
    }

    public Settings getSettings() {
        Settings settings = new Settings();
        settings.applySettings(controller.getSettings());
        settings.setSafeHeight(getSafeHeight());
        settings.setDepthPerPass(getDepthPerPass());
        settings.setFeedSpeed(getFeedSpeed());
        settings.setToolDiameter(getToolDiameter());
        settings.setToolShape(getEndmillShape());
        settings.setToolStepOver(getStepOver());
        settings.setVBitAngle(getVBitAngle());
        settings.setPlungeSpeed(getPlungeSpeed());
        settings.setLaserDiameter(getLaserDiameter());
        settings.setMaxSpindleSpeed((int) getMaxSpindleSpeed());
        settings.setDetectMaxSpindleSpeed(getDetectMaxSpindleSpeed());
        settings.setSpindleDirection(getSpindleDirection());
        settings.setCoolantMode((CoolantMode) coolantMode.getSelectedItem());
        settings.setFlatnessPrecision(getFlatnessPrecision());
        settings.setArcFitting(arcFitting.isSelected());
        settings.setUseToolChanges(useToolChanges.isSelected());
        if (librarySnapshot != null) {
            settings.setCurrentToolId(librarySnapshot.getId());
            settings.setCurrentToolSnapshot(new ToolDefinition(librarySnapshot));
            settings.setToolNumber(librarySnapshot.getToolNumber());
        } else {
            settings.setCurrentToolId(null);
            settings.setCurrentToolSnapshot(null);
            settings.setToolNumber(ToolDefinition.UNASSIGNED_TOOL_NUMBER);
        }
        return settings;
    }
}
