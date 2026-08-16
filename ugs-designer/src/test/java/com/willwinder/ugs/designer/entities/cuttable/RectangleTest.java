package com.willwinder.ugs.designer.entities.cuttable;

import com.willwinder.ugs.designer.entities.EntitySetting;
import com.willwinder.ugs.designer.entities.EventType;
import com.willwinder.ugs.designer.model.Size;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.EnumSet;
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
    public void copy_shouldRetainAllCuttableSettingsFromAnExistingEntity() {
        Rectangle rectangle = new Rectangle(1, 1);
        rectangle.setCutType(CutType.POCKET);
        rectangle.setStartDepth(1.5);
        rectangle.setTargetDepth(4.5);
        rectangle.setSpindleSpeed(80);
        rectangle.setPasses(3);
        rectangle.setFeedRate(1234);
        rectangle.setLeadInPercent(40);
        rectangle.setToolPathAngle(45);
        rectangle.setToolPathDirection(ToolPathDirection.VERTICAL);
        rectangle.setDirection(Direction.CONVENTIONAL);
        rectangle.setPlungeType(PlungeType.STRAIGHT);
        rectangle.setFinishingPass(true);
        rectangle.setStockToLeave(0.3);
        rectangle.setIncludeInExport(false);
        rectangle.setHidden(true);
        EnumSet<EntitySetting> settings = rectangle.getSettings();

        Cuttable copy = (Cuttable) rectangle.copy();

        assertEquals(CutType.POCKET, copy.getCutType());
        assertEquals(1.5, copy.getStartDepth(), 0.1);
        assertEquals(4.5, copy.getTargetDepth(), 0.1);
        assertEquals(80, copy.getSpindleSpeed());
        assertEquals(3, copy.getPasses());
        assertEquals(1234, copy.getFeedRate());
        assertEquals(40, copy.getLeadInPercent());
        assertEquals(45, copy.getToolPathAngle(), 0.1);
        assertEquals(ToolPathDirection.VERTICAL, copy.getToolPathDirection());
        assertEquals(Direction.CONVENTIONAL, copy.getDirection());
        assertEquals(PlungeType.STRAIGHT, copy.getPlungeType());
        assertTrue(copy.isFinishingPass());
        assertEquals(0.3, copy.getStockToLeave(), 0.1);
        assertFalse(copy.getIncludeInExport());
        assertTrue(copy.isHidden());
        assertEquals(settings, copy.getSettings());
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