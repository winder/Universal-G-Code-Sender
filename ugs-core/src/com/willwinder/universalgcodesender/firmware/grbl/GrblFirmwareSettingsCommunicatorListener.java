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
package com.willwinder.universalgcodesender.firmware.grbl;

import com.willwinder.universalgcodesender.GrblUtils;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.firmware.FirmwareSetting;
import com.willwinder.universalgcodesender.firmware.FirmwareSettingsException;
import com.willwinder.universalgcodesender.firmware.IFirmwareSettingsListener;
import com.willwinder.universalgcodesender.listeners.CommunicatorListener;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.GrblLookups;
import com.willwinder.universalgcodesender.utils.ThreadHelper;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A serial communicator for handling settings on a GRBL controller.
 *
 * When updating a setting through {@link GrblFirmwareSettingsCommunicatorListener#updateSettingOnController(FirmwareSetting)}
 * the method will block until the update process is finished or if the operation took too long.
 *
 * @author Joacim Breiler
 */
public class GrblFirmwareSettingsCommunicatorListener implements CommunicatorListener {
    private static final Logger logger = Logger.getLogger(GrblFirmwareSettingsCommunicatorListener.class.getName());

    /**
     * Number of seconds to wait until the controller has persisted the setting
     */
    private static final int UPDATE_TIMEOUT_SECONDS = 2;

    /**
     * Parser for settings message from GRBL containing the key and value. Ex: $13=0
     * Starting in GRBL 1.1 the description is disabled by default.
     */
    private static final Pattern SETTING_MESSAGE_REGEX = Pattern.compile("\\$(\\d+)=([^ ]*)");

    /**
     * A lookup helper for fetching setting descriptions
     */
    private final GrblLookups grblLookups;

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

    /**
     * Constructor for creating a serial communicator
     *
     * @param controller the controller that is used for storing settings
     */
    public GrblFirmwareSettingsCommunicatorListener(IController controller) {
        this.grblLookups = new GrblLookups("setting_codes");
        this.controller = controller;
        this.listeners = Collections.synchronizedSet(new HashSet<>());
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

    /**
     * Converts a response message to a firmware setting. If the response message isn't in the format
     * {@code $[key]=[value]} an empty optional will be returned.
     *
     * @param response the response message from the controller
     * @return the converted firmware setting or an empty optional if the response was unknown.
     */
    private Optional<FirmwareSetting> convertMessageToSetting(String response) {
        Matcher settingMatcher = SETTING_MESSAGE_REGEX.matcher(response);
        if (!settingMatcher.find()) {
            return Optional.empty();
        }

        String key = "$" + settingMatcher.group(1);
        String value = settingMatcher.group(2);
        String units = "";
        String description = "";
        String shortDescription = "";

        String[] lookup = grblLookups.lookup(settingMatcher.group(1));
        if (lookup != null) {
            shortDescription = lookup[1];
            units = lookup[2];
            description = lookup[3];
        }

        return Optional.of(new FirmwareSetting(key, value, units, description, shortDescription));
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
                GcodeCommand command = controller.createCommand(setting.getKey() + "=" + setting.getValue());
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

    @Override
    public void rawResponseListener(String response) {

        // Make sure we either got a setting response or has sent an update command to the controller.
        if (!SETTING_MESSAGE_REGEX.matcher(response).find() && !isUpdatingSettings()) {
            return;
        }

        Optional<FirmwareSetting> setting = convertMessageToSetting(response);
        if (setting.isPresent()) {
            updatedSetting = setting.get();
            newSetting = null;
            listeners.forEach(listener -> listener.onUpdatedFirmwareSetting(updatedSetting));
        } else if (isUpdatingSettings() && GrblUtils.isErrorResponse(response)) {
            logger.log(Level.WARNING, "Couldn't update setting " + newSetting.getKey() + " with value " + newSetting.getValue());
            updatedSetting = null;
            newSetting = null;
            controller.cancelCommands();
        } else if (isUpdatingSettings() && GrblUtils.isOkResponse(response)) {
            logger.log(Level.INFO, "Updated setting " + newSetting.getKey() + " to " + newSetting.getValue());
            updatedSetting = newSetting;
            newSetting = null;
            listeners.forEach(listener -> listener.onUpdatedFirmwareSetting(updatedSetting));
        } else {
            logger.log(Level.WARNING, "Got unexpected message while waiting for setting update status: " + response);
        }
    }

    @Override
    public void commandSent(GcodeCommand command) {
        // We are about to receive all settings from the controller
        if (command.getCommandString().startsWith(GrblUtils.GRBL_VIEW_SETTINGS_COMMAND)) {
            newSetting = null;
            updatedSetting = null;
        }
    }

    @Override
    public void commandSkipped(GcodeCommand command) {

    }

    @Override
    public void communicatorPausedOnError() {

    }
}
