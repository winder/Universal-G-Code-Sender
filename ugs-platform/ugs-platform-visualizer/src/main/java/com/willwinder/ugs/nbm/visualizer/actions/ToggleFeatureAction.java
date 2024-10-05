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
package com.willwinder.ugs.nbm.visualizer.actions;

import com.willwinder.ugs.nbm.visualizer.options.VisualizerOptions;
import com.willwinder.universalgcodesender.i18n.Localization;
import org.openide.awt.Actions;
import org.openide.util.ImageUtilities;

import javax.swing.AbstractAction;
import javax.swing.Action;
import java.awt.event.ActionEvent;

/**
 * A action base class for toggling a feature in the visualizer
 *
 * @author Joacim Breiler
 */
public class ToggleFeatureAction extends AbstractAction {
    public static final String SMALL_ICON_PATH = "icons/grid.svg";
    public static final String LARGE_ICON_PATH = "icons/grid24.svg";

    private final String toggleKey;

    protected ToggleFeatureAction(String toggleKey, String descriptionKey) {
        this.toggleKey = toggleKey;

        String title = Localization.getString(toggleKey);
        putValue(NAME, title);
        putValue("menuText", title);
        putValue(SHORT_DESCRIPTION, Localization.getString(descriptionKey));

        putValue("iconBase", SMALL_ICON_PATH);
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(SMALL_ICON, ImageUtilities.loadImageIcon(SMALL_ICON_PATH, false));
        putValue(LARGE_ICON_KEY, ImageUtilities.loadImageIcon(LARGE_ICON_PATH, false));

        updateState();
        VisualizerOptions.addListener(this::updateState);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        boolean newValue = !VisualizerOptions.getBooleanOption(toggleKey, true);
        VisualizerOptions.setBooleanOption(toggleKey, newValue);

        updateState();
    }

    private void updateState() {
        boolean value = VisualizerOptions.getBooleanOption(toggleKey, true);
        putValue(Action.SELECTED_KEY, value);
        putValue(Actions.ACTION_VALUE_TOGGLE, value);
        setEnabled(true);
        VisualizerOptions.setBooleanOption(toggleKey, value);
    }
}
