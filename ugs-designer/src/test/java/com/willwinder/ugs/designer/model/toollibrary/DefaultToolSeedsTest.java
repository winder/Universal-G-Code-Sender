/*
    Copyright 2026 Damian Nikodem

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
package com.willwinder.ugs.designer.model.toollibrary;

import org.junit.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class DefaultToolSeedsTest {

    @Test
    public void producesExpectedTotalCount() {
        List<ToolDefinition> tools = DefaultToolSeeds.create();
        // 5 non-V shapes × 5 sizes + 3 V angles × 5 sizes + 1 custom = 41
        assertEquals(41, tools.size());
    }

    @Test
    public void allEntriesHaveDistinctStableIds() {
        List<ToolDefinition> tools = DefaultToolSeeds.create();
        Set<String> ids = new HashSet<>();
        for (ToolDefinition t : tools) {
            assertNotNull("every seed should have an id", t.getId());
            assertTrue("ids should be stable built-in slugs",
                    t.getId().startsWith("builtin:"));
            assertTrue("duplicate id: " + t.getId(), ids.add(t.getId()));
        }
    }

    @Test
    public void allEntriesHaveSaneNumericValues() {
        for (ToolDefinition t : DefaultToolSeeds.create()) {
            if (t.isCustomSentinel()) continue;
            assertTrue("feed > 0 for " + t.getName(), t.getFeedSpeed() > 0);
            assertTrue("plunge > 0 for " + t.getName(), t.getPlungeSpeed() > 0);
            assertTrue("depth > 0 for " + t.getName(), t.getDepthPerPass() > 0);
            assertTrue("stepover in (0,1] for " + t.getName(),
                    t.getStepOverPercent() > 0 && t.getStepOverPercent() <= 1);
            assertTrue("diameter > 0 for " + t.getName(), t.getDiameter() > 0);
            assertTrue("max spindle > 0 for " + t.getName(), t.getMaxSpindleSpeed() > 0);
            assertTrue("built-in flag set for " + t.getName(), t.isBuiltIn());
        }
    }

    @Test
    public void vBitsAllCarryAnAngle() {
        long vBitCount = DefaultToolSeeds.create().stream()
                .filter(t -> t.getShape() == EndmillShape.V_BIT)
                .peek(t -> assertNotNull("V-bit " + t.getName() + " missing angle", t.getVBitAngleDegrees()))
                .count();
        assertEquals(15, vBitCount);
    }

    @Test
    public void customSentinelIsPresentAndFlagged() {
        ToolDefinition custom = DefaultToolSeeds.create().stream()
                .filter(ToolDefinition::isCustomSentinel)
                .findFirst()
                .orElseThrow();
        assertEquals(DefaultToolSeeds.CUSTOM_SENTINEL_ID, custom.getId());
        assertEquals(EndmillShape.CUSTOM, custom.getShape());
        assertTrue(custom.isBuiltIn());
    }
}
