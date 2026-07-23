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
package com.willwinder.universalgcodesender.services.interceptor;

import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InterceptingGcodeStreamReaderTest {

    private CommandInterceptorService service;
    private IGcodeStreamReader delegate;

    @Before
    public void setUp() {
        service = mock(CommandInterceptorService.class);
        delegate = mock(IGcodeStreamReader.class);
    }

    @Test
    public void getNextCommand_shouldPassThroughNonMatchingCommands() throws IOException {
        GcodeCommand command = new GcodeCommand("G0 X10");
        when(delegate.getNextCommand()).thenReturn(command);
        when(service.findInterceptor(any())).thenReturn(java.util.Optional.empty());
        InterceptingGcodeStreamReader reader = new InterceptingGcodeStreamReader(delegate, service);

        GcodeCommand result = reader.getNextCommand();

        assertThat(result).isSameAs(command);
        assertThat(reader.isGated()).isFalse();
    }

    @Test
    public void getNextCommand_shouldGateAndConsumeMatchingCommand() throws IOException {
        GcodeCommand toolChange = new GcodeCommand("M6");
        CommandInterceptor interceptor = mock(CommandInterceptor.class);
        when(delegate.getNextCommand()).thenReturn(toolChange);
        when(service.findInterceptor(toolChange)).thenReturn(java.util.Optional.of(interceptor));
        InterceptingGcodeStreamReader reader = new InterceptingGcodeStreamReader(delegate, service);

        GcodeCommand result = reader.getNextCommand();

        assertThat(result).isNull();
        assertThat(reader.isGated()).isTrue();
        verify(service).onTriggerReached(interceptor, toolChange, reader);
    }

    @Test
    public void getNextCommand_shouldNotReadDelegateWhileGated() throws IOException {
        GcodeCommand toolChange = new GcodeCommand("M6");
        CommandInterceptor interceptor = mock(CommandInterceptor.class);
        when(delegate.getNextCommand()).thenReturn(toolChange);
        when(service.findInterceptor(toolChange)).thenReturn(java.util.Optional.of(interceptor));
        InterceptingGcodeStreamReader reader = new InterceptingGcodeStreamReader(delegate, service);
        reader.getNextCommand();

        GcodeCommand result = reader.getNextCommand();

        assertThat(result).isNull();
        assertThat(reader.ready()).isFalse();
        verify(delegate, times(1)).getNextCommand();
    }

    @Test
    public void ready_shouldReturnFalseWhenGated() throws IOException {
        GcodeCommand toolChange = new GcodeCommand("M6");
        when(delegate.ready()).thenReturn(true);
        when(delegate.getNextCommand()).thenReturn(toolChange);
        when(service.findInterceptor(toolChange)).thenReturn(java.util.Optional.of(mock(CommandInterceptor.class)));
        InterceptingGcodeStreamReader reader = new InterceptingGcodeStreamReader(delegate, service);

        reader.getNextCommand();

        assertThat(reader.ready()).isFalse();
    }

    @Test
    public void getNextCommand_shouldResumeAfterUngate() throws IOException {
        GcodeCommand toolChange = new GcodeCommand("M6");
        GcodeCommand nextMove = new GcodeCommand("G0 X20");
        when(delegate.getNextCommand()).thenReturn(toolChange, nextMove);
        when(service.findInterceptor(toolChange)).thenReturn(java.util.Optional.of(mock(CommandInterceptor.class)));
        when(service.findInterceptor(nextMove)).thenReturn(java.util.Optional.empty());
        InterceptingGcodeStreamReader reader = new InterceptingGcodeStreamReader(delegate, service);
        reader.getNextCommand();

        reader.ungate();
        GcodeCommand result = reader.getNextCommand();

        assertThat(reader.isGated()).isFalse();
        assertThat(result).isSameAs(nextMove);
    }
}
