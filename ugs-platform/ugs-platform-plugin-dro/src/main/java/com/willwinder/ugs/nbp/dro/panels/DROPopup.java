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
package com.willwinder.ugs.nbp.dro.panels;

import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.utils.Settings;

import javax.swing.*;
import java.awt.*;

/**
 * @author wwinder
 */
public class DROPopup extends JPopupMenu {
    private final BackendAPI backend;
    private final Settings settings;

    public DROPopup(BackendAPI backend) {
        super();
        this.backend = backend;
        this.settings = backend.getSettings();
    }

    @Override
    public void show(Component invoker, int x, int y) {
        removeAll();

        for (Axis a : Axis.values()) {
            boolean add = a.isLinear();

            // Linear axis by default, or if connected use capabilities.
            if (this.backend.isConnected()) {
                add = this.backend.getController().getCapabilities().hasAxis(a);
            }

            if (add) {
                final JCheckBoxMenuItem menu = new JCheckBoxMenuItem(
                        String.format("Disable Axis: %s", a.name()),
                        !this.settings.isAxisEnabled(a));
                menu.addActionListener(e -> {
                    this.settings.setAxisEnabled(a, !menu.isSelected());
                });
                add(menu);
            }
        }

        super.show(invoker, x, y);
    }
}
