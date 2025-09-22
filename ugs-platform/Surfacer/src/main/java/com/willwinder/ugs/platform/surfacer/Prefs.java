/*
 * Copyright (C) 2025 dimic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.platform.surfacer;

import java.util.prefs.Preferences;
import org.openide.util.NbPreferences;

/**
 *
 * @author dimic
 */
public class Prefs {

    public static final Preferences PREFS = NbPreferences.forModule(SurfacerTopComponent.class);
    
    // -------------------------------------------------------------------------    
    // In the following key values, '~' means convert units (like mm to in),
    //  '*' means value is a unit rate
    public static final String KEY_PATTERN = "pattern";
    public static final String KEY_ANGLE = "angle";
    public static final String KEY_CLIMB_CUT = "climbCut";
    public static final String KEY_X0 = "x0~";
    public static final String KEY_Y0 = "y0~";
    public static final String KEY_X1 = "x1~";
    public static final String KEY_Y1 = "y1~";

    public static final String KEY_Z_SAFE = "zSafe~";
    public static final String KEY_Z_START = "zStart~";
    public static final String KEY_DEPTH = "depth~";
    public static final String KEY_CUT_STEP = "cutStep~";
    public static final String KEY_PLUNGE_RATE = "plungeRate*~";

    public static final String KEY_FINISH_CUT = "finishCut~";
    public static final String KEY_FINISH_COUNT = "finishCount";
    public static final String KEY_FINISH_FEEDRATE = "finishFeedrate*~";

    public static final String KEY_TOOL_DIAMETER = "toolDiameter~";
    public static final String KEY_OVERLAP = "overlap";
    public static final String KEY_SPINDLE_SPEED = "spindleSpeed";
    public static final String KEY_XY_FEEDRATE = "xyFeedrate*~";
    // -------------------------------------------------------------------------

    Double getDouble(String key) { return PREFS.getDouble(key, 0); }
    Integer getInt(String key) { return PREFS.getInt(key, 0); }
    Boolean getBoolean(String key) { return PREFS.getBoolean(key, true); }

    void putDouble(String key, double value) { PREFS.putDouble(key, value); }
    void putInt(String key, int value) { PREFS.putInt(key, value); }
    void putBoolean(String key, boolean value) { PREFS.putBoolean(key, value); }

    Boolean isRateKey(String key) { return key.contains("*"); }
    Boolean isConvertKey(String key) { return key.contains("~"); }
    
    int pattern()           { return getInt(Prefs.KEY_PATTERN); }
    double angle()          { return getDouble(Prefs.KEY_ANGLE); }
    boolean climbCut()      { return getBoolean(Prefs.KEY_CLIMB_CUT); }
    double x0()             { return getDouble(Prefs.KEY_X0); }
    double y0()             { return getDouble(Prefs.KEY_Y0); }
    double x1()             { return getDouble(Prefs.KEY_X1); }
    double y1()             { return getDouble(Prefs.KEY_Y1); }
    
    double zSafe()          { return getDouble(Prefs.KEY_Z_SAFE); }
    double zStart()         { return getDouble(Prefs.KEY_Z_START); }
    double depth()          { return getDouble(Prefs.KEY_DEPTH); }
    double cutStep()        { return getDouble(Prefs.KEY_CUT_STEP); }
    double plungeRate()     { return getDouble(Prefs.KEY_PLUNGE_RATE); }

    double finishCut()      { return getDouble(Prefs.KEY_FINISH_CUT); }
    int finishCount()       { return getInt(Prefs.KEY_FINISH_COUNT); }
    double finishFeedrate() { return getDouble(Prefs.KEY_FINISH_FEEDRATE); }
    
    double toolDiameter()   { return getDouble(Prefs.KEY_TOOL_DIAMETER); }
    double overlap()        { return getDouble(Prefs.KEY_OVERLAP); }
    double spindleSpeed()   { return getDouble(Prefs.KEY_SPINDLE_SPEED); }
    double xyFeedrate()     { return getDouble(Prefs.KEY_XY_FEEDRATE); }
   
}
