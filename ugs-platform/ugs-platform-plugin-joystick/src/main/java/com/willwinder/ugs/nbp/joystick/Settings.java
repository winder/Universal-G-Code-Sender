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
    public static final String SETTINGS_CUSTOM_MAPPING = "customMapping";

    public static final String DEFAULT_CUSTOM_MAPPING = "# Custom UGS controllers\n" +
            "03000000412300003780000000000000,Arduino Micro,a:b0,b:b1,x:b2,y:b3,back:b4,guide:b5,start:b6,leftstick:b7,rightstick:b8,leftshoulder:b9,rightshoulder:b10,dpup:h0.1,dpdown:h0.4,dpleft:h0.8,dpright:h0.2,-leftx:a0,+leftx:a1,-lefty:a2,+lefty:a3,-rightx:a4,+rightx:a5,-righty:a6,-righty:a7,lefttrigger:b11,righttrigger:b12,platform:Windows,\n" +
            "03000000412300003e00000000000000,Arduino Due,platform:Windows,a:b10,b:b8,x:b9,y:b11,guide:b12,leftshoulder:b0,rightshoulder:b1,dpup:-a1,dpdown:+a1,dpleft:-a0,dpright:+a0,\n" +
            "03000000072000004512000000000000,JOYSTICK FZ,platform:Windows,guide:b0,leftstick:b4,rightstick:b1,leftshoulder:b2,rightshoulder:b3,dpup:h0.1,dpdown:h0.4,dpleft:h0.8,dpright:h0.2,leftx:a0,lefty:a1,rightx:a5,righty:a4~,\n" +
            "78696e70757401000000000000000000,XInput Controller,platform:Windows,a:b0,b:b1,x:b2,y:b3,back:b6,start:b7,guide:b10,leftshoulder:b4,rightshoulder:b5,leftstick:b8,rightstick:b9,leftx:a0,lefty:a1,rightx:a3,righty:a4,lefttrigger:a2,righttrigger:a5,dpup:h0.1,dpleft:h0.8,dpdown:h0.4,dpright:h0.2,\n" +
            "03000000786901006e70000000000000,XInput Controller,platform:Windows,a:b0,b:b1,back:b6,dpdown:h0.4,dpleft:h0.8,dpright:h0.2,dpup:h0.1,guide:b10,leftshoulder:b4,leftstick:b8,lefttrigger:a2,leftx:a0,lefty:a1,rightshoulder:b5,rightstick:b9,righttrigger:a5,rightx:a3,righty:a4,start:b7,x:b2,y:b3,\n" +
            "030000006d04000015c2000000000000,Logitech Extreme 3D Pro M/N: J-UK17 P/N: 863225-1000,a:b11,b:b10,x:b8,y:b9,back:b6,guide:b7,start:b5,leftstick:b2,rightstick:-a1,leftshoulder:b4,rightshoulder:b0,dpup:h0.1,dpdown:h0.4,dpleft:h0.8,dpright:h0.2,leftx:a0,lefty:a2,platform:Windows,\n" +
            "03000000790000000600000000000000,G-Shark GS-GP702,a:b2,b:b1,back:b8,dpdown:h0.4,dpleft:h0.8,dpright:h0.2,dpup:h0.1,leftshoulder:b4,leftstick:b10,lefttrigger:b6,leftx:a0,lefty:a1,rightshoulder:b5,rightstick:b11,righttrigger:b7,rightx:a2,righty:a4,start:b9,x:b3,y:b0,platform:Windows,\n";

    private static final Preferences preferences = NbPreferences.forModule(JoystickService.class);

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
     * @param reversed        if the axis should be inverted
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

    /**
     * Gets the custom mapping in the gamecontrollerdb-format (https://github.com/gabomdq/SDL_GameControllerDB)
     *
     * @return the custom mapping
     */
    public static String getCustomMapping() {
        return preferences.get(SETTINGS_CUSTOM_MAPPING, DEFAULT_CUSTOM_MAPPING);
    }

    /**
     * Sets the custom mapping in the gamecontrollerdb-format (https://github.com/gabomdq/SDL_GameControllerDB)
     *
     * @param customMapping the custom mapping
     */
    public static void setCustomMapping(String customMapping) {
        preferences.put(SETTINGS_CUSTOM_MAPPING, customMapping);
    }
}
