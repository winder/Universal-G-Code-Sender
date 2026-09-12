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

import javafx.scene.Parent;
import javafx.scene.control.SplitPane;

import java.util.Map;

/**
 * Applies divider positions to a {@link SplitPane} so that they actually stick.
 * <p>
 * Setting a divider position is only a request: the skin validates it during the next layout pass
 * against the <em>current pixel position</em> of the neighbouring dividers, and it discards it
 * entirely in a pass where the split pane itself changed size, pinning items that do not resize
 * with the parent to whatever size they had before. Both happen when a side pane is collapsed or
 * expanded, since that recreates the dividers and shows or hides a rail next to the split pane.
 * <p>
 * The positions are therefore applied after laying the window out at its final size, and re-applied
 * until every divider has reached its position, each pass settling at least the outermost divider
 * that its neighbour was still blocking.
 */
public final class SplitPaneDividers {

    private static final double POSITION_TOLERANCE = 0.01;

    private SplitPaneDividers() {
    }

    /**
     * Sets the dividers at the given indexes to the given positions (0-1). Indexes that do not
     * exist are ignored.
     */
    public static void apply(SplitPane splitPane, Map<Integer, Double> positions) {
        int dividerCount = splitPane.getDividers().size();
        for (int pass = 0; pass < Math.max(1, dividerCount); pass++) {
            layoutWindow(splitPane);
            positions.forEach((index, position) -> {
                if (index < dividerCount) {
                    splitPane.setDividerPosition(index, position);
                }
            });
            layoutWindow(splitPane);
            if (allApplied(splitPane, positions)) {
                return;
            }
        }
    }

    /**
     * Lays out the whole window the split pane is in (or just the split pane if it is not in a
     * scene yet), so its size and dividers reflect all pending changes.
     */
    public static void layoutWindow(SplitPane splitPane) {
        Parent root = splitPane.getScene() != null ? splitPane.getScene().getRoot() : splitPane;
        root.applyCss();
        root.layout();
    }

    private static boolean allApplied(SplitPane splitPane, Map<Integer, Double> positions) {
        return positions.entrySet().stream()
                .filter(entry -> entry.getKey() < splitPane.getDividers().size())
                .allMatch(entry -> Math.abs(splitPane.getDividers().get(entry.getKey()).getPosition() - entry.getValue()) < POSITION_TOLERANCE);
    }
}
