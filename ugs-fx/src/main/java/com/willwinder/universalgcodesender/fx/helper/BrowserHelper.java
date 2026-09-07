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
package com.willwinder.universalgcodesender.fx.helper;

import javafx.application.HostServices;

import java.awt.Desktop;
import java.net.URI;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Opens links in the system web browser. Prefers the JavaFX host services registered by the
 * application on startup and falls back to the AWT desktop integration when none is available.
 */
public final class BrowserHelper {
    private static final Logger LOGGER = Logger.getLogger(BrowserHelper.class.getSimpleName());
    private static HostServices hostServices;

    private BrowserHelper() {
    }

    public static void setHostServices(HostServices services) {
        hostServices = services;
    }

    public static void open(String url) {
        if (url == null || url.isBlank()) {
            return;
        }

        if (hostServices != null) {
            hostServices.showDocument(url);
            return;
        }

        Thread.ofVirtual().start(() -> openWithDesktop(url));
    }

    private static void openWithDesktop(String url) {
        try {
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                Desktop.getDesktop().browse(URI.create(url));
            } else {
                LOGGER.log(Level.WARNING, () -> "Could not open browser for " + url);
            }
        } catch (Exception e) {
            LOGGER.log(Level.WARNING, e, () -> "Could not open browser for " + url);
        }
    }
}
