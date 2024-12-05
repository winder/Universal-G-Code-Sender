/*
    Copyright 2018-2024 Will Winder

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
import com.willwinder.universalgcodesender.listeners.DefaultControllerListener;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.GrblLookups;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A firmware settings interceptor that will update the settings that is sent or retrieved.
 *
 * @author Joacim Breiler
 */
public class GrblFirmwareSettingsInterceptor extends DefaultControllerListener {

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
    private final GrblFirmwareSettings firmwareSettings;
    private final IController controller;

    /**
     * Constructor for creating a serial communicator
     *
     * @param controller the controller that is used for storing settings
     */
    public GrblFirmwareSettingsInterceptor(IController controller, GrblFirmwareSettings firmwareSettings) {
        this.grblLookups = new GrblLookups("setting_codes");
        this.firmwareSettings = firmwareSettings;
        this.controller = controller;
        controller.addListener(this);
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
     * Unregisters this as a listener to the controller
     */
    public void destroy() {
        controller.removeListener(this);
    }

    @Override
    public void commandComplete(GcodeCommand command) {
        if (!command.isOk()) {
            return;
        }

        // If the command was a setting command
        if (SETTING_MESSAGE_REGEX.matcher(command.getCommandString()).find()) {
            convertMessageToSetting(command.getCommandString())
                    .ifPresent(firmwareSettings::updateFirmwareSetting);
        } else if (command.getCommandString().equals(GrblUtils.GRBL_VIEW_SETTINGS_COMMAND)) {
            Arrays.stream(command.getResponse().split("\n"))
                    .filter(line -> SETTING_MESSAGE_REGEX.matcher(line).find())
                    .map(this::convertMessageToSetting)
                    .filter(Optional::isPresent)
                    .map(Optional::get)
                    .forEach(firmwareSettings::updateFirmwareSetting);
        }
    }
}
