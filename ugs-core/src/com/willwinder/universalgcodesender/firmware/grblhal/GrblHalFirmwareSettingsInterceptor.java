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

import com.willwinder.universalgcodesender.GrblUtils;
import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.listeners.DefaultControllerListener;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;

import static com.willwinder.universalgcodesender.firmware.grblhal.GrblHalFirmwareSettings.SETTING_MESSAGE_REGEX;

/**
 * Listens for setting values reported by the controller and stores them together with the setting
 * metadata fetched using $ES.
 *
 * @author Joacim Breiler
 */
public class GrblHalFirmwareSettingsInterceptor extends DefaultControllerListener {

    private final GrblHalFirmwareSettings firmwareSettings;
    private final IController controller;

    public GrblHalFirmwareSettingsInterceptor(IController controller, GrblHalFirmwareSettings firmwareSettings) {
        this.firmwareSettings = firmwareSettings;
        this.controller = controller;
        controller.addListener(this);
    }

    @Override
    public void commandComplete(GcodeCommand command) {
        if (!command.isOk()) {
            return;
        }

        if (SETTING_MESSAGE_REGEX.matcher(command.getCommandString()).find()) {
            firmwareSettings.convertMessageToSetting(command.getCommandString())
                    .ifPresent(firmwareSettings::updateFirmwareSetting);
        } else if (command.getCommandString().equals(GrblUtils.GRBL_VIEW_SETTINGS_COMMAND)) {
            Arrays.stream(StringUtils.split(StringUtils.defaultString(command.getResponse()), "\n"))
                    .map(firmwareSettings::convertMessageToSetting)
                    .flatMap(Optional::stream)
                    .forEach(firmwareSettings::updateFirmwareSetting);
        }
    }

    /**
     * Unregisters this as a listener to the controller
     */
    public void destroy() {
        controller.removeListener(this);
    }
}
