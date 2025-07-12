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

package com.willwinder.ugs.nbp.designer.entities;

import java.util.List;

/**
 * What settings that is possible to set on an entity.
 *
 * @author Joacim Breiler
 */
public enum EntitySetting {
    POSITION_X("X"),
    POSITION_Y("Y"),
    WIDTH("Width"),
    HEIGHT("Height"),
    ROTATION("Rotation"),
    CUT_TYPE("Cut type"),
    TEXT("Text"),
    START_DEPTH("Start depth"),
    TARGET_DEPTH("Target depth"),
    ANCHOR("Anchor"),
    FONT_FAMILY("Font"),
    LOCK_RATIO("Lock ratio"),
    SPINDLE_SPEED("Spindle speed"),
    PASSES("Passes"),
    FEED_RATE("Feed rate"),
    LEAD_IN_PERCENT("Lead in percent"),
    LEAD_OUT_PERCENT("Lead out percent"),
    INCLUDE_IN_EXPORT("Include in export");

    public static final List<EntitySetting> DEFAULT_ENDMILL_SETTINGS = List.of(
            EntitySetting.CUT_TYPE,
            EntitySetting.ANCHOR,
            EntitySetting.HEIGHT,
            EntitySetting.WIDTH,
            EntitySetting.FONT_FAMILY,
            EntitySetting.LOCK_RATIO,
            EntitySetting.POSITION_X,
            EntitySetting.POSITION_Y,
            EntitySetting.ROTATION,
            EntitySetting.START_DEPTH,
            EntitySetting.TARGET_DEPTH,
            EntitySetting.SPINDLE_SPEED,
            EntitySetting.FEED_RATE,
            EntitySetting.TEXT,
            EntitySetting.INCLUDE_IN_EXPORT);

    public static final List<EntitySetting> DEFAULT_SURFACE_SETTINGS = List.of(
            EntitySetting.CUT_TYPE,
            EntitySetting.ANCHOR,
            EntitySetting.HEIGHT,
            EntitySetting.WIDTH,
            EntitySetting.FONT_FAMILY,
            EntitySetting.LOCK_RATIO,
            EntitySetting.POSITION_X,
            EntitySetting.POSITION_Y,
            EntitySetting.ROTATION,
            EntitySetting.START_DEPTH,
            EntitySetting.TARGET_DEPTH,
            EntitySetting.SPINDLE_SPEED,
            EntitySetting.FEED_RATE,
            EntitySetting.TEXT,
            EntitySetting.LEAD_IN_PERCENT,
            EntitySetting.LEAD_OUT_PERCENT,
            EntitySetting.INCLUDE_IN_EXPORT);

    public static final List<EntitySetting> DEFAULT_LASER_SETTINGS = List.of(
            EntitySetting.CUT_TYPE,
            EntitySetting.ANCHOR,
            EntitySetting.HEIGHT,
            EntitySetting.WIDTH,
            EntitySetting.FONT_FAMILY,
            EntitySetting.LOCK_RATIO,
            EntitySetting.POSITION_X,
            EntitySetting.POSITION_Y,
            EntitySetting.ROTATION,
            EntitySetting.SPINDLE_SPEED,
            EntitySetting.PASSES,
            EntitySetting.FEED_RATE,
            EntitySetting.TEXT,
            EntitySetting.INCLUDE_IN_EXPORT);

    private final String label;

    EntitySetting(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
