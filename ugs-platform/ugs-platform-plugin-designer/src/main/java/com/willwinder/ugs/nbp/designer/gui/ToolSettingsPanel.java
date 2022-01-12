/*
    Copyright 2021 Will Winder

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
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.awt.*;
import java.text.ParseException;

/**
 * @author Joacim Breiler
 */
public class ToolSettingsPanel extends JPanel {
    private final Controller controller;
    private JTextField toolDiameter;
    private JTextField feedSpeed;
    private JTextField plungeSpeed;
    private JTextField depthPerPass;
    private JTextField stepOver;
    private JTextField safeHeight;

    public ToolSettingsPanel(Controller controller) {
        this.controller = controller;
        initComponents();
        setMinimumSize(new Dimension(300, 300));
        setPreferredSize(new Dimension(300, 300));
    }

    private void initComponents() {
        setLayout(new MigLayout("fill", "[20%][80%]"));

        add(new JLabel("Tool diameter"));
        toolDiameter = new TextFieldWithUnit(Unit.MM, 3, controller.getSettings().getToolDiameter());
        add(toolDiameter, "grow, wrap");

        add(new JLabel("Feed speed"));
        feedSpeed = new TextFieldWithUnit(Unit.MM_PER_MINUTE, 0, controller.getSettings().getFeedSpeed());
        add(feedSpeed, "grow, wrap");

        add(new JLabel("Plunge speed"));
        plungeSpeed = new TextFieldWithUnit(Unit.MM_PER_MINUTE, 0, controller.getSettings().getPlungeSpeed());
        add(plungeSpeed, "grow, wrap");

        add(new JLabel("Depth per pass"));
        depthPerPass = new TextFieldWithUnit(Unit.MM, 2, controller.getSettings().getDepthPerPass());
        add(depthPerPass, "grow, wrap");

        add(new JLabel("Step over"));
        stepOver =  new TextFieldWithUnit(Unit.PERCENT, 2,
                controller.getSettings().getToolStepOver());
        add(stepOver, "grow, wrap");

        add(new JLabel("Safe height"));
        safeHeight = new TextFieldWithUnit(Unit.MM, 2, controller.getSettings().getSafeHeight());
        add(safeHeight, "grow, wrap");
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

    public Settings getSettings() {
        Settings settings = new Settings();
        settings.applySettings(controller.getSettings());
        settings.setSafeHeight(getSafeHeight());
        settings.setDepthPerPass(getDepthPerPass());
        settings.setFeedSpeed(getFeedSpeed());
        settings.setToolDiameter(getToolDiameter());
        settings.setToolStepOver(getStepOver());
        settings.setPlungeSpeed(getPlungeSpeed());
        return settings;
    }
}
