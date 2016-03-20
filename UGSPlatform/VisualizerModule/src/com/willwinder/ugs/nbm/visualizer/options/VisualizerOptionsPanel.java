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

    private void setColorOption(String option, Color color) {
        NbPreferences.forModule(VisualizerOptionsPanel.class).putInt(option, color.getRGB());
    }

    @Override
    public void load() {
        for (colorPref cp : colorPreferences) {
            Color c = getColorOption(cp.preference, cp.defaultColor);
            this.add(new Option<>(cp.localized, "", c));
        }
    }

    @Override
    public void store() {
        // n^2 whoop whoo
        for (int i = 0; i < optionTable.getModel().getRowCount(); i++) {
            String preference = (String) optionTable.getModel().getValueAt(i, 0);
            for (colorPref cp : colorPreferences) {
                if (cp.localized.equals(preference)) {
                    setColorOption(cp.preference, (Color)optionTable.getModel().getValueAt(i,1));
                }
            }
        }
    }

    @Override
    public boolean valid() {
        // TODO check whether form is consistent and complete
        return true;
    }
}
