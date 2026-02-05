/*
    Copyright 2021 Will Winder

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
package com.willwinder.ugs.nbp.designer.entities.cuttable;

import com.willwinder.ugs.nbp.designer.entities.EntitySetting;
import static com.willwinder.ugs.nbp.designer.entities.EntitySetting.DEFAULT_ENDMILL_SETTINGS;
import static com.willwinder.ugs.nbp.designer.entities.EntitySetting.DEFAULT_LASER_SETTINGS;
import static com.willwinder.ugs.nbp.designer.entities.EntitySetting.DEFAULT_POCKET_SETTINGS;
import static com.willwinder.ugs.nbp.designer.entities.EntitySetting.DEFAULT_SURFACE_SETTINGS;

import java.util.Collections;
import java.util.List;

/**
 * @author Joacim Breiler
 */
public enum CutType {
    NONE("None", List.of(EntitySetting.CUT_TYPE), Collections.emptyList()),
    POCKET("Mill - Pocket", DEFAULT_POCKET_SETTINGS, List.of(Direction.CLIMB, Direction.CONVENTIONAL)),
    SURFACE("Mill - Surface", DEFAULT_SURFACE_SETTINGS, List.of(Direction.CLIMB, Direction.CONVENTIONAL, Direction.BOTH)),
    ON_PATH("Mill - On path", DEFAULT_ENDMILL_SETTINGS, Collections.emptyList()),
    INSIDE_PATH("Mill - Inside path", DEFAULT_ENDMILL_SETTINGS, Collections.emptyList()),
    OUTSIDE_PATH("Mill - Outside path", DEFAULT_ENDMILL_SETTINGS, Collections.emptyList()),
    LASER_ON_PATH("Laser - On path", DEFAULT_LASER_SETTINGS, Collections.emptyList()),
    LASER_FILL("Laser - Fill", DEFAULT_LASER_SETTINGS, Collections.emptyList()),
    LASER_RASTER("Laser - Raster", DEFAULT_LASER_SETTINGS, Collections.emptyList()),
    CENTER_DRILL("Center drill", DEFAULT_ENDMILL_SETTINGS, Collections.emptyList()),
    ;

    private final String name;

    private final List<EntitySetting> settings;
    private final List<Direction> directions;

    CutType(String name, List<EntitySetting> settings, List<Direction> directions) {
        this.name = name;
        this.settings = settings;
        this.directions = directions;
    }

    public String getName() {
        return name;
    }

    public List<EntitySetting> getSettings() {
        return settings;
    }

    /**
     * Get supported directions for this cut type
     *
     * @return a list of supported directions
     */
    public List<Direction> getDirections() {
        return directions;
    }
}
