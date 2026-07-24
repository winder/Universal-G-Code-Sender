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

import java.util.List;

/**
 * Describes a request for operator input from an interceptor. The step id lets a user interface render a
 * screen tailored to the current step, and the options describe which responses the operator may give.
 *
 * @author Joacim Breiler
 */
public record InterceptorPrompt(String stepId, List<UserResponse> options) {
    public InterceptorPrompt(String stepId, List<UserResponse> options) {
        this.stepId = stepId;
        this.options = List.copyOf(options);
    }

    public boolean hasOption(UserResponse option) {
        return options.contains(option);
    }
}
