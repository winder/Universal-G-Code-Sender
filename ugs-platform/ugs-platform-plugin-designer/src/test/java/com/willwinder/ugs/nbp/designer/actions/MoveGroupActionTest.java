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

import com.willwinder.ugs.nbp.designer.entities.Anchor;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Group;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.nbp.designer.model.Size;
import org.junit.Test;

import java.awt.geom.Point2D;

import static org.junit.Assert.assertEquals;

/**
 * Unit tests for MoveGroupAction
 *
 * @author giro-dev
 */
public class MoveGroupActionTest {

    @Test
    public void testMoveX() {
        Group group = new Group();
        Rectangle rect = new Rectangle(10, 20);
        rect.setSize(new Size(100, 50));
        group.addChild(rect);

        Point2D original = group.getPosition(Anchor.CENTER);

        MoveGroupAction action = MoveGroupAction.moveX(group, Anchor.CENTER, 100.0);
        action.redo();

        Point2D newPos = group.getPosition(Anchor.CENTER);
        assertEquals(100.0, newPos.getX(), 0.001);
        assertEquals(original.getY(), newPos.getY(), 0.001);  // Y unchanged
    }

    @Test
    public void testMoveY() {
        Group group = new Group();
        Rectangle rect = new Rectangle(10, 20);
        rect.setSize(new Size(100, 50));
        group.addChild(rect);

        Point2D original = group.getPosition(Anchor.CENTER);

        MoveGroupAction action = MoveGroupAction.moveY(group, Anchor.CENTER, 200.0);
        action.redo();

        Point2D newPos = group.getPosition(Anchor.CENTER);
        assertEquals(original.getX(), newPos.getX(), 0.001);  // X unchanged
        assertEquals(200.0, newPos.getY(), 0.001);
    }

    @Test
    public void testMoveTo() {
        Group group = new Group();
        Rectangle rect = new Rectangle(10, 20);
        rect.setSize(new Size(100, 50));
        group.addChild(rect);

        Point2D newPosition = new Point2D.Double(150, 250);

        MoveGroupAction action = MoveGroupAction.moveTo(group, Anchor.CENTER, newPosition);
        action.redo();

        Point2D resultPos = group.getPosition(Anchor.CENTER);
        assertEquals(150.0, resultPos.getX(), 0.001);
        assertEquals(250.0, resultPos.getY(), 0.001);
    }

    @Test
    public void testUndo() {
        Group group = new Group();
        Rectangle rect = new Rectangle(10, 20);
        rect.setSize(new Size(100, 50));
        group.addChild(rect);

        Point2D original = group.getPosition(Anchor.CENTER);

        MoveGroupAction action = MoveGroupAction.moveX(group, Anchor.CENTER, 100.0);
        action.redo();
        action.undo();

        Point2D restored = group.getPosition(Anchor.CENTER);
        assertEquals(original.getX(), restored.getX(), 0.001);
        assertEquals(original.getY(), restored.getY(), 0.001);
    }

    @Test
    public void testToString() {
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        group.addChild(rect);

        MoveGroupAction xAction = MoveGroupAction.moveX(group, Anchor.CENTER, 50);
        assertEquals("Change group X position", xAction.toString());

        MoveGroupAction yAction = MoveGroupAction.moveY(group, Anchor.CENTER, 50);
        assertEquals("Change group Y position", yAction.toString());

        MoveGroupAction bothAction = MoveGroupAction.moveTo(group, Anchor.CENTER, new Point2D.Double(50, 50));
        assertEquals("Move group", bothAction.toString());
    }
}

