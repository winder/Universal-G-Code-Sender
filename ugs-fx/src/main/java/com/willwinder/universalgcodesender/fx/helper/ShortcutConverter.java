/*
    Copyright 2025 Joacim Breiler

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

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyEvent;
import org.apache.commons.lang3.SystemUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ShortcutConverter {

    /**
     * Platform-independent modifier token used in default shortcuts. It resolves to the platform
     * menu-shortcut modifier so a single default definition works across operating systems.
     */
    public static final String PLATFORM_MODIFIER_TOKEN = "SHORTCUT";

    /**
     * The platform menu-shortcut modifier: {@code META} (the Command key) on macOS and
     * {@code CTRL} on Windows and Linux.
     */
    public static String getPlatformModifier() {
        return SystemUtils.IS_OS_MAC ? "META" : "CTRL";
    }

    /**
     * Resolves the {@link #PLATFORM_MODIFIER_TOKEN} in a shortcut string to the platform
     * menu-shortcut modifier, turning e.g. {@code "SHORTCUT+S"} into {@code "META+S"} on macOS and
     * {@code "CTRL+S"} on Windows and Linux. Other parts are passed through untouched (upper-cased).
     */
    public static String resolvePlatformShortcut(String shortcut) {
        if (shortcut == null || shortcut.isBlank()) {
            return shortcut;
        }

        return Arrays.stream(shortcut.split("\\+"))
                .map(part -> part.equalsIgnoreCase(PLATFORM_MODIFIER_TOKEN) ? getPlatformModifier() : part.toUpperCase())
                .collect(Collectors.joining("+"));
    }

    /**
     * Parses a shortcut string like "CTRL+SHIFT+S" into a KeyCodeCombination.
     */
    public static KeyCodeCombination fromString(String shortcut) {
        if (shortcut == null || shortcut.isBlank()) {
            throw new IllegalArgumentException("Shortcut string cannot be null or empty");
        }

        String[] parts = shortcut.toUpperCase().split("\\+");
        List<KeyCombination.Modifier> modifiers = new ArrayList<>();
        KeyCode keyCode = null;

        for (String part : parts) {
            switch (part) {
                case "CTRL", "CONTROL" -> modifiers.add(KeyCombination.CONTROL_DOWN);
                case "SHIFT" -> modifiers.add(KeyCombination.SHIFT_DOWN);
                case "ALT" -> modifiers.add(KeyCombination.ALT_DOWN);
                case "META", "COMMAND", "CMD" -> modifiers.add(KeyCombination.META_DOWN);
                default -> {
                    // Assume it's the main key
                    keyCode = KeyCode.valueOf(part);
                }
            }
        }

        if (keyCode == null) {
            throw new IllegalArgumentException("No valid key code found in shortcut: " + shortcut);
        }

        return new KeyCodeCombination(keyCode, modifiers.toArray(new KeyCombination.Modifier[0]));
    }

    /**
     * Converts a KeyCodeCombination to a "CTRL+SHIFT+S" style string.
     */
    public static String toString(KeyCodeCombination combo) {
        List<String> parts = new ArrayList<>();

        if (combo.getControl() == KeyCombination.ModifierValue.DOWN) parts.add("CTRL");
        if (combo.getShift() == KeyCombination.ModifierValue.DOWN) parts.add("SHIFT");
        if (combo.getAlt() == KeyCombination.ModifierValue.DOWN) parts.add("ALT");
        if (combo.getMeta() == KeyCombination.ModifierValue.DOWN) parts.add("META");

        parts.add(combo.getCode().getName().toUpperCase());

        return String.join("+", parts);
    }

    public static String toString(KeyEvent e) {
        try {
            return toString(new KeyCodeCombination(
                    e.getCode(),
                    e.isShiftDown() ? KeyCombination.SHIFT_DOWN : KeyCombination.SHIFT_ANY,
                    e.isControlDown() ? KeyCombination.CONTROL_DOWN : KeyCombination.CONTROL_ANY,
                    e.isAltDown() ? KeyCombination.ALT_DOWN : KeyCombination.ALT_ANY,
                    e.isMetaDown() ? KeyCombination.META_DOWN : KeyCombination.META_ANY
            ));
        } catch (IllegalArgumentException ex) {
            return "";
        }
    }
}