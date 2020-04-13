/*
    Copyright 2020 Will Winder

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
package com.willwinder.ugs.nbp.joystick;

import com.willwinder.ugs.nbp.joystick.model.JoystickControl;
import com.willwinder.ugs.nbp.joystick.service.JoystickService;
import org.openide.util.NbPreferences;

import java.util.prefs.Preferences;

/**
 * A settings object for the joystick module
 *
 * @author Joacim breiler
 */
public class Settings {
    public static final String SETTINGS_ACTIVE = "active";
    public static final String SETTINGS_VERSION = "version";

    private static Preferences preferences = NbPreferences.forModule(JoystickService.class);

    protected Settings() {
    }

    /**
     * Gets the current version of the settings. If 0 the settings hasn't been initialized yet.
     *
     * @return the version number of the settings
     */
    public static int getVersion() {
        return preferences.getInt(SETTINGS_VERSION, 0);
    }

    /**
     * Sets the current settings version
     *
     * @param version the version of the settings
     */
    public static void setVersion(int version) {
        preferences.putInt(SETTINGS_VERSION, version);
    }

    /**
     * Returns if the joystick module should be active and loaded on startup
     *
     * @return true if the plugin should run on startup
     */
    public static boolean isActive() {
        return preferences.getBoolean(SETTINGS_ACTIVE, false);
    }

    /**
     * Sets if the joystick service should be started
     *
     * @param active set to true to active the joystick service
     */
    public static void setActive(boolean active) {
        preferences.putBoolean(SETTINGS_ACTIVE, active);
    }

    /**
     * Sets the action id of the action to be mapped with the given control.
     * The action id can be fetched from the @{link ActionManager}.
     * To unbind an action set an empty string or null.
     *
     * @param joystickControl the joystick control to map with action
     * @param actionId        the action id to bind with joystick control.
     */
    public static void setActionMapping(JoystickControl joystickControl, String actionId) {
        preferences.put(joystickControl.name(), actionId);
    }

    /**
     * Returns the action id of the action to be mapped with the given control.
     * The action id can be fetched from the @{link ActionManager}
     *
     * @param joystickControl the joystick control to map with action
     * @return the action id to bind with joystick control. If no action was bound an empty string will be returned.
     */
    public static String getActionMapping(JoystickControl joystickControl) {
        return preferences.get(joystickControl.name(), "");
    }

    /**
     * Sets if the axis values should be inverted
     *
     * @param joystickControl the axis we wish change settings for
     * @param reversed if the axis should be inverted
     */
    public static void setReverseAxis(JoystickControl joystickControl, boolean reversed) {
        preferences.putBoolean(joystickControl.name() + "_reverse", reversed);
    }

    /**
     * Returns if the given axis control should be inverted
     *
     * @param joystickControl the axis we want to get settings for
     * @return if the axis should be inverted
     */
    public static boolean isReverseAxis(JoystickControl joystickControl) {
        return preferences.getBoolean(joystickControl.name() + "_reverse", false);
    }

    /**
     * Returns the axis threshold value that is required before we activate any action. This is needed as
     * some controllers doesn't return a zero value for a analog controllers.
     *
     * @return a float value between 0.0 and 1.0
     */
    public static float getAxisThreshold() {
        return preferences.getFloat("axisThreshold", 0.03f);
    }

    /**
     * Sets the axis threshold value that is considered as zero.
     *
     * @param threshold the threshold value between 0.0 and 1.0
     */
    public static void setAxisThreshold(float threshold) {
        preferences.putFloat("axisThreshold", threshold);
    }
}
