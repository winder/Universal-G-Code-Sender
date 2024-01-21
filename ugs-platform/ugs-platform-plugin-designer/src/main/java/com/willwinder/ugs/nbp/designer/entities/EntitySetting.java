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
    LOCK_RATIO("Lock ratio");

    private final String label;

    EntitySetting(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }
}
