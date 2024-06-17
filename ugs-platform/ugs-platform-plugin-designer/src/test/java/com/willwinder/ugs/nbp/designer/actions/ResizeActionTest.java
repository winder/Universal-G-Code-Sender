package com.willwinder.ugs.nbp.designer.actions;

import com.willwinder.ugs.nbp.designer.entities.Anchor;
import com.willwinder.ugs.nbp.designer.entities.Entity;
import com.willwinder.ugs.nbp.designer.entities.cuttable.Rectangle;
import com.willwinder.ugs.nbp.designer.model.Size;
import static org.junit.Assert.assertEquals;
import org.junit.Test;

import java.awt.geom.Point2D;
import java.util.List;

public class ResizeActionTest {

    @Test
    public void resizeWithTopLeftAnchor() {
        Entity entity = new Rectangle(10, 10);
        entity.setSize(new Size(10, 10));

        ResizeAction resizeAction = new ResizeAction(List.of(entity), Anchor.TOP_LEFT, new Size(10, 10), new Size(20, 20));
        resizeAction.redo();

        assertEquals(new Size(20, 20), entity.getSize());
        assertEquals(new Point2D.Double(10, 0), entity.getPosition());
    }

    @Test
    public void resizeWithBottomLeftAnchor() {
        Entity entity = new Rectangle(10, 10);
        entity.setSize(new Size(10, 10));

        ResizeAction resizeAction = new ResizeAction(List.of(entity), Anchor.BOTTOM_LEFT, new Size(10, 10), new Size(20, 20));
        resizeAction.redo();

        assertEquals(new Size(20, 20), entity.getSize());
        assertEquals(new Point2D.Double(10, 10), entity.getPosition());
    }

    @Test
    public void resizeWithBottomRightAnchor() {
        Entity entity = new Rectangle(10, 10);
        entity.setSize(new Size(10, 10));

        ResizeAction resizeAction = new ResizeAction(List.of(entity), Anchor.BOTTOM_RIGHT, new Size(10, 10), new Size(20, 20));
        resizeAction.redo();

        assertEquals(new Size(20, 20), entity.getSize());
        assertEquals(new Point2D.Double(0, 10), entity.getPosition());
    }

    @Test
    public void resizeWithTopRightAnchor() {
        Entity entity = new Rectangle(10, 10);
        entity.setSize(new Size(10, 10));

        ResizeAction resizeAction = new ResizeAction(List.of(entity), Anchor.TOP_RIGHT, new Size(10, 10), new Size(20, 20));
        resizeAction.redo();

        assertEquals(new Size(20, 20), entity.getSize());
        assertEquals(new Point2D.Double(0, 0), entity.getPosition());
    }

    @Test
    public void resizeWithTopCenterAnchor() {
        Entity entity = new Rectangle(10, 10);
        entity.setSize(new Size(10, 10));

        ResizeAction resizeAction = new ResizeAction(List.of(entity), Anchor.TOP_CENTER, new Size(10, 10), new Size(20, 20));
        resizeAction.redo();

        assertEquals(new Size(20, 20), entity.getSize());
        assertEquals(new Point2D.Double(5, 0), entity.getPosition());
    }

    @Test
    public void resizeWithBottomCenterAnchor() {
        Entity entity = new Rectangle(10, 10);
        entity.setSize(new Size(10, 10));

        ResizeAction resizeAction = new ResizeAction(List.of(entity), Anchor.BOTTOM_CENTER, new Size(10, 10), new Size(20, 20));
        resizeAction.redo();

        assertEquals(new Size(20, 20), entity.getSize());
        assertEquals(new Point2D.Double(5, 10), entity.getPosition());
    }

    @Test
    public void resizeWithLeftCenterAnchor() {
        Entity entity = new Rectangle(10, 10);
        entity.setSize(new Size(10, 10));

        ResizeAction resizeAction = new ResizeAction(List.of(entity), Anchor.LEFT_CENTER, new Size(10, 10), new Size(20, 20));
        resizeAction.redo();

        assertEquals(new Size(20, 20), entity.getSize());
        assertEquals(new Point2D.Double(10, 5), entity.getPosition());
    }

    @Test
    public void resizeWithRightCenterAnchor() {
        Entity entity = new Rectangle(10, 10);
        entity.setSize(new Size(10, 10));

        ResizeAction resizeAction = new ResizeAction(List.of(entity), Anchor.RIGHT_CENTER, new Size(10, 10), new Size(20, 20));
        resizeAction.redo();

        assertEquals(new Size(20, 20), entity.getSize());
        assertEquals(new Point2D.Double(0, 5), entity.getPosition());
    }

    @Test
    public void resizeWithCenterAnchor() {
        Entity entity = new Rectangle(10, 10);
        entity.setSize(new Size(10, 10));

        ResizeAction resizeAction = new ResizeAction(List.of(entity), Anchor.CENTER, new Size(10, 10), new Size(20, 20));
        resizeAction.redo();

        assertEquals(new Size(20, 20), entity.getSize());
        assertEquals(new Point2D.Double(5, 5), entity.getPosition());
    }
}