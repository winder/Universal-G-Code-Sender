/*
    Copyright 2022 Will Winder

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
package com.willwinder.ugs.nbm.visualizer;

import com.willwinder.ugs.nbm.visualizer.actions.CameraResetPreset;
import com.willwinder.ugs.nbm.visualizer.actions.CameraXPreset;
import com.willwinder.ugs.nbm.visualizer.actions.CameraYPreset;
import com.willwinder.ugs.nbm.visualizer.actions.CameraZPreset;
import com.willwinder.ugs.nbp.core.actions.OutlineAction;
import com.willwinder.ugs.nbp.core.ui.ToolBar;

import javax.swing.*;

/**
 * A toolbar for visualizer actions
 *
 * @author Joacim Breiler
 */
public class VisualizerToolBar extends ToolBar {
    public VisualizerToolBar() {
        setFloatable(false);
        initComponents();
    }

    private void initComponents() {
        createAndAddButton(new CameraResetPreset());
        createAndAddButton(new CameraXPreset());
        createAndAddButton(new CameraYPreset());
        createAndAddButton(new CameraZPreset());
        addSeparator();
        createAndAddButton(new OutlineAction());
    }

    private void createAndAddButton(Action action) {
        JButton resetPresetButton = new JButton(action);
        resetPresetButton.setText("");
        resetPresetButton.setToolTipText((String) action.getValue(Action.SHORT_DESCRIPTION));
        resetPresetButton.setContentAreaFilled(false);
        resetPresetButton.setBorderPainted(false);
        this.add(resetPresetButton);
    }
}
