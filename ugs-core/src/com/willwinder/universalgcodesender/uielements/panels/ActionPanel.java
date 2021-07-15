/*
    Copyright 2016 Will Winder

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
package com.willwinder.universalgcodesender.uielements.panels;

import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.uielements.macros.MacroActionPanel;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;

public class ActionPanel extends JPanel {

    private final ActionButtonPanel actionButtonPanel;
    private final MacroActionPanel macroActionPanel;

    public ActionPanel(BackendAPI backend) {
        actionButtonPanel = new ActionButtonPanel(backend);
        macroActionPanel = new MacroActionPanel(backend);

        initComponents();
    }

    private void initComponents() {
        MigLayout layout = new MigLayout("fill");
        setLayout(layout);

        add(actionButtonPanel);
        add(macroActionPanel, "grow");
    }
}
