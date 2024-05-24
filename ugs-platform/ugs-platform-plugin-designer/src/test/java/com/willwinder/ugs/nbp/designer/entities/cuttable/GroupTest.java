package com.willwinder.ugs.nbp.designer.entities.cuttable;

import static org.junit.Assert.assertEquals;
import org.junit.Test;

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
}
