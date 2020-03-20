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
package com.willwinder.universalgcodesender.firmware.tinyg;

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.firmware.FirmwareSetting;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettings;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettingsListener;
import com.willwinder.universalgcodesender.listeners.CommunicatorListener;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.types.GcodeCommand;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Handles configurations for TinyG
 *
 * @author Joacim Breiler
 */
public class TinyGFirmwareSettings implements CommunicatorListener, IFirmwareSettings, IFirmwareSettingsListener {
    private static final Logger LOGGER = Logger.getLogger(TinyGFirmwareSettings.class.getName());

    private final Map<String, FirmwareSetting> settings = new ConcurrentHashMap<>();

    private final TinyGFirmwareSettingsCommunicatorListener serialCommunicatorDelegate;

    public TinyGFirmwareSettings(IController controller) {
        this.serialCommunicatorDelegate = new TinyGFirmwareSettingsCommunicatorListener(controller);
        this.serialCommunicatorDelegate.addListener(this);
    }

    @Override
    public Optional<FirmwareSetting> getSetting(String key) {
        return Optional.ofNullable(settings.get(key));
    }

    @Override
    public FirmwareSetting setValue(String key, String value) throws FirmwareSettingsException {
        final FirmwareSetting oldSetting = getSetting(key)
                .orElseThrow(() -> new FirmwareSettingsException("Couldn't find setting with key " + key + " to update."));

        // The setting already contains the value so we do not update
        if (oldSetting.getValue().equals(value)) {
            return oldSetting;
        }

        // Make a copy of existing property and send it to our controller
        final FirmwareSetting newSetting = new FirmwareSetting(oldSetting.getKey(), value, oldSetting.getUnits(), oldSetting.getDescription(), oldSetting.getShortDescription());
        return serialCommunicatorDelegate
                .updateSettingOnController(newSetting)
                .orElse(oldSetting);
    }

    @Override
    public void addListener(IFirmwareSettingsListener listener) {
        serialCommunicatorDelegate.addListener(listener);
    }

    @Override
    public void removeListener(IFirmwareSettingsListener listener) {
        serialCommunicatorDelegate.removeListener(listener);
    }

    @Override
    public boolean isHomingEnabled() throws FirmwareSettingsException {
        return true;
    }

    @Override
    public void setHomingEnabled(boolean enabled) throws FirmwareSettingsException {
        // TODO add setting
    }

    @Override
    public UnitUtils.Units getReportingUnits() {
        return null;
    }

    @Override
    public List<FirmwareSetting> getAllSettings() {
        return new ArrayList<>(settings.values());
    }

    @Override
    public boolean isHardLimitsEnabled() throws FirmwareSettingsException {
        return false;
    }

    @Override
    public void setHardLimitsEnabled(boolean enabled) throws FirmwareSettingsException {
        // TODO add setting
    }

    @Override
    public boolean isSoftLimitsEnabled() throws FirmwareSettingsException {
        return false;
    }

    @Override
    public void setSoftLimitsEnabled(boolean enabled) throws FirmwareSettingsException {
        // TODO add setting
    }

    @Override
    public boolean isInvertDirection(Axis axis) throws FirmwareSettingsException {
        return false;
    }

    @Override
    public void setInvertDirection(Axis axis, boolean inverted) throws FirmwareSettingsException {

    }

    @Override
    public void setStepsPerMillimeter(Axis axis, double stepsPerMillimeter) throws FirmwareSettingsException {

    }

    @Override
    public double getStepsPerMillimeter(Axis axis) throws FirmwareSettingsException {
        return 0;
    }

    @Override
    public void setSoftLimit(Axis axis, double limit) throws FirmwareSettingsException {

    }

    @Override
    public double getSoftLimit(Axis axis) throws FirmwareSettingsException {
        return 0;
    }

    @Override
    public boolean isHomingDirectionInverted(Axis axis) {
        return false;
    }

    @Override
    public void setHomingDirectionInverted(Axis axis, boolean inverted) {
        // TODO add setting
    }

    @Override
    public boolean isHardLimitsInverted() throws FirmwareSettingsException {
        return false;
    }

    @Override
    public void setHardLimitsInverted(boolean inverted) throws FirmwareSettingsException {
        // TODO add setting
    }

    @Override
    public void setSettings(List<FirmwareSetting> settings) throws FirmwareSettingsException {
        settings.forEach(setting -> {
            try {
                setValue(setting.getKey(), setting.getValue());
            } catch (FirmwareSettingsException e) {
                LOGGER.warning("Couldn't set the firmware setting " + setting.getKey() + " to value " + setting.getValue() + ". Error message: " + e.getMessage());
            }
        });
    }

    @Override
    public double getMaximumRate(Axis axis) throws FirmwareSettingsException {
        return 0;
    }

    /*
     * IFirmwareSettingsListener
     */
    @Override
    public void onUpdatedFirmwareSetting(FirmwareSetting setting) {
        LOGGER.log(Level.FINE, "Updating setting " + setting.getKey() + " = " + setting.getValue());
        settings.put(setting.getKey(), setting);
    }

    /*
     * SerialCommunicatorListener
     */
    @Override
    public void rawResponseListener(String response) {
        serialCommunicatorDelegate.rawResponseListener(response);
    }

    @Override
    public void commandSent(GcodeCommand command) {
        serialCommunicatorDelegate.commandSent(command);
    }

    @Override
    public void commandSkipped(GcodeCommand command) {
        serialCommunicatorDelegate.commandSkipped(command);
    }

    @Override
    public void communicatorPausedOnError() {
        serialCommunicatorDelegate.communicatorPausedOnError();
    }

}
