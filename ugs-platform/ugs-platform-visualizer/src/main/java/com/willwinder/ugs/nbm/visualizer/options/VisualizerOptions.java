/*
    Copyright 2016-2022 Will Winder

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

import com.willwinder.ugs.nbp.lib.options.Option;
import com.willwinder.universalgcodesender.i18n.Localization;
import org.openide.util.NbPreferences;

import java.awt.Color;
import java.util.ArrayList;

/**
 *
 * @author wwinder
 */
public class VisualizerOptions extends ArrayList<Option<?>> {
    // GcodeRenderer clear color
    public static String VISUALIZER_OPTION_BG = "platform.visualizer.color.background";

    // Tool renderable
    public static String VISUALIZER_OPTION_TOOL = "platform.visualizer.tool";
    public static String VISUALIZER_OPTION_TOOL_DESC = "platform.visualizer.tool.desc";
    public static String VISUALIZER_OPTION_TOOL_COLOR = "platform.visualizer.color.tool";

    // GcodeModel renderable
    public static String VISUALIZER_OPTION_MODEL = "platform.visualizer.model";
    public static String VISUALIZER_OPTION_MODEL_DESC = "platform.visualizer.model.desc";
    public static String VISUALIZER_OPTION_LINEAR = "platform.visualizer.color.linear";
    public static String VISUALIZER_OPTION_RAPID = "platform.visualizer.color.rapid";
    public static String VISUALIZER_OPTION_ARC = "platform.visualizer.color.arc";
    public static String VISUALIZER_OPTION_PLUNGE = "platform.visualizer.color.plunge";
    public static String VISUALIZER_OPTION_COMPLETE = "platform.visualizer.color.completed";

    // Highlight renderable
    public static String VISUALIZER_OPTION_HIGHLIGHT = "platform.visualizer.highlight";
    public static String VISUALIZER_OPTION_HIGHLIGHT_DESC = "platform.visualizer.highlight.desc";
    public static String VISUALIZER_OPTION_HIGHLIGHT_COLOR = "platform.visualizer.color.highlight";

    // Grid renderable
    public static String VISUALIZER_OPTION_GRID = "platform.visualizer.grid";
    public static String VISUALIZER_OPTION_GRID_DESC = "platform.visualizer.grid.desc";
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

    public static String VISUALIZER_OPTION_MOUSE_OVER = "platform.visualizer.mouseover";
    public static String VISUALIZER_OPTION_MOUSE_OVER_DESC = "platform.visualizer.mouseover.desc";

    public static String VISUALIZER_OPTION_ORIENTATION_CUBE = "platform.visualizer.orientation.cube";
    public static String VISUALIZER_OPTION_ORIENTATION_CUBE_DESC = "platform.visualizer.orientation.cube.desc";

    public static String VISUALIZER_OPTION_SELECTION = "platform.visualizer.selection";
    public static String VISUALIZER_OPTION_SELECTION_DESC = "platform.visualizer.selection.desc";

    public static String VISUALIZER_OPTION_SIZE_DISPLAY = "platform.visualizer.size.display";
    public static String VISUALIZER_OPTION_SIZE_DISPLAY_DESC = "platform.visualizer.size.display.desc";

    public static String VISUALIZER_OPTION_EDITOR_POSITION = "platform.visualizer.editor.position";
    public static String VISUALIZER_OPTION_EDITOR_POSITION_DESC = "platform.visualizer.editor.position.desc";

    public static String VISUALIZER_OPTION_AUTOLEVEL_PREVIEW = "platform.visualizer.autolevel.preview";
    public static String VISUALIZER_OPTION_AUTOLEVEL_PREVIEW_DESC = "platform.visualizer.autolevel.preview.desc";

    public static String VISUALIZER_OPTION_PROBE_PREVIEW = "platform.visualizer.probe.preview";
    public static String VISUALIZER_OPTION_PROBE_PREVIEW_DESC = "platform.visualizer.probe.preview.desc";

    public static String VISUALIZER_OPTION_DOWEL_PREVIEW = "platform.visualizer.dowel.preview";
    public static String VISUALIZER_OPTION_DOWEL_PREVIEW_DESC = "platform.visualizer.dowel.preview.desc";

    // Machine boundries
    public static final String VISUALIZER_OPTION_BOUNDRY = "platform.visualizer.boundary";
    public static final String VISUALIZER_OPTION_BOUNDRY_DESC = "platform.visualizer.boundary.desc";
    public static final String VISUALIZER_OPTION_BOUNDRY_BASE = "platform.visualizer.color.boundry-base";
    public static final String VISUALIZER_OPTION_BOUNDRY_SIDES = "platform.visualizer.color.boundry-sides";
    public static String VISUALIZER_OPTION_BOUNDARY_INVERT_X = "platform.visualizer.boundary.invert.x";
    public static String VISUALIZER_OPTION_BOUNDARY_INVERT_Y = "platform.visualizer.boundary.invert.y";
    public static String VISUALIZER_OPTION_BOUNDARY_INVERT_Z = "platform.visualizer.boundary.invert.z";
    public static String VISUALIZER_OPTION_BOUNDARY_INVERT_DESC = "platform.visualizer.boundary.invert.desc";

    public VisualizerOptions() {
        // GcodeRenderer clear color
        add(getOption(VISUALIZER_OPTION_BG, "", new Color(220,235,255)));

        // Tool renderable
        add(getOption(VISUALIZER_OPTION_TOOL, Localization.getString(VISUALIZER_OPTION_TOOL_DESC), true));
        add(getOption(VISUALIZER_OPTION_TOOL_COLOR, "", new Color(237,255,0)));

        // GcodeModel renderable
        add(getOption(VISUALIZER_OPTION_MODEL, Localization.getString(VISUALIZER_OPTION_MODEL_DESC), true));
        add(getOption(VISUALIZER_OPTION_LINEAR, "", new Color(0,0,158)));
        add(getOption(VISUALIZER_OPTION_RAPID, "", new Color(204,204,0)));
        add(getOption(VISUALIZER_OPTION_ARC, "", new Color(178,34,34)));
        add(getOption(VISUALIZER_OPTION_PLUNGE, "", new Color(0,100,0)));
        add(getOption(VISUALIZER_OPTION_COMPLETE, "", new Color(190,190,190)));

        // Highlight renderable
        add(getOption(VISUALIZER_OPTION_HIGHLIGHT, Localization.getString(VISUALIZER_OPTION_HIGHLIGHT_DESC), true));
        add(getOption(VISUALIZER_OPTION_HIGHLIGHT_COLOR, "", new Color(237,255,0)));

        // Grid renderable
        add(getOption(VISUALIZER_OPTION_GRID, Localization.getString(VISUALIZER_OPTION_GRID_DESC), true));
        add(getOption(VISUALIZER_OPTION_XY_GRID, "", new Color(179,179,179, 29)));
        add(getOption(VISUALIZER_OPTION_XY_PLANE, "", new Color(77,77,77,29)));
        add(getOption(VISUALIZER_OPTION_X, "", new Color(230,0,0)));
        add(getOption(VISUALIZER_OPTION_Y, "", new Color(0,230,0)));
        add(getOption(VISUALIZER_OPTION_Z, "", new Color(0,0,230)));

        // SizeDisplay renderable
        add(getOption(VISUALIZER_OPTION_SIZE_DISPLAY, Localization.getString(VISUALIZER_OPTION_SIZE_DISPLAY_DESC), true));
        add(getOption(VISUALIZER_OPTION_SIZE, "", new Color(128,128,128)));

        // Autoleveler surface mesh
        add(getOption(VISUALIZER_OPTION_AUTOLEVEL_PREVIEW, Localization.getString(VISUALIZER_OPTION_AUTOLEVEL_PREVIEW_DESC), true));
        add(getOption(VISUALIZER_OPTION_HIGH, "", new Color(0, 255, 0, 128)));
        add(getOption(VISUALIZER_OPTION_LOW, "", new Color(255, 0, 0, 128)));

        // Machine boundries
        add(getOption(VISUALIZER_OPTION_BOUNDRY, Localization.getString(VISUALIZER_OPTION_BOUNDRY_DESC), true));
        add(getOption(VISUALIZER_OPTION_BOUNDRY_BASE, "", new Color(167, 183, 206, 64)));
        add(getOption(VISUALIZER_OPTION_BOUNDRY_SIDES, "", new Color(119, 139, 168, 64)));
        add(getOption(VISUALIZER_OPTION_BOUNDARY_INVERT_X, Localization.getString(VISUALIZER_OPTION_BOUNDARY_INVERT_DESC), false));
        add(getOption(VISUALIZER_OPTION_BOUNDARY_INVERT_Y, Localization.getString(VISUALIZER_OPTION_BOUNDARY_INVERT_DESC), false));
        add(getOption(VISUALIZER_OPTION_BOUNDARY_INVERT_Z, Localization.getString(VISUALIZER_OPTION_BOUNDARY_INVERT_DESC), false));

        add(getOption(VISUALIZER_OPTION_MOUSE_OVER, Localization.getString(VISUALIZER_OPTION_MOUSE_OVER_DESC), true));
        add(getOption(VISUALIZER_OPTION_ORIENTATION_CUBE, Localization.getString(VISUALIZER_OPTION_ORIENTATION_CUBE_DESC), true));
        add(getOption(VISUALIZER_OPTION_SELECTION, Localization.getString(VISUALIZER_OPTION_SELECTION_DESC), true));
        add(getOption(VISUALIZER_OPTION_EDITOR_POSITION, Localization.getString(VISUALIZER_OPTION_EDITOR_POSITION_DESC), true));
        add(getOption(VISUALIZER_OPTION_PROBE_PREVIEW, Localization.getString(VISUALIZER_OPTION_PROBE_PREVIEW_DESC), true));
        add(getOption(VISUALIZER_OPTION_DOWEL_PREVIEW, Localization.getString(VISUALIZER_OPTION_DOWEL_PREVIEW_DESC), true));
    }

    private Option<Color> getOption(String op, String description, Color def) {
        return new Option<>(op, Localization.getString(op), description, getColorOption(op, def));
    }

    private Option<Boolean> getOption(String op, String desc, boolean defaultValue) {
        return new Option<>(op, Localization.getString(op), desc, Boolean.parseBoolean(getStringOption(op, Boolean.toString(defaultValue))));
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

    public static String getStringOption(String option, String defaultValue) {
        return NbPreferences.forModule(VisualizerOptions.class).get(option, defaultValue);
    }

    public static void setStringOption(String option, String value) {
        NbPreferences.forModule(VisualizerOptions.class).put(option, value);
    }

    public static boolean getBooleanOption(String option, boolean defaultValue) {
        return NbPreferences.forModule(VisualizerOptions.class).getBoolean(option, defaultValue);
    }

    public static void setBooleanOption(String option, boolean value) {
        NbPreferences.forModule(VisualizerOptions.class).putBoolean(option, value);
    }
}
