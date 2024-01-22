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

public class FieldEventDispatcherTest {
    private FieldEventDispatcher target;

    @Before
    public void setUp() {
        target = new FieldEventDispatcher();
    }

    @Test
    public void updateValueShouldNotifyListeners() {
        Thread thread = Thread.currentThread();
        target.addListener((EntitySetting entitySetting, Object object) -> {
            if (entitySetting != EntitySetting.WIDTH && !object.equals(1.0)) {
                assertEquals(thread, Thread.currentThread());
                fail("Got unknown setting " + entitySetting + " and value " + object);
            }
        });

        target.addListener((EntitySetting entitySetting, Object object) -> {
            if (entitySetting != EntitySetting.WIDTH && !object.equals(1.0)) {
                assertEquals(thread, Thread.currentThread());
                fail("Got unknown setting " + entitySetting + " and value " + object);
            }
        });

        target.updateValue(EntitySetting.WIDTH, 1.0);
    }
}