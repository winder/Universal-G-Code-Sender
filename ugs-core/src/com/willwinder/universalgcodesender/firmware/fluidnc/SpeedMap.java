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
package com.willwinder.universalgcodesender.firmware.fluidnc;

import java.util.HashMap;
import java.util.Map;

/**
 * A class for parsing FluidNC speed maps
 *
 * @author Joacim Breiler
 */
public class SpeedMap {
    private final Map<Integer, Double> speeds = new HashMap<>();

    public SpeedMap(String speedMaps) {
        String[] speeds = speedMaps.split(" ");
        for (String speedMap : speeds) {
            String[] split = speedMap.split("=");
            if (split.length != 2) {
                continue;
            }

            this.speeds.put(Integer.parseInt(split[0].trim()), Double.parseDouble(split[1].trim().replace("%", "")));
        }
    }

    public int getMax() {
        return speeds.keySet().stream()
                .max((s1, s2) -> speeds.get(s1).compareTo(speeds.get(s2)))
                .orElse(0);
    }
}
