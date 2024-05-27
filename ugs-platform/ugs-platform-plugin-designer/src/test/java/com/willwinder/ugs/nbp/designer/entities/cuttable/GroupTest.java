package com.willwinder.ugs.nbp.designer.entities.cuttable;

import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import static org.junit.Assert.assertEquals;
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
    public void getEntitySettingsShouldReturnACombinedListOfSettings() {
        Point point1 = new Point();
        Point point2 = new Point();

        Group group = new Group();
        assertEquals(List.of(), group.getSettings());

        group.addChild(point1);
        assertEquals(List.of(EntitySetting.POSITION_X, EntitySetting.POSITION_Y, EntitySetting.CUT_TYPE, EntitySetting.START_DEPTH, EntitySetting.TARGET_DEPTH), group.getSettings());

        group.addChild(point2);
        assertEquals(List.of(EntitySetting.POSITION_X, EntitySetting.POSITION_Y, EntitySetting.CUT_TYPE, EntitySetting.START_DEPTH, EntitySetting.TARGET_DEPTH), group.getSettings());

        Rectangle rectangle = new Rectangle();
        group.addChild(rectangle);
        assertEquals(List.of(EntitySetting.POSITION_X, EntitySetting.POSITION_Y, EntitySetting.CUT_TYPE, EntitySetting.START_DEPTH, EntitySetting.TARGET_DEPTH, EntitySetting.ANCHOR, EntitySetting.WIDTH, EntitySetting.HEIGHT, EntitySetting.SPINDLE_SPEED, EntitySetting.PASSES, EntitySetting.FEED_RATE), group.getSettings());
    }
}
