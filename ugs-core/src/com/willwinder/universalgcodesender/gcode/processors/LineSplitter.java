/*
    Copyright 2017-2020 Will Winder

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

import com.google.common.collect.Iterables;
import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.GcodeParser.GcodeMeta;
import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils.SplitCommand;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.Code;
import static com.willwinder.universalgcodesender.gcode.util.Code.*;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserUtils;
import com.willwinder.universalgcodesender.model.Position;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Split lines into a series of smaller line segments.
 *
 * @author wwinder
 */
public class LineSplitter implements CommandProcessor {
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

    private Code hasLine(List<GcodeMeta> commands) {
        if (commands == null) return null;
        for (GcodeMeta command : commands) {
            switch(command.code){
                case G0:
                    return G0;
                case G1:
                    return G1;
            }
        }
        return null;
    }

    @Override
    public List<String> processCommand(String commandString, GcodeState state) throws GcodeParserException {
        List<GcodeParser.GcodeMeta> commands = GcodeParserUtils.processCommand(commandString, 0, state);

        List<String> results = new ArrayList<>();

        Code code = hasLine(commands);
        if (code == null) {
            return Collections.singletonList(commandString);
        }

        SplitCommand sc = GcodePreprocessorUtils.extractMotion(code, commandString);
        if (sc.remainder.length() > 0) {
            results.add(sc.remainder);
        }

        GcodeMeta command = Iterables.getLast(commands);

        if (command == null || command.point == null) {
            throw new GcodeParserException("Internal parser error: missing data.");
        }

        // line length
        Position start = state.currentPoint;
        Position end = command.point.point();
        Position current = start;
        double length = start.distanceXYZ(end);

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

                Position next = new Position(newX, newY, newZ, start.getUnits());
                results.add(GcodePreprocessorUtils.generateLineFromPoints(
                        command.code, current, next, command.state.inAbsoluteMode, null));
                current = next;
            }

            // Add the last line point.
            results.add(GcodePreprocessorUtils.generateLineFromPoints(
                    command.code, current, end, command.state.inAbsoluteMode, null));
        } else {
            return Collections.singletonList(commandString);
        }

        return results;
    }
}
