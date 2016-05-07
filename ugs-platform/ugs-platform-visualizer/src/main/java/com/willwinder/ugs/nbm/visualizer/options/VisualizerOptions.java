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

import com.willwinder.ugs.nbp.lib.options.OptionTable.Option;
import com.willwinder.universalgcodesender.i18n.Localization;
import java.awt.Color;
import java.util.ArrayList;
import org.openide.util.NbPreferences;

/**
 *
 * @author wwinder
 */
public class VisualizerOptions extends ArrayList<Option> {

    public class ColorPref {
        public String preference;
        public String localized;
        public Color defaultColor;
        public ColorPref(String p, String l, Color c) {
            preference = p;
            localized = l;
            defaultColor = c;
        }
    }


    public VisualizerOptions() {
        // GcodeRenderer clear color
        add(getOption("platform.visualizer.color.background", "", new Color(220,235,255)));

        // Tool renderable
        add(getOption("platform.visualizer.color.tool", "", new Color(237,255,0)));

        // GcodeModel renderable
        add(getOption("platform.visualizer.color.linear", "", new Color(0,0,158)));
        add(getOption("platform.visualizer.color.rapid", "", new Color(204,204,0)));
        add(getOption("platform.visualizer.color.arc", "", new Color(178,34,34)));
        add(getOption("platform.visualizer.color.plunge", "", new Color(0,100,0)));
        add(getOption("platform.visualizer.color.completed", "", new Color(190,190,190)));

        // Highlight renderable
        add(getOption("platform.visualizer.color.highlight", "", new Color(237,255,0)));

        // Grid renderable
        add(getOption("platform.visualizer.color.xy-grid", "", new Color(179,179,179)));
        add(getOption("platform.visualizer.color.xy-plane", "", new Color(77,77,77,29)));
        add(getOption("platform.visualizer.color.x-axis", "", new Color(230,0,0)));
        add(getOption("platform.visualizer.color.y-axis", "", new Color(0,0,230)));
        add(getOption("platform.visualizer.color.z-axis", "", new Color(0,230,0)));
       
        // SizeDisplay renderable
        add(getOption("platform.visualizer.color.sizedisplay", "", new Color(128,128,128)));

    }

    private Option getOption(String op, String desc, Color def) {
        return new Option<>(op, Localization.getString(op), desc, getColorOption(op, def));
    }

    public Option getOptionForKey(String key) {
        for (Option op : this) {
            if (op.option.equals(key)) {
                return op;
            }
        }
        return null;
    }

    public static float[] colorToFloatArray(Color c) {
        float[] ret = new float[4];
        ret[0] = c.getRed()/255f;
        ret[1] = c.getGreen()/255f;
        ret[2] = c.getBlue()/255f;
        ret[3] = c.getAlpha()/255f;
        return ret;
    }

    public static Color getColorOption(String option, Color defaultColor) {
        int pref = NbPreferences.forModule(VisualizerOptions.class).getInt(option, defaultColor.getRGB());
        return new Color(pref, true);
    }

    public static void setColorOption(String option, Color color) {
        NbPreferences.forModule(VisualizerOptions.class).putInt(option, color.getRGB());
    }
}
