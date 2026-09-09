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

/**
 * Decides which tool profile is cutting a segment. Implementations are called once per segment
 * and are expected to cache whatever lookups they do.
 */
@FunctionalInterface
public interface ToolResolver {
    /**
     * The tool for a segment together with how it was decided.
     */
    ResolvedTool describe(CutSegment cut);

    default ToolProfile resolve(CutSegment cut) {
        return describe(cut).profile();
    }

    /**
     * A resolver that uses the same tool for every segment.
     */
    static ToolResolver fixed(ToolProfile profile) {
        ResolvedTool tool = ResolvedTool.of(profile, ResolvedTool.Source.FIXED, profile.toString());
        return cut -> tool;
    }
}
