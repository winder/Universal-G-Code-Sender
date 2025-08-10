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
package com.willwinder.universalgcodesender.fx.service;

import com.willwinder.universalgcodesender.fx.helper.ShortcutConverter;
import com.willwinder.universalgcodesender.fx.model.ShortcutEvent;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import javafx.scene.Scene;
import static javafx.scene.input.KeyEvent.KEY_PRESSED;
import static javafx.scene.input.KeyEvent.KEY_RELEASED;
import org.apache.commons.lang3.StringUtils;

import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.prefs.Preferences;

/**
 * Stores action shortcut mappings to a preferences file.
 */
public class ShortcutService {
    private static final Preferences preferences = Preferences.userNodeForPackage(ShortcutService.class);

    private static final ObservableMap<String, String> shortcuts =
            FXCollections.observableHashMap();

    private static final Set<String> pressedKeys = ConcurrentHashMap.newKeySet();

    static {
        loadFromPreferences();
    }

    private static void loadFromPreferences() {
        shortcuts.clear();

        ActionRegistry.getInstance().getActions().forEach(action ->
                Optional.ofNullable(preferences.get(action.getId(), null))
                        .ifPresent(shortcut -> shortcuts.put(action.getId(), shortcut)));
    }

    public static ObservableMap<String, String> getShortcuts() {
        return shortcuts;
    }

    public static void setShortcut(String actionId, String shortcut) {
        preferences.put(actionId, shortcut);
        shortcuts.put(actionId, shortcut);
    }

    public static Optional<String> getShortcut(String actionId) {
        return Optional.ofNullable(shortcuts.getOrDefault(actionId, null));
    }

    public static void removeShortcut(String id) {
        shortcuts.remove(id);
        preferences.remove(id);
    }

    public static Optional<String> getActionId(String shortcut) {
        if (!shortcuts.containsValue(shortcut)) {
            return Optional.empty();
        }

        return shortcuts.keySet().stream()
                .filter(actionId -> {
                    String currentShortcut = shortcuts.getOrDefault(actionId, "");
                    return currentShortcut != null && currentShortcut.equals(shortcut);
                })
                .findFirst();
    }


    public static void registerListener(Scene scene) {
        scene.addEventFilter(KEY_PRESSED, e -> {
            String shortcut = ShortcutConverter.toString(e);
            if (StringUtils.isEmpty(shortcut) || pressedKeys.contains(shortcut)) {
                return;
            }

            pressedKeys.add(shortcut);
            ShortcutService.getActionId(shortcut)
                    .flatMap(id -> ActionRegistry.getInstance().getAction(id))
                    .ifPresent(a -> a.handle(new ShortcutEvent(ShortcutEvent.SHORTCUT_PRESSED)));
        });

        scene.addEventFilter(KEY_RELEASED, e -> {
            String shortcut = ShortcutConverter.toString(e);
            if (StringUtils.isEmpty(shortcut) || !pressedKeys.contains(shortcut)) {
                return;
            }
            pressedKeys.remove(shortcut);
            ShortcutService.getActionId(shortcut)
                    .flatMap(id -> ActionRegistry.getInstance().getAction(id))
                    .ifPresent(a -> a.handle(new ShortcutEvent(ShortcutEvent.SHORTCUT_RELEASED)));
        });
    }
}
