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
package com.willwinder.ugs.designer.io.ugsd;

import com.willwinder.ugs.designer.entities.EntityGroup;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.gui.Drawing;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.model.CoolantMode;
import com.willwinder.ugs.designer.model.Design;
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.ugs.designer.model.toollibrary.EndmillShape;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import com.willwinder.universalgcodesender.model.UnitUtils;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * The settings of a design must survive a save and reopen. They used to be stored in the
 * application preferences only, which meant that a design opened on another machine, or after the
 * preferences had been changed, silently cut with different values than it was designed with.
 */
public class UgsDesignWriterTest {

    @Mock
    private Controller controller;

    @Mock
    private Drawing drawing;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void write_shouldPersistAllSettings() {
        Settings settings = createModifiedSettings();

        Settings writtenSettings = writeAndRead(settings).getSettings();

        assertNotNull(writtenSettings);
        assertEquals(1234, writtenSettings.getFeedSpeed());
        assertEquals(321, writtenSettings.getPlungeSpeed());
        assertEquals(6.5, writtenSettings.getToolDiameter(), 0.1);
        assertEquals(EndmillShape.V_BIT, writtenSettings.getToolShape());
        assertEquals(22.5, writtenSettings.getStockThickness(), 0.1);
        assertEquals(12.5, writtenSettings.getSafeHeight(), 0.1);
        assertEquals(UnitUtils.Units.INCH, writtenSettings.getPreferredUnits());
        assertEquals(0.75, writtenSettings.getToolStepOver(), 0.1);
        assertEquals(90, writtenSettings.getVBitAngle(), 0.1);
        assertEquals(3.5, writtenSettings.getDepthPerPass(), 0.1);
        assertEquals(0.35, writtenSettings.getLaserDiameter(), 0.1);
        assertEquals(24000, writtenSettings.getMaxSpindleSpeed());
        assertFalse(writtenSettings.getDetectMaxSpindleSpeed());
        assertEquals("M4", writtenSettings.getSpindleDirection());
        assertEquals(CoolantMode.MIST, writtenSettings.getCoolantMode());
        assertEquals(0.05, writtenSettings.getFlatnessPrecision(), 0.01);
        assertFalse(writtenSettings.getArcFitting());
        assertTrue(writtenSettings.getUseToolChanges());
        assertEquals(4, writtenSettings.getToolNumber());
        assertEquals("tool-id", writtenSettings.getCurrentToolId());
    }

    @Test
    public void write_shouldPersistDepthPerPassDeviatingFromSelectedTool() {
        Settings settings = new Settings();
        settings.setCurrentToolSnapshot(createTool());
        settings.setCurrentToolId("tool-id");
        settings.setDepthPerPass(3.5);

        Design design = writeAndRead(settings);

        assertEquals("The edited depth must be stored, not the depth of the selected tool",
                3.5, design.getSettings().getDepthPerPass(), 0.1);
        assertEquals(1.0, design.getToolSnapshot().getDepthPerPass(), 0.1);
    }

    private Design writeAndRead(Settings settings) {
        when(controller.getSettings()).thenReturn(settings);
        when(controller.getDrawing()).thenReturn(drawing);
        EntityGroup rootEntity = new EntityGroup();
        rootEntity.addChild(new Rectangle());
        when(drawing.getRootEntity()).thenReturn(rootEntity);

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        new UgsDesignWriter().write(outputStream, controller);

        return new UgsDesignReader()
                .read(new ByteArrayInputStream(outputStream.toByteArray()))
                .orElseThrow();
    }

    private static Settings createModifiedSettings() {
        Settings settings = new Settings();
        settings.setFeedSpeed(1234);
        settings.setPlungeSpeed(321);
        settings.setToolDiameter(6.5);
        settings.setToolShape(EndmillShape.V_BIT);
        settings.setStockThickness(22.5);
        settings.setSafeHeight(12.5);
        settings.setPreferredUnits(UnitUtils.Units.INCH);
        settings.setToolStepOver(0.75);
        settings.setVBitAngle(90);
        settings.setDepthPerPass(3.5);
        settings.setLaserDiameter(0.35);
        settings.setMaxSpindleSpeed(24000);
        settings.setDetectMaxSpindleSpeed(false);
        settings.setSpindleDirection("M4");
        settings.setCoolantMode(CoolantMode.MIST);
        settings.setFlatnessPrecision(0.05);
        settings.setArcFitting(false);
        settings.setUseToolChanges(true);
        settings.setToolNumber(4);
        settings.setCurrentToolId("tool-id");
        return settings;
    }

    private static ToolDefinition createTool() {
        ToolDefinition tool = new ToolDefinition();
        tool.setId("tool-id");
        tool.setName("3mm endmill");
        tool.setDiameter(3);
        tool.setFeedSpeed(1000);
        tool.setPlungeSpeed(400);
        tool.setDepthPerPass(1);
        tool.setStepOverPercent(0.3);
        tool.setMaxSpindleSpeed(255);
        return tool;
    }
}
