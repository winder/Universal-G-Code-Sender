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
package com.willwinder.ugs.nbp.designer.gui.selectionsettings;

import com.willwinder.ugs.nbp.designer.entities.Anchor;
import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import com.willwinder.ugs.nbp.designer.entities.cuttable.CutType;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThrows;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SelectionSettingsModelTest {

    private SelectionSettingsModel target;

    @Before
    public void setUp() {
        target = new SelectionSettingsModel();
    }

    @Test
    public void putShouldSetThePropertyAndNotify() {
        Thread mainThread = Thread.currentThread();
        AtomicBoolean hasBeenNotified = new AtomicBoolean(false);
        target.addListener((entitySetting) -> {
            if (entitySetting == EntitySetting.WIDTH) {
                hasBeenNotified.set(true);
            } else {
                fail("Got unwanted update: " + entitySetting);
            }

            assertEquals("The notification must be done on the same thread", mainThread, Thread.currentThread());
        });

        target.put(EntitySetting.WIDTH, 10.0);

        assertEquals(10.0, target.getWidth(), 0.01);
        assertEquals(10.0, (Double) target.get(EntitySetting.WIDTH), 0.01);
        assertTrue(hasBeenNotified.get());
    }

    @Test
    public void putShouldNotSetThePropertyIfNotChanged() {
        target.setHeight(10.0);
        target.addListener((entitySetting) -> {
            fail("Got unwanted update: " + entitySetting);
        });

        target.put(EntitySetting.HEIGHT, 10.0);

        assertEquals(10.0, target.getHeight(), 0.01);
        assertEquals(10.0, (Double) target.get(EntitySetting.HEIGHT), 0.01);
    }

    @Test
    public void setSizeMayNotNotifyBeforeBothValuesHaveChanged() {
        target.addListener((entitySetting) -> {
            if (entitySetting != EntitySetting.WIDTH && entitySetting != EntitySetting.HEIGHT) {
                fail("Got unknown update " + entitySetting);
            }
            if (target.getWidth() != 10.0 || target.getHeight() != 10.0) {
                fail("Set size notified before both values where set");
            }
        });

        target.setSize(10, 10);

        assertEquals(10.0, target.getWidth(), 0.01);
        assertEquals(10.0, target.getHeight(), 0.01);
        assertEquals(10.0, (Double) target.get(EntitySetting.HEIGHT), 0.01);
    }

    @Test
    public void putValueWithWrongTypeShouldThrowException() {
        target.addListener((e) -> {
            fail("Should not notify any change");
        });

        assertThrows(SelectionSettingsModelException.class, () -> target.put(EntitySetting.POSITION_X, "1.0"));

        assertEquals(0.0, target.getPositionX(), 0.01);
    }

    @Test
    public void putAllValues() {
        List<EntitySetting> notifiedSettings = new ArrayList<>();
        target.addListener(notifiedSettings::add);

        target.put(EntitySetting.POSITION_X, 1.0);
        target.put(EntitySetting.POSITION_Y, 2.0);
        target.put(EntitySetting.WIDTH, 3.0);
        target.put(EntitySetting.HEIGHT, 4.0);
        target.put(EntitySetting.ROTATION, 5.0);
        target.put(EntitySetting.CUT_TYPE, CutType.POCKET);
        target.put(EntitySetting.TEXT, "Banana");
        target.put(EntitySetting.START_DEPTH, 6.0);
        target.put(EntitySetting.TARGET_DEPTH, 7.0);
        target.put(EntitySetting.ANCHOR, Anchor.TOP_RIGHT);
        target.put(EntitySetting.FONT_FAMILY, Font.SERIF);
        target.put(EntitySetting.LOCK_RATIO, false);

        assertEquals(1.0, target.getPositionX(), 0.01);
        assertEquals(2.0, target.getPositionY(), 0.01);
        assertEquals(3.0, target.getWidth(), 0.01);
        assertEquals(4.0, target.getHeight(), 0.01);
        assertEquals(5.0, target.getRotation(), 0.01);
        assertEquals(CutType.POCKET, target.getCutType());
        assertEquals("Banana", target.getText());
        assertEquals(6.0, target.getStartDepth(), 0.01);
        assertEquals(7.0, target.getTargetDepth(), 0.01);
        assertEquals(Anchor.TOP_RIGHT, target.getAnchor());
        assertEquals(Font.SERIF, target.getFontFamily());
        assertFalse(target.getLockRatio());

        EntitySetting[] expectedSettings = EntitySetting.values();
        Arrays.sort(expectedSettings);

        EntitySetting[] currentSettings = notifiedSettings.toArray(new EntitySetting[0]);
        Arrays.sort(currentSettings);

        assertArrayEquals("All settings have not been set properly", expectedSettings, currentSettings);
    }
}