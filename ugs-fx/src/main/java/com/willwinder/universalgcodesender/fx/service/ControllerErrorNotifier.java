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
package com.willwinder.universalgcodesender.fx.service;

import com.willwinder.universalgcodesender.listeners.MessageListener;
import com.willwinder.universalgcodesender.listeners.MessageType;
import com.willwinder.universalgcodesender.model.BackendAPI;

import java.util.function.BiConsumer;

/**
 * Surfaces the errors and alarms that the controllers report to the console, such as a rejected
 * command or a triggered hard limit, as toasts so they are noticed without the terminal open.
 */
public class ControllerErrorNotifier implements MessageListener {
    private final BiConsumer<String, Runnable> errorToast;
    private final Runnable openConsole;

    ControllerErrorNotifier(BiConsumer<String, Runnable> errorToast, Runnable openConsole) {
        this.errorToast = errorToast;
        this.openConsole = openConsole;
    }

    public static void register(BackendAPI backend, Runnable openConsole) {
        backend.addMessageListener(new ControllerErrorNotifier(ToastService.getInstance()::showError, openConsole));
    }

    @Override
    public void onMessage(MessageType messageType, String message) {
        if (messageType != MessageType.ERROR) {
            return;
        }

        if (!message.isEmpty()) {
            errorToast.accept(message, openConsole);
        }
    }
}
