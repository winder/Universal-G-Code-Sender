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
import com.willwinder.universalgcodesender.Utils;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
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

    public ToolSettingsPanel(Controller controller) {
        this.controller = controller;
        initComponents();
    }

    private void initComponents() {
        setLayout(new MigLayout("fill", "[20%][80%]"));

        add(new JLabel("Bit settings"), "span, wrap");
        add(new JLabel("Tool diameter"));
        toolDiameter = new JTextField(Utils.formatter.format(controller.getSettings().getToolDiameter()));
        add(toolDiameter, "grow, wrap");

        add(new JLabel("Feed speed"));
        feedSpeed = new JTextField(Utils.formatter.format(controller.getSettings().getFeedSpeed()));
        add(feedSpeed, "grow, wrap");

        add(new JLabel("Plunge speed"));
        plungeSpeed = new JTextField(Utils.formatter.format(controller.getSettings().getPlungeSpeed()));
        add(plungeSpeed, "grow, wrap");

        add(new JLabel("Depth per pass"));
        depthPerPass = new JTextField(Utils.formatter.format(controller.getSettings().getDepthPerPass()));
        add(depthPerPass, "grow, wrap");

        add(new JLabel("Step over"));
        stepOver = new JTextField(Utils.formatter.format(controller.getSettings().getToolStepOver()));
        add(stepOver, "grow, wrap");
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
            return Utils.formatter.parse(stepOver.getText()).doubleValue();
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
}
