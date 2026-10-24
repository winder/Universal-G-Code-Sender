package com.willwinder.ugs.designer.actions;

import com.willwinder.ugs.designer.entities.cuttable.Rectangle;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.util.List;

public class NestActionTest {

    @Test
    public void redo_shouldPackEntitiesFromTheirBottomLeftCornerAndBeUndoable() {
        Rectangle first = new Rectangle(10, 10, 10, 10);
        Rectangle second = new Rectangle(100, 100, 10, 10);

        NestAction action = new NestAction(List.of(first, second), 100, 5, false);
        action.redo();

        assertEquals(new Point2D.Double(10, 10), first.getPosition());
        assertEquals(new Point2D.Double(25, 10), second.getPosition());
        assertTrue(action.fitsWithin(25, 10));
        assertFalse(action.fitsWithin(24, 10));

        action.undo();

        assertEquals(new Point2D.Double(10, 10), first.getPosition());
        assertEquals(new Point2D.Double(100, 100), second.getPosition());
    }

    @Test
    public void redo_shouldRotateEntitiesWhenAllowedAndRevertOnUndo() {
        Rectangle square = new Rectangle(0, 0, 10, 10);
        Rectangle tall = new Rectangle(50, 50, 5, 30);

        NestAction action = new NestAction(List.of(square, tall), 40, 0, true);
        action.redo();

        assertEquals(0, tall.getBounds().getX(), 0.01);
        assertEquals(0, tall.getBounds().getY(), 0.01);
        assertEquals(30, tall.getBounds().getWidth(), 0.01);
        assertEquals(5, tall.getBounds().getHeight(), 0.01);
        assertEquals(30, square.getBounds().getX(), 0.01);

        action.undo();

        assertEquals(50, tall.getBounds().getX(), 0.01);
        assertEquals(50, tall.getBounds().getY(), 0.01);
        assertEquals(5, tall.getBounds().getWidth(), 0.01);
        assertEquals(30, tall.getBounds().getHeight(), 0.01);
    }
}
