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

import com.willwinder.universalgcodesender.model.Axis;
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
     * Returns if homing is enabled in the machine
     *
     * @return true if enabled
     */
    boolean isHomingEnabled() throws FirmwareSettingsException;

    /**
     * Enables or disables homing on the controller
     *
     * @param enabled true to enable
     */
    void setHomingEnabled(boolean enabled) throws FirmwareSettingsException;

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

    /**
     * Returns if hard limit switches are enabled in the controller
     *
     * @return true if enabled
     */
    boolean isHardLimitsEnabled() throws FirmwareSettingsException;

    /**
     * Enables or disables the limit switches in the controller
     *
     * @param enabled true to enable
     */
    void setHardLimitsEnabled(boolean enabled) throws FirmwareSettingsException;

    /**
     * Returns if soft limits are enabled in the controller
     *
     * @return true if enabled
     */
    boolean isSoftLimitsEnabled() throws FirmwareSettingsException;

    /**
     * Enables or disables the soft limits in the controller
     *
     * @param enabled true to enable
     */
    void setSoftLimitsEnabled(boolean enabled) throws FirmwareSettingsException;

    /**
     * Returns if the direction x is inverted on the controller
     *
     * @return true if inverted
     */
    boolean isInvertDirectionX();

    /**
     * Inverts the step direction for X-axis
     *
     * @param inverted if the direction should be inverted
     */
    void setInvertDirectionX(boolean inverted) throws FirmwareSettingsException;

    /**
     * Returns if the direction y is inverted on the controller
     *
     * @return true if inverted
     */
    boolean isInvertDirectionY();

    /**
     * Inverts the step direction for Y-axis
     *
     * @param inverted if the direction should be inverted
     */
    void setInvertDirectionY(boolean inverted) throws FirmwareSettingsException;

    /**
     * Returns if the direction z is inverted on the controller
     *
     * @return true if inverted
     */
    boolean isInvertDirectionZ();

    /**
     * Inverts the step direction for Z-axis
     *
     * @param inverted if the direction should be inverted
     */
    void setInvertDirectionZ(boolean inverted) throws FirmwareSettingsException;

    /**
     * Return the number of steps needed to move the machine one millimeter.
     *
     * @param axis the axis to retrieve the setting for
     * @return number of steps per mm
     * @throws FirmwareSettingsException if the settings couldn't be fetched
     */
    int getStepsPerMillimeter(Axis axis) throws FirmwareSettingsException;

    /**
     * Returns the soft limit for the X axis in millimeters
     *
     * @return the limit length in milimeters
     * @throws FirmwareSettingsException if the settings couldn't be fetched
     */
    double getSoftLimitX() throws FirmwareSettingsException;

    /**
     * Sets the soft limit for the X axis in millimeters.
     *
     * @param limit the limit in millimeters
     * @throws FirmwareSettingsException if the setting couldn't be saved
     */
    void setSoftLimitX(double limit) throws FirmwareSettingsException;

    /**
     * Returns the soft limit for the Y axis in millimeters
     *
     * @return the limit length in milimeters
     * @throws FirmwareSettingsException if the settings couldn't be fetched
     */
    double getSoftLimitY() throws FirmwareSettingsException;

    /**
     * Sets the soft limit for the Y axis in millimeters.
     *
     * @param limit the limit in millimeters
     * @throws FirmwareSettingsException if the setting couldn't be saved
     */
    void setSoftLimitY(double limit) throws FirmwareSettingsException;

    /**
     * Returns the soft limit for the Z axis in millimeters
     *
     * @return the limit length in milimeters
     * @throws FirmwareSettingsException if the settings couldn't be fetched
     */
    double getSoftLimitZ() throws FirmwareSettingsException;

    /**
     * Sets the soft limit for the X axis in millimeters.
     *
     * @param limit the limit in millimeters
     * @throws FirmwareSettingsException if the setting couldn't be saved
     */
    void setSoftLimitZ(double limit) throws FirmwareSettingsException;

    /**
     * Returns the soft limit for the given axis in millimeters
     *
     * @param axis the axis to retrieve
     * @return the soft limits in millimeters
     * @throws FirmwareSettingsException if the setting couldn't be retreived
     */
    double getSoftLimit(Axis axis) throws FirmwareSettingsException;

    /**
     * Returns if the homing direction for the X-axis be inverted
     *
     * @return true to perform homing to X-
     */
    boolean isHomingDirectionInvertedX();

    /**
     * Sets if the homing direction should be inverted for the X-axis
     *
     * @param inverted set to true if homing should be performed to X-
     */
    void setHomingDirectionInvertedX(boolean inverted) throws FirmwareSettingsException;

    /**
     * Returns if the homing direction for the Y-axis be inverted
     *
     * @return true to perform homing to Y-
     */
    boolean isHomingDirectionInvertedY();

    /**
     * Sets if the homing direction should be inverted for the Y-axis
     *
     * @param inverted set to true if homing should be performed to Y-
     */
    void setHomingDirectionInvertedY(boolean inverted) throws FirmwareSettingsException;

    /**
     * Returns if the homing direction for the Z-axis be inverted
     *
     * @return true to perform homing to Z-
     */
    boolean isHomingDirectionInvertedZ();

    /**
     * Sets if the homing direction should be inverted for the Z-axis
     *
     * @param inverted set to true if homing should be performed to Z-
     */
    void setHomingDirectionInvertedZ(boolean inverted) throws FirmwareSettingsException;


    /**
     * Sets if the limit pins should be inverted
     *
     * @param inverted set to true if limit pins should be inverted
     */
    void setHardLimitsInverted(boolean inverted) throws FirmwareSettingsException;

    /**
     * Returns if the limit pins are inverted
     *
     * @return true if the limit pins are inverted
     */
    boolean isHardLimitsInverted() throws FirmwareSettingsException;

    /**
     * Sets multiple firmware settings
     *
     * @param settings to update
     */
    void setSettings(List<FirmwareSetting> settings) throws FirmwareSettingsException;
}
