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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class UndoActionList implements UndoableAction {

    public final List<UndoableAction> actionList;

    public UndoActionList(List<? extends UndoableAction> actionList) {
        this.actionList = new ArrayList<>(actionList);
    }

    public UndoActionList() {
        actionList = new ArrayList<>();
    }

    public void add(UndoableAction undoableAction) {
        actionList.add(undoableAction);
    }

    @Override
    public void redo() {
        actionList.forEach(UndoableAction::redo);
    }

    @Override
    public void undo() {
        actionList.forEach(UndoableAction::undo);
    }

    @Override
    public String toString() {
        return actionList.stream()
                .findFirst()
                .map(Object::toString)
                .orElse("action");
    }

    public List<UndoableAction> getActions() {
        return Collections.unmodifiableList(actionList);
    }
}
