/*
    Copyright 2016-2021 Will Winder

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
import com.willwinder.ugs.nbm.visualizer.shared.IRenderableRegistrationService;
import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import com.willwinder.ugs.nbm.visualizer.shared.RenderableCheckBox;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.Position;
import org.openide.util.Lookup;

import javax.swing.*;
import java.awt.*;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;

/**
 * @author wwinder
 */
public class VisualizerPopupMenu extends JPopupMenu {
    private final BackendAPI backend;
    private final Position position;

    public VisualizerPopupMenu(BackendAPI backend, Position clickedWorkPosition) {
        this.backend = backend;
        this.position = clickedWorkPosition;
    }

    @Override
    public void show(Component invoker, int x, int y) {
        removeAll();

        createViewPresetSubmenu();
        createShowRenderablesSubmenu();

        CoordinatesSubMenu coordSubMenu = new CoordinatesSubMenu(backend, position);
        add(coordSubMenu);

        addActions();

        super.show(invoker, x, y);
    }

    private void addActions() {
        List<Action> actionList = VisualizerPopupActionsManager.getActionList();
        if (!actionList.isEmpty()) {
            addSeparator();
            actionList.forEach(action -> add(new JMenuItem(action)));
        }
    }

    private void createShowRenderablesSubmenu() {
        IRenderableRegistrationService renderableService =
                Lookup.getDefault().lookup(IRenderableRegistrationService.class);
        Collection<Renderable> renderables = renderableService.getRenderables();

        JMenu menu = new JMenu(Localization.getString("platform.visualizer.popup.showFeatures"));
        add(menu);
        renderables.stream()
                .sorted(Comparator.comparing(Renderable::getTitle))
                .map(RenderableCheckBox::new)
                .forEach(menu::add);
    }

    private void createViewPresetSubmenu() {
        JMenu menu = new JMenu(Localization.getString("platform.visualizer.popup.viewPresets"));
        add(menu);

        JMenuItem menuItem = new JMenuItem(new CameraResetPreset());
        menuItem.setText(Localization.getString("platform.visualizer.popup.presets.reset"));
        menu.add(menuItem);

        menuItem = new JMenuItem(new CameraZPreset());
        menuItem.setText(Localization.getString("platform.visualizer.popup.presets.top"));
        menu.add(menuItem);

        menuItem = new JMenuItem(new CameraXPreset());
        menuItem.setText(Localization.getString("platform.visualizer.popup.presets.left"));
        menu.add(menuItem);

        menuItem = new JMenuItem(new CameraYPreset());
        menuItem.setText(Localization.getString("platform.visualizer.popup.presets.front"));
        menu.add(menuItem);
    }
}
