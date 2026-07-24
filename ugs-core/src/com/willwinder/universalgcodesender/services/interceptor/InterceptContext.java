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

import com.willwinder.universalgcodesender.IController;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.types.GcodeCommand;

/**
 * The context that is handed to a {@link CommandInterceptor} while it is executing. It provides access to
 * the controller, the command that triggered the interception and a way to block for operator input.
 *
 * @author Joacim Breiler
 */
public class InterceptContext {
    private final BackendAPI backend;
    private final GcodeCommand triggerCommand;
    private final CommandInterceptorService service;

    InterceptContext(BackendAPI backend, GcodeCommand triggerCommand, CommandInterceptorService service) {
        this.backend = backend;
        this.triggerCommand = triggerCommand;
        this.service = service;
    }

    public IController getController() {
        return backend.getController();
    }

    public BackendAPI getBackend() {
        return backend;
    }

    public GcodeCommand getTriggerCommand() {
        return triggerCommand;
    }

    /**
     * Blocks the interceptor routine until the operator responds to the given prompt. While blocked the
     * service is in the {@link InterceptorState#WAITING_FOR_USER} state.
     *
     * @param prompt the prompt describing the step, message and the available responses
     * @return the operator response, which is either {@link UserResponse#CONTINUE} or {@link UserResponse#SKIP}
     * @throws InterceptAbortedException if the operator chose to abort
     */
    public UserResponse awaitUserPrompt(InterceptorPrompt prompt) throws InterceptAbortedException {
        return service.awaitUserPrompt(prompt);
    }
}
