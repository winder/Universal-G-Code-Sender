/*
    Copyright 2016 Will Winder

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
    // GcodeRenderer clear color
    public static String VISUALIZER_OPTION_BG = "platform.visualizer.color.background";

    // Tool renderable
    public static String VISUALIZER_OPTION_TOOL = "platform.visualizer.color.tool";

    // GcodeModel renderable
    public static String VISUALIZER_OPTION_LINEAR = "platform.visualizer.color.linear";
    public static String VISUALIZER_OPTION_RAPID = "platform.visualizer.color.rapid";
    public static String VISUALIZER_OPTION_ARC = "platform.visualizer.color.arc";
    public static String VISUALIZER_OPTION_PLUNGE = "platform.visualizer.color.plunge";
    public static String VISUALIZER_OPTION_COMPLETE = "platform.visualizer.color.completed";

    // Highlight renderable
    public static String VISUALIZER_OPTION_HIGHLIGHT = "platform.visualizer.color.highlight";

    // Grid renderable
    public static String VISUALIZER_OPTION_XY_GRID = "platform.visualizer.color.xy-grid";
    public static String VISUALIZER_OPTION_XY_PLANE = "platform.visualizer.color.xy-plane";
    public static String VISUALIZER_OPTION_X = "platform.visualizer.color.x-axis";
    public static String VISUALIZER_OPTION_Y = "platform.visualizer.color.y-axis";
    public static String VISUALIZER_OPTION_Z = "platform.visualizer.color.z-axis";
   
    // SizeDisplay renderable
    public static String VISUALIZER_OPTION_SIZE = "platform.visualizer.color.sizedisplay";

    // Autoleveler surface mesh
    public static String VISUALIZER_OPTION_HIGH = "platform.visualizer.color.surface.high";
    public static String VISUALIZER_OPTION_LOW = "platform.visualizer.color.surface.low";

    // Machine boundries
    public static final String VISUALIZER_OPTION_BOUNDRY_BASE = "platform.visualizer.color.boundry-base";
    public static final String VISUALIZER_OPTION_BOUNDRY_SIDES = "platform.visualizer.color.boundry-sides";


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
        add(getOption(VISUALIZER_OPTION_BG, "", new Color(220,235,255)));

        // Tool renderable
        add(getOption(VISUALIZER_OPTION_TOOL, "", new Color(237,255,0)));

        // GcodeModel renderable
        add(getOption(VISUALIZER_OPTION_LINEAR, "", new Color(0,0,158)));
        add(getOption(VISUALIZER_OPTION_RAPID, "", new Color(204,204,0)));
        add(getOption(VISUALIZER_OPTION_ARC, "", new Color(178,34,34)));
        add(getOption(VISUALIZER_OPTION_PLUNGE, "", new Color(0,100,0)));
        add(getOption(VISUALIZER_OPTION_COMPLETE, "", new Color(190,190,190)));

        // Highlight renderable
        add(getOption(VISUALIZER_OPTION_HIGHLIGHT, "", new Color(237,255,0)));

        // Grid renderable
        add(getOption(VISUALIZER_OPTION_XY_GRID, "", new Color(179,179,179, 29)));
        add(getOption(VISUALIZER_OPTION_XY_PLANE, "", new Color(77,77,77,29)));
        add(getOption(VISUALIZER_OPTION_X, "", new Color(230,0,0)));
        add(getOption(VISUALIZER_OPTION_Y, "", new Color(0,230,0)));
        add(getOption(VISUALIZER_OPTION_Z, "", new Color(0,0,230)));

        // SizeDisplay renderable
        add(getOption(VISUALIZER_OPTION_SIZE, "", new Color(128,128,128)));

        // Autoleveler surface mesh
        add(getOption(VISUALIZER_OPTION_HIGH, "", new Color(0, 255, 0, 128)));
        add(getOption(VISUALIZER_OPTION_LOW, "", new Color(255, 0, 0, 128)));

        // Machine boundries
        add(getOption(VISUALIZER_OPTION_BOUNDRY_BASE, "", new Color(167, 183, 206, 64)));
        add(getOption(VISUALIZER_OPTION_BOUNDRY_SIDES, "", new Color(119, 139, 168, 64)));
    }

    private Option<Color> getOption(String op, String desc, Color def) {
        return new Option<>(op, Localization.getString(op), desc, getColorOption(op, def));
    }

    public Option<Color> getOptionForKey(String key) {
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
