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
import com.willwinder.ugs.designer.gui.toollibrary.ToolLibraryPickerDialog;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.logic.ToolLibraryService;
import com.willwinder.ugs.designer.model.Settings;
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
    private JPanel diameterSlot;
    private TextFieldWithUnit toolDiameter;
    private UnitUtils.Units diameterDisplayUnit = UnitUtils.Units.MM;
    private JTextField feedSpeed;
    private JTextField plungeSpeed;
    private JTextField depthPerPass;
    private JTextField stepOver;
    private JTextField safeHeight;
    private JCheckBox detectMaxSpindleSpeed;
    private TextFieldWithUnit laserDiameter;
    private TextFieldWithUnit maxSpindleSpeed;
    private JComboBox<String> spindleDirection;
    private TextFieldWithUnit flatnessPrecision;

    private transient ToolDefinition librarySnapshot;

    public ToolSettingsPanel(Controller controller) {
        this.controller = controller;
        this.libraryService = LookupService.lookupOptional(ToolLibraryService.class).orElse(null);
        initComponents();
        setMinimumSize(new Dimension(360, 480));
        setPreferredSize(new Dimension(360, 480));
        initialiseLibrarySnapshot();
        attachDeviationHighlighters();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill", "[30%][70%]"));

        pickFromLibraryButton = new JButton("Pick from Library…");
        pickFromLibraryButton.addActionListener(e -> onPickFromLibrary());
        add(pickFromLibraryButton, "spanx, growx, split 2");
        selectedToolLabel = new JLabel(" ");
        add(selectedToolLabel, "wrap, growx");

        add(new JSeparator(SwingConstants.HORIZONTAL), "spanx, grow, wrap, hmin 2");

        add(new JLabel("Tool diameter"));
        diameterSlot = new JPanel(new MigLayout("insets 0, fill"));
        rebuildDiameterField(UnitUtils.Units.MM, controller.getSettings().getToolDiameter());
        add(diameterSlot, TOOL_FIELD_CONSTRAINT);

        add(new JLabel("Tool step over (%)"));
        stepOver = new TextFieldWithUnit(Unit.PERCENT, 2,
                controller.getSettings().getToolStepOver() * 100);
        add(stepOver, TOOL_FIELD_CONSTRAINT);

        add(new JSeparator(SwingConstants.HORIZONTAL), "spanx, grow, wrap, hmin 2");

        add(new JLabel("Default feed speed (" + Unit.MM_PER_MINUTE.getAbbreviation() + ")"));
        feedSpeed = new TextFieldWithUnit(Unit.MM_PER_MINUTE, 0, controller.getSettings().getFeedSpeed());
        add(feedSpeed, TOOL_FIELD_CONSTRAINT);

        add(new JLabel("Plunge speed (" + Unit.MM_PER_MINUTE.getAbbreviation() + ")"));
        plungeSpeed = new TextFieldWithUnit(Unit.MM_PER_MINUTE, 0, controller.getSettings().getPlungeSpeed());
        add(plungeSpeed, TOOL_FIELD_CONSTRAINT);

        add(new JLabel("Depth per pass (" + Unit.MM.getAbbreviation() + ")"));
        depthPerPass = new TextFieldWithUnit(Unit.MM, 2, controller.getSettings().getDepthPerPass());
        add(depthPerPass, TOOL_FIELD_CONSTRAINT);

        add(new JLabel("Safe height (" + Unit.MM.getAbbreviation() + ")"));
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

        add(new JLabel("Arc precision"));
        flatnessPrecision = new TextFieldWithUnit(Unit.MM, 3, controller.getSettings().getFlatnessPrecision());
        add(flatnessPrecision, TOOL_FIELD_CONSTRAINT);
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
    }

    private void initialiseLibrarySnapshot() {
        Settings settings = controller.getSettings();
        String activeId = settings.getCurrentToolId();
        if (activeId != null && libraryService != null) {
            Optional<ToolDefinition> tool = libraryService.getById(activeId);
            if (tool.isPresent()) {
                librarySnapshot = tool.get();
                SwingUtilities.invokeLater(() -> applyLibrarySnapshotToFields(false));
                return;
            }
        }
        if (settings.getCurrentToolSnapshot() != null) {
            librarySnapshot = new ToolDefinition(settings.getCurrentToolSnapshot());
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

        // JFormattedTextFields (inherited from JTextField) — watch document changes
        DeviationHighlighter.attachText(feedSpeed,
                () -> librarySnapshot == null ? null : String.valueOf(librarySnapshot.getFeedSpeed()));
        DeviationHighlighter.attachText(plungeSpeed,
                () -> librarySnapshot == null ? null : String.valueOf(librarySnapshot.getPlungeSpeed()));
        DeviationHighlighter.attachText(depthPerPass,
                () -> librarySnapshot == null ? null : String.valueOf(librarySnapshot.getDepthPerPass()));
        DeviationHighlighter.attachText(stepOver,
                () -> librarySnapshot == null ? null
                        : Utils.formatter.format(librarySnapshot.getStepOverPercent() * 100));
        DeviationHighlighter.attachCombo(spindleDirection,
                () -> librarySnapshot == null ? null : librarySnapshot.getSpindleDirection());
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
        Optional<ToolDefinition> picked = ToolLibraryPickerDialog.pick(
                SwingUtilities.getWindowAncestor(this),
                controller.getSettings().getPreferredUnits());
        picked.ifPresent(tool -> {
            librarySnapshot = tool;
            applyLibrarySnapshotToFields(true);
        });
    }

    private void applyLibrarySnapshotToFields(boolean populateAll) {
        if (librarySnapshot == null) return;
        rebuildDiameterField(librarySnapshot.getDiameterUnit(), librarySnapshot.getDiameter());
        // Re-attach highlighter to the new field
        DeviationHighlighter.attachDouble(toolDiameter, () -> librarySnapshot == null ? null
                : valueInDisplayUnit(librarySnapshot.getDiameterInMm()));
        if (populateAll) {
            try {
                feedSpeed.setText(String.valueOf(librarySnapshot.getFeedSpeed()));
                plungeSpeed.setText(String.valueOf(librarySnapshot.getPlungeSpeed()));
                depthPerPass.setText(Utils.formatter.format(librarySnapshot.getDepthPerPass()));
                stepOver.setText(Utils.formatter.format(librarySnapshot.getStepOverPercent() * 100));
                maxSpindleSpeed.setDoubleValue(librarySnapshot.getMaxSpindleSpeed());
                spindleDirection.setSelectedItem(librarySnapshot.getSpindleDirection());
            } catch (RuntimeException ignored) {
                // Bad format — leave field as-is
            }
        }
        updateSelectedToolLabel();
    }

    private void updateSelectedToolLabel() {
        if (librarySnapshot == null) {
            selectedToolLabel.setText("— Custom —");
        } else {
            selectedToolLabel.setText(librarySnapshot.getName() == null
                    ? "— Custom —" : librarySnapshot.getName());
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
        try {
            return Utils.formatter.parse(stepOver.getText()).doubleValue() / 100;
        } catch (ParseException e) {
            return controller.getSettings().getToolStepOver();
        }
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

    public Settings getSettings() {
        Settings settings = new Settings();
        settings.applySettings(controller.getSettings());
        settings.setSafeHeight(getSafeHeight());
        settings.setDepthPerPass(getDepthPerPass());
        settings.setFeedSpeed(getFeedSpeed());
        settings.setToolDiameter(getToolDiameter());
        settings.setToolStepOver(getStepOver());
        settings.setPlungeSpeed(getPlungeSpeed());
        settings.setLaserDiameter(getLaserDiameter());
        settings.setMaxSpindleSpeed((int) getMaxSpindleSpeed());
        settings.setDetectMaxSpindleSpeed(getDetectMaxSpindleSpeed());
        settings.setSpindleDirection(getSpindleDirection());
        settings.setFlatnessPrecision(getFlatnessPrecision());
        if (librarySnapshot != null) {
            settings.setCurrentToolId(librarySnapshot.getId());
            settings.setCurrentToolSnapshot(new ToolDefinition(librarySnapshot));
        } else {
            settings.setCurrentToolId(null);
            settings.setCurrentToolSnapshot(null);
        }
        return settings;
    }
}
