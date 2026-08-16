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
package com.willwinder.ugs.designer.logic;

import com.willwinder.ugs.designer.actions.SimpleUndoManager;
import com.willwinder.ugs.designer.entities.selection.SelectionManager;
import com.willwinder.ugs.designer.model.Design;
import com.willwinder.ugs.designer.model.Settings;
import com.willwinder.ugs.designer.model.toollibrary.ToolDefinition;
import com.willwinder.universalgcodesender.services.LookupService;
import org.junit.Assume;
import org.junit.Before;
import org.junit.Test;

import java.awt.GraphicsEnvironment;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * Opening a design must restore the settings it was saved with, also when they deviate from the
 * tool the design is bound to — a depth per pass tuned for one design used to be replaced by the
 * depth of the selected tool as soon as the design was reopened.
 */
public class ControllerTest {

    private Controller controller;

    @Before
    public void setUp() {
        Assume.assumeFalse(GraphicsEnvironment.isHeadless());
        LookupService.remove(ToolLibraryService.class);
        controller = new Controller(new SelectionManager(), new SimpleUndoManager());
    }

    @Test
    public void setDesign_shouldApplySettingsStoredInDesign() {
        Design design = new Design();
        design.setSettings(createSettings(3.5));

        controller.setDesign(design);

        assertEquals(3.5, controller.getSettings().getDepthPerPass(), 1e-9);
        assertEquals(22.5, controller.getSettings().getStockThickness(), 1e-9);
    }

    @Test
    public void setDesign_shouldKeepStoredSettingsThatDeviateFromTheTool() {
        Design design = new Design();
        design.setSettings(createSettings(3.5));
        design.setToolSnapshot(createTool());

        controller.setDesign(design);

        assertEquals(3.5, controller.getSettings().getDepthPerPass(), 1e-9);
        assertNotNull(controller.getSettings().getCurrentToolSnapshot());
        assertEquals("tool-id", controller.getSettings().getCurrentToolId());
    }

    @Test
    public void setDesign_shouldApplyToolWhenDesignHasNoSettings() {
        controller.getSettings().setDepthPerPass(3.5);
        Design design = new Design();
        design.setToolSnapshot(createTool());

        controller.setDesign(design);

        assertEquals(1.0, controller.getSettings().getDepthPerPass(), 1e-9);
    }

    private static Settings createSettings(double depthPerPass) {
        Settings settings = new Settings();
        settings.setDepthPerPass(depthPerPass);
        settings.setStockThickness(22.5);
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
