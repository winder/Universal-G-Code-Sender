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
package com.willwinder.ugs.designer.io.svg;

import com.willwinder.ugs.designer.entities.Entity;
import com.willwinder.ugs.designer.entities.EntityGroup;
import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.designer.gui.Drawing;
import com.willwinder.ugs.designer.logic.Controller;
import com.willwinder.ugs.designer.model.Design;
import com.willwinder.ugs.designer.model.Size;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

public class SvgWriterTest {

    @Mock
    private Controller controller;

    @Mock
    private Drawing drawing;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    public void write_shouldGenerateSvgWithMillimeterDimensions() {
        EntityGroup root = new EntityGroup();
        Rectangle rectangle = new Rectangle(10, 20);
        rectangle.setSize(new Size(30, 40));
        root.addChild(rectangle);
        when(drawing.getRootEntity()).thenReturn(root);
        when(controller.getDrawing()).thenReturn(drawing);

        String svg = write(controller);

        assertTrue(svg.contains("width=\"30mm\""));
        assertTrue(svg.contains("height=\"40mm\""));
        assertTrue(svg.contains("viewBox=\"0 0 30 40\""));
    }

    @Test
    public void write_shouldBeReadableBySvgReader() {
        EntityGroup root = new EntityGroup();
        Rectangle rectangle = new Rectangle(10, 20);
        rectangle.setSize(new Size(30, 40));
        root.addChild(rectangle);
        when(drawing.getRootEntity()).thenReturn(root);
        when(controller.getDrawing()).thenReturn(drawing);

        String svg = write(controller);
        Design design = new SvgReader()
                .read(new ByteArrayInputStream(svg.getBytes(StandardCharsets.UTF_8)))
                .orElseThrow(() -> new RuntimeException("Could not read the written SVG"));

        Entity entity = design.getEntities().get(0);
        assertEquals(30, entity.getSize().getWidth(), 0.1);
        assertEquals(40, entity.getSize().getHeight(), 0.1);
        assertEquals(0, entity.getPosition().getX(), 0.1);
        assertEquals(0, entity.getPosition().getY(), 0.1);
    }

    private static String write(Controller controller) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        new SvgWriter().write(out, controller);
        return out.toString(StandardCharsets.UTF_8);
    }
}
