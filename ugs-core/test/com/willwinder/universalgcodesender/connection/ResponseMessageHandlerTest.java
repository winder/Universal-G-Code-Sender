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
package com.willwinder.universalgcodesender.connection;

import com.willwinder.universalgcodesender.AbstractCommunicator;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class ResponseMessageHandlerTest {

    private ResponseMessageHandler responseMessageHandler;

    @Before
    public void setUp() {
        responseMessageHandler = new ResponseMessageHandler();
    }

    @Test
    public void responseWithNoLineEndingShouldNotDispatchMessage() throws Exception {
        // Given
        AbstractCommunicator communicator = mock(AbstractCommunicator.class);
        responseMessageHandler.addListener(communicator);

        // When
        responseMessageHandler.handleResponse("test");

        // Then
        verify(communicator, times(0)).handleResponseMessage(any());
    }

    @Test
    public void responseWithCarrierReturnShouldNotDispatchMessage() throws Exception {
        // Given
        AbstractCommunicator communicator = mock(AbstractCommunicator.class);
        responseMessageHandler.addListener(communicator);

        // When
        responseMessageHandler.handleResponse("test\r");

        // Then
        verify(communicator, times(0)).handleResponseMessage(any());
    }

    @Test
    public void responseWithUnixLineEndingShouldDispatchMessage() throws Exception {
        // Given
        AbstractCommunicator communicator = mock(AbstractCommunicator.class);
        responseMessageHandler.addListener(communicator);

        // When
        responseMessageHandler.handleResponse("test\n");

        // Then
        verify(communicator, times(1)).handleResponseMessage("test");
    }

    @Test
    public void responseWithWindowsLineEndingShouldDispatchMessage() throws Exception {
        // Given
        AbstractCommunicator communicator = mock(AbstractCommunicator.class);
        responseMessageHandler.addListener(communicator);

        // When
        responseMessageHandler.handleResponse("test\r\n");

        // Then
        verify(communicator, times(1)).handleResponseMessage("test");
    }

    @Test
    public void responseWithMultipleMessagesShouldDispatchMessages() throws Exception {
        // Given
        AbstractCommunicator communicator = mock(AbstractCommunicator.class);
        responseMessageHandler.addListener(communicator);

        // When
        responseMessageHandler.handleResponse("test1\r\n test2 \ntest3");

        // Then
        verify(communicator, times(1)).handleResponseMessage("test1");
        verify(communicator, times(1)).handleResponseMessage(" test2 ");
        verify(communicator, times(0)).handleResponseMessage("test3");
    }

    @Test
    public void multipleResponsesShouldDispatchMessages() throws Exception {
        // Given
        AbstractCommunicator communicator = mock(AbstractCommunicator.class);
        responseMessageHandler.addListener(communicator);

        // When
        responseMessageHandler.handleResponse("test1\r\n ");
        responseMessageHandler.handleResponse("test2");
        responseMessageHandler.handleResponse(" \r\n");
        responseMessageHandler.handleResponse("test3\n");

        // Then
        verify(communicator, times(1)).handleResponseMessage("test1");
        verify(communicator, times(1)).handleResponseMessage(" test2 ");
        verify(communicator, times(1)).handleResponseMessage("test3");
    }
}
