/*
    Copyright 2022-2024 Will Winder

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
import com.willwinder.ugs.nbm.visualizer.actions.ToggleBoundaryFeatureAction;
import com.willwinder.ugs.nbm.visualizer.actions.ToggleGridFeatureAction;
import com.willwinder.ugs.nbm.visualizer.actions.ToggleModelFeatureAction;
import com.willwinder.ugs.nbm.visualizer.actions.ToggleMouseFeatureAction;
import com.willwinder.ugs.nbm.visualizer.actions.ToggleOrientationFeatureAction;
import com.willwinder.ugs.nbm.visualizer.actions.TogglePlaneFeatureAction;
import com.willwinder.ugs.nbm.visualizer.actions.ToggleSelectFeatureAction;
import com.willwinder.ugs.nbm.visualizer.actions.ToggleSizeFeatureAction;
import com.willwinder.ugs.nbm.visualizer.actions.ToggleToolFeatureAction;
import com.willwinder.ugs.nbp.core.actions.OutlineAction;
import com.willwinder.ugs.nbp.core.actions.ToggleUnitAction;
import com.willwinder.ugs.nbp.core.ui.ToolBar;
import org.openide.awt.DropDownButtonFactory;
import org.openide.util.ImageUtilities;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JToggleButton;
import java.awt.event.ActionListener;

/**
 * A toolbar for visualizer actions
 *
 * @author Joacim Breiler
 */
public class VisualizerToolBar extends ToolBar {
    private JButton cameraPresetButton = null;

    public VisualizerToolBar() {
        setFloatable(false);
        initComponents();
    }

    private void initComponents() {
        add(createCameraPresetDropDownButton());
        addSeparator();
        createAndAddToggleButton(new TogglePlaneFeatureAction());
        createAndAddToggleButton(new ToggleGridFeatureAction());
        createAndAddToggleButton(new ToggleSizeFeatureAction());
        createAndAddToggleButton(new ToggleToolFeatureAction());
        createAndAddToggleButton(new ToggleBoundaryFeatureAction());
        createAndAddToggleButton(new ToggleSelectFeatureAction());
        createAndAddToggleButton(new ToggleOrientationFeatureAction());
        createAndAddToggleButton(new ToggleModelFeatureAction());
        createAndAddToggleButton(new ToggleMouseFeatureAction());
        addSeparator();
        createAndAddButton(new OutlineAction());
        add(Box.createGlue());
        createAndAddButton(new ToggleUnitAction());
    }

    private void createAndAddToggleButton(Action action) {
        JToggleButton resetPresetButton = new JToggleButton(action);
        resetPresetButton.setText("");
        resetPresetButton.setToolTipText((String) action.getValue(Action.SHORT_DESCRIPTION));
        this.add(resetPresetButton);
    }

    private void createAndAddButton(Action action) {
        JButton resetPresetButton = new JButton(action);
        resetPresetButton.setText("");
        resetPresetButton.setToolTipText((String) action.getValue(Action.SHORT_DESCRIPTION));
        this.add(resetPresetButton);
    }


    private JButton createCameraPresetDropDownButton() {
        // An action listener that listens to the popup menu items and changes the current action
        ActionListener toolMenuListener = e -> {
            if (cameraPresetButton == null) {
                return;
            }

            JMenuItem source = (JMenuItem) e.getSource();
            cameraPresetButton.setIcon((Icon) source.getAction().getValue(Action.LARGE_ICON_KEY));
            cameraPresetButton.setSelected(false);
            cameraPresetButton.setAction(source.getAction());
        };

        CameraResetPreset cameraResetPreset = new CameraResetPreset();
        JPopupMenu popupMenu = new JPopupMenu();
        addDropDownAction(popupMenu, cameraResetPreset, toolMenuListener);
        addDropDownAction(popupMenu, new CameraXPreset(), toolMenuListener);
        addDropDownAction(popupMenu, new CameraYPreset(), toolMenuListener);
        addDropDownAction(popupMenu, new CameraZPreset(), toolMenuListener);
        cameraPresetButton = DropDownButtonFactory.createDropDownButton(ImageUtilities.loadImageIcon(CameraResetPreset.LARGE_ICON_PATH, false), popupMenu);
        cameraPresetButton.setAction(cameraResetPreset);
        return cameraPresetButton;
    }

    private void addDropDownAction(JPopupMenu popupMenu, Action action, ActionListener actionListener) {
        JMenuItem menuItem = new JMenuItem(action);
        if (actionListener != null) {
            menuItem.addActionListener(actionListener);
        }
        popupMenu.add(menuItem);
    }

}
