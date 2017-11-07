/*
    Copyright 2016-2017 Will Winder

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
package com.willwinder.universalgcodesender.gcode.util;

import com.willwinder.universalgcodesender.model.UnitUtils.Units;

/**
 *
 * @author wwinder
 */
public class GcodeUtils {
    public static String unitCommand(Units units) {
      // Change units.
      switch(units) {
        case MM:
          return "G21";
        case INCH:
          return "G20";
        default:
          return "";
      }
    }

    /**
     *
     * @param command Something like "G0"
     * @param units Appends "G21" or "G20"
     * @param distance The distance to use
     * @param dirX Whether to append the X coord.
     * @param dirY Whether to append the Y coord.
     * @param dirZ Whether to append the Z coord.
     */
    public static String generateXYZ(String command, Units units,
            String distance, String feedRate, int dirX, int dirY, int dirZ) {
        StringBuilder sb = new StringBuilder();

      // Add units.
        sb.append(unitCommand(units));

        // Set command.
        sb.append(command);

        if (dirX != 0) {
            sb.append("X");
            if (dirX < 0) {
                sb.append("-");
            }
            sb.append(distance);
        }

        if (dirY != 0) {
            sb.append("Y");
            if (dirY < 0) {
                sb.append("-");
            }
            sb.append(distance);
        }

        if (dirZ != 0) {
            sb.append("Z");
            if (dirZ < 0) {
                sb.append("-");
            }
            sb.append(distance);
        }

        if (feedRate != null) {
            sb.append("F").append(feedRate);
        }

        return sb.toString();
    }
}
