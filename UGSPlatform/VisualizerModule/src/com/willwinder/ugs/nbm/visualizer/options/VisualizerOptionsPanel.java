/*
    Copywrite 2016 Will Winder

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
package com.willwinder.ugs.nbm.visualizer.options;

import com.willwinder.ugs.nbp.lib.options.AbstractOptionsPanel;
import com.willwinder.ugs.nbp.lib.options.IChanged;
import com.willwinder.ugs.nbp.lib.options.OptionTable.Option;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import org.openide.util.NbPreferences;

final class VisualizerOptionsPanel extends AbstractOptionsPanel {
    private Collection<colorPref> colorPreferences;

    private class colorPref {
        public String preference;
        public String localized;
        public Color defaultColor;
        public colorPref(String p, String l, Color c) {
            preference = p;
            localized = l;
            defaultColor = c;
        }
    }

    public VisualizerOptionsPanel(IChanged changer) {
        super(changer);
        colorPreferences = new ArrayList<>();

        colorPreferences.add(new colorPref("vizualizer.color.background", "Background Color", new Color(220,235,255)));
        colorPreferences.add(new colorPref("vizualizer.color.tool", "Tool Color", new Color(237,255,0)));
        colorPreferences.add(new colorPref("vizualizer.color.linear", "Linear Movement Color (G1)", new Color(0,0,158)));
        colorPreferences.add(new colorPref("vizualizer.color.rapid", "Rapid Movement Color (G0)", new Color(204,204,0)));
        colorPreferences.add(new colorPref("vizualizer.color.arc", "Arc Movement Color (G2/G3)", new Color(178,34,34)));
        colorPreferences.add(new colorPref("vizualizer.color.plunge", "Plunge/Raise Movement Color", new Color(0,100,0)));
        colorPreferences.add(new colorPref("vizializer.color.highlight", "Editor Line Highlight Color", new Color(237,255,0)));
    }

    private Color getColorOption(String option, Color defaultColor) {
        int pref = NbPreferences.forModule(VisualizerOptionsPanel.class).getInt(option, defaultColor.getRGB());
        return new Color(pref, true);
    }

    @Override
    public void load() {
        // TODO read settings and initialize GUI
        // Example:        
        // someCheckBox.setSelected(Preferences.userNodeForPackage(VisualizerPanel.class).getBoolean("someFlag", false));
        // or for org.openide.util with API spec. version >= 7.4:
        // someCheckBox.setSelected(NbPreferences.forModule(VisualizerPanel.class).getBoolean("someFlag", false));
        // or:
        // someTextField.setText(SomeSystemOption.getDefault().getSomeStringProperty());

        for (colorPref cp : colorPreferences) {
            Color c = getColorOption(cp.preference, cp.defaultColor);
            this.add(new Option<>(cp.localized, "", c));
        }
    }

    @Override
    public void store() {
        // TODO store modified settings
        // Example:
        // Preferences.userNodeForPackage(VisualizerPanel.class).putBoolean("someFlag", someCheckBox.isSelected());
        // or for org.openide.util with API spec. version >= 7.4:
        // NbPreferences.forModule(VisualizerPanel.class).putBoolean("someFlag", someCheckBox.isSelected());
        // or:
        // SomeSystemOption.getDefault().setSomeStringProperty(someTextField.getText());
    }

    @Override
    public boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }
}
