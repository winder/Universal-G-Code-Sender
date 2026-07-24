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

/**
 * An interceptor that can take control of a running stream when a matching command is about to be sent to
 * the controller. Implementations are registered with the {@link CommandInterceptorService}.
 *
 * @author Joacim Breiler
 */
public interface CommandInterceptor {

    /**
     * Determines if this interceptor should take control before the given command is streamed to the
     * controller. This is evaluated on the streaming thread and must be fast and free of side effects.
     *
     * @param command the command that is about to be streamed
     * @return true if this interceptor should handle the command
     */
    boolean matches(GcodeCommand command);

    /**
     * Executes the interceptor routine. This is called on a dedicated background thread after the commands
     * already sent to the controller have finished executing and the machine is idle. Implementations may
     * block, send commands through {@link InterceptContext#getController()} and wait for user inputs
     *
     * @param context the context giving access to the controller and to user interaction
     * @throws InterceptException if the routine could not be completed
     */
    void execute(InterceptContext context) throws InterceptException;
}
