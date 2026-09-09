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
package com.willwinder.universalgcodesender.fx.component.visualizer.simulation;

import java.util.List;

/**
 * Cuts a program into a {@link HeightField}, either all at once with {@link #runAll()} or step
 * by step with {@link #advanceTo(int)} as the controller reports commands finished. Segments are
 * applied in program order and each is applied exactly once, which is all the height field needs
 * since it only ever records the lowest surface.
 *
 * <p>Not thread safe: run it on a single background thread and hand the finished field over.
 */
public final class StockSimulation {
    private final HeightField field;
    private final List<CutSegment> cuts;
    private final ToolResolver tools;
    private int next;

    public StockSimulation(HeightField field, List<CutSegment> cuts, ToolResolver tools) {
        this.field = field;
        this.cuts = cuts;
        this.tools = tools;
    }

    public HeightField field() {
        return field;
    }

    public List<CutSegment> cuts() {
        return cuts;
    }

    public boolean isComplete() {
        return next >= cuts.size();
    }

    /**
     * Number of cutting segments applied so far.
     */
    public int applied() {
        return next;
    }

    public void runAll() {
        while (next < cuts.size()) {
            apply(cuts.get(next++));
        }
    }

    /**
     * Applies every remaining segment produced by a command up to and including
     * {@code commandNumber}.
     *
     * @return true if any segment was applied
     */
    public boolean advanceTo(int commandNumber) {
        int before = next;
        while (next < cuts.size() && cuts.get(next).commandNumber() <= commandNumber) {
            apply(cuts.get(next++));
        }
        return next != before;
    }

    private void apply(CutSegment cut) {
        ToolProfile profile = tools.resolve(cut);
        SegmentSweep.sweep(field, profile, cut.x0(), cut.y0(), cut.z0(), cut.x1(), cut.y1(), cut.z1());
    }
}
