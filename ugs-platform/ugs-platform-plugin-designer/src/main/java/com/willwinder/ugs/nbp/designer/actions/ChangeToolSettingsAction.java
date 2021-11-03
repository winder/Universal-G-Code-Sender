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
package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.logic.Controller;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Joacim Breiler
 */
public class ChangeToolSettingsAction extends AbstractAction implements UndoableAction {

    private final double previousToolDiameter;
    private final double newToolDiameter;
    private final transient Controller controller;
    private final int previousFeedSpeed;
    private final int newFeedSpeed;
    private final int previousPlungeSpeed;
    private final int newPlungeSpeed;
    private final double previousStepOver;
    private final double newStepOver;
    private final double previousDepthPerPass;
    private final double newDepthPerPass;

    public ChangeToolSettingsAction(Controller controller, double toolDiameter, int feedSpeed, int plungeSpeed, double depthPerPass, double stepOver) {
        this.previousToolDiameter = controller.getSettings().getToolDiameter();
        this.newToolDiameter = toolDiameter;
        this.previousFeedSpeed = controller.getSettings().getFeedSpeed();
        this.newFeedSpeed = feedSpeed;
        this.previousPlungeSpeed = controller.getSettings().getPlungeSpeed();
        this.newPlungeSpeed = plungeSpeed;
        this.previousStepOver = controller.getSettings().getToolStepOver();
        this.newStepOver = stepOver;
        this.previousDepthPerPass = controller.getSettings().getDepthPerPass();
        this.newDepthPerPass = depthPerPass;
        this.controller = controller;
        putValue("menuText", "Change tool settings");
        putValue(NAME, "Change tool settings");
    }

    @Override
    public void redo() {
        actionPerformed(null);
    }

    @Override
    public void undo() {
        this.controller.getSettings().setToolDiameter(previousToolDiameter);
        this.controller.getSettings().setFeedSpeed(previousFeedSpeed);
        this.controller.getSettings().setPlungeSpeed(previousPlungeSpeed);
        this.controller.getSettings().setDepthPerPass(previousDepthPerPass);
        this.controller.getSettings().setToolStepOver(previousStepOver);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.controller.getSettings().setToolDiameter(newToolDiameter);
        this.controller.getSettings().setFeedSpeed(newFeedSpeed);
        this.controller.getSettings().setPlungeSpeed(newPlungeSpeed);
        this.controller.getSettings().setDepthPerPass(newDepthPerPass);
        this.controller.getSettings().setToolStepOver(newStepOver);
    }

    @Override
    public String toString() {
        return "tool settings";
    }
}
