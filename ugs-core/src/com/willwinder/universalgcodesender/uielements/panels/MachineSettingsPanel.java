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
package com.willwinder.universalgcodesender.uielements.panels;

import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.uielements.IChanged;
import com.willwinder.universalgcodesender.uielements.helpers.AbstractUGSSettings;
import com.willwinder.universalgcodesender.utils.Settings;
import com.willwinder.universalgcodesender.utils.SettingsFactory;
import net.miginfocom.swing.MigLayout;

import javax.swing.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author wwinder
 */
public class MachineSettingsPanel extends AbstractUGSSettings {
    private final Map<Axis, JCheckBoxMenuItem> disabledAxes = new HashMap<>();

    public MachineSettingsPanel(Settings settings, IChanged changer) {
        super(settings, changer);
        String label = Localization.getString("settings.disableAxis");
        Arrays.stream(Axis.values()).forEach(axis -> {
            JCheckBoxMenuItem item = new JCheckBoxMenuItem(String.format(label, axis.name()));
            item.addActionListener(e -> change());
            disabledAxes.put(axis, item);
        });
        super.updateComponents();
    }

    public MachineSettingsPanel(Settings settings) {
        this(settings, null);
    }

    @Override
    public String getHelpMessage() {
        // This seems to be for UGS classic, I didn't see it in the platform.
        return Localization.getString("settings.help.disableAxis") + "\n\n";
    }

    @Override
    public void save() {
        disabledAxes.entrySet().forEach(entry ->
                settings.setAxisEnabled(
                        entry.getKey(),
                        !entry.getValue().isSelected()));
        SettingsFactory.saveSettings(settings);
    }

    @Override
    public void restoreDefaults() {
        updateComponents(new Settings());
        save();
    }

    @Override
    protected void updateComponentsInternal(Settings s) {
        this.removeAll();

        setLayout(new MigLayout("", "fill"));
        // Make sure they are added in enum-order.
        Arrays.stream(Axis.values()).forEach(a -> {
            JCheckBoxMenuItem item = disabledAxes.get(a);
            item.setSelected(!settings.isAxisEnabled(a));
            add(item, "wrap");
        });
    }
}
