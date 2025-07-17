package com.willwinder.ugs.nbp.designer.platform;
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

import com.willwinder.ugs.nbp.designer.model.Settings;
import org.openide.util.NbPreferences;

import java.util.prefs.Preferences;

/**
 * Saves/Loads the designer settings into the netbeans platform.
 */
public class SettingsAdapter {
    private static final Preferences preferences = NbPreferences.forModule(DesignerTopComponent.class);
    private static final String MAX_SPINDLE_SPEED = "maxSpindleSpeed";
    private static final String DETECT_MAX_SPINDLE_SPEED = "detectMaxSpindleSpeed";
    private static final String LASER_DIAMETER = "laserDiameter";
    private static final String FEED_SPEED = "feedSpeed";
    private static final String PLUNGE_SPEED = "plungeSpeed";
    private static final String TOOL_DIAMETER = "toolDiameter";
    private static final String SAFE_HEIGHT = "safeHeight";
    private static final String TOOL_STEP_OVER = "toolStepOver";
    private static final String DEPTH_PER_PASS = "depthPerPass";
    private static final String STOCK_THICKNESS = "stockThickness";
    private static final String SPINDLE_DIRECTION = "spindleDirection";
    private static final String FLATNESS_PRECISION = "flatnessPrecision";

    private SettingsAdapter() {}

    public static Settings loadSettings() {
        Settings settings = new Settings();
        settings.setMaxSpindleSpeed(preferences.getInt(MAX_SPINDLE_SPEED, 255));
        settings.setDetectMaxSpindleSpeed(preferences.getBoolean(DETECT_MAX_SPINDLE_SPEED, true));
        settings.setLaserDiameter(preferences.getDouble(LASER_DIAMETER, 0.2d));
        settings.setDepthPerPass(preferences.getDouble(DEPTH_PER_PASS, 1d));
        settings.setFeedSpeed(preferences.getInt(FEED_SPEED, 1000));
        settings.setPlungeSpeed(preferences.getInt(PLUNGE_SPEED, 400));
        settings.setToolDiameter(preferences.getDouble(TOOL_DIAMETER, 3d));
        settings.setSafeHeight(preferences.getDouble(SAFE_HEIGHT, 5d));
        settings.setToolStepOver(preferences.getDouble(TOOL_STEP_OVER, 0.3));
        settings.setStockThickness(preferences.getDouble(STOCK_THICKNESS, 10));
        settings.setSpindleDirection(preferences.get(SPINDLE_DIRECTION, "M3"));
        settings.setFlatnessPrecision(preferences.getDouble(FLATNESS_PRECISION, 0.1d));
        return settings;
    }

    public static void saveSettings(Settings settings) {
        preferences.putInt(MAX_SPINDLE_SPEED, settings.getMaxSpindleSpeed());
        preferences.putBoolean(DETECT_MAX_SPINDLE_SPEED, settings.getDetectMaxSpindleSpeed());
        preferences.putDouble(LASER_DIAMETER, settings.getLaserDiameter());
        preferences.putDouble(DEPTH_PER_PASS, settings.getDepthPerPass());
        preferences.putInt(FEED_SPEED, settings.getFeedSpeed());
        preferences.putInt(PLUNGE_SPEED, settings.getPlungeSpeed());
        preferences.putDouble(TOOL_DIAMETER, settings.getToolDiameter());
        preferences.putDouble(SAFE_HEIGHT, settings.getSafeHeight());
        preferences.putDouble(TOOL_STEP_OVER, settings.getToolStepOver());
        preferences.putDouble(STOCK_THICKNESS, settings.getStockThickness());
        preferences.put(SPINDLE_DIRECTION, settings.getSpindleDirection());
        preferences.putDouble(FLATNESS_PRECISION, settings.getFlatnessPrecision());
    }
}
