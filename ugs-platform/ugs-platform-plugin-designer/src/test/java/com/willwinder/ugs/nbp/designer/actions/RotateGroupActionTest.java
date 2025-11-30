/*
    Copyright 2024 Will Winder

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
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for RotateGroupAction
 *
 * @author giro-dev
 */
public class RotateGroupActionTest {

    @Test
    public void testAbsoluteRotation() {
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        rect.setSize(new Size(100, 50));
        group.addChild(rect);

        RotateGroupAction action = RotateGroupAction.rotateTo(group, 45.0);
        action.redo();

        assertEquals(45.0, group.getRotation(), 0.001);
    }

    @Test
    public void testRelativeRotation() {
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        rect.setSize(new Size(100, 50));
        group.addChild(rect);

        group.setRotation(30.0);

        RotateGroupAction action = RotateGroupAction.rotateBy(group, 15.0);
        action.redo();

        assertEquals(45.0, group.getRotation(), 0.001);
    }

    @Test
    public void testNegativeRotation() {
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        group.addChild(rect);

        RotateGroupAction action = RotateGroupAction.rotateTo(group, -45.0);
        action.redo();

        // Should normalize to 315 degrees
        assertEquals(315.0, group.getRotation(), 0.001);
    }

    @Test
    public void testRotationOver360() {
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        group.addChild(rect);

        RotateGroupAction action = RotateGroupAction.rotateTo(group, 405.0);
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

        RotateGroupAction action = RotateGroupAction.rotateTo(group, 90.0);
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

        RotateGroupAction action = RotateGroupAction.rotateTo(group, 45.0);
        action.redo();
        action.undo();
        action.redo();

        assertEquals(45.0, group.getRotation(), 0.001);
    }

    @Test
    public void testGetRotationDelta() {
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        group.addChild(rect);

        group.setRotation(30.0);

        RotateGroupAction action = RotateGroupAction.rotateTo(group, 75.0);

        assertEquals(45.0, action.getRotationDelta(), 0.001);
    }

    @Test
    public void testToString() {
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        group.addChild(rect);

        group.setRotation(0.0);

        RotateGroupAction action = RotateGroupAction.rotateBy(group, 45.0);

        assertTrue(action.toString().contains("45"));
        assertTrue(action.toString().contains("Rotate group"));
    }

    @Test
    public void testFullCircleRotation() {
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        group.addChild(rect);

        group.setRotation(30.0);

        RotateGroupAction action = RotateGroupAction.rotateBy(group, 360.0);
        action.redo();

        // Should return to original (normalized)
        assertEquals(30.0, group.getRotation(), 0.001);
    }

    @Test
    public void testMultipleRotations() {
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        group.addChild(rect);

        // Rotate 45 degrees three times
        RotateGroupAction action1 = RotateGroupAction.rotateBy(group, 45.0);
        action1.redo();
        assertEquals(45.0, group.getRotation(), 0.001);

        RotateGroupAction action2 = RotateGroupAction.rotateBy(group, 45.0);
        action2.redo();
        assertEquals(90.0, group.getRotation(), 0.001);

        RotateGroupAction action3 = RotateGroupAction.rotateBy(group, 45.0);
        action3.redo();
        assertEquals(135.0, group.getRotation(), 0.001);

        // Undo all
        action3.undo();
        assertEquals(90.0, group.getRotation(), 0.001);
        action2.undo();
        assertEquals(45.0, group.getRotation(), 0.001);
        action1.undo();
        assertEquals(0.0, group.getRotation(), 0.001);
    }
}

