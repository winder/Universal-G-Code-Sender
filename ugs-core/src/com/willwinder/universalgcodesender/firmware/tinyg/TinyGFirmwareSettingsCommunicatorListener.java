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

import com.google.gson.JsonObject;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.TinyGUtils;
import com.willwinder.universalgcodesender.firmware.FirmwareSetting;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettingsListener;
import com.willwinder.universalgcodesender.firmware.grbl.GrblFirmwareSettingsCommunicatorListener;
import com.willwinder.universalgcodesender.listeners.CommunicatorListener;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.types.TinyGGcodeCommand;
import com.willwinder.universalgcodesender.utils.ThreadHelper;

import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Handles the communication between the controller and the firmware settings engine.
 * It will try to parse all responses to see if it contains the result of a changed
 * setting. If it finds one it will notify all listeners.
 *
 * @author Joacim Breiler
 */
public class TinyGFirmwareSettingsCommunicatorListener implements CommunicatorListener {

    /**
     * Number of seconds to wait until the controller has persisted the setting
     */
    private static final int UPDATE_TIMEOUT_SECONDS = 2;

    /**
     * A connected controller
     */
    private final IController controller;

    /**
     * All listeners for listening to changed settings
     */
    private final Set<IFirmwareSettingsListener> listeners;

    /**
     * When updating a setting, the new setting is temporarily saved here
     */
    private FirmwareSetting newSetting;

    /**
     * When updating a setting, the old setting is temporarily saved here
     */
    private FirmwareSetting updatedSetting;

    public TinyGFirmwareSettingsCommunicatorListener(IController controller) {
        this.controller = controller;
        this.listeners = Collections.synchronizedSet(new HashSet<>());
    }

    /**
     * Adds a listener for new settings.
     *
     * @param listener a listener to add
     */
    public void addListener(IFirmwareSettingsListener listener) {
        listeners.add(listener);
    }

    /**
     * Removes a listener.
     *
     * @param listener a listener to remove
     */
    public void removeListener(IFirmwareSettingsListener listener) {
        listeners.remove(listener);
    }


    @Override
    public void rawResponseListener(String response) {
        try {
            JsonObject jsonObject = TinyGUtils.jsonToObject(response);
            if (TinyGGcodeCommand.isOkErrorResponse(response)) {
                JsonObject responseJson = jsonObject.get("r").getAsJsonObject();
                extractSettingGroupFromResponse(responseJson);
            }
        } catch (Exception ignored) {
            // Some TinyG responses aren't JSON, those will end up here.
        }
    }

    private void extractSettingGroupFromResponse(JsonObject responseJson) {
        for (TinyGSettingGroupType group : TinyGSettingGroupType.values()) {
            if (responseJson.has(group.getGroupName())) {
                JsonObject groupSettingsJson = responseJson.get(group.getGroupName()).getAsJsonObject();
                extractSettingFromResponse(group, groupSettingsJson);
            }
        }
    }

    private void extractSettingFromResponse(TinyGSettingGroupType group, JsonObject groupSettingsJson) {
        for (TinyGSettingType setting : group.getSettingTypes()) {
            if (groupSettingsJson.has(setting.getSettingName())) {
                String key = group.getGroupName() + setting.getSettingName();
                String value = groupSettingsJson.get(setting.getSettingName()).getAsString();
                String description = group.getDescription() + " - " + setting.getDescription();
                String shortDescription = group.getDescription() + " - " + setting.getShortDescription();

                updatedSetting = new FirmwareSetting(key,
                        value,
                        setting.getType(),
                        description,
                        shortDescription);

                newSetting = null;
                listeners.forEach(listener -> listener.onUpdatedFirmwareSetting(updatedSetting));
            }
        }
    }

    @Override
    public void commandSent(GcodeCommand command) {
        // Not used
    }

    @Override
    public void commandSkipped(GcodeCommand command) {
        // Not used
    }

    @Override
    public void communicatorPausedOnError() {
        // Not used
    }

    /**
     * Returns if the controller is ready to receive setting commands.
     *
     * @return true if controller is ready
     */
    private boolean canSendToController() {
        return controller != null && controller.isCommOpen() && !controller.isStreaming();
    }

    /**
     * Sends a command to update a setting on the controller. The method will block until we get a response
     * from the controller or if a timeout is triggered if the setting took too long to update.
     *
     * @param setting the setting to update
     * @return the updated setting or an empty optional if it couldn't be updated.
     * @throws FirmwareSettingsException will be thrown if the controller isn't ready to receive setting updates or if
     *                                   the update took to long and caused a timeout
     */
    public Optional<FirmwareSetting> updateSettingOnController(FirmwareSetting setting) throws FirmwareSettingsException {

        if (isUpdatingSettings()) {
            throw new FirmwareSettingsException("The settings are being updated in another thread.");
        }

        if (!canSendToController()) {
            throw new FirmwareSettingsException("The controller is not ready to receive commands.");
        }

        boolean previousSingleStepMode = controller.getSingleStepMode();
        boolean previousStatusUpdatesEnabled = controller.getStatusUpdatesEnabled();
        controller.setStatusUpdatesEnabled(false);
        controller.setSingleStepMode(true);

        try {
            try {
                updatedSetting = null;
                newSetting = setting;
                TinyGSettingGroupType groupType = TinyGSettingGroupType.fromSettingKey(setting.getKey()).orElseThrow(() -> new IllegalArgumentException("Unknown setting group for key " + setting.getKey()));
                TinyGSettingType settingType = TinyGSettingType.fromSettingKey(setting.getKey()).orElseThrow(() -> new IllegalArgumentException("Unknown setting type for key " + setting.getKey()));

                GcodeCommand command = controller.createCommand("{" + groupType.getGroupName() + ": {" + settingType.getSettingName() + ":" + setting.getValue() + "}}");
                controller.sendCommandImmediately(command);
            } catch (Exception e) {
                throw new FirmwareSettingsException("Couldn't send update setting command to the controller: " + setting.getKey() + "=" + setting.getValue() + ".", e);
            }

            waitUntilUpdateFinished();
        } finally {
            controller.setSingleStepMode(previousSingleStepMode);
            controller.setStatusUpdatesEnabled(previousStatusUpdatesEnabled);
        }

        // Reset internal states
        Optional<FirmwareSetting> result = Optional.ofNullable(updatedSetting);
        updatedSetting = null;
        newSetting = null;
        return result;
    }

    /**
     * Block and wait until the setting has been updated or until a timeout has occured.
     * The timeout will wait {@link GrblFirmwareSettingsCommunicatorListener#UPDATE_TIMEOUT_SECONDS}.
     *
     * @throws FirmwareSettingsException if a timeout has occured.
     */
    private void waitUntilUpdateFinished() throws FirmwareSettingsException {
        try {
            ThreadHelper.waitUntil(() -> !isUpdatingSettings(), UPDATE_TIMEOUT_SECONDS, TimeUnit.SECONDS);
        } catch (TimeoutException ignored) {
            newSetting = null;
            throw new FirmwareSettingsException("Timeout while updating the setting on the controller.");
        }
    }

    /**
     * Returns if the serial communicator is still sending the setting command
     * and is awaiting reply from the controller.
     *
     * @return true if a setting is updating
     */
    private boolean isUpdatingSettings() {
        return newSetting != null;
    }
}
