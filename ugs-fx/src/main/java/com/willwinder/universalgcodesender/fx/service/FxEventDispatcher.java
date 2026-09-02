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

import com.willwinder.universalgcodesender.listeners.UGSEventListener;
import com.willwinder.universalgcodesender.model.UGSEvent;
import com.willwinder.universalgcodesender.model.UGSEventDispatcher;
import javafx.application.Platform;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Delivers backend events to the JavaFX UI on the JavaFX thread, so its listeners may update
 * controls directly and the backend's file processing can run on any thread. The core services,
 * which react to a loaded file by re-parsing it and gathering statistics, keep receiving events
 * on the thread that raised them, so that work stays off the JavaFX thread too.
 */
public class FxEventDispatcher extends UGSEventDispatcher {
    private static final String UI_PACKAGE = "com.willwinder.universalgcodesender.fx.";

    private final Map<UGSEventListener, UGSEventListener> uiListeners = new ConcurrentHashMap<>();

    @Override
    public void addListener(UGSEventListener listener) {
        if (isUiListener(listener)) {
            super.addListener(uiListeners.computeIfAbsent(listener, FxEventDispatcher::onFxThread));
        } else {
            super.addListener(listener);
        }
    }

    @Override
    public void removeListener(UGSEventListener listener) {
        UGSEventListener wrapper = uiListeners.remove(listener);
        super.removeListener(wrapper != null ? wrapper : listener);
    }

    private static boolean isUiListener(UGSEventListener listener) {
        return listener.getClass().getName().startsWith(UI_PACKAGE);
    }

    private static UGSEventListener onFxThread(UGSEventListener listener) {
        return event -> {
            if (Platform.isFxApplicationThread()) {
                listener.UGSEvent(event);
                return;
            }
            try {
                Platform.runLater(() -> listener.UGSEvent(event));
            } catch (IllegalStateException toolkitNotRunning) {
                listener.UGSEvent(event);
            }
        };
    }
}
