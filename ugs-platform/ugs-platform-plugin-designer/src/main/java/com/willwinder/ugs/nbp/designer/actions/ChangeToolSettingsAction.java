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
import com.willwinder.ugs.nbp.designer.model.Settings;

import javax.swing.*;
import java.awt.event.ActionEvent;

/**
 * @author Joacim Breiler
 */
public class ChangeToolSettingsAction extends AbstractAction implements UndoableAction {

    private final Settings previousSettings;
    private final Settings newSettings;
    private final transient Controller controller;

    public ChangeToolSettingsAction(Controller controller, Settings settings) {
        this.previousSettings = new Settings(controller.getSettings());
        this.newSettings = settings;
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
        this.controller.getSettings().applySettings(previousSettings);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.controller.getSettings().applySettings(newSettings);
    }

    @Override
    public String toString() {
        return "tool settings";
    }
}
