/*
    Copyright 2025 Albert Giró Quer

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
package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.nbp.designer.model.Size;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

/**
 * Unit tests for RotateAction
 *
 * @author giro-dev
 */
public class RotateActionTest {

    @Test
    public void testAbsoluteRotation() {
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        rect.setSize(new Size(100, 50));
        group.addChild(rect);

        RotateAction action = RotateAction.rotateTo(group, 45.0);
        action.redo();

        assertEquals(45.0, group.getRotation(), 0.001);
    }

    @Test
    public void testNegativeRotation() {
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        group.addChild(rect);

        RotateAction action = RotateAction.rotateTo(group, -45.0);
        action.redo();

        // Should normalize to 315 degrees
        assertEquals(315.0, group.getRotation(), 0.001);
    }

    @Test
    public void testRotationOver360() {
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        group.addChild(rect);

        RotateAction action = RotateAction.rotateTo(group, 405.0);
        action.redo();

        // Should normalize to 45 degrees
        assertEquals(45.0, group.getRotation(), 0.001);
    }

    @Test
    public void testUndo() {
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        group.addChild(rect);

        group.setRotation(30.0);
        double original = group.getRotation();

        RotateAction action = RotateAction.rotateTo(group, 90.0);
        action.redo();
        assertEquals(90.0, group.getRotation(), 0.001);

        action.undo();
        assertEquals(original, group.getRotation(), 0.001);
    }

    @Test
    public void testRedoAfterUndo() {
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        group.addChild(rect);

        RotateAction action = RotateAction.rotateTo(group, 45.0);
        action.redo();
        action.undo();
        action.redo();

        assertEquals(45.0, group.getRotation(), 0.001);
    }
}

