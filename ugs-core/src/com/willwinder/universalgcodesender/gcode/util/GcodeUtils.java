/*
    Copyright 2016-2020 Will Winder

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
    public static final String GCODE_RETURN_TO_XY_ZERO_LOCATION = "G90 G0 X0 Y0";
    public static final String GCODE_RETURN_TO_Z_ZERO_LOCATION = "G90 G0 Z0";

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
     * on the axises with the given feed rate.
     *
     * @param command   the base command to use, ie: G91G1 or G1
     * @param feedRate the maximum feed rate
     * @param p partial position of movement
     */
    public static String generateMoveCommand(String command, double feedRate, PartialPosition p) {
        StringBuilder sb = new StringBuilder();

        sb.append(GcodeUtils.unitCommand(p.getUnits()));
        sb.append(command);
        sb.append(p.getFormattedGCode(Utils.formatter));

        if (feedRate > 0) {
            String convertedFeedRate = Utils.formatter.format(feedRate);
            if (convertedFeedRate != null) {
                sb.append("F").append(convertedFeedRate);
            }
        }

        return sb.toString();
    }

    /**
     * Generate a command to move to a specific coordinate
     *
     * @param command  the base command to use, ie: G91G1 or G1
     * @param position the position to move to
     * @return a command string
     */
    public static String generateMoveToCommand(String command, PartialPosition position) {
        return generateMoveToCommand(command, position, 0);
    }

    /**
     * Generate a command to move to a specific coordinate
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
