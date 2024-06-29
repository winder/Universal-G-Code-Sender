package com.willwinder.ugs.nbp.designer.entities.cuttable;

import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import com.willwinder.ugs.nbp.designer.entities.EventType;
import com.willwinder.ugs.nbp.designer.model.Size;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicBoolean;

public class RectangleTest {

    @Test
    public void setSize() {
        Rectangle rectangle = new Rectangle(1, 1);
        rectangle.setSize(new Size(30, 30));
        assertEquals(30, rectangle.getSize().getWidth(), 0.1);
        assertEquals(30, rectangle.getSize().getHeight(), 0.1);
    }

    @Test
    public void setRotation() {
        Rectangle rectangle = new Rectangle(1, 1);
        assertEquals(0, rectangle.getRotation(), 0.1);
        rectangle.setRotation(10);
        assertEquals(10, rectangle.getRotation(), 0.1);
    }

    @Test
    public void setRotationShouldDispatchRotationEvent() {
        AtomicBoolean triggeredEvent = new AtomicBoolean(false);
        Rectangle rectangle = new Rectangle(1, 1);
        rectangle.addListener(entityEvent -> {
            if (entityEvent.getType() == EventType.ROTATED) {
                triggeredEvent.set(true);
            }
        });
        rectangle.setRotation(10);
        assertTrue(triggeredEvent.get());
    }

    @Test
    public void getSettings() {
        Rectangle rectangle = new Rectangle(1, 1);
        assertTrue(rectangle.getSettings().contains(EntitySetting.ANCHOR));
        assertTrue(rectangle.getSettings().contains(EntitySetting.WIDTH));
        assertTrue(rectangle.getSettings().contains(EntitySetting.HEIGHT));
        assertTrue(rectangle.getSettings().contains(EntitySetting.POSITION_X));
        assertTrue(rectangle.getSettings().contains(EntitySetting.POSITION_Y));
        assertTrue(rectangle.getSettings().contains(EntitySetting.ROTATION));
    }
}