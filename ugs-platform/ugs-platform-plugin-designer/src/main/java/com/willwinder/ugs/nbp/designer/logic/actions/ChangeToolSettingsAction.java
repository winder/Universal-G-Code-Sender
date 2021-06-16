package com.willwinder.ugs.nbp.designer.logic.actions;

import com.willwinder.ugs.nbp.designer.logic.Controller;

import javax.swing.*;
import java.awt.event.ActionEvent;

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
}
