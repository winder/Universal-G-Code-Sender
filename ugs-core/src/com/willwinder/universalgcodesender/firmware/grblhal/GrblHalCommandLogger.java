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
import com.willwinder.universalgcodesender.firmware.FirmwareSetting;
import com.willwinder.universalgcodesender.firmware.grbl.commands.GrblSystemCommand;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.DefaultControllerListener;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.services.MessageService;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Collectors;

/**
 * Listens to completed commands and dispatches them as log messages to the console. Error and alarm
 * responses are described using the codes that the controller reported through $EE and $EA, and
 * settings are described using the metadata it reported through $ES.
 *
 * @author Joacim Breiler
 */
public class GrblHalCommandLogger extends DefaultControllerListener {
    private final GrblHalCodes codes;
    private final GrblHalFirmwareSettings firmwareSettings;
    private MessageService messageService;

    public GrblHalCommandLogger(MessageService messageService, GrblHalCodes codes, GrblHalFirmwareSettings firmwareSettings) {
        this.messageService = messageService;
        this.codes = codes;
        this.firmwareSettings = firmwareSettings;
    }

    @Override
    public void commandComplete(GcodeCommand command) {
        if (messageService == null) {
            return;
        }

        if (command instanceof GrblSystemCommand) {
            messageService.dispatchMessage(MessageType.VERBOSE, command.getResponse() + "\n");
            return;
        }

        if (command.isError()) {
            logErrorMessage(command);
        } else {
            logInfoMessage(command);
        }
    }

    @Override
    public void commandSent(GcodeCommand command) {
        if (messageService == null) {
            return;
        }

        MessageType messageType = command instanceof GrblSystemCommand ? MessageType.VERBOSE : MessageType.INFO;
        messageService.dispatchMessage(messageType, ">>> " + StringUtils.trimToEmpty(command.getCommandString()) + "\n");
    }

    private void logInfoMessage(GcodeCommand command) {
        if (command.getResponse() == null) {
            return;
        }

        String message = command.getResponse();
        if (command.getCommandString().equals(GrblUtils.GRBL_VIEW_SETTINGS_COMMAND)) {
            message = appendSettingNamesToResponse(command.getResponse());
        }

        messageService.dispatchMessage(MessageType.INFO, message + "\n");
    }

    private void logErrorMessage(GcodeCommand command) {
        // Don't log errors on jog commands
        if (command.getCommandString().startsWith("$J=")) {
            return;
        }

        String message = String.format(Localization.getString("controller.exception.sendError"),
                command.getCommandString(), codes.lookupCode(command.getResponse())).replaceAll("\\.\\.", ".");

        messageService.dispatchMessage(MessageType.ERROR, message + "\n");
    }

    private String appendSettingNamesToResponse(String response) {
        return response.lines()
                .map(this::appendSettingName)
                .collect(Collectors.joining("\n"));
    }

    private String appendSettingName(String line) {
        return firmwareSettings.convertMessageToSetting(line)
                .map(GrblHalCommandLogger::formatSetting)
                .orElse(line);
    }

    private static String formatSetting(FirmwareSetting setting) {
        String value = setting.getKey() + " = " + setting.getValue();
        if (StringUtils.isEmpty(setting.getShortDescription())) {
            return value;
        }

        String units = StringUtils.isEmpty(setting.getUnits()) ? "" : ", " + setting.getUnits();
        return value + "   (" + setting.getShortDescription() + units + ")";
    }

    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }
}
