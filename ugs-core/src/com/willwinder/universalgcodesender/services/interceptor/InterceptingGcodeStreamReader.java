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

import java.io.IOException;
import java.util.Optional;

/**
 * A {@link IGcodeStreamReader} decorator that gates the underlying stream when a command matches a
 * registered {@link CommandInterceptor}. When gated it stops handing out commands so that the buffered
 * communicator naturally stops filling the controller buffer, without marking the stream as finished. Once
 * the interceptor routine has completed, the stream is ungated and can be resumed by re-invoking
 * {@link com.willwinder.universalgcodesender.communicator.ICommunicator#streamCommands()}.
 *
 * @author Joacim Breiler
 */
public class InterceptingGcodeStreamReader implements IGcodeStreamReader {
    private final IGcodeStreamReader delegate;
    private final CommandInterceptorService service;
    private volatile boolean gated = false;

    private GcodeCommand pendingCommand;
    private CommandInterceptor pendingInterceptor;

    public InterceptingGcodeStreamReader(IGcodeStreamReader delegate, CommandInterceptorService service) {
        this.delegate = delegate;
        this.service = service;
    }

    @Override
    public boolean ready() {
        if (gated) {
            return false;
        }
        // While a trigger is pending we still need to be polled (so it can be released once the active
        // commands have responded), even if the delegate has no more rows.
        return pendingCommand != null || delegate.ready();
    }

    @Override
    public int getNumRows() {
        return delegate.getNumRows();
    }

    @Override
    public int getNumRowsRemaining() {
        return delegate.getNumRowsRemaining();
    }

    @Override
    public GcodeCommand getNextCommand() throws IOException {
        if (gated) {
            return null;
        }

        if (pendingCommand == null) {
            GcodeCommand next = delegate.getNextCommand();
            if (next == null) {
                return null;
            }

            Optional<CommandInterceptor> interceptor = service.findInterceptor(next);
            if (interceptor.isEmpty()) {
                return next;
            }

            pendingCommand = next;
            pendingInterceptor = interceptor.get();
        }

        // Do not trigger the interception until every command that was already sent has received a response
        // from the controller. Otherwise an error in the buffer (leaving the machine in a hold state) would
        // race with the interception. This method is re-invoked as each active command completes.
        if (service.hasActiveCommands()) {
            return null;
        }

        GcodeCommand triggerCommand = pendingCommand;
        CommandInterceptor interceptor = pendingInterceptor;
        pendingCommand = null;
        pendingInterceptor = null;

        gated = true;
        service.onTriggerReached(interceptor, triggerCommand, this);
        // Replace the intercepted command with a blank line so it is not executed by the controller, but is
        // still streamed and counted as a row so the stream progress and completion stay correct.
        return new GcodeCommand("", triggerCommand.getOriginalCommandString(), triggerCommand.getComment(), triggerCommand.getCommandNumber());
    }

    public void ungate() {
        gated = false;
    }

    public boolean isGated() {
        return gated;
    }

    @Override
    public void close() throws IOException {
        delegate.close();
    }
}
