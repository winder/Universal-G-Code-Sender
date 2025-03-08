package com.willwinder.ugs.nbp.designer.entities.cuttable;

import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

import java.util.List;

public class GroupTest {
    @Test
    public void setLaserPowerShoouldSetTheLaserPowerOnAllChildren() {
        Rectangle rectangle = new Rectangle(1, 1);

        Group group = new Group();
        group.addChild(rectangle);
        group.setSpindleSpeed(10);

        assertEquals(10, rectangle.getSpindleSpeed(), 0.1);
    }

    @Test
    public void getLaserPowerShouldGetTheHighestValue() {
        Rectangle rectangle1 = new Rectangle(1, 1);
        rectangle1.setSpindleSpeed(11);

        Rectangle rectangle2 = new Rectangle(1, 1);
        rectangle2.setSpindleSpeed(10);

        Group group = new Group();
        group.addChild(rectangle1);
        group.addChild(rectangle2);

        assertEquals(11, group.getSpindleSpeed(), 0.1);
    }

    @Test
    public void getSettingsShouldReturnACombinedListOfSettings() {
        Point point1 = new Point();
        Point point2 = new Point();

        Group group = new Group();
        assertEquals(List.of(), group.getSettings());

        group.addChild(point1);
        assertEquals(List.of(EntitySetting.POSITION_X, EntitySetting.POSITION_Y, EntitySetting.CUT_TYPE, EntitySetting.SPINDLE_SPEED, EntitySetting.START_DEPTH, EntitySetting.TARGET_DEPTH), group.getSettings());

        group.addChild(point2);
        assertEquals(List.of(EntitySetting.POSITION_X, EntitySetting.POSITION_Y, EntitySetting.CUT_TYPE, EntitySetting.SPINDLE_SPEED, EntitySetting.START_DEPTH, EntitySetting.TARGET_DEPTH), group.getSettings());

        Rectangle rectangle = new Rectangle();
        group.addChild(rectangle);
        assertEquals(List.of(EntitySetting.POSITION_X, EntitySetting.POSITION_Y, EntitySetting.CUT_TYPE, EntitySetting.SPINDLE_SPEED, EntitySetting.START_DEPTH, EntitySetting.TARGET_DEPTH), group.getSettings());
    }

    @Test
    public void getSettingsReturnCutTypeIfTheyAreTheSame() {
        Group group = new Group();

        Rectangle rectangle1 = new Rectangle();
        rectangle1.setCutType(CutType.LASER_FILL);
        group.addChild(rectangle1);

        Rectangle rectangle2 = new Rectangle();
        rectangle2.setCutType(CutType.LASER_FILL);
        group.addChild(rectangle2);

        assertTrue(group.getSettings().contains(EntitySetting.CUT_TYPE));

        rectangle2.setCutType(CutType.ON_PATH);
        assertFalse(group.getSettings().contains(EntitySetting.CUT_TYPE));
    }

    @Test
    public void getSettingsReturnStartDepthTypeIfTheyAreTheSame() {
        Group group = new Group();

        Rectangle rectangle1 = new Rectangle();
        rectangle1.setStartDepth(10.1);
        group.addChild(rectangle1);

        Rectangle rectangle2 = new Rectangle();
        rectangle2.setStartDepth(10.1);
        group.addChild(rectangle2);

        assertTrue(group.getSettings().contains(EntitySetting.START_DEPTH));

        rectangle2.setStartDepth(10.2);
        assertFalse(group.getSettings().contains(EntitySetting.START_DEPTH));
    }

    @Test
    public void getSettingsReturnTargetDepthTypeIfTheyAreTheSame() {
        Group group = new Group();

        Rectangle rectangle1 = new Rectangle();
        rectangle1.setTargetDepth(10.1);
        group.addChild(rectangle1);

        Rectangle rectangle2 = new Rectangle();
        rectangle2.setTargetDepth(10.1);
        group.addChild(rectangle2);

        assertTrue(group.getSettings().contains(EntitySetting.TARGET_DEPTH));

        rectangle2.setTargetDepth(10.2);
        assertFalse(group.getSettings().contains(EntitySetting.TARGET_DEPTH));
    }

    @Test
    public void getSettingsReturnSpindleSpeedTypeIfTheyAreTheSame() {
        Group group = new Group();

        Rectangle rectangle1 = new Rectangle();
        rectangle1.setSpindleSpeed(10);
        group.addChild(rectangle1);

        Rectangle rectangle2 = new Rectangle();
        rectangle2.setSpindleSpeed(10);
        group.addChild(rectangle2);

        assertTrue(group.getSettings().contains(EntitySetting.SPINDLE_SPEED));

        rectangle2.setSpindleSpeed(11);
        assertFalse(group.getSettings().contains(EntitySetting.SPINDLE_SPEED));
    }

    @Test
    public void getSettingsReturnFeedRateTypeIfTheyAreTheSame() {
        Group group = new Group();

        Rectangle rectangle1 = new Rectangle();
        rectangle1.setFeedRate(10);
        group.addChild(rectangle1);

        Rectangle rectangle2 = new Rectangle();
        rectangle2.setFeedRate(10);
        group.addChild(rectangle2);

        assertTrue(group.getSettings().contains(EntitySetting.FEED_RATE));

        rectangle2.setFeedRate(11);
        assertFalse(group.getSettings().contains(EntitySetting.FEED_RATE));
    }
}
