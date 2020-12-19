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
package com.willwinder.ugs.nbp.toolbox.settings;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.willwinder.ugs.nbp.toolbox.ToolboxTopComponent;
import org.openide.util.NbPreferences;

import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.prefs.Preferences;

/**
 * A settings object for the toolbox module
 *
 * @author Joacim breiler
 */
public class Settings {
    public static final String SETTINGS_VERSION = "version";

    private static Preferences preferences = NbPreferences.forModule(ToolboxTopComponent.class);

    private static Set<ISettingsListener> settingListeners = new HashSet<>();

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
     * Sets a list of action id for actions that should be available in the toolbox
     *
     * @return a list of actions
     */
    public static List<String> getActions() {
        String defaultValue = "[\"Actions/Machine/com-willwinder-ugs-nbp-core-actions-ResetCoordinatesToZeroAction.instance\",\"Actions/Machine/com-willwinder-ugs-nbp-core-actions-ReturnToZeroAction.instance\",\"Actions/Machine/com-willwinder-ugs-nbp-core-actions-SoftResetAction.instance\",\"Actions/Machine/com-willwinder-ugs-nbp-core-actions-HomeAction.instance\",\"Actions/Machine/com-willwinder-ugs-nbp-core-actions-UnlockAction.instance\",\"Actions/Machine/com-willwinder-ugs-nbp-core-actions-GetStateAction.instance\",\"Actions/Machine/com-willwinder-ugs-nbp-core-actions-CheckModeAction.instance\"]";
        String value = preferences.get("actionIdList", defaultValue);

        Gson gson = new Gson();
        Type listType = new TypeToken<List<String>>() {}.getType();
        return gson.fromJson(value, listType);
    }

    /**
     * Sets a list of action id for actions that should be available in the toolbox
     *
     * @param actionIdList a list of action ids
     */
    public static void setActions(List<String> actionIdList) {
        Gson gson = new Gson();
        String value = gson.toJson(actionIdList);
        preferences.put("actionIdList", value);
        settingListeners.forEach(ISettingsListener::settingsChanged);
    }

    /**
     * Adds a listener for listening to changed settings events
     *
     * @param settingsListener a settings listener to add
     */
    public static void addSettingsListener(ISettingsListener settingsListener) {
        settingListeners.add(settingsListener);
    }

    /**
     * Removes a listener for listening to changed settings events
     *
     * @param settingsListener a settings listener to remove
     */
    public static void removeSettingsListener(ISettingsListener settingsListener) {
        settingListeners.add(settingsListener);
    }
}
