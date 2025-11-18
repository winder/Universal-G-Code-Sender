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
import static org.junit.Assert.assertNotEquals;

/**
 * Unit tests for ResizeGroupAction
 *
 * @author giro-dev
 */
public class ResizeGroupActionTest {

    @Test
    public void testResizeWidth_WithLockedRatio() {
        // Create a group with a rectangle (100x50)
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        rect.setSize(new Size(100, 50));
        group.addChild(rect);

        // Store original size
        Size originalSize = group.getSize();
        assertEquals(100.0, originalSize.getWidth(), 0.001);
        assertEquals(50.0, originalSize.getHeight(), 0.001);

        // Resize width to 200 with locked ratio
        ResizeGroupAction action = new ResizeGroupAction(
                group,
                Anchor.CENTER,
                ResizeGroupAction.DimensionType.WIDTH,
                200.0,
                true  // locked ratio
        );

        action.redo();

        // Verify width changed and height scaled proportionally
        Size newSize = group.getSize();
        assertEquals(200.0, newSize.getWidth(), 0.001);
        assertEquals(100.0, newSize.getHeight(), 0.001);  // Should be 50 * 2 = 100
    }

    @Test
    public void testResizeWidth_WithoutLockedRatio() {
        // Create a group with a rectangle (100x50)
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        rect.setSize(new Size(100, 50));
        group.addChild(rect);

        // Resize width to 150 without locked ratio
        ResizeGroupAction action = new ResizeGroupAction(
                group,
                Anchor.CENTER,
                ResizeGroupAction.DimensionType.WIDTH,
                150.0,
                false  // ratio not locked
        );

        action.redo();

        // Verify only width changed
        Size newSize = group.getSize();
        assertEquals(150.0, newSize.getWidth(), 0.001);
        assertEquals(50.0, newSize.getHeight(), 0.001);  // Should remain unchanged
    }

    @Test
    public void testResizeHeight_WithLockedRatio() {
        // Create a group with a rectangle (100x50)
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        rect.setSize(new Size(100, 50));
        group.addChild(rect);

        // Resize height to 100 with locked ratio
        ResizeGroupAction action = new ResizeGroupAction(
                group,
                Anchor.CENTER,
                ResizeGroupAction.DimensionType.HEIGHT,
                100.0,
                true  // locked ratio
        );

        action.redo();

        // Verify height changed and width scaled proportionally
        Size newSize = group.getSize();
        assertEquals(200.0, newSize.getWidth(), 0.001);  // Should be 100 * 2 = 200
        assertEquals(100.0, newSize.getHeight(), 0.001);
    }

    @Test
    public void testResizeHeight_WithoutLockedRatio() {
        // Create a group with a rectangle (100x50)
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        rect.setSize(new Size(100, 50));
        group.addChild(rect);

        // Resize height to 75 without locked ratio
        ResizeGroupAction action = new ResizeGroupAction(
                group,
                Anchor.CENTER,
                ResizeGroupAction.DimensionType.HEIGHT,
                75.0,
                false  // ratio not locked
        );

        action.redo();

        // Verify only height changed
        Size newSize = group.getSize();
        assertEquals(100.0, newSize.getWidth(), 0.001);  // Should remain unchanged
        assertEquals(75.0, newSize.getHeight(), 0.001);
    }

    @Test
    public void testUndo_RestoresOriginalState() {
        // Create a group with a rectangle
        Group group = new Group();
        Rectangle rect = new Rectangle(10, 20);
        rect.setSize(new Size(100, 50));
        group.addChild(rect);

        // Store original state
        Size originalSize = new Size(group.getSize().getWidth(), group.getSize().getHeight());
        Point2D originalPosition = group.getPosition(Anchor.TOP_LEFT);

        // Resize
        ResizeGroupAction action = new ResizeGroupAction(
                group,
                Anchor.TOP_LEFT,
                ResizeGroupAction.DimensionType.WIDTH,
                200.0,
                true
        );

        action.redo();

        // Verify size changed
        assertNotEquals(originalSize.getWidth(), group.getSize().getWidth(), 0.001);

        // Undo
        action.undo();

        // Verify original state restored
        Size restoredSize = group.getSize();
        Point2D restoredPosition = group.getPosition(Anchor.TOP_LEFT);

        assertEquals(originalSize.getWidth(), restoredSize.getWidth(), 0.001);
        assertEquals(originalSize.getHeight(), restoredSize.getHeight(), 0.001);
        assertEquals(originalPosition.getX(), restoredPosition.getX(), 0.001);
        assertEquals(originalPosition.getY(), restoredPosition.getY(), 0.001);
    }

    @Test
    public void testResizeWithDifferentAnchors() {
        // Test with TOP_LEFT anchor
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        rect.setSize(new Size(100, 50));
        group.addChild(rect);

        Point2D originalTopLeft = group.getPosition(Anchor.TOP_LEFT);

        ResizeGroupAction action = new ResizeGroupAction(
                group,
                Anchor.TOP_LEFT,
                ResizeGroupAction.DimensionType.WIDTH,
                200.0,
                false
        );

        action.redo();

        // Verify TOP_LEFT position remains the same
        Point2D newTopLeft = group.getPosition(Anchor.TOP_LEFT);
        assertEquals(originalTopLeft.getX(), newTopLeft.getX(), 0.001);
        assertEquals(originalTopLeft.getY(), newTopLeft.getY(), 0.001);
    }

    @Test
    public void testResizeWithCenterAnchor() {
        // Test with CENTER anchor
        Group group = new Group();
        Rectangle rect = new Rectangle(50, 50);
        rect.setSize(new Size(100, 50));
        group.addChild(rect);

        Point2D originalCenter = group.getPosition(Anchor.CENTER);

        ResizeGroupAction action = new ResizeGroupAction(
                group,
                Anchor.CENTER,
                ResizeGroupAction.DimensionType.WIDTH,
                200.0,
                false
        );

        action.redo();

        // Verify CENTER position remains the same
        Point2D newCenter = group.getPosition(Anchor.CENTER);
        assertEquals(originalCenter.getX(), newCenter.getX(), 0.001);
        assertEquals(originalCenter.getY(), newCenter.getY(), 0.001);
    }

    @Test
    public void testRedoAfterUndo() {
        // Create a group
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        rect.setSize(new Size(100, 50));
        group.addChild(rect);

        ResizeGroupAction action = new ResizeGroupAction(
                group,
                Anchor.CENTER,
                ResizeGroupAction.DimensionType.WIDTH,
                200.0,
                true
        );

        // Execute, undo, then redo
        action.redo();
        Size afterRedo1 = new Size(group.getSize().getWidth(), group.getSize().getHeight());

        action.undo();
        action.redo();

        Size afterRedo2 = group.getSize();

        // Verify same state after redo
        assertEquals(afterRedo1.getWidth(), afterRedo2.getWidth(), 0.001);
        assertEquals(afterRedo1.getHeight(), afterRedo2.getHeight(), 0.001);
    }

    @Test
    public void testToString() {
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        rect.setSize(new Size(100, 50));
        group.addChild(rect);

        ResizeGroupAction widthAction = new ResizeGroupAction(
                group, Anchor.CENTER, ResizeGroupAction.DimensionType.WIDTH, 200.0, false
        );
        assertEquals("Change group width", widthAction.toString());

        ResizeGroupAction heightAction = new ResizeGroupAction(
                group, Anchor.CENTER, ResizeGroupAction.DimensionType.HEIGHT, 100.0, false
        );
        assertEquals("Change group height", heightAction.toString());
    }

    @Test
    public void testResizeWithZeroDimension() {
        // Edge case: what happens with zero dimensions
        Group group = new Group();
        Rectangle rect = new Rectangle(0, 0);
        rect.setSize(new Size(100, 50));
        group.addChild(rect);

        ResizeGroupAction action = new ResizeGroupAction(
                group,
                Anchor.CENTER,
                ResizeGroupAction.DimensionType.WIDTH,
                0.0,
                true  // locked ratio
        );

        action.redo();

        Size newSize = group.getSize();
        assertEquals(0.0, newSize.getWidth(), 0.001);
        // Height should also be 0 when width is 0 with locked ratio
        assertEquals(0.0, newSize.getHeight(), 0.001);
    }
}

