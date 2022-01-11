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
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Joacim Breiler
 */
public class ChangeCutSettingsAction extends AbstractAction implements UndoableAction {

    private final transient Controller controller;
    private final List<Double> previousStartDepth;
    private final List<Double> previousCutDepth;
    private final List<CutType> previousCutType;
    private final double newStartDepth;
    private final double newCutDepth;
    private final CutType newCutType;
    private final List<Cuttable> cuttableList;

    public ChangeCutSettingsAction(Controller controller, List<Cuttable> cuttableList, double startDepth, double targetDepth, CutType cutType) {
        this.cuttableList = cuttableList;
        previousStartDepth = cuttableList.stream().map(Cuttable::getStartDepth).collect(Collectors.toList());
        previousCutDepth = cuttableList.stream().map(Cuttable::getTargetDepth).collect(Collectors.toList());
        previousCutType = cuttableList.stream().map(Cuttable::getCutType).collect(Collectors.toList());
        newStartDepth = startDepth;
        newCutDepth = targetDepth;
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
        for (int i = 0; i < cuttableList.size(); i++) {
            cuttableList.get(i).setStartDepth(previousStartDepth.get(i));
            cuttableList.get(i).setTargetDepth(previousCutDepth.get(i));
            cuttableList.get(i).setCutType(previousCutType.get(i));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (Cuttable cuttable : cuttableList) {
            cuttable.setStartDepth(newStartDepth);
            cuttable.setTargetDepth(newCutDepth);
            cuttable.setCutType(newCutType);
        }
        this.controller.getDrawing().repaint();
    }

    @Override
    public String toString() {
        return "cut settings";
    }
}