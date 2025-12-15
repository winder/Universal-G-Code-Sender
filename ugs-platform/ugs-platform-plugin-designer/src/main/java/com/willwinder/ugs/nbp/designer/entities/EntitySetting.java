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
    POSITION_X("X", "positionX"),
    POSITION_Y("Y", "positionY"),
    WIDTH("Width", "width"),
    HEIGHT("Height", "height"),
    ANCHOR("Anchor", "anchor"),
    ROTATION("Rotation", "rotation"),
    LOCK_RATIO("Lock ratio", "lockRatio"),
    CUT_TYPE("Cut type", "cutType"),
    TEXT("Text", "text"),
    FONT_FAMILY("Font", "fontFamily"),
    START_DEPTH("Start depth", "startDepth"),
    TARGET_DEPTH("Target depth", "targetDepth"),
    SPINDLE_SPEED("Spindle speed", "spindleSpeed"),
    PASSES("Passes", "passes"),
    FEED_RATE("Feed rate", "feedRate"),
    LEAD_IN_PERCENT("Lead in percent", "leadInPercent"),
    INCLUDE_IN_EXPORT("Include in export", "includeInExport"),
    TOOL_PATH_DIRECTION("Tool path direction", "toolPathDirection"),;

    public static final List<EntitySetting> TRANSFORMATION_SETTINGS = List.of(
            EntitySetting.POSITION_X,
            EntitySetting.POSITION_Y,
            EntitySetting.WIDTH,
            EntitySetting.HEIGHT,
            EntitySetting.ROTATION,
            EntitySetting.ANCHOR,
            EntitySetting.LOCK_RATIO
    );

    public static final List<EntitySetting> DEFAULT_ENDMILL_SETTINGS = List.of(
            EntitySetting.CUT_TYPE,
            EntitySetting.START_DEPTH,
            EntitySetting.TARGET_DEPTH,
            EntitySetting.SPINDLE_SPEED,
            EntitySetting.FEED_RATE,
            EntitySetting.INCLUDE_IN_EXPORT);

    public static final List<EntitySetting> DEFAULT_SURFACE_SETTINGS = List.of(
            EntitySetting.CUT_TYPE,
            EntitySetting.START_DEPTH,
            EntitySetting.TARGET_DEPTH,
            EntitySetting.SPINDLE_SPEED,
            EntitySetting.FEED_RATE,
            EntitySetting.LEAD_IN_PERCENT,
            EntitySetting.INCLUDE_IN_EXPORT,
            EntitySetting.TOOL_PATH_DIRECTION);

    public static final List<EntitySetting> DEFAULT_LASER_SETTINGS = List.of(
            EntitySetting.CUT_TYPE,
            EntitySetting.SPINDLE_SPEED,
            EntitySetting.PASSES,
            EntitySetting.FEED_RATE,
            EntitySetting.INCLUDE_IN_EXPORT);

    public static final List<EntitySetting> DEFAULT_TEXT_SETTINGS = List.of(
            EntitySetting.TEXT,
            EntitySetting.FONT_FAMILY);


    private final String label;
    private final String propertyName;

    EntitySetting(String label, String propertyName) {
        this.label = label;
        this.propertyName = propertyName;
    }

    public String getLabel() {
        return label;
    }

    public String getPropertyName() {
        return propertyName;
    }

    /**
     * Find an EntitySetting by its property name.
     *
     * @param propertyName the property name to search for
     * @return the matching EntitySetting, or null if not found
     */
    public static EntitySetting fromPropertyName(String propertyName) {
        if (propertyName == null) throw new IllegalArgumentException("propertyName cannot be null");
        for (EntitySetting setting : values()) {
            if (setting.propertyName.equals(propertyName)) {
                return setting;
            }
        }
        return null;
    }
}
