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

import com.willwinder.universalgcodesender.fx.component.visualizer.scene.Bounds3;

import java.util.List;
import java.util.Optional;

/**
 * Where the virtual block of material sits and how finely it is sampled.
 *
 * <p>When a program comes without stock information the block is derived from the program itself
 * by {@link #fromCuts(List, ToolResolver)}: the XY extent of the cutting moves inflated by the
 * largest tool radius and a margin, a top at Z zero when every cut is at or below zero and at the
 * highest cut otherwise, and a bottom at the deepest cut, on the assumption that the deepest cut is
 * meant to go through the material. Vertical moves are left out when finding
 * the top, since a plunge at feed rate starts at the retract height rather than on the stock.
 */
public record StockDefinition(Bounds3 bounds, double cellSize) {
    /**
     * Upper bound on nodes in the height field, which bounds both memory and the time a sweep
     * takes.
     */
    public static final int MAX_NODES = Math.powExact(2048, 2);
    /**
     * A tool footprint is sampled with at least this many nodes across its radius.
     */
    public static final double NODES_PER_RADIUS = 64;
    public static final double MIN_CELL_SIZE = 0.01;
    public static final double MARGIN = 2;
    public static final double MIN_THICKNESS = 1;
    private static final double EPSILON = 1e-6;

    public HeightField createHeightField() {
        return new HeightField(bounds, cellSize);
    }

    /**
     * The block for a program: derived from its cutting moves, or the block the user gave, sampled
     * finely enough for the smallest tool. Empty when there is nothing to cut.
     */
    public static Optional<StockDefinition> resolve(StockSpec spec, List<CutSegment> cuts, ToolResolver tools) {
        if (cuts.isEmpty()) {
            return Optional.empty();
        }
        if (spec.mode() == StockSpec.Mode.MANUAL && spec.manualBounds() != null) {
            Bounds3 bounds = spec.manualBounds();
            if (bounds.width() <= 0 || bounds.height() <= 0 || bounds.depth() <= 0) {
                return Optional.empty();
            }
            return Optional.of(new StockDefinition(bounds, cellSizeFor(bounds, smallestRadius(cuts, tools))));
        }
        return fromCuts(cuts, tools);
    }

    /**
     * Derives the stock from the cutting moves of a program. Empty when there is nothing to cut.
     */
    public static Optional<StockDefinition> fromCuts(List<CutSegment> cuts, ToolResolver tools) {
        if (cuts.isEmpty()) {
            return Optional.empty();
        }

        Bounds3 cutBounds = null;
        double highestLateralCut = Double.NEGATIVE_INFINITY;
        double minRadius = Double.POSITIVE_INFINITY;
        double maxRadius = 0;
        for (CutSegment cut : cuts) {
            Bounds3 segment = Bounds3.ofPoint(cut.x0(), cut.y0(), cut.z0()).include(cut.x1(), cut.y1(), cut.z1());
            cutBounds = cutBounds == null ? segment : cutBounds.union(segment);
            if (!cut.isVertical()) {
                highestLateralCut = Math.max(highestLateralCut, cut.maxZ());
            }
            ToolProfile profile = tools.resolve(cut);
            minRadius = Math.min(minRadius, profile.radius());
            maxRadius = Math.max(maxRadius, profile.radius());
        }

        // A program of nothing but plunges, such as drilling, has to make do with the plunge heights
        double highestCut = highestLateralCut == Double.NEGATIVE_INFINITY ? cutBounds.maxZ() : highestLateralCut;
        double top = highestCut <= EPSILON ? 0 : highestCut;
        double bottom = Math.min(cutBounds.minZ(), top - MIN_THICKNESS);

        double inflate = maxRadius + MARGIN;
        Bounds3 bounds = new Bounds3(
                cutBounds.minX() - inflate, cutBounds.minY() - inflate, bottom,
                cutBounds.maxX() + inflate, cutBounds.maxY() + inflate, top);
        return Optional.of(new StockDefinition(bounds, cellSizeFor(bounds, minRadius)));
    }


    private static double smallestRadius(List<CutSegment> cuts, ToolResolver tools) {
        double minRadius = Double.POSITIVE_INFINITY;
        for (CutSegment cut : cuts) {
            minRadius = Math.min(minRadius, tools.resolve(cut).radius());
        }
        return minRadius;
    }

    /**
     * Fine enough to sample the smallest tool, but never so fine that the grid exceeds
     * {@link #MAX_NODES}.
     */
    public static double cellSizeFor(Bounds3 bounds, double smallestRadius) {
        double forTool = smallestRadius / NODES_PER_RADIUS;
        double forBudget = Math.sqrt(bounds.width() * bounds.height() / MAX_NODES);
        return Math.max(MIN_CELL_SIZE, Math.max(forTool, forBudget));
    }
}
