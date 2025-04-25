/*
    Copyright 2025 Will Winder

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
import static com.willwinder.universalgcodesender.GrblUtils.isGrblSettingMessage;
import static com.willwinder.universalgcodesender.GrblUtils.lookupCode;
import com.willwinder.universalgcodesender.firmware.grbl.commands.GrblSystemCommand;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.listeners.DefaultControllerListener;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.services.MessageService;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.types.GrblSettingMessage;
import org.apache.commons.lang3.StringUtils;

import java.util.stream.Collectors;

/**
 * A logger that will listen to completed commands and dispatches
 * them as log messages to the console.
 */
public class GrblCommandLogger extends DefaultControllerListener {

    private MessageService messageService;

    public GrblCommandLogger(MessageService messageService) {
        this.messageService = messageService;
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

        MessageType messageType = MessageType.INFO;
        if (command instanceof GrblSystemCommand) {
            messageType = MessageType.VERBOSE;
        }

        messageService.dispatchMessage(messageType, ">>> " + StringUtils.trimToEmpty(command.getCommandString()) + "\n");
    }

    private void logInfoMessage(GcodeCommand command) {
        if (command.getResponse() == null) {
            return;
        }

        String message = command.getResponse();
        if (command.getCommandString().equals(GrblUtils.GRBL_VIEW_SETTINGS_COMMAND)) {
            message = appendSettingDescriptionsToResponse(command.getResponse());
        }

        messageService.dispatchMessage(MessageType.INFO, message + "\n");
    }

    private static String appendSettingDescriptionsToResponse(String response) {
        return response
                .lines()
                .map(line -> {
                    if (isGrblSettingMessage(line)) {
                        return new GrblSettingMessage(line).toString();
                    }

                    return line;
                })
                .collect(Collectors.joining("\n"));
    }

    private void logErrorMessage(GcodeCommand command) {
        // Don't log errors on jog commands
        if (command.getCommandString().startsWith("$J=")) {
            return;
        }

        String message = String.format(Localization.getString("controller.exception.sendError"), command.getCommandString(),
                lookupCode(command.getResponse())).replaceAll("\\.\\.", ".");

        messageService.dispatchMessage(MessageType.ERROR, message + "\n");
    }

    public void setMessageService(MessageService messageService) {
        this.messageService = messageService;
    }
}
