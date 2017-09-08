/**
 * Split lines into a series of smaller line segments.
 */
/*
    Copyright 2017 Will Winder

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
package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.GcodeParser.GcodeMeta;
import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.i18n.Localization;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class LineSplitter implements ICommandProcessor {
    final double maxSegmentLength;

    /**
     * @param segmentLength length of longest single line.
     */
    public LineSplitter(double segmentLength) {
        this.maxSegmentLength = segmentLength;
    }

    @Override
    public String getHelp() {
        return "Split G0 and G1 commands into multiple commands.";
    }

    private boolean hasLine(List<GcodeMeta> commands) {
        if (commands == null) return false;
        for (GcodeMeta command : commands) {
            switch(command.code){
                case G0:
                case G1:
                    return true;
            }
        }
        return false;
    }

    @Override
    public List<String> processCommand(String commandString, GcodeState state) throws GcodeParserException {
        List<GcodeParser.GcodeMeta> commands = GcodeParser.processCommand(commandString, 0, state);

        List<String> results = new ArrayList<>();

        if (!hasLine(commands)) {
            return Collections.singletonList(commandString);
        }

        // Make sure there is just one command (the G0/G1 command).
        if (commands.size() != 1) {
            throw new GcodeParserException(Localization.getString("parser.processor.general.multiple-commands"));
        }

        // We have already verified that there is a single G0/G1 command, split it up.
        GcodeMeta command = commands.get(0);

        if (command == null || command.point == null) {
            throw new GcodeParserException("Internal parser error: missing data.");
        }

        // line length
        Point3d start = state.currentPoint;
        Point3d end = command.point.point();
        Point3d current = start;
        double length = start.distance(end);

        // Check if line needs splitting.
        if (length > this.maxSegmentLength) {
            int numSegments = (int) Math.ceil(length/this.maxSegmentLength);
            double segmentLength = length / Math.ceil(length / this.maxSegmentLength);

            // Create line segments, stop before the last one which uses the end point.
            for (int i = 1; i < numSegments; i++) {
                double k = 1 / (length / (i * segmentLength));
                double newX = start.x + k * (end.x - start.x);
                double newY = start.y + k * (end.y - start.y);
                double newZ = start.z + k * (end.z - start.z);

                Point3d next = new Point3d(newX, newY, newZ);
                results.add(GcodePreprocessorUtils.generateLineFromPoints(
                        command.code, current, next, command.state.inAbsoluteMode, null));
                current = next;
            }

            // Add the last line point.
            results.add(GcodePreprocessorUtils.generateLineFromPoints(
                    command.code, current, end, command.state.inAbsoluteMode, null));
        } else {
            results.add(commandString);
        }

        return results;
    }
}
