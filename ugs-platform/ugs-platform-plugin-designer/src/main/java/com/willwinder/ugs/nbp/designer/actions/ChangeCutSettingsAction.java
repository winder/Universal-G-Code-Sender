package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.entities.cuttable.CutType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.logic.Controller;

import javax.swing.*;
import java.awt.event.ActionEvent;

public class ChangeCutSettingsAction extends AbstractAction implements UndoableAction {

    private final transient Controller controller;
    private final double previousCutDepth;
    private final CutType previousCutType;
    private final double newCutDepth;
    private final CutType newCutType;
    private final Cuttable cuttable;

    public ChangeCutSettingsAction(Controller controller, Cuttable cuttable, double cutDepth, CutType cutType) {
        this.cuttable = cuttable;
        previousCutDepth = cuttable.getCutDepth();
        previousCutType = cuttable.getCutType();
        newCutDepth = cutDepth;
        newCutType = cutType;

        this.controller = controller;
        putValue("menuText", "Change stock settings");
        putValue(NAME, "Change stock settings");
    }
    @Override
    public void redo() {
        actionPerformed(null);
    }

    @Override
    public void undo() {
        cuttable.setCutDepth(previousCutDepth);
        cuttable.setCutType(previousCutType);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        cuttable.setCutDepth(newCutDepth);
        cuttable.setCutType(newCutType);
        this.controller.getDrawing().repaint();
    }
}