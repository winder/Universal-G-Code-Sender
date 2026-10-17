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

import com.willwinder.universalgcodesender.fx.component.ToastPane;

/**
 * Shows short-lived messages to the user. The {@link ToastPane} it draws on is placed on top of
 * the main window by the application at startup.
 */
public class ToastService {
    private static final ToastService INSTANCE = new ToastService();
    private ToastPane toastPane;

    private ToastService() {
    }

    public static ToastService getInstance() {
        return INSTANCE;
    }

    public synchronized ToastPane getToastPane() {
        if (toastPane == null) {
            toastPane = new ToastPane();
        }
        return toastPane;
    }

    public void showInfo(String message) {
        getToastPane().show(ToastPane.Type.INFO, message);
    }

    public void showInfo(String message, Runnable onClick) {
        getToastPane().show(ToastPane.Type.INFO, message, onClick);
    }

    public void showError(String message) {
        getToastPane().show(ToastPane.Type.ERROR, message);
    }

    public void showError(String message, Runnable onClick) {
        getToastPane().show(ToastPane.Type.ERROR, message, onClick);
    }
}
