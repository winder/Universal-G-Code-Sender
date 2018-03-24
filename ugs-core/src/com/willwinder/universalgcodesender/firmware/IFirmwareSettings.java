/*
    Copyright 2018 Will Winder

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
package com.willwinder.universalgcodesender.firmware;

import com.willwinder.universalgcodesender.model.UnitUtils;

import java.util.List;
import java.util.Optional;

/**
 * An interface for fetching and setting the controller firmware settings.
 *
 * @author Joacim Breiler
 */
public interface IFirmwareSettings {
    /**
     * Returns the setting from the given key.
     *
     * @param key the name of the setting to fetch.
     * @return The setting if found otherwise an empty optional
     */
    Optional<FirmwareSetting> getSetting(String key);

    /**
     * Sets a settings value on the controller firmware. Notice that intensive usage of
     * this method may limit the lifespan on the controllers EEPROMs where the settings
     * are stored. So use with caution!
     *
     * @param key   the name of the setting to set or update
     * @param value the value of the setting
     * @return the current value. If the value got changed the new one will be returned,
     * otherwise the old one.
     * @throws FirmwareSettingsException exception will be thrown if key or value has
     *                                   an invalid format or if the setting command
     *                                   couldn't be sent to the controller
     */
    FirmwareSetting setValue(String key, String value) throws FirmwareSettingsException;

    /**
     * Adds a settings listener that will be notified when the settings have changed on
     * the controller.
     *
     * @param listener the listener add
     */
    void addListener(IFirmwareSettingsListener listener);

    /**
     * Removes a settings listener.
     *
     * @param listener the listener to remove.
     */
    void removeListener(IFirmwareSettingsListener listener);

    /**
     * Returns true if homing is enabled in the machine
     *
     * @return true if enabled
     */
    boolean isHomingEnabled();

    /**
     * Returns the units in which the controller reports it's coordinates
     *
     * @return the units for the controller
     */
    UnitUtils.Units getReportingUnits();

    /**
     * Returns all settings for the controller
     *
     * @return all settings as a list
     */
    List<FirmwareSetting> getAllSettings();
}
