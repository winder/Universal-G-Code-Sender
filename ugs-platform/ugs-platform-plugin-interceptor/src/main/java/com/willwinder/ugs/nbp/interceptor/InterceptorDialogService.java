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
package com.willwinder.ugs.nbp.interceptor;

import com.willwinder.ugs.nbp.interceptor.toolchange.ToolChangeDialog;
import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.BackendAPI;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.events.ControllerStateEvent;
import com.willwinder.universalgcodesender.services.LookupService;
import com.willwinder.universalgcodesender.services.interceptor.CommandInterceptor;
import com.willwinder.universalgcodesender.services.interceptor.InterceptorState;
import com.willwinder.universalgcodesender.services.interceptor.InterceptorStateEvent;
import com.willwinder.universalgcodesender.services.interceptor.ToolChangeInterceptor;
import org.openide.modules.OnStart;
import org.openide.util.lookup.ServiceProvider;

import javax.swing.SwingUtilities;

/**
 * A dialog service that listens for {@link InterceptorStateEvent}s emitted while a command interceptor has control of
 * a running stream. The dialog shows the progress as they arrive and lets the operator continue when the interceptor
 * is waiting for input, or abort the interception. The operator response is fed back to the
 * {@link com.willwinder.universalgcodesender.services.interceptor.CommandInterceptorService}.
 *
 * @author Joacim Breiler
 */
@OnStart
@ServiceProvider(service = InterceptorDialogService.class)
public class InterceptorDialogService implements Runnable, UGSEventListener {
    private BackendAPI backend;
    private InterceptorDialog dialog;

    @Override
    public void run() {
        backend = LookupService.lookup(BackendAPI.class);
        if (backend != null) {
            backend.addUGSEventListener(this);
        }
    }

    @Override
    public void UGSEvent(UGSEvent event) {
        if (event instanceof InterceptorStateEvent interceptorStateEvent) {
            SwingUtilities.invokeLater(() -> handleInterceptorEvent(interceptorStateEvent));
        } else if (event instanceof ControllerStateEvent controllerStateEvent) {
            SwingUtilities.invokeLater(() -> {
                if (dialog != null) {
                    dialog.handleControllerState(controllerStateEvent);
                }
            });
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
            if (event.getInterceptor().isEmpty()) {
                return;
            }

            CommandInterceptor interceptor = event.getInterceptor().get();
            if (interceptor instanceof ToolChangeInterceptor) {
                dialog = new ToolChangeDialog(backend);
            } else {
                dialog = new GenericDialog(backend);
            }
        }

        dialog.handleEvent(event);
    }
}
