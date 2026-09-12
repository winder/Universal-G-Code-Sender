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

import javafx.application.Platform;
import javafx.beans.property.DoubleProperty;
import javafx.collections.ListChangeListener;
import javafx.scene.Node;
import javafx.scene.control.SplitPane;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Two-way persistence of {@link SplitPane} divider positions against settings properties: restores
 * the stored positions on startup and writes the user's drags back to them.
 * <p>
 * It is built to survive JavaFX's quirks around split panes:
 * <ul>
 *     <li>JavaFX recreates {@link SplitPane.Divider} instances whenever items are added or removed
 *     (e.g. the inspector docking or a side pane collapsing), so a cached divider reference goes
 *     stale. Each divider is tracked by the item <em>directly before it</em> and re-wired whenever
 *     the dividers list changes. Anchoring to an item rather than an index keeps the mapping
 *     correct when items earlier in the pane are removed and the divider indexes shift.</li>
 *     <li>Recreated dividers start at a default position and the skin validates each restored
 *     position against its neighbours, so all dividers of a split pane are restored together
 *     through {@link SplitPaneDividers#apply}. One instance per split pane coordinates that.</li>
 *     <li>Applying the stored values (and the layout settling) must not be saved back over them, so
 *     a guard distinguishes programmatic restores from genuine user drags.</li>
 * </ul>
 * Call {@link #install} once the owning window is shown, so the panes have their real size.
 */
public final class SplitPaneDividerPersistence {

    private final SplitPane splitPane;
    private final Map<Node, DoubleProperty> settingsByItemBefore = new LinkedHashMap<>();
    private final Map<Node, SplitPane.Divider> wiredDividersByItemBefore = new HashMap<>();
    private boolean restoring;

    private SplitPaneDividerPersistence(SplitPane splitPane) {
        this.splitPane = splitPane;
        // Re-wire after every dividers change, deferred so it runs on a clean pulse rather than
        // re-entrantly during the items mutation that triggered the change.
        splitPane.getDividers().addListener((ListChangeListener<SplitPane.Divider>) change ->
                Platform.runLater(this::wire));
    }

    /**
     * Starts persisting the divider that follows {@code itemBefore} in {@code splitPane} to
     * {@code setting}. Neither the item nor the divider needs to be present yet — the divider is
     * wired as soon as it appears (and again if it is recreated), which is what lets the dynamically
     * docked inspector divider and the collapsible side panes be restored too.
     */
    public static void install(SplitPane splitPane, Node itemBefore, DoubleProperty setting) {
        SplitPaneDividerPersistence persistence = (SplitPaneDividerPersistence) splitPane.getProperties()
                .computeIfAbsent(SplitPaneDividerPersistence.class, key -> new SplitPaneDividerPersistence(splitPane));
        persistence.settingsByItemBefore.put(itemBefore, setting);
        persistence.wire();
    }

    private void wire() {
        Map<Integer, Double> positions = new HashMap<>();
        settingsByItemBefore.forEach((itemBefore, setting) -> {
            // Divider i sits between item i and item i + 1, so the divider after an item shares its index.
            int dividerIndex = splitPane.getItems().indexOf(itemBefore);
            if (dividerIndex < 0 || splitPane.getDividers().size() <= dividerIndex) {
                wiredDividersByItemBefore.remove(itemBefore);
                return;
            }

            SplitPane.Divider divider = splitPane.getDividers().get(dividerIndex);
            if (wiredDividersByItemBefore.get(itemBefore) != divider) {
                wiredDividersByItemBefore.put(itemBefore, divider);
                divider.positionProperty().addListener((obs, oldVal, newVal) -> {
                    if (!restoring) {
                        setting.set(newVal.doubleValue());
                    }
                });
            }
            positions.put(dividerIndex, setting.get());
        });

        if (positions.isEmpty()) {
            return;
        }
        restoring = true;
        SplitPaneDividers.apply(splitPane, positions);
        restoring = false;
    }
}
