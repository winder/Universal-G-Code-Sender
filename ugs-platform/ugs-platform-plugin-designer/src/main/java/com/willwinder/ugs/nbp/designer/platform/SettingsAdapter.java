package com.willwinder.ugs.nbp.designer.platform;
/*
    Copyright 2026 Will Winder

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

import com.willwinder.ugs.designer.model.CoolantMode;
import com.willwinder.ugs.designer.model.PenMode;
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
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
    private static final String TOOL_SHAPE = "toolShape";
    private static final String SAFE_HEIGHT = "safeHeight";
    private static final String TOOL_STEP_OVER = "toolStepOver";
    private static final String V_BIT_ANGLE = "vBitAngle";
    private static final String DEPTH_PER_PASS = "depthPerPass";
    private static final String STOCK_THICKNESS = "stockThickness";
    private static final String SPINDLE_DIRECTION = "spindleDirection";
    private static final String COOLANT_MODE = "coolantMode";
    private static final String FLATNESS_PRECISION = "flatnessPrecision";
    private static final String ARC_FITTING = "arcFitting";
    private static final String USE_TOOL_CHANGES = "useToolChanges";
    private static final String PEN_MODE = "penMode";
    private static final String PEN_WIDTH = "penWidth";
    private static final String PEN_DOWN_DEPTH = "penDownDepth";
    private static final String PEN_DOWN_SPINDLE_SPEED = "penDownSpindleSpeed";
    private static final String PEN_UP_SPINDLE_SPEED = "penUpSpindleSpeed";
    private static final String PEN_DOWN_COMMAND = "penDownCommand";
    private static final String PEN_UP_COMMAND = "penUpCommand";

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
        settings.setToolShape(readToolShape());
        settings.setSafeHeight(preferences.getDouble(SAFE_HEIGHT, 5d));
        settings.setToolStepOver(preferences.getDouble(TOOL_STEP_OVER, 0.3));
        settings.setVBitAngle(preferences.getDouble(V_BIT_ANGLE, 60d));
        settings.setStockThickness(preferences.getDouble(STOCK_THICKNESS, 10));
        settings.setSpindleDirection(preferences.get(SPINDLE_DIRECTION, "M3"));
        settings.setCoolantMode(readCoolantMode());
        settings.setFlatnessPrecision(preferences.getDouble(FLATNESS_PRECISION, 0.02d));
        settings.setArcFitting(preferences.getBoolean(ARC_FITTING, false));
        settings.setUseToolChanges(preferences.getBoolean(USE_TOOL_CHANGES, false));
        settings.setPenMode(readPenMode());
        settings.setPenWidth(preferences.getDouble(PEN_WIDTH, 0.5d));
        settings.setPenDownDepth(preferences.getDouble(PEN_DOWN_DEPTH, 0.5d));
        settings.setPenDownSpindleSpeed(preferences.getInt(PEN_DOWN_SPINDLE_SPEED, 1000));
        settings.setPenUpSpindleSpeed(preferences.getInt(PEN_UP_SPINDLE_SPEED, 0));
        settings.setPenDownCommand(preferences.get(PEN_DOWN_COMMAND, "M3"));
        settings.setPenUpCommand(preferences.get(PEN_UP_COMMAND, "M5"));
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
        preferences.put(TOOL_SHAPE, settings.getToolShape().name());
        preferences.putDouble(SAFE_HEIGHT, settings.getSafeHeight());
        preferences.putDouble(TOOL_STEP_OVER, settings.getToolStepOver());
        preferences.putDouble(V_BIT_ANGLE, settings.getVBitAngle());
        preferences.putDouble(STOCK_THICKNESS, settings.getStockThickness());
        preferences.put(SPINDLE_DIRECTION, settings.getSpindleDirection());
        preferences.put(COOLANT_MODE, settings.getCoolantMode().name());
        preferences.putDouble(FLATNESS_PRECISION, settings.getFlatnessPrecision());
        preferences.putBoolean(ARC_FITTING, settings.getArcFitting());
        preferences.putBoolean(USE_TOOL_CHANGES, settings.getUseToolChanges());
        preferences.put(PEN_MODE, settings.getPenMode().name());
        preferences.putDouble(PEN_WIDTH, settings.getPenWidth());
        preferences.putDouble(PEN_DOWN_DEPTH, settings.getPenDownDepth());
        preferences.putInt(PEN_DOWN_SPINDLE_SPEED, settings.getPenDownSpindleSpeed());
        preferences.putInt(PEN_UP_SPINDLE_SPEED, settings.getPenUpSpindleSpeed());
        preferences.put(PEN_DOWN_COMMAND, settings.getPenDownCommand());
        preferences.put(PEN_UP_COMMAND, settings.getPenUpCommand());
    }

    private static PenMode readPenMode() {
        String stored = preferences.get(PEN_MODE, PenMode.Z_AXIS.name());
        try {
            return PenMode.valueOf(stored);
        } catch (IllegalArgumentException e) {
            return PenMode.Z_AXIS;
        }
    }

    private static CoolantMode readCoolantMode() {
        String stored = preferences.get(COOLANT_MODE, CoolantMode.NONE.name());
        try {
            return CoolantMode.valueOf(stored);
        } catch (IllegalArgumentException e) {
            return CoolantMode.NONE;
        }
    }

    private static EndmillShape readToolShape() {
        String stored = preferences.get(TOOL_SHAPE, EndmillShape.UPCUT.name());
        try {
            return EndmillShape.valueOf(stored);
        } catch (IllegalArgumentException e) {
            return EndmillShape.UPCUT;
        }
    }
}
