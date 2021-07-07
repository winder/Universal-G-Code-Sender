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

import com.willwinder.ugs.nbp.designer.entities.cuttable.CutType;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.logic.Controller;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Joacim Breiler
 */
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