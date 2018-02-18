/*
    Copyright 2016-2018 Will Winder

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

import com.willwinder.ugs.nbm.visualizer.actions.JogToHereAction;
import com.willwinder.ugs.nbm.visualizer.actions.MoveCameraAction;
import com.willwinder.ugs.nbm.visualizer.shared.GcodeRenderer;
import com.willwinder.ugs.nbm.visualizer.shared.IRenderableRegistrationService;
import com.willwinder.ugs.nbm.visualizer.shared.Renderable;
import com.willwinder.ugs.nbm.visualizer.shared.RenderableCheckBox;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.BackendAPI;
import org.openide.util.Lookup;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;
import java.util.Collection;
import java.util.Comparator;

/**
 * @author wwinder
 */
public class VisualizerPopupMenu extends JPopupMenu {
    private final JogToHereAction jogToHereAction;
    private final JMenuItem jogToHere = new JMenuItem();
    private final DecimalFormat decimalFormatter =
            new DecimalFormat("#.#####", Localization.dfs);

    private final GcodeRenderer gcodeRenderer;

    public VisualizerPopupMenu(BackendAPI backend, GcodeRenderer gcodeRenderer) {
        jogToHereAction = new JogToHereAction(backend);

        jogToHere.setText(String.format(Localization.getString("platform.visualizer.jogToHere"), 0, 0));

        jogToHere.setAction(jogToHereAction);

        this.gcodeRenderer = gcodeRenderer;
    }

    @Override
    public void show(Component invoker, int x, int y) {
        removeAll();

        createViewPresetSubmenu();
        createShowRenderablesSubmenu();

        add(jogToHere);

        super.show(invoker, x, y);
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

        JMenuItem menuItem = new JMenuItem(new MoveCameraAction(gcodeRenderer, MoveCameraAction.CAMERA_POSITION, MoveCameraAction.ROTATION_ISOMETRIC, 1));
        menuItem.setText(Localization.getString("platform.visualizer.popup.presets.reset"));
        menu.add(menuItem);

        menuItem = new JMenuItem(new MoveCameraAction(gcodeRenderer, MoveCameraAction.CAMERA_POSITION, MoveCameraAction.ROTATION_TOP, 1));
        menuItem.setText(Localization.getString("platform.visualizer.popup.presets.top"));
        menu.add(menuItem);

        menuItem = new JMenuItem(new MoveCameraAction(gcodeRenderer, MoveCameraAction.CAMERA_POSITION, MoveCameraAction.ROTATION_LEFT, 1));
        menuItem.setText(Localization.getString("platform.visualizer.popup.presets.left"));
        menu.add(menuItem);

        menuItem = new JMenuItem(new MoveCameraAction(gcodeRenderer, MoveCameraAction.CAMERA_POSITION, MoveCameraAction.ROTATION_FRONT, 1));
        menuItem.setText(Localization.getString("platform.visualizer.popup.presets.front"));
        menu.add(menuItem);
    }

    public void setJogLocation(double x, double y) {
        String strX = decimalFormatter.format(x);
        String strY = decimalFormatter.format(y);

        jogToHereAction.setJogLocation(strX, strY);
        String jogToHereString = Localization.getString("platform.visualizer.popup.jogToHere");
        jogToHereString = jogToHereString.replaceAll("%f", "%s");
        jogToHere.setText(String.format(jogToHereString, strX, strY));
    }
}
