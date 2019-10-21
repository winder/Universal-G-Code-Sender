/*
    Copyright 2016-2019 Will Winder

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

import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;

/**
 * @author wwinder
 */
public class GcodeUtils {

    /**
     * Generates a gcode command for switching units.
     *
     * @param units the units to switch to
     * @return the gcode command to switch units.
     */
    public static String unitCommand(Units units) {
        // Change units.
        switch (units) {
            case MM:
                return Code.G21.name();
            case INCH:
                return Code.G20.name();
            default:
                return "";
        }
    }

    /**
     * Generates a move command given a base command. The command will be appended with the relative movement to be made
     * on the axises with the given distance and feed rate.
     *
     * @param command  the base command to use, ie: G91G1 or G1
     * @param distance the distance to move in the currently selected unit (G20 or G21)
     * @param dirX     1 for positive movement, 0 for no movement, -1 for negative movement
     * @param dirY     1 for positive movement, 0 for no movement, -1 for negative movement
     * @param dirZ     1 for positive movement, 0 for no movement, -1 for negative movement
     * @param units    the units to use for movement
     */
    public static String generateMoveCommand(String command, double distance, double feedRate, int dirX, int dirY, int dirZ, Units units) {
        StringBuilder sb = new StringBuilder();

        // Scale the feed rate and distance to the current coordinate units
        String convertedDistance = Utils.formatter.format(distance);
        String convertedFeedRate = Utils.formatter.format(feedRate);

        // Set command.
        sb.append(GcodeUtils.unitCommand(units));
        sb.append(command);

        if (dirX != 0) {
            sb.append("X");
            if (dirX < 0) {
                sb.append("-");
            }
            sb.append(convertedDistance);
        }

        if (dirY != 0) {
            sb.append("Y");
            if (dirY < 0) {
                sb.append("-");
            }
            sb.append(convertedDistance);
        }

        if (dirZ != 0) {
            sb.append("Z");
            if (dirZ < 0) {
                sb.append("-");
            }
            sb.append(convertedDistance);
        }

        if (convertedFeedRate != null) {
            sb.append("F").append(convertedFeedRate);
        }

        return sb.toString();
    }

    /**
     * Generate a command to move to a specific coordinate
     *
     *
     * @param command  the base command to use, ie: G91G1 or G1
     * @param position the position to move to
     * @param feedRate the feed rate to use using the position units / minute
     * @return a command string
     */
    public static String generateMoveToCommand(String command, PartialPosition position, double feedRate) {
        StringBuilder sb = new StringBuilder();

        sb.append(unitCommand(position.getUnits()));
        sb.append(command);

        // Add all axises
        sb.append(position.getFormattedGCode());

        String convertedFeedRate = Utils.formatter.format(feedRate);
        if (feedRate > 0 && convertedFeedRate != null) {
            sb.append("F").append(convertedFeedRate);
        }

        return sb.toString();
    }

}