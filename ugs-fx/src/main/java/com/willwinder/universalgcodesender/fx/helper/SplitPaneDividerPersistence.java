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
import javafx.scene.control.SplitPane;

/**
 * Two-way persistence for a single {@link SplitPane} divider position against a settings property:
 * restores the stored position on startup and writes the user's drags back to it.
 * <p>
 * It is built to survive JavaFX's quirks around split panes:
 * <ul>
 *     <li>JavaFX recreates {@link SplitPane.Divider} instances whenever items are added or removed
 *     (e.g. the inspector docking), so a cached divider reference goes stale. This tracks the
 *     divider <em>by index</em> and re-wires whenever the dividers list changes.</li>
 *     <li>A position set before the pane has been laid out is discarded by the first layout pulse,
 *     so the pane is laid out before the stored value is applied.</li>
 *     <li>Applying the stored value (and the layout settling) must not be saved back over it, so a
 *     guard distinguishes programmatic restores from genuine user drags.</li>
 * </ul>
 * Call {@link #install} once the owning window is shown, so the panes have their real size.
 */
public final class SplitPaneDividerPersistence {

    private final SplitPane splitPane;
    private final int dividerIndex;
    private final DoubleProperty setting;

    private SplitPane.Divider wiredDivider;
    private boolean restoring;

    private SplitPaneDividerPersistence(SplitPane splitPane, int dividerIndex, DoubleProperty setting) {
        this.splitPane = splitPane;
        this.dividerIndex = dividerIndex;
        this.setting = setting;
    }

    /**
     * Starts persisting the divider at {@code dividerIndex} of {@code splitPane} to {@code setting}.
     * The divider does not need to exist yet — it is wired as soon as it appears (and again if it is
     * recreated), which is what lets the dynamically docked inspector divider be restored too.
     */
    public static void install(SplitPane splitPane, int dividerIndex, DoubleProperty setting) {
        SplitPaneDividerPersistence persistence = new SplitPaneDividerPersistence(splitPane, dividerIndex, setting);
        // Re-wire after every dividers change, deferred so it runs on a clean pulse rather than
        // re-entrantly during the items mutation that triggered the change.
        splitPane.getDividers().addListener((ListChangeListener<SplitPane.Divider>) change ->
                Platform.runLater(persistence::wire));
        persistence.wire();
    }

    private void wire() {
        if (splitPane.getDividers().size() <= dividerIndex) {
            wiredDivider = null;
            return;
        }

        SplitPane.Divider divider = splitPane.getDividers().get(dividerIndex);
        if (divider == wiredDivider) {
            return;
        }
        wiredDivider = divider;

        divider.positionProperty().addListener((obs, oldVal, newVal) -> {
            if (!restoring) {
                setting.set(newVal.doubleValue());
            }
        });

        restoring = true;
        // Realise the (possibly freshly created) divider before positioning it, otherwise the first
        // layout pulse discards the value.
        splitPane.applyCss();
        splitPane.layout();
        divider.setPosition(setting.get());
        restoring = false;
    }
}
