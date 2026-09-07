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
package com.willwinder.universalgcodesender.fx.component.visualizer.input;

import com.willwinder.universalgcodesender.fx.settings.VisualizerSettings.ModifierKey;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

/**
 * A mouse button together with the modifier key that has to be held for it, as the visualizer
 * settings describe the pan, rotate and primary actions. {@link ModifierKey#NONE} means no
 * modifier may be held at all, so a mapping with a modifier never shadows one without.
 */
public record MouseMapping(MouseButton button, ModifierKey modifier) {

    public static MouseMapping parse(String button, String modifier, MouseButton fallbackButton, ModifierKey fallbackModifier) {
        return new MouseMapping(parseButton(button, fallbackButton), ModifierKey.fromString(modifier, fallbackModifier));
    }

    /**
     * Whether a press or click event was made with this mapping.
     */
    public boolean matchesPress(MouseEvent event) {
        return event.getButton() == button && matchesModifier(event);
    }

    /**
     * Whether a press was made with this button and, if the mapping has one, its modifier, while
     * allowing further modifiers to be held. Editing tools read Shift and Alt themselves, so the
     * press that starts a gesture must not be refused because of them.
     */
    public boolean matchesPressWithExtraModifiers(MouseEvent event) {
        return event.getButton() == button && (modifier == ModifierKey.NONE || matchesModifier(event));
    }

    /**
     * Whether the button of this mapping is still held during a drag, with its modifier.
     */
    public boolean matchesDrag(MouseEvent event) {
        return isButtonDown(event) && matchesModifier(event);
    }

    private boolean matchesModifier(MouseEvent event) {
        return switch (modifier) {
            case NONE -> !event.isShiftDown() && !event.isControlDown() && !event.isAltDown() && !event.isMetaDown();
            case SHIFT -> event.isShiftDown();
            case CTRL -> event.isControlDown();
            case ALT -> event.isAltDown();
            case META -> event.isMetaDown();
        };
    }

    private boolean isButtonDown(MouseEvent event) {
        return switch (button) {
            case PRIMARY -> event.isPrimaryButtonDown();
            case MIDDLE -> event.isMiddleButtonDown();
            case SECONDARY -> event.isSecondaryButtonDown();
            case BACK -> event.isBackButtonDown();
            case FORWARD -> event.isForwardButtonDown();
            default -> false;
        };
    }

    private static MouseButton parseButton(String value, MouseButton fallback) {
        if (value == null || value.isBlank()) {
            return fallback;
        }
        try {
            return MouseButton.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return fallback;
        }
    }
}
