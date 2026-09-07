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
package com.willwinder.universalgcodesender.fx.interceptor;

import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.services.interceptor.CommandInterceptor;
import com.willwinder.universalgcodesender.services.interceptor.InterceptorState;
import com.willwinder.universalgcodesender.services.interceptor.InterceptorStateEvent;
import com.willwinder.universalgcodesender.services.interceptor.ToolChangeInterceptor;
import javafx.stage.Window;

import java.util.Optional;
import java.util.logging.Logger;

/**
 * Listens for {@link InterceptorStateEvent}s emitted while a command interceptor has control of a running
 * stream and shows a dialog matching the active interceptor. The dialog presents the progress and lets the
 * operator continue or abort, feeding the response back to the
 * {@link com.willwinder.universalgcodesender.services.interceptor.CommandInterceptorService}.
 *
 * <p>Events are delivered on the JavaFX application thread by the
 * {@link com.willwinder.universalgcodesender.fx.service.FxEventDispatcher}.
 *
 * @author Joacim Breiler
 */
public class InterceptorDialogService implements UGSEventListener {
    private static final Logger LOGGER = Logger.getLogger(InterceptorDialogService.class.getName());

    private final BackendAPI backend;
    private final Window owner;
    private InterceptorDialog dialog;

    public InterceptorDialogService(BackendAPI backend, Window owner) {
        this.backend = backend;
        this.owner = owner;
    }

    @Override
    public void UGSEvent(UGSEvent event) {
        if (event instanceof InterceptorStateEvent interceptorStateEvent) {
            handleInterceptorEvent(interceptorStateEvent);
        } else if (event instanceof ControllerStateEvent controllerStateEvent && dialog != null) {
            dialog.handleControllerState(controllerStateEvent);
        }
    }

    private void handleInterceptorEvent(InterceptorStateEvent event) {
        if (event.getState() == InterceptorState.INACTIVE) {
            if (dialog != null) {
                InterceptorDialog closing = dialog;
                dialog = null;
                closing.handleEvent(event);
            }
            return;
        }

        if (dialog == null) {
            dialog = event.getInterceptor().flatMap(this::createDialog).orElse(null);
            if (dialog == null) {
                return;
            }
        }

        dialog.handleEvent(event);
    }

    private Optional<InterceptorDialog> createDialog(CommandInterceptor interceptor) {
        if (interceptor instanceof ToolChangeInterceptor) {
            return Optional.of(new ToolChangeDialog(owner, backend));
        }

        LOGGER.warning(() -> "No dialog available for interceptor " + interceptor.getClass().getSimpleName());
        return Optional.empty();
    }
}
