/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.fx.settings;

import com.willwinder.universalgcodesender.model.Unit;
import com.willwinder.universalgcodesender.model.UnitValue;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;

import java.util.prefs.Preferences;

public final class ProbeSettings {
    private static final Preferences prefs = Preferences.userNodeForPackage(ProbeSettings.class);

    private ProbeSettings() {
        zPlateThickness.addListener((obs, oldVal, newVal) -> putDouble(Z_PLATE_THICKNESS, newVal.convertTo(Unit.MM).doubleValue()));
    }

    private static ProbeSettings instance;
    public static final String Z_PLATE_THICKNESS = "probe.z.plateThickness";
    private static final String Z_DISTANCE = "probe.z.distance";
    private static final String PROBE_DIAMETER = "probe.settings.diameter";
    private static final String FAST_FIND_RATE = "probe.settings.fastFindRate";
    private static final String SLOW_FIND_RATE = "probe.settings.slowFindRate";
    private static final String RETRACT_DISTANCE = "probe.settings.retractDistance";
    private static final String DELAY_AFTER_RETRACT = "probe.settings.delayAfterRetract";
    private static final String COMPENSATE_SOFT_LIMITS = "probe.settings.compensateSoftLimits";
    private static final String SKIP_PROBE_CHECK = "probe.settings.skipProbeCheck";

    private final ObjectProperty<UnitValue> zPlateThickness = new SimpleObjectProperty<>(new UnitValue(Unit.MM, prefs.getDouble(Z_PLATE_THICKNESS, 20)));
    private final ObjectProperty<UnitValue> probeZDistance = new SimpleObjectProperty<>(new UnitValue(Unit.MM, prefs.getDouble(Z_DISTANCE, 50)));

    public static ProbeSettings getInstance() {
        if (instance == null) {
            instance = new ProbeSettings();
        }

        return instance;
    }

    public static void putDouble(String key, double value) {
        prefs.putDouble(key, value);
    }


    public static UnitValue getProbeDiameter() {
        return new UnitValue(Unit.MM, prefs.getDouble(PROBE_DIAMETER, 2));
    }

    public static void setProbeDiameter(UnitValue diameter) {
        prefs.putDouble(PROBE_DIAMETER, diameter.convertTo(Unit.MM).doubleValue());
    }

    public static UnitValue getFastFindRate() {
        return new UnitValue(Unit.MM_PER_MINUTE, prefs.getDouble(FAST_FIND_RATE, 100));
    }

    public static void setFastFindRate(UnitValue rate) {
        prefs.putDouble(FAST_FIND_RATE, rate.convertTo(Unit.MM_PER_MINUTE).doubleValue());
    }

    public static UnitValue getSlowFindRate() {
        return new UnitValue(Unit.MM_PER_MINUTE, prefs.getDouble(SLOW_FIND_RATE, 10));
    }

    public static void setSlowFindRate(UnitValue rate) {
        prefs.putDouble(SLOW_FIND_RATE, rate.convertTo(Unit.MM_PER_MINUTE).doubleValue());
    }

    public static UnitValue getRetractDistance() {
        return new UnitValue(Unit.MM, prefs.getDouble(RETRACT_DISTANCE, 5));
    }

    public static UnitValue getDelayAfterRetract() {
        return new UnitValue(Unit.SECONDS, prefs.getDouble(DELAY_AFTER_RETRACT, 1));
    }

    public static void setDelayAfterRetract(UnitValue delayAfterRetract) {
        prefs.putDouble(DELAY_AFTER_RETRACT, delayAfterRetract.convertTo(Unit.SECONDS).doubleValue());
    }

    public static void setRetractDistance(UnitValue retractDistance) {
        prefs.putDouble(RETRACT_DISTANCE, retractDistance.convertTo(Unit.MM).doubleValue());
    }

    public static void setCompensateSoftLimits(Boolean compensationEnabled) {
        prefs.putBoolean(COMPENSATE_SOFT_LIMITS, compensationEnabled);
    }

    public static boolean getCompensateSoftLimits() {
        return prefs.getBoolean(COMPENSATE_SOFT_LIMITS, true);
    }

    public static boolean getSkipProbeCheck() {
        return prefs.getBoolean(SKIP_PROBE_CHECK, false);
    }

    public static void setSkipProbeCheck(boolean skipProbeCheck) {
        prefs.putBoolean(SKIP_PROBE_CHECK, skipProbeCheck);
    }

    public ObjectProperty<UnitValue> zPlateThicknessProperty() {
        return zPlateThickness;
    }

    public ObjectProperty<UnitValue> probeZDistanceProperty() {
        return probeZDistance;
    }
}