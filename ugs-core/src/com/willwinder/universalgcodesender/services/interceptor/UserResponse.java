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
 * The response from the operator when an interceptor routine is waiting for user input.
 *
 * @author Joacim Breiler
 */
public enum UserResponse {
    /**
     * The operator wants to continue the interceptor routine.
     */
    CONTINUE,

    /**
     * The operator wants to skip the current optional step of the interceptor routine.
     */
    SKIP,

    /**
     * The operator wants to abort the interceptor routine and cancel the running stream.
     */
    ABORT
}
