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
package com.willwinder.universalgcodesender.fx.settings;

import java.util.prefs.Preferences;

/**
 * The area and spacing used the last time objects were nested, kept in the application
 * preferences so the nest dialog opens with the values that were used before.
 */
public final class NestSettings {
    private static final Preferences prefs = Preferences.userNodeForPackage(NestSettings.class);
    private static final String MAX_WIDTH = "nest.maxWidth";
    private static final String MAX_HEIGHT = "nest.maxHeight";
    private static final String SPACING = "nest.spacing";
    private static final String ROTATION_ALLOWED = "nest.rotationAllowed";

    private NestSettings() {
    }

    public static double getMaxWidth() {
        return prefs.getDouble(MAX_WIDTH, 200);
    }

    public static void setMaxWidth(double maxWidth) {
        prefs.putDouble(MAX_WIDTH, maxWidth);
    }

    public static double getMaxHeight() {
        return prefs.getDouble(MAX_HEIGHT, 200);
    }

    public static void setMaxHeight(double maxHeight) {
        prefs.putDouble(MAX_HEIGHT, maxHeight);
    }

    public static double getSpacing() {
        return prefs.getDouble(SPACING, 2);
    }

    public static void setSpacing(double spacing) {
        prefs.putDouble(SPACING, spacing);
    }

    public static boolean isRotationAllowed() {
        return prefs.getBoolean(ROTATION_ALLOWED, true);
    }

    public static void setRotationAllowed(boolean rotationAllowed) {
        prefs.putBoolean(ROTATION_ALLOWED, rotationAllowed);
    }
}
