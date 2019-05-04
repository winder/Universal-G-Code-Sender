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
 * All setting types for tinyg / g2core controllers
 *
 * @author Joacim Breiler
 */
public enum TinyGSettingType {
    // Axis settings
    AXIS_MODE("am", "integer"),
    VELOCITY_MAXIMUM("vm", "integer"),
    FEED_RATE_MAXIMUM("fr", "integer"),
    TRAVEL_MINIMUM("tn", "integer"),
    TRAVEL_MAXIMUM("tm", "integer"),
    JERK_MAXIMUM("jm", "integer"),
    JERK_HIGH("jh", "integer"),
    RADIUS_SETTING("ra", "integer"),
    HOMING_INPUT("hi", "integer"),
    HOMING_DIRECTION("hd", "integer"),
    SEARCH_VELOCITY("sv", "integer"),
    LATCH_VELOCITY("lv", "integer"),
    LATCH_BACKOFF("lb", "integer"),
    ZERO_BACKOFF("zb", "integer"),

    // Motor settings
    MAP_TO_AXIS("ma", "integer"),
    STEP_ANGLE("sa", "integer"),
    TRAVEL_REVOLUTION("tr", "integer"),
    MICROSTEPS("mi", "integer"),
    STEPS_PER_UNIT("su", "integer"),
    POLARITY("po", "integer"),
    POWER_MODE("pm", "integer"),
    POWER_LEVEL("pl", "integer");

    public static final TinyGSettingType[] AXIS_SETTINGS = new TinyGSettingType[]{
            AXIS_MODE,
            VELOCITY_MAXIMUM,
            FEED_RATE_MAXIMUM,
            TRAVEL_MINIMUM,
            TRAVEL_MAXIMUM,
            JERK_MAXIMUM,
            JERK_HIGH,
            RADIUS_SETTING,
            HOMING_INPUT,
            HOMING_DIRECTION,
            SEARCH_VELOCITY,
            LATCH_VELOCITY,
            LATCH_BACKOFF,
            ZERO_BACKOFF
    };

    public static final TinyGSettingType[] MOTOR_SETTINGS = new TinyGSettingType[]{
            MAP_TO_AXIS,
            STEP_ANGLE,
            TRAVEL_REVOLUTION,
            MICROSTEPS,
            STEPS_PER_UNIT,
            POLARITY,
            POWER_MODE,
            POWER_LEVEL
    };

    private final String settingName;
    private final String type;

    TinyGSettingType(String settingName, String type) {
        this.settingName = settingName;
        this.type = type;
    }

    public static Optional<TinyGSettingType> fromSettingKey(String key) {
        return Arrays.stream(values())
                .filter(group -> key.endsWith(group.getSettingName()))
                .findFirst();
    }

    /**
     * Returns the tinyg setting name, usually a two character name
     *
     * @return the setting name
     */
    public String getSettingName() {
        return settingName;
    }

    /**
     * Returns the data type that the setting is stored as
     *
     * @return the data type as string
     */
    public String getType() {
        return type;
    }

    /**
     * Returns the short description of the setting
     *
     * @return the short description
     */
    public String getShortDescription() {
        return Localization.getString("firmware.tinyg.setting.short.description." + settingName);
    }

    /**
     * Returns the full description of the setting
     *
     * @return the description
     */
    public String getDescription() {
        return Localization.getString("firmware.tinyg.setting.description." + settingName);
    }
}
