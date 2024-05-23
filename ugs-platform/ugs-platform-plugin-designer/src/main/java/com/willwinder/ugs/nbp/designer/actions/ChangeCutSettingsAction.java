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

import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Cuttable;
import com.willwinder.ugs.nbp.designer.logic.Controller;

import javax.swing.AbstractAction;
import java.awt.event.ActionEvent;
import java.util.ArrayList;
import java.util.List;

/**
 * @author Joacim Breiler
 */
public class ChangeCutSettingsAction extends AbstractAction implements UndoableAction {

    private final transient Controller controller;
    private final transient List<Cuttable> cuttableList;
    private final Object newValue;
    private final List<Object> previousValue;
    private final EntitySetting entitySetting;

    public ChangeCutSettingsAction(Controller controller, List<Cuttable> cuttableList, EntitySetting entitySetting, Object value) {
        this.cuttableList = new ArrayList<>(cuttableList);
        this.entitySetting = entitySetting;
        previousValue = cuttableList.stream().map(c -> c.getEntitySetting(entitySetting).orElse(null)).toList();
        newValue = value;

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
            cuttableList.get(i).setEntitySetting(entitySetting, previousValue.get(i));
        }
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        for (Cuttable cuttable : cuttableList) {
            cuttable.setEntitySetting(entitySetting, newValue);
        }
        this.controller.getDrawing().repaint();
    }

    @Override
    public String toString() {
        return "cut settings";
    }
}
