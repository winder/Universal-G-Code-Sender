/*
    Copyright 2021-2024 Will Winder

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
package com.willwinder.ugs.nbp.designer.gui;

import com.willwinder.ugs.nbp.designer.logic.Controller;
import com.willwinder.ugs.nbp.designer.model.Settings;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.uielements.TextFieldUnit;
import com.willwinder.universalgcodesender.uielements.TextFieldWithUnit;
import net.miginfocom.swing.MigLayout;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Dimension;
import java.text.ParseException;

/**
 * @author Joacim Breiler
 */
public class ToolSettingsPanel extends JPanel {
    public static final String TOOL_FIELD_CONSTRAINT = "grow, wrap";
    private final transient Controller controller;
    private JTextField toolDiameter;
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

    public ToolSettingsPanel(Controller controller) {
        this.controller = controller;
        initComponents();
        setMinimumSize(new Dimension(300, 400));
        setPreferredSize(new Dimension(300, 400));
    }
    private void initComponents() {
        setLayout(new MigLayout("fill", "[20%][80%]" ));

        add(new JLabel("Tool diameter" ));
        toolDiameter = new TextFieldWithUnit(TextFieldUnit.MM, 3, controller.getSettings().getToolDiameter());
        add(toolDiameter, TOOL_FIELD_CONSTRAINT);

        add(new JLabel("Tool step over" ));
        stepOver = new TextFieldWithUnit(TextFieldUnit.PERCENT, 2,
                controller.getSettings().getToolStepOver());
        add(stepOver, TOOL_FIELD_CONSTRAINT);

        add(new JSeparator(SwingConstants.HORIZONTAL), "spanx, grow, wrap, hmin 2" );

        add(new JLabel("Default feed speed" ));
        feedSpeed = new TextFieldWithUnit(TextFieldUnit.MM_PER_MINUTE, 0, controller.getSettings().getFeedSpeed());
        add(feedSpeed, TOOL_FIELD_CONSTRAINT);

        add(new JLabel("Plunge speed" ));
        plungeSpeed = new TextFieldWithUnit(TextFieldUnit.MM_PER_MINUTE, 0, controller.getSettings().getPlungeSpeed());
        add(plungeSpeed, TOOL_FIELD_CONSTRAINT);

        add(new JLabel("Depth per pass" ));
        depthPerPass = new TextFieldWithUnit(TextFieldUnit.MM, 2, controller.getSettings().getDepthPerPass());
        add(depthPerPass, TOOL_FIELD_CONSTRAINT);

        add(new JLabel("Safe height" ));
        safeHeight = new TextFieldWithUnit(TextFieldUnit.MM, 2, controller.getSettings().getSafeHeight());
        add(safeHeight, TOOL_FIELD_CONSTRAINT);

        add(new JSeparator(SwingConstants.HORIZONTAL), "spanx, grow, wrap, hmin 2" );

        add(new JLabel("Detect max spindle speed" ));
        detectMaxSpindleSpeed = new JCheckBox("", controller.getSettings().getDetectMaxSpindleSpeed());
        add(detectMaxSpindleSpeed, TOOL_FIELD_CONSTRAINT);

        add(new JLabel("Max spindle speed" ));
        maxSpindleSpeed = new TextFieldWithUnit(TextFieldUnit.ROTATIONS_PER_MINUTE, 0, controller.getSettings().getMaxSpindleSpeed());
        add(maxSpindleSpeed, TOOL_FIELD_CONSTRAINT);

        add(new JSeparator(SwingConstants.HORIZONTAL), "spanx, grow, wrap, hmin 2" );

        add(new JLabel("Laser diameter" ));
        laserDiameter = new TextFieldWithUnit(TextFieldUnit.MM, 3, controller.getSettings().getLaserDiameter());
        add(laserDiameter, TOOL_FIELD_CONSTRAINT);

        add(new JSeparator(SwingConstants.HORIZONTAL), "spanx, grow, wrap, hmin 2" );

        add(new JLabel("Spindle Start Command" ));
        spindleDirection = new JComboBox<>(new DefaultComboBoxModel<>(new String[]{"M3","M4","M5"}));
        add(spindleDirection, TOOL_FIELD_CONSTRAINT);

        add(new JLabel("Arc precision" ));
        flatnessPrecision = new TextFieldWithUnit(TextFieldUnit.MM, 3, controller.getSettings().getFlatnessPrecision());
        add(flatnessPrecision, TOOL_FIELD_CONSTRAINT);
    }

    public double getToolDiameter() {
        try {
            return Utils.formatter.parse(toolDiameter.getText()).doubleValue();
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
        return settings;
    }
}
