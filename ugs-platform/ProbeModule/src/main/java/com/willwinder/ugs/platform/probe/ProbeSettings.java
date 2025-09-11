/*
    Copyright 2023 Will Winder

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
package com.willwinder.ugs.platform.probe;

import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.WorkCoordinateSystem;
import org.openide.util.NbPreferences;

import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;

public class ProbeSettings {
    private static final Preferences preferences = NbPreferences.forModule(ProbeTopComponent.class);
    public static final String XYZ_X_DISTANCE = "xyzXDistance";
    public static final String XYZ_Y_DISTANCE = "xyzYDistance";
    public static final String XYZ_Z_DISTANCE = "xyzZDistance";
    public static final String XYZ_X_OFFSET = "xyzXOffset";
    public static final String XYZ_Y_OFFSET = "xyzYOffset";
    public static final String XYZ_Z_OFFSET = "xyzZOffset";
    public static final String OUTSIDE_X_DISTANCE = "outsideXDistance";
    public static final String OUTSIDE_Y_DISTANCE = "outsideYDistance";
    public static final String OUTSIDE_X_OFFSET = "outsideXOffset";
    public static final String OUTSIDE_Y_OFFSET = "outsideYOffset";
    public static final String HC_DIAMETER = "hcDiameter";
    public static final String Z_DISTANCE = "zDistance";
    public static final String Z_OFFSET = "zOffset";
    public static final String SETTINGS_PROBE_DIAMETER = "settingsProbeDiameter";
    public static final String SETTINGS_FAST_FIND_RATE = "settingsFastFindRate";
    public static final String SETTINGS_SLOW_MEASURE_RATE = "settingsSlowMeasureRate";
    public static final String SETTINGS_RETRACT_AMOUNT = "settingsRetractAmount";
    public static final String SETTINGS_DELAY_AFTER_RETRACT = "settingsDelayAfterRetract";
    public static final String SETTINGS_WORK_COORDINATE_SYSTEM = "settingsWorkCoordinateSystem";
    public static final String SETTINGS_UNITS = "settingsUnits";
    public static final String SELECTED_TAB_INDEX = "selectedTabIndex";
    public static final String SETTINGS_COMPENSATE_FOR_SOFT_LIMITS = "settingsCompensateForSoftLimits";

    private ProbeSettings() {
    }

    public static void addPreferenceChangeListener(PreferenceChangeListener pcl) {
        preferences.addPreferenceChangeListener(pcl);
    }

    public static void removePreferenceChangeListener(PreferenceChangeListener pcl) {
        preferences.removePreferenceChangeListener(pcl);
    }

    public static double getXyzXDistance() {
        return preferences.getDouble(XYZ_X_DISTANCE, 10);
    }

    public static void setXyzXDistance(double xyzXDistance) {
        preferences.putDouble(XYZ_X_DISTANCE, xyzXDistance);
    }

    public static double getXyzYDistance() {
        return preferences.getDouble(XYZ_Y_DISTANCE, 10);
    }

    public static void setXyzYDistance(double xyzYDistance) {
        preferences.putDouble(XYZ_Y_DISTANCE, xyzYDistance);
    }

    public static double getXyzZDistance() {
        return preferences.getDouble(XYZ_Z_DISTANCE, -10);
    }

    public static void setXyzZDistance(double xyzZDistance) {
        preferences.putDouble(XYZ_Z_DISTANCE, xyzZDistance);
    }

    public static double getXyzXOffset() {
        return preferences.getDouble(XYZ_X_OFFSET, 0);
    }

    public static void setXyzXOffset(double xyzXOffset) {
        preferences.putDouble(XYZ_X_OFFSET, xyzXOffset);
    }

    public static double getXyzYOffset() {
        return preferences.getDouble(XYZ_Y_OFFSET, 0);
    }

    public static void setXyzYOffset(double xyzYOffset) {
        preferences.putDouble(XYZ_Y_OFFSET, xyzYOffset);
    }

    public static double getXyzZOffset() {
        return preferences.getDouble(XYZ_Z_OFFSET, 0);
    }

    public static void setXyzZOffset(double xyzZOffset) {
        preferences.putDouble(XYZ_Z_OFFSET, xyzZOffset);
    }

    public static double getOutsideXDistance() {
        return preferences.getDouble(OUTSIDE_X_DISTANCE, 10);
    }

    public static void setOutsideXDistance(double outsideXDistance) {
        preferences.putDouble(OUTSIDE_X_DISTANCE, outsideXDistance);
    }

    public static double getOutsideYDistance() {
        return preferences.getDouble(OUTSIDE_Y_DISTANCE, 10);
    }

    public static void setOutsideYDistance(double outsideYDistance) {
        preferences.putDouble(OUTSIDE_Y_DISTANCE, outsideYDistance);

    }

    public static double getOutsideXOffset() {
        return preferences.getDouble(OUTSIDE_X_OFFSET, 0);
    }

    public static void setOutsideXOffset(double outsideXOffset) {
        preferences.putDouble(OUTSIDE_X_OFFSET, outsideXOffset);

    }

    public static double getOutsideYOffset() {
        return preferences.getDouble(OUTSIDE_Y_OFFSET, 0);
    }

    public static void setOutsideYOffset(double outsideYOffset) {
        preferences.putDouble(OUTSIDE_Y_OFFSET, outsideYOffset);
    }

    public static double getHcDiameter() {
        return preferences.getDouble(HC_DIAMETER, 10);
    }

    public static void setHcDiameter(double hcDiameter) {
        preferences.putDouble(HC_DIAMETER, hcDiameter);
    }

    public static double getzDistance() {
        return preferences.getDouble(Z_DISTANCE, -10);
    }

    public static void setzDistance(double zDistance) {
        preferences.putDouble(Z_DISTANCE, zDistance);
    }

    public static double getzOffset() {
        return preferences.getDouble(Z_OFFSET, 0);
    }

    public static void setzOffset(double zOffset) {
        preferences.putDouble(Z_OFFSET, zOffset);
    }

    public static double getSettingsProbeDiameter() {
        return preferences.getDouble(SETTINGS_PROBE_DIAMETER, 10);
    }

    public static void setSettingsProbeDiameter(double settingsProbeDiameter) {
        preferences.putDouble(SETTINGS_PROBE_DIAMETER, settingsProbeDiameter);
    }

    public static double getSettingsFastFindRate() {
        return preferences.getDouble(SETTINGS_FAST_FIND_RATE, 100);
    }

    public static void setSettingsFastFindRate(double settingsFastFindRate) {
        preferences.putDouble(SETTINGS_FAST_FIND_RATE, settingsFastFindRate);
    }

    public static double getSettingsSlowMeasureRate() {
        return preferences.getDouble(SETTINGS_SLOW_MEASURE_RATE, 10);
    }

    public static void setSettingsSlowMeasureRate(double settingsSlowMeasureRate) {
        preferences.putDouble(SETTINGS_SLOW_MEASURE_RATE, settingsSlowMeasureRate);
    }

    public static double getSettingsRetractAmount() {
        return preferences.getDouble(SETTINGS_RETRACT_AMOUNT, 1);
    }

    public static void setSettingsRetractAmount(double settingsRetractAmount) {
        preferences.putDouble(SETTINGS_RETRACT_AMOUNT, settingsRetractAmount);
    }

    public static double getSettingsDelayAfterRetract() {
        return preferences.getDouble(SETTINGS_DELAY_AFTER_RETRACT, 1);
    }

    public static void setSettingsDelayAfterRetract(double settingsDelayAfterRetract) {
        preferences.putDouble(SETTINGS_DELAY_AFTER_RETRACT, settingsDelayAfterRetract);
    }

    public static WorkCoordinateSystem getSettingsWorkCoordinate() {
        String wcs = preferences.get(SETTINGS_WORK_COORDINATE_SYSTEM, WorkCoordinateSystem.G54.name());
        for (WorkCoordinateSystem s : WorkCoordinateSystem.values()) {
            if (s.name().equalsIgnoreCase(wcs)) {
                return s;
            }
        }
        return WorkCoordinateSystem.G54;
    }

    public static void setSettingsWorkCoordinate(WorkCoordinateSystem settingsWorkCoordinate) {
        preferences.put(SETTINGS_WORK_COORDINATE_SYSTEM, settingsWorkCoordinate.name());
    }

    public static UnitUtils.Units getSettingsUnits() {
        String unitsAsString = preferences.get(SETTINGS_UNITS, UnitUtils.Units.MM.name());

        try {
            return UnitUtils.Units.valueOf(unitsAsString);
        } catch (IllegalArgumentException e) {
            return UnitUtils.Units.MM;
        }
    }

    public static void setSettingsUnits(UnitUtils.Units settingsUnits) {
        UnitUtils.Units previousUnits = getSettingsUnits();
        if (previousUnits != settingsUnits) {
            double scale = UnitUtils.scaleUnits(previousUnits, settingsUnits);
            setzDistance(getzDistance() * scale);
            setzOffset(getzOffset() * scale);
            setHcDiameter(getHcDiameter() * scale);
            setXyzXDistance(getXyzXDistance() * scale);
            setXyzYDistance(getXyzYDistance() * scale);
            setXyzZDistance(getXyzZDistance() * scale);
            setXyzXOffset(getXyzXOffset() * scale);
            setXyzYOffset(getXyzYOffset() * scale);
            setXyzZOffset(getXyzZOffset() * scale);
            setOutsideXOffset(getOutsideXOffset() * scale);
            setOutsideYOffset(getOutsideYOffset() * scale);
            setOutsideXDistance(getOutsideXDistance() * scale);
            setOutsideYDistance(getOutsideYDistance() * scale);
            setSettingsProbeDiameter(getSettingsProbeDiameter() * scale);
            setSettingsRetractAmount(getSettingsRetractAmount() * scale);
            setSettingsFastFindRate(getSettingsFastFindRate() * scale);
            setSettingsSlowMeasureRate(getSettingsSlowMeasureRate() * scale);

            // Finally sync parameters to UI
            preferences.put(SETTINGS_UNITS, settingsUnits.name());
        }
    }

    public static int getSelectedTabIdx() {
        return preferences.getInt(SELECTED_TAB_INDEX, 0);
    }

    public static void setSelectedTabIdx(int selectedTabIdx) {
        preferences.putInt(SELECTED_TAB_INDEX, selectedTabIdx);
    }

    public static boolean getCompensateForSoftLimits() {
        return Boolean.parseBoolean(preferences.get(SETTINGS_COMPENSATE_FOR_SOFT_LIMITS, Boolean.TRUE.toString()));
    }

    public static void setCompensateForSoftLimits(boolean compensateForSoftLimits) {
        preferences.put(SETTINGS_COMPENSATE_FOR_SOFT_LIMITS, Boolean.toString(compensateForSoftLimits));
    }
}
