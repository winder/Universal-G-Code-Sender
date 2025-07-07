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

import com.willwinder.universalgcodesender.firmware.grbl.commands.GrblCommand;
import com.willwinder.universalgcodesender.firmware.grbl.commands.GrblSystemCommand;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.services.MessageService;
import org.junit.Before;
import org.junit.Test;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;

public class GrblCommandLoggerTest {

    @Mock
    private MessageService messageService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void commandCompleteWithNoResponseShouldNotOutputLogAnything() {
        GrblCommandLogger logger = new GrblCommandLogger(messageService);

        GrblCommand command = new GrblCommand("test");
        logger.commandComplete(command);

        Mockito.verifyNoInteractions(messageService);
    }

    @Test
    public void commandCompleteShouldOutputItsResponse() {
        GrblCommandLogger logger = new GrblCommandLogger(messageService);

        GrblCommand command = new GrblCommand("test");
        command.appendResponse("response");
        logger.commandComplete(command);

        Mockito.verify(messageService, Mockito.times(1)).dispatchMessage(eq(MessageType.INFO), eq("response\n"));
    }

    @Test
    public void commandCompleteShouldOutputErrorResponse() {
        GrblCommandLogger logger = new GrblCommandLogger(messageService);

        GrblCommand command = new GrblCommand("test");
        command.appendResponse("response");
        command.setError(true);
        logger.commandComplete(command);

        Mockito.verify(messageService, Mockito.times(1)).dispatchMessage(eq(MessageType.ERROR), eq("An error was detected while sending 'test': response. Streaming has been paused.\n"));
    }

    @Test
    public void commandCompleteShouldNotOutputErrorResponseOnJogCommands() {
        GrblCommandLogger logger = new GrblCommandLogger(messageService);

        GrblCommand command = new GrblCommand("$J=jog command");
        command.appendResponse("response");
        command.setError(true);
        logger.commandComplete(command);

        Mockito.verifyNoInteractions(messageService);
    }

    @Test
    public void commandCompleteOnSettingsCommandShouldAppendGrblSettingsDescription() {
        GrblCommandLogger logger = new GrblCommandLogger(messageService);

        GrblCommand command = new GrblCommand("$$");
        command.appendResponse("$100=100");
        logger.commandComplete(command);

        Mockito.verify(messageService, Mockito.times(1)).dispatchMessage(eq(MessageType.INFO), eq("$100 = 100    (X-axis travel resolution, step/mm)\n"));
    }

    @Test
    public void commandCompleteOnSettingsCommandWithUnknownSettingShouldJustReturnResponse() {
        GrblCommandLogger logger = new GrblCommandLogger(messageService);

        GrblCommand command = new GrblCommand("$$");
        command.appendResponse("$10000=100");
        logger.commandComplete(command);

        Mockito.verify(messageService, Mockito.times(1)).dispatchMessage(eq(MessageType.INFO), eq("$10000 = 100\n"));
    }

    @Test
    public void commandCompleteOnSettingsCommandWithOtherTextResponse() {
        GrblCommandLogger logger = new GrblCommandLogger(messageService);

        GrblCommand command = new GrblCommand("$$");
        command.appendResponse("something");
        logger.commandComplete(command);

        Mockito.verify(messageService, Mockito.times(1)).dispatchMessage(eq(MessageType.INFO), eq("something\n"));
    }

    @Test
    public void commandCompleteOnSystemCommandShouldLogVerboseMessageResponse() {
        GrblCommandLogger logger = new GrblCommandLogger(messageService);

        GrblCommand command = new GrblSystemCommand("$$");
        command.appendResponse("something");
        logger.commandComplete(command);

        Mockito.verify(messageService, Mockito.times(1)).dispatchMessage(eq(MessageType.VERBOSE), eq("something\n"));
    }

    @Test
    public void commandCompleteWhenNoMessageServiceShouldNotThrowException() {
        GrblCommandLogger logger = new GrblCommandLogger(null);

        GrblCommand command = new GrblSystemCommand("$$");
        command.appendResponse("something");
        logger.commandComplete(command);
    }

    @Test
    public void commandSentShouldLogCommand() {
        GrblCommandLogger logger = new GrblCommandLogger(messageService);

        GrblCommand command = new GrblCommand("test");
        logger.commandSent(command);

        Mockito.verify(messageService, Mockito.times(1)).dispatchMessage(eq(MessageType.INFO), eq(">>> test\n"));
    }

    @Test
    public void commandSentWithSystemCommandShouldLogCommandWithVerbose() {
        GrblCommandLogger logger = new GrblCommandLogger(messageService);

        GrblCommand command = new GrblSystemCommand("test");
        logger.commandSent(command);

        Mockito.verify(messageService, Mockito.times(1)).dispatchMessage(eq(MessageType.VERBOSE), eq(">>> test\n"));
    }

    @Test
    public void commandSentWithoutMessageServiceShouldNotThrowException() {
        GrblCommandLogger logger = new GrblCommandLogger(null);

        GrblCommand command = new GrblSystemCommand("test");
        logger.commandSent(command);
    }

    @Test
    public void setMessageServiceShouldLogThroughNewService() {
        GrblCommandLogger logger = new GrblCommandLogger(null);
        logger.setMessageService(messageService);

        GrblCommand command = new GrblCommand("test");
        logger.commandSent(command);

        Mockito.verify(messageService, Mockito.times(1)).dispatchMessage(eq(MessageType.INFO), eq(">>> test\n"));
    }
}