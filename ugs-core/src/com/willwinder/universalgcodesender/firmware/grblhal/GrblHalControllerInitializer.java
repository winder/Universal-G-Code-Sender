/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.firmware.grblhal;

import com.willwinder.universalgcodesender.ControllerException;
import com.willwinder.universalgcodesender.IControllerInitializer;
import com.willwinder.universalgcodesender.firmware.grbl.GrblBuildOptions;
import com.willwinder.universalgcodesender.firmware.grbl.GrblVersion;
import com.willwinder.universalgcodesender.firmware.grbl.commands.GetParserStateCommand;
import com.willwinder.universalgcodesender.firmware.grbl.commands.GetSettingsCommand;
import com.willwinder.universalgcodesender.firmware.grbl.commands.GetStatusCommand;
import com.willwinder.universalgcodesender.firmware.grblhal.commands.GetBuildInfoCommand;
import com.willwinder.universalgcodesender.firmware.grblhal.commands.AbstractGetCodesCommand;
import com.willwinder.universalgcodesender.firmware.grblhal.commands.GetAlarmCodesCommand;
import com.willwinder.universalgcodesender.firmware.grblhal.commands.GetErrorCodesCommand;
import com.willwinder.universalgcodesender.firmware.grblhal.commands.GetSettingDetailsCommand;
import com.willwinder.universalgcodesender.firmware.grblhal.commands.GetSettingGroupsCommand;
import com.willwinder.universalgcodesender.firmware.grblhal.commands.GrblHalSystemCommand;
import com.willwinder.universalgcodesender.listeners.ControllerState;
import com.willwinder.universalgcodesender.listeners.MessageType;
import static com.willwinder.universalgcodesender.utils.ControllerUtils.sendAndWaitForCompletion;
import static com.willwinder.universalgcodesender.utils.ControllerUtils.sendAndWaitForCompletionWithRetry;

import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Initializes a connection to a grblHAL controller without requiring the controller to be reset.
 * <p>
 * 1. Query the machine for a status report, if the machine is in a holding or alarm state a blank
 * line is sent to see if the controller still is responsive
 * 2. Fetch the build info to figure out the firmware version and its capabilities
 * 3. Fetch the metadata and groups for the settings and the error and alarm codes
 * 4. Fetch the settings and the parser state
 *
 * @author Joacim Breiler
 */
public class GrblHalControllerInitializer implements IControllerInitializer {
    private static final Logger LOGGER = Logger.getLogger(GrblHalControllerInitializer.class.getSimpleName());

    /**
     * The time to wait for the controller to boot before we start querying it
     */
    private static final Duration STARTUP_DELAY = Duration.ofSeconds(2);

    private static final int STATUS_QUERY_RETRIES = 10;
    private static final long STATUS_QUERY_TIMEOUT = 1000;

    /**
     * The settings enumeration is several kilobytes and needs a longer timeout than usual
     */
    private static final Duration SETTING_DETAILS_TIMEOUT = Duration.ofSeconds(10);

    /**
     * The code enumerations are several kilobytes and needs a longer timeout than usual
     */
    private static final Duration CODES_TIMEOUT = Duration.ofSeconds(10);

    private final AtomicBoolean isInitializing = new AtomicBoolean(false);
    private final AtomicBoolean isInitialized = new AtomicBoolean(false);
    private final GrblHalController controller;
    private final Duration startupDelay;

    private GrblVersion version = GrblVersion.NO_VERSION;
    private GrblBuildOptions buildOptions = new GrblBuildOptions();
    private GrblHalOptions extendedOptions = new GrblHalOptions();

    public GrblHalControllerInitializer(GrblHalController controller) {
        this(controller, STARTUP_DELAY);
    }

    GrblHalControllerInitializer(GrblHalController controller, Duration startupDelay) {
        this.controller = controller;
        this.startupDelay = startupDelay;
    }

    @Override
    public boolean initialize() {
        if (isInitializing.get() || isInitialized.get()) {
            return false;
        }

        controller.resetBuffers();
        controller.setControllerState(ControllerState.CONNECTING);
        isInitializing.set(true);
        try {
            Thread.sleep(startupDelay.toMillis());
            if (!isControllerResponsive()) {
                isInitializing.set(false);
                controller.getMessageService().dispatchMessage(MessageType.INFO, "*** Device is in a holding or alarm state and needs to be reset\n");
                controller.issueSoftReset();
                return false;
            }

            Thread.sleep(startupDelay.toMillis());
            fetchBuildInfo();
            fetchSettingDetails();
            fetchSettingGroups();
            fetchCodes();
            fetchControllerState();

            controller.getMessageService().dispatchMessage(MessageType.INFO, String.format("*** Connected to %s\n", getVersionString()));
            isInitialized.set(true);
            isInitializing.set(false);
            return true;
        } catch (Exception e) {
            isInitialized.set(false);
            isInitializing.set(false);
            closeConnection();
            throw new ControllerException(e.getMessage());
        }
    }

    private boolean isControllerResponsive() throws Exception {
        GetStatusCommand statusCommand = queryForStatusReport();
        if (!statusCommand.isDone() || statusCommand.isError()) {
            controller.closeCommPort();
            throw new IllegalStateException("Could not query the device status");
        }

        ControllerState state = statusCommand.getControllerStatus().getState();

        // Some commands are not available in check mode
        if (state == ControllerState.CHECK) {
            return false;
        }

        if (state == ControllerState.SLEEP || state == ControllerState.DOOR || state == ControllerState.HOLD || state == ControllerState.ALARM) {
            try {
                // Figure out if it is still responsive even if it is in a holding or alarm state
                sendAndWaitForCompletion(controller, new GrblHalSystemCommand(""));
            } catch (Exception e) {
                return false;
            }
        }

        return true;
    }

    private GetStatusCommand queryForStatusReport() throws InterruptedException {
        return sendAndWaitForCompletionWithRetry(GetStatusCommand::new, controller, STATUS_QUERY_TIMEOUT, STATUS_QUERY_RETRIES, executionNumber -> {
            if (executionNumber == 1) {
                controller.getMessageService().dispatchMessage(MessageType.INFO, "*** Fetching device status\n");
            } else {
                controller.getMessageService().dispatchMessage(MessageType.INFO, "*** Fetching device status (" + executionNumber + " of " + STATUS_QUERY_RETRIES + ")...\n");
            }
        });
    }

    private void fetchBuildInfo() throws InterruptedException {
        controller.getMessageService().dispatchMessage(MessageType.INFO, "*** Fetching device version\n");
        GetBuildInfoCommand buildInfoCommand = sendAndWaitForCompletion(controller, new GetBuildInfoCommand());

        Optional<GrblVersion> optionalVersion = buildInfoCommand.getVersion();
        if (optionalVersion.isEmpty()) {
            controller.getMessageService().dispatchMessage(MessageType.ERROR, "*** Could not detect the grblHAL version\n");
            throw new ControllerException("Could not detect the grblHAL version");
        }

        version = optionalVersion.get();
        buildOptions = buildInfoCommand.getBuildOptions();
        extendedOptions = buildInfoCommand.getExtendedOptions();

        // Older builds of grblHAL do not report the firmware name, assume that the user knows what
        // they are connecting to and only warn about it.
        if (!buildInfoCommand.isGrblHal()) {
            controller.getMessageService().dispatchMessage(MessageType.INFO, "*** The controller did not identify itself as grblHAL\n");
        }

        buildInfoCommand.getPlugins().forEach(plugin ->
                controller.getMessageService().dispatchMessage(MessageType.VERBOSE, "*** Loaded plugin " + plugin + "\n"));
    }

    /**
     * Enumerates every setting supported by the controller. Not all builds of grblHAL support this,
     * if it fails the settings will be listed without their names.
     */
    private void fetchSettingDetails() {
        try {
            controller.getMessageService().dispatchMessage(MessageType.INFO, "*** Fetching setting details\n");
            GetSettingDetailsCommand command = sendAndWaitForCompletion(controller, new GetSettingDetailsCommand(), SETTING_DETAILS_TIMEOUT);
            if (command.isError()) {
                controller.getMessageService().dispatchMessage(MessageType.INFO, "*** The controller could not enumerate its settings\n");
                return;
            }

            controller.getFirmwareSettings().updateSettingDetails(command.getSettingDetails());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Timed out fetching the setting details", e);
        }
    }

    /**
     * Enumerates the setting groups so that each setting can be associated with a group name. Not
     * all builds of grblHAL support this, if it fails the settings will have no group.
     */
    private void fetchSettingGroups() {
        try {
            GetSettingGroupsCommand command = sendAndWaitForCompletion(controller, new GetSettingGroupsCommand(), SETTING_DETAILS_TIMEOUT);
            controller.getFirmwareSettings().updateSettingGroups(command.isError() ? List.of() : command.getSettingGroups());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Timed out fetching the setting groups", e);
        }
    }

    /**
     * Enumerates the error and alarm codes supported by the controller so that its own descriptions
     * can be used when reporting them. Codes are cleared if the controller can't enumerate them.
     */
    private void fetchCodes() {
        controller.getMessageService().dispatchMessage(MessageType.INFO, "*** Fetching error and alarm codes\n");
        GrblHalCodes codes = controller.getCodes();
        codes.updateErrorCodes(fetchCodes(new GetErrorCodesCommand()));
        codes.updateAlarmCodes(fetchCodes(new GetAlarmCodesCommand()));
    }

    private List<GrblHalCode> fetchCodes(AbstractGetCodesCommand command) {
        try {
            AbstractGetCodesCommand result = sendAndWaitForCompletion(controller, command, CODES_TIMEOUT);
            if (result.isError()) {
                return List.of();
            }

            return result.getCodes();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            LOGGER.log(Level.WARNING, "Timed out fetching the codes using " + command.getCommandString(), e);
            return List.of();
        }
    }

    private void fetchControllerState() throws InterruptedException {
        controller.getMessageService().dispatchMessage(MessageType.INFO, "*** Fetching device settings\n");
        sendAndWaitForCompletion(controller, new GetSettingsCommand());
        controller.getMessageService().dispatchMessage(MessageType.INFO, "*** Fetching device state\n");
        sendAndWaitForCompletion(controller, new GetParserStateCommand());
    }

    private void closeConnection() {
        try {
            controller.closeCommPort();
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "Could not properly close the connection", e);
        }
    }

    @Override
    public void reset() {
        isInitializing.set(false);
        isInitialized.set(false);
        version = GrblVersion.NO_VERSION;
        buildOptions = new GrblBuildOptions();
        extendedOptions = new GrblHalOptions();
    }

    @Override
    public boolean isInitialized() {
        return isInitialized.get();
    }

    public boolean isInitializing() {
        return isInitializing.get();
    }

    public GrblVersion getVersion() {
        return version;
    }

    public GrblBuildOptions getBuildOptions() {
        return buildOptions;
    }

    public GrblHalOptions getExtendedOptions() {
        return extendedOptions;
    }

    /**
     * Returns a version string for the connected controller, ie {@code grblHAL 1.1f}
     *
     * @return the version as a human readable string
     */
    public String getVersionString() {
        if (version.getVersionNumber() <= 0) {
            return GrblHalUtils.FIRMWARE_NAME;
        }

        String versionLetter = version.getVersionLetter() == '-' ? "" : String.valueOf(version.getVersionLetter());
        return GrblHalUtils.FIRMWARE_NAME + " " + version.getVersionNumber() + versionLetter;
    }
}
