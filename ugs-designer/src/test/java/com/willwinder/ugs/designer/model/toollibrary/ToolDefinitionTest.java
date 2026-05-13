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

import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

public class ToolDefinitionTest {

    @Test
    public void defaultConstructorProducesUuidAndSaneDefaults() {
        ToolDefinition tool = new ToolDefinition();
        assertNotNull(tool.getId());
        assertEquals(EndmillShape.UPCUT, tool.getShape());
        assertEquals(UnitUtils.Units.MM, tool.getDiameterUnit());
        assertEquals("M3", tool.getSpindleDirection());
        assertFalse(tool.isBuiltIn());
        assertFalse(tool.isCustomSentinel());
    }

    @Test
    public void copyConstructorIsDeep() {
        ToolDefinition original = new ToolDefinition();
        original.setName("1/4\" Upcut");
        original.setDiameter(0.25);
        original.setDiameterUnit(UnitUtils.Units.INCH);
        original.setVBitAngleDegrees(60.0);

        ToolDefinition copy = new ToolDefinition(original);
        copy.setName("changed");
        copy.setDiameter(99);
        assertEquals("1/4\" Upcut", original.getName());
        assertEquals(0.25, original.getDiameter(), 1e-9);
    }

    @Test
    public void diameterConversionToMmForInchTool() {
        ToolDefinition quarter = quarterInchUpcut();
        assertEquals(6.35, quarter.getDiameterInMm(), 1e-6);
    }

    @Test
    public void diameterConversionToMmForMmTool() {
        ToolDefinition mmTool = new ToolDefinition();
        mmTool.setDiameter(3.0);
        mmTool.setDiameterUnit(UnitUtils.Units.MM);
        assertEquals(3.0, mmTool.getDiameterInMm(), 1e-9);
    }

    @Test
    public void applyToSettingsConvertsDiameterToMm() {
        Settings base = new Settings();
        base.setSafeHeight(7.5);
        ToolDefinition quarter = quarterInchUpcut();
        Settings applied = quarter.applyToSettings(base);
        assertEquals(6.35, applied.getToolDiameter(), 1e-6);
        assertEquals("Safe height preserved", 7.5, applied.getSafeHeight(), 1e-9);
        assertEquals(quarter.getId(), applied.getCurrentToolId());
        assertNotNull(applied.getCurrentToolSnapshot());
    }

    @Test
    public void matchesValuesIgnoresBuiltInFlag() {
        ToolDefinition a = quarterInchUpcut();
        ToolDefinition b = new ToolDefinition(a);
        b.setBuiltIn(!a.isBuiltIn());
        assertTrue(a.matchesValues(b));
    }

    @Test
    public void matchesValuesDetectsFieldDivergence() {
        ToolDefinition a = quarterInchUpcut();
        ToolDefinition b = new ToolDefinition(a);
        b.setFeedSpeed(b.getFeedSpeed() + 100);
        assertFalse(a.matchesValues(b));
    }

    @Test
    public void equalsAndHashCodeUseIdOnly() {
        ToolDefinition a = quarterInchUpcut();
        ToolDefinition b = new ToolDefinition(a);
        b.setName("different name");
        assertEquals(a, b);
        assertEquals(a.hashCode(), b.hashCode());

        ToolDefinition c = new ToolDefinition(a);
        c.setId("other-id");
        assertNotEquals(a, c);
    }

    private ToolDefinition quarterInchUpcut() {
        ToolDefinition t = new ToolDefinition();
        t.setId("test-quarter-upcut");
        t.setName("1/4\" Upcut");
        t.setShape(EndmillShape.UPCUT);
        t.setDiameter(0.25);
        t.setDiameterUnit(UnitUtils.Units.INCH);
        t.setFeedSpeed(900);
        t.setPlungeSpeed(300);
        t.setDepthPerPass(2.0);
        t.setStepOverPercent(0.4);
        t.setMaxSpindleSpeed(18000);
        t.setSpindleDirection("M3");
        return t;
    }
}
