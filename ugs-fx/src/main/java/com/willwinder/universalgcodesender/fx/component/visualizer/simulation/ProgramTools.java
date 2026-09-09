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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * The distinct tools a program is simulated with, in the order they are first used.
 */
public final class ProgramTools {
    private ProgramTools() {
    }

    /**
     * @param tool         the resolved tool
     * @param firstCommand the command number of the first cut made with it
     * @param cuts         how many cutting segments use it
     */
    public record ProgramTool(ResolvedTool tool, int firstCommand, int cuts) {
    }

    public static List<ProgramTool> collect(List<CutSegment> cuts, ToolResolver resolver) {
        Map<String, ProgramTool> byLabel = new LinkedHashMap<>();
        for (CutSegment cut : cuts) {
            ResolvedTool tool = resolver.describe(cut);
            // A fallback used for two different requested numbers is listed twice, once per number to assign
            String key = tool.source() + "|" + tool.label() + "|" + tool.requestedToolNumber();
            ProgramTool existing = byLabel.get(key);
            if (existing == null) {
                byLabel.put(key, new ProgramTool(tool, cut.commandNumber(), 1));
            } else {
                byLabel.put(key, new ProgramTool(existing.tool(), existing.firstCommand(), existing.cuts() + 1));
            }
        }
        return new ArrayList<>(byLabel.values());
    }
}
