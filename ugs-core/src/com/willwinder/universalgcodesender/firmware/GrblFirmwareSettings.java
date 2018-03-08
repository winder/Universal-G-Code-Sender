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

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.listeners.SerialCommunicatorListener;
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
 * Handles the firmware settings on a GRBL controller. It needs to be registered as a listener
 * to {@link com.willwinder.universalgcodesender.AbstractCommunicator#setListenAll(SerialCommunicatorListener)}
 * for it to be able to process all commands to/from the controller.
 *
 * @author Joacim Breiler
 * @author MerrellM
 */
public class GrblFirmwareSettings implements SerialCommunicatorListener, IFirmwareSettingsListener, IFirmwareSettings {
    private static final Logger LOGGER = Logger.getLogger(GrblFirmwareSettings.class.getName());

    /**
     * Setting keys for GRBL
     */
    private static final String KEY_REPORTING_UNITS_IN_INCHES = "$13";
    private static final String KEY_HOMING_ENABLED = "$22";

    /**
     * A GRBL settings description lookups
     */
    private final Map<String, FirmwareSetting> settings = new ConcurrentHashMap<>();

    /**
     * A delegate for all serial communication handling
     */
    private final GrblFirmwareSettingsSerialCommunicator serialCommunicatorDelegate;

    public GrblFirmwareSettings(IController controller) {
        this.serialCommunicatorDelegate = new GrblFirmwareSettingsSerialCommunicator(controller);
        this.serialCommunicatorDelegate.addListener(this);
    }

    /**
     * Sets a property value on the controller. It will wait until the setting has been stored,
     * if this fails a {@link FirmwareSettingsException} will be thrown.
     *
     * @param key   the name of the setting to update
     * @param value the value of the setting
     * @return the value stored on the controller
     * @throws FirmwareSettingsException if the value couldn't be persisted on the controller.
     */
    @Override
    synchronized public FirmwareSetting setValue(final String key, final String value) throws FirmwareSettingsException {

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
    public Optional<FirmwareSetting> getSetting(String key) {
        return Optional.ofNullable(settings.get(key));
    }

    @Override
    public List<FirmwareSetting> getAllSettings() {
        return new ArrayList<>(settings.values());
    }

    @Override
    public boolean isHomingEnabled() {
        return getSetting(KEY_HOMING_ENABLED)
                .map(FirmwareSetting::getValue)
                .map("1"::equals)
                .orElse(false);
    }

    @Override
    public UnitUtils.Units getReportingUnits() {
        return getSetting(KEY_REPORTING_UNITS_IN_INCHES)
                .map(FirmwareSetting::getValue)
                .map(value -> {
                    if ("0".equals(value)) {
                        return UnitUtils.Units.MM;
                    } else if ("1".equals(value)) {
                        return UnitUtils.Units.INCH;
                    } else {
                        return UnitUtils.Units.UNKNOWN;
                    }
                })
                .orElse(UnitUtils.Units.UNKNOWN);
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
    public void messageForConsole(String msg) {
        serialCommunicatorDelegate.messageForConsole(msg);
    }

    @Override
    public void verboseMessageForConsole(String msg) {
        serialCommunicatorDelegate.verboseMessageForConsole(msg);
    }

    @Override
    public void errorMessageForConsole(String msg) {
        serialCommunicatorDelegate.errorMessageForConsole(msg);
    }

    /*
     * IFirmwareSettingsListener
     */
    @Override
    public void onUpdatedFirmwareSetting(FirmwareSetting setting) {
        LOGGER.log(Level.FINE, "Updating setting " + setting.getKey() + " = " + setting.getValue());
        settings.put(setting.getKey(), setting);
    }
}
