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
        add(getOption("visualizer.color.background", "Background Color", "", new Color(220,235,255)));

        // Tool renderable
        add(getOption("visualizer.color.tool", "Tool Color", "", new Color(237,255,0)));

        // GcodeModel renderable
        add(getOption("visualizer.color.linear", "Linear Movement Color (G1)", "", new Color(0,0,158)));
        add(getOption("visualizer.color.rapid", "Rapid Movement Color (G0)", "", new Color(204,204,0)));
        add(getOption("visualizer.color.arc", "Arc Movement Color (G2/G3)", "", new Color(178,34,34)));
        add(getOption("visualizer.color.plunge", "Plunge/Raise Movement Color", "", new Color(0,100,0)));
        add(getOption("visualizer.color.completed", "Color of lines which have been completed", "", new Color(190,190,190)));

        // Highlight renderable
        add(getOption("visualizer.color.highlight", "Editor Line Highlight Color", "", new Color(237,255,0)));

        // Grid renderable
        add(getOption("visualizer.color.xy-grid", "Color (and opacity) of XY grid lines.", "", new Color(179,179,179)));
        add(getOption("visualizer.color.xy-plane", "Color (and opacity) of XY plane.", "", new Color(77,77,77,29)));
        add(getOption("visualizer.color.x-axis", "Color (and opacity) of X-Axis line.", "", new Color(230,0,0)));
        add(getOption("visualizer.color.y-axis", "Color (and opacity) of Y-Axis line.", "", new Color(0,0,230)));
        add(getOption("visualizer.color.z-axis", "Color (and opacity) of Z-Axis line.", "", new Color(0,230,0)));
       
        // SizeDisplay renderable
        add(getOption("visualizer.color.sizedisplay", "Color of size display lines and text.", "", new Color(128,128,128)));

    }

    private Option getOption(String op, String loc, String desc, Color def) {
        return new Option<>(op, loc, desc, getColorOption(op, def));
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
