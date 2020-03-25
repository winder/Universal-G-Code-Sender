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

    private static Preferences preferences = NbPreferences.forModule(JoystickService.class);

    protected Settings() {
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
}
