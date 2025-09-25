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

    public static final int PATTERNS_RASTER = 0;
    public static final int PATTERNS_SPIRAL = 1;

    public static final int    DEFAULT_PATTERN = PATTERNS_RASTER;
    public static final double DEFAULT_ANGLE = 0;
    public static final boolean DEFAULT_CLIMB_CUT = true;
    
    public static final double DEFAULT_X0 = 0;
    public static final double DEFAULT_X1 = 10;
    public static final double DEFAULT_Y0 = 0;
    public static final double DEFAULT_Y1 = 10;
       
    public static final double DEFAULT_Z_SAFE = 1;
    public static final double DEFAULT_Z_START = 0;
    public static final double DEFAULT_DEPTH = -0.20;
    public static final double DEFAULT_CUT_STEP = -0.20;
    public static final double DEFAULT_PLUNGE_RATE = 25;
    public static final double DEFAULT_XY_FEEDRATE = 400;

    public static final double DEFAULT_FINISH_CUT = -0.05;
    public static final int    DEFAULT_FINISH_COUNT = 0;
    public static final double DEFAULT_FINISH_FEEDRATE = 800;
    
    public static final double DEFAULT_TOOL_DIAMETER = 3.175;
    public static final double DEFAULT_OVERLAP = 0.40;
    public static final double DEFAULT_SPINDLE_SPEED = 10000;
        
    void putDouble(String key, double value) { PREFS.putDouble(key, value); }
    void putInt(String key, int value) { PREFS.putInt(key, value); }
    void putBoolean(String key, boolean value) { PREFS.putBoolean(key, value); }

    Boolean isRateKey(String key) { return key.contains("*"); }
    Boolean isConvertKey(String key) { return key.contains("~"); }
    
    int     pattern()       { return PREFS.getInt(KEY_PATTERN, DEFAULT_PATTERN); }
    double  angle()         { return PREFS.getDouble(KEY_ANGLE, DEFAULT_ANGLE); }
    boolean climbCut()      { return PREFS.getBoolean(KEY_CLIMB_CUT, DEFAULT_CLIMB_CUT); }
    double  x0()            { return PREFS.getDouble(KEY_X0, DEFAULT_X0); }
    double  y0()            { return PREFS.getDouble(KEY_Y0, DEFAULT_Y0); }
    double  x1()            { return PREFS.getDouble(KEY_X1, DEFAULT_X1); }
    double  y1()            { return PREFS.getDouble(KEY_Y1, DEFAULT_Y1); }
    
    double zSafe()          { return PREFS.getDouble(Prefs.KEY_Z_SAFE, DEFAULT_Z_SAFE); }
    double zStart()         { return PREFS.getDouble(Prefs.KEY_Z_START, DEFAULT_Z_START); }
    double depth()          { return PREFS.getDouble(Prefs.KEY_DEPTH, DEFAULT_DEPTH); }
    double cutStep()        { return PREFS.getDouble(Prefs.KEY_CUT_STEP, DEFAULT_CUT_STEP); }
    double plungeRate()     { return PREFS.getDouble(Prefs.KEY_PLUNGE_RATE, DEFAULT_PLUNGE_RATE); }

    double finishCut()      { return PREFS.getDouble(Prefs.KEY_FINISH_CUT, DEFAULT_FINISH_CUT); }
    int    finishCount()    { return PREFS.getInt(Prefs.KEY_FINISH_COUNT, DEFAULT_FINISH_COUNT); }
    double finishFeedrate() { return PREFS.getDouble(Prefs.KEY_FINISH_FEEDRATE, DEFAULT_FINISH_FEEDRATE); }
    
    double toolDiameter()   { return PREFS.getDouble(Prefs.KEY_TOOL_DIAMETER, DEFAULT_TOOL_DIAMETER); }
    double overlap()        { return PREFS.getDouble(Prefs.KEY_OVERLAP, DEFAULT_OVERLAP); }
    double spindleSpeed()   { return PREFS.getDouble(Prefs.KEY_SPINDLE_SPEED, DEFAULT_SPINDLE_SPEED); }
    double xyFeedrate()     { return PREFS.getDouble(Prefs.KEY_XY_FEEDRATE, DEFAULT_XY_FEEDRATE); }
   
    public void updateAllPrefs() {
        PREFS.putInt(KEY_PATTERN, pattern());
        PREFS.putDouble(KEY_ANGLE, angle());
        PREFS.putBoolean(KEY_CLIMB_CUT, climbCut());
        PREFS.putDouble(KEY_X0, x0());
        PREFS.putDouble(KEY_Y0, y0());
        PREFS.putDouble(KEY_X1, x1());
        PREFS.putDouble(KEY_Y1, y1());
       
        PREFS.putDouble(KEY_Z_SAFE, zSafe());
        PREFS.putDouble(KEY_Z_START, zStart());
        PREFS.putDouble(KEY_DEPTH, depth());
        PREFS.putDouble(KEY_CUT_STEP, cutStep());
        PREFS.putDouble(KEY_PLUNGE_RATE, plungeRate());

        PREFS.putDouble(KEY_FINISH_CUT, finishCut());
        PREFS.putInt(KEY_FINISH_COUNT, finishCount());
        PREFS.putDouble(KEY_FINISH_FEEDRATE, finishFeedrate());
        
        PREFS.putDouble(KEY_TOOL_DIAMETER, toolDiameter());
        PREFS.putDouble(KEY_OVERLAP, overlap());
        PREFS.putDouble(KEY_SPINDLE_SPEED, spindleSpeed());
        PREFS.putDouble(KEY_XY_FEEDRATE, xyFeedrate());

        PREFS.putDouble(KEY_SPINDLE_SPEED, spindleSpeed());
    }
    
}
