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

import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;

public class SelectionSettingsModelTest {

    private SelectionSettingsModel target;

    @Before
    public void setUp() {
        target = new SelectionSettingsModel();
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
    public void setLaserPower() {
        target.addListener((entitySetting) -> {
            if (entitySetting != EntitySetting.SPINDLE_SPEED) {
                fail("Got unknown update " + entitySetting);
            }
            if (target.getSpindleSpeed() != 10.0) {
                fail("Set laser power notified before value where set");
            }
        });

        target.setSpindleSpeed(10);

        assertEquals(10, target.getSpindleSpeed());
    }
}