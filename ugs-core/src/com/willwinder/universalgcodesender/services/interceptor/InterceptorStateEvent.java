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

import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.types.GcodeCommand;

import java.util.Optional;

/**
 * An event that is dispatched on the UGS event bus whenever the {@link CommandInterceptorService} changes
 * its virtual state. UI components can observe this to display an overlay on top of the regular controller
 * state and to react to the interceptor requesting user input.
 *
 * @author Joacim Breiler
 */
public class InterceptorStateEvent implements UGSEvent {

    private final InterceptorState state;
    private final InterceptorState previousState;
    private final transient CommandInterceptor interceptor;
    private final transient GcodeCommand triggerCommand;
    private final transient InterceptorPrompt prompt;

    public InterceptorStateEvent(InterceptorState state, InterceptorState previousState, CommandInterceptor interceptor, GcodeCommand triggerCommand, InterceptorPrompt prompt) {
        this.state = state;
        this.previousState = previousState;
        this.interceptor = interceptor;
        this.triggerCommand = triggerCommand;
        this.prompt = prompt;
    }

    public InterceptorState getState() {
        return state;
    }

    public InterceptorState getPreviousState() {
        return previousState;
    }

    public Optional<CommandInterceptor> getInterceptor() {
        return Optional.ofNullable(interceptor);
    }

    public Optional<GcodeCommand> getTriggerCommand() {
        return Optional.ofNullable(triggerCommand);
    }

    public Optional<InterceptorPrompt> getPrompt() {
        return Optional.ofNullable(prompt);
    }
}
