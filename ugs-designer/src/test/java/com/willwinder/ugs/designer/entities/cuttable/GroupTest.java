package com.willwinder.ugs.designer.entities.cuttable;

import com.willwinder.ugs.designer.entities.EntityGroup;
import com.willwinder.ugs.designer.entities.EntitySetting;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.EnumSet;

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
    public void copy_shouldRetainSettingsOnChildren() {
        Rectangle rectangle = new Rectangle(1, 1);
        rectangle.setFeedRate(1234);
        rectangle.setLeadInPercent(40);
        rectangle.setToolPathDirection(ToolPathDirection.VERTICAL);

        Group group = new Group();
        group.addChild(rectangle);

        Group copy = (Group) group.copy();

        Cuttable child = (Cuttable) copy.getChildren().get(0);
        assertEquals(1234, child.getFeedRate());
        assertEquals(40, child.getLeadInPercent());
        assertEquals(ToolPathDirection.VERTICAL, child.getToolPathDirection());
    }

    @Test
    public void getSettingsShouldReturnACombinedListOfSettings() {
        Point point1 = new Point();
        Point point2 = new Point();

        Group group = new Group();
        assertEquals(EnumSet.noneOf(EntitySetting.class), group.getSettings());

        group.addChild(point1);
        assertEquals(EnumSet.of(EntitySetting.POSITION_X, EntitySetting.POSITION_Y, EntitySetting.CUT_TYPE, EntitySetting.SPINDLE_SPEED, EntitySetting.START_DEPTH, EntitySetting.TARGET_DEPTH), group.getSettings());

        group.addChild(point2);
        assertEquals(EnumSet.of(EntitySetting.POSITION_X, EntitySetting.POSITION_Y, EntitySetting.CUT_TYPE, EntitySetting.SPINDLE_SPEED, EntitySetting.START_DEPTH, EntitySetting.TARGET_DEPTH), group.getSettings());

        Rectangle rectangle = new Rectangle();
        group.addChild(rectangle);
        assertEquals(EnumSet.of(EntitySetting.POSITION_X, EntitySetting.POSITION_Y, EntitySetting.CUT_TYPE, EntitySetting.SPINDLE_SPEED, EntitySetting.START_DEPTH, EntitySetting.TARGET_DEPTH), group.getSettings());
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
    public void getSettingsShouldReturnLeadInPercentIfTheyAreTheSame() {
        Group group = new Group();

        Rectangle rectangle1 = new Rectangle();
        rectangle1.setLeadInPercent(10);
        group.addChild(rectangle1);

        Rectangle rectangle2 = new Rectangle();
        rectangle2.setLeadInPercent(10);
        group.addChild(rectangle2);

        assertTrue(group.getSettings().contains(EntitySetting.LEAD_IN_PERCENT));

        rectangle2.setLeadInPercent(11);
        assertFalse(group.getSettings().contains(EntitySetting.LEAD_IN_PERCENT));
    }

    @Test
    public void getSettingsShouldReturnIncludeInExportIfTheyAreTheSame() {
        Group group = new Group();

        Rectangle rectangle1 = new Rectangle();
        rectangle1.setIncludeInExport(true);
        group.addChild(rectangle1);

        Rectangle rectangle2 = new Rectangle();
        rectangle2.setIncludeInExport(true);
        group.addChild(rectangle2);

        assertTrue(group.getSettings().contains(EntitySetting.INCLUDE_IN_EXPORT));

        rectangle2.setIncludeInExport(false);
        assertFalse(group.getSettings().contains(EntitySetting.INCLUDE_IN_EXPORT));
    }

    @Test
    public void getSettingsShouldReturnToolPathAngleIfTheyAreTheSame() {
        Group group = new Group();

        Rectangle rectangle1 = new Rectangle();
        rectangle1.setToolPathAngle(45);
        group.addChild(rectangle1);

        Rectangle rectangle2 = new Rectangle();
        rectangle2.setToolPathAngle(45);
        group.addChild(rectangle2);

        assertTrue(group.getSettings().contains(EntitySetting.TOOL_PATH_ANGLE));

        rectangle2.setToolPathAngle(90);
        assertFalse(group.getSettings().contains(EntitySetting.TOOL_PATH_ANGLE));
    }

    @Test
    public void getSettingsShouldReturnDirectionIfTheyAreTheSame() {
        Group group = new Group();

        Rectangle rectangle1 = new Rectangle();
        rectangle1.setDirection(Direction.CLIMB);
        group.addChild(rectangle1);

        Rectangle rectangle2 = new Rectangle();
        rectangle2.setDirection(Direction.CLIMB);
        group.addChild(rectangle2);

        assertTrue(group.getSettings().contains(EntitySetting.DIRECTION));

        rectangle2.setDirection(Direction.CONVENTIONAL);
        assertFalse(group.getSettings().contains(EntitySetting.DIRECTION));
    }

    @Test
    public void getSettingsShouldReturnPassesIfTheyAreTheSame() {
        Group group = new Group();

        Rectangle rectangle1 = new Rectangle();
        rectangle1.setPasses(2);
        group.addChild(rectangle1);

        Rectangle rectangle2 = new Rectangle();
        rectangle2.setPasses(2);
        group.addChild(rectangle2);

        assertTrue(group.getSettings().contains(EntitySetting.PASSES));

        rectangle2.setPasses(3);
        assertFalse(group.getSettings().contains(EntitySetting.PASSES));
    }

    @Test
    public void getSettingsShouldReturnSettingsWithoutComparableValuesEvenIfTheyDiffer() {
        Group group = new Group();
        Rectangle rectangle1 = new Rectangle(10, 10);
        rectangle1.setCornerRadius(1d);
        group.addChild(rectangle1);

        Rectangle rectangle2 = new Rectangle(50, 50);
        rectangle2.setCornerRadius(20d);
        group.addChild(rectangle2);

        EnumSet<EntitySetting> settings = group.getSettings();

        assertTrue(settings.contains(EntitySetting.POSITION_X));
        assertTrue(settings.contains(EntitySetting.WIDTH));
        assertTrue(settings.contains(EntitySetting.CORNER_RADIUS));
    }

    @Test
    public void getSettingsShouldOnlyReturnSettingsSharedByAllChildren() {
        Group group = new Group();
        group.addChild(new Rectangle());
        group.addChild(new Point());

        EnumSet<EntitySetting> settings = group.getSettings();

        assertFalse(settings.contains(EntitySetting.WIDTH));
        assertTrue(settings.contains(EntitySetting.POSITION_X));
        assertTrue(settings.contains(EntitySetting.POSITION_Y));
    }

    @Test
    public void getSettingsShouldIgnoreChildrenThatAreNotCuttable() {
        Group group = new Group();
        group.addChild(new EntityGroup());
        group.addChild(new Rectangle());

        EnumSet<EntitySetting> settings = group.getSettings();

        assertTrue(settings.contains(EntitySetting.WIDTH));
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
