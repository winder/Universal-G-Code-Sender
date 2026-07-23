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

/**
 * The state of the {@link CommandInterceptorService}. This is a virtual state that is separate from the
 * firmware reported {@link com.willwinder.universalgcodesender.listeners.ControllerState} and signals when
 * the interceptor service has taken control over a running stream.
 *
 * @author Joacim Breiler
 */
public enum InterceptorState {
    /**
     * No interception in progress, the stream (if any) is running normally.
     */
    INACTIVE,

    /**
     * A trigger command has been reached. The stream has stopped feeding new commands and we are waiting for
     * the commands already sent to the controller to finish executing.
     */
    PENDING,

    /**
     * The interceptor routine is executing commands (moves, probing, ...).
     */
    RUNNING,

    /**
     * The interceptor routine is blocked waiting for the operator to confirm or provide input.
     */
    WAITING_FOR_USER,

    /**
     * The routine has finished, the parser modal state is being restored and the stream is being resumed.
     */
    RESUMING,

    /**
     * The interceptor routine failed or timed out. The stream is left paused so the operator can decide how
     * to proceed.
     */
    FAILED
}
