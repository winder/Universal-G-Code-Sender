/*
    Copyright 2026 Joacim Breiler

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
package com.willwinder.universalgcodesender.firmware.grbl.commands;

import com.willwinder.universalgcodesender.GrblUtils;
import com.willwinder.universalgcodesender.Utils;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.Unit;
import static com.willwinder.universalgcodesender.model.UnitUtils.Units.MM;
import com.willwinder.universalgcodesender.model.UnitValue;
import com.willwinder.universalgcodesender.types.ProbeGcodeCommand;
import org.apache.commons.lang3.StringUtils;

import java.util.Arrays;
import java.util.Optional;

/**
 * A probe command that will probe in one direction and parse the probe coordinate response
 *
 * @author Joacim Breiler
 */
public class GrblProbeCommand extends ProbeGcodeCommand {
    public GrblProbeCommand(PartialPosition distance, UnitValue feedrate) {
        super(generateCommand(distance, feedrate));

        // Reset the modal state to previous state
        setTemporaryParserModalChange(true);
    }

    private static String generateCommand(PartialPosition distance, UnitValue feedrate) {
        StringBuilder sb = new StringBuilder();
        sb.append(distance.getUnits() == MM ? Code.G21 : Code.G20)
                .append(" ").append(Code.G91)
                .append(" ").append(Code.G38_2);

        for (Axis axis : Axis.values()) {
            if (distance.hasAxis(axis)) {
                double v = distance.getAxis(axis);
                sb.append(" ")
                        .append(axis)
                        .append(Utils.formatter.format(v));
            }
        }

        int feedRate = (int) Math.round(feedrate.convertTo(distance.getUnits() == MM ? Unit.MM_PER_MINUTE : Unit.INCHES_PER_MINUTE).doubleValue());
        sb.append(" F")
                .append(feedRate);
        return sb.toString();
    }

    @Override
    public void appendResponse(String response) {
        // We don't care about status strings
        if (GrblUtils.isGrblStatusStringV1(response) || GrblUtils.isGrblStatusString(response)) {
            return;
        }

        super.appendResponse(response);
    }

    @Override
    public Optional<Position> getProbedPosition() {
        String[] lines = StringUtils.split(getResponse(), "\n");

        return Arrays.stream(lines)
                .filter(line -> line.startsWith("[PRB:"))
                .map(line -> {
                    // Response in format: [PRB:-259.579,-149.578,-55.614:1]
                    String coordinate = StringUtils.substringBetween(line, "[PRB:", ":");
                    String[] split = StringUtils.split(coordinate, ",");
                    return new Position(Double.parseDouble(split[0]), Double.parseDouble(split[1]), Double.parseDouble(split[2]), MM);
                })
                .findFirst();

    }
}
