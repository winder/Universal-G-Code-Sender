/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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
        add(getOption("visualizer.color.background", "Background Color", "", new Color(220,235,255)));
        add(getOption("visualizer.color.tool", "Tool Color", "", new Color(237,255,0)));
        add(getOption("visualizer.color.linear", "Linear Movement Color (G1)", "", new Color(0,0,158)));
        add(getOption("visualizer.color.rapid", "Rapid Movement Color (G0)", "", new Color(204,204,0)));
        add(getOption("visualizer.color.arc", "Arc Movement Color (G2/G3)", "", new Color(178,34,34)));
        add(getOption("visualizer.color.plunge", "Plunge/Raise Movement Color", "", new Color(0,100,0)));
        add(getOption("visualizer.color.highlight", "Editor Line Highlight Color", "", new Color(237,255,0)));
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

    public static Color getColorOption(String option, Color defaultColor) {
        int pref = NbPreferences.forModule(VisualizerOptions.class).getInt(option, defaultColor.getRGB());
        return new Color(pref, true);
    }

    public static void setColorOption(String option, Color color) {
        NbPreferences.forModule(VisualizerOptions.class).putInt(option, color.getRGB());
    }
}
