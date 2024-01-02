/*
    Copyright 2024 Will Winder

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

import com.willwinder.ugs.nbp.designer.entities.cuttable.Text;

public class ChangeTextAction implements UndoableAction {

    private final Text entity;
    private final String newText;
    private final String oldText;

    public ChangeTextAction(Text entity, String text) {
        this.entity = entity;
        this.newText = text;
        this.oldText = entity.getText();
    }

    @Override
    public void redo() {
        entity.setText(newText);
    }

    @Override
    public void undo() {
        entity.setText(oldText);
    }

    @Override
    public String toString() {
        return "changed text";
    }
}
