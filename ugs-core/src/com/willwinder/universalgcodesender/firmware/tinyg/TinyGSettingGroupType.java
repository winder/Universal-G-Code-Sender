/*
    Copyright 2018 Will Winder

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
package com.willwinder.universalgcodesender.firmware.tinyg;

import com.willwinder.universalgcodesender.i18n.Localization;

import java.util.Arrays;
import java.util.Optional;

/**
 * An enum for describing which setting groups that are available on the
 * tinyg/g2core controller
 *
 * @author Joacim Breiler
 */
public enum TinyGSettingGroupType {
    AXIS_X("x", TinyGSettingType.AXIS_SETTINGS),
    AXIS_Y("y", TinyGSettingType.AXIS_SETTINGS),
    AXIS_Z("z", TinyGSettingType.AXIS_SETTINGS),
    AXIS_A("a", TinyGSettingType.AXIS_SETTINGS),
    AXIS_B("b", TinyGSettingType.AXIS_SETTINGS),
    AXIS_C("c", TinyGSettingType.AXIS_SETTINGS),
    MOTOR_1("1", TinyGSettingType.MOTOR_SETTINGS),
    MOTOR_2("2", TinyGSettingType.MOTOR_SETTINGS),
    MOTOR_3("3", TinyGSettingType.MOTOR_SETTINGS),
    MOTOR_4("4", TinyGSettingType.MOTOR_SETTINGS),
    MOTOR_5("5", TinyGSettingType.MOTOR_SETTINGS),
    MOTOR_6("6", TinyGSettingType.MOTOR_SETTINGS);

    private final String groupName;
    private final TinyGSettingType[] settingTypes;

    TinyGSettingGroupType(String groupName, TinyGSettingType[] settingTypes) {
        this.groupName = groupName;
        this.settingTypes = settingTypes;
    }

    public static Optional<TinyGSettingGroupType> fromSettingKey(String key) {
        return Arrays.stream(values())
                .filter(group -> key.startsWith(group.getGroupName()))
                .findFirst();
    }

    public String getGroupName() {
        return groupName;
    }

    public String getDescription() {
        return Localization.getString("firmware.tinyg.group.description." + getGroupName());
    }

    public TinyGSettingType[] getSettingTypes() {
        return settingTypes;
    }
}
