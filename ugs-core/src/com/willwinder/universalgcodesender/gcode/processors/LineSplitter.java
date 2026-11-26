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
 * Shared G0/G1 splitting. Subclasses decide where a straight move is cut.
 *
 * @author wwinder
 */
public abstract class LineSplitter implements CommandProcessor {

    @Override
    public String getHelp() {
        return "Split G0 and G1 commands into multiple commands.";
    }

    /**
     * Parameters {@code t} in {@code (0, 1)} where the move from {@code start} to {@code end} is cut.
     * Empty when the move should be left unchanged.
     */
    protected abstract List<Double> splitParameters(Position start, Position end);

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

        GcodeMeta command = Iterables.getLast(commands);

        if (command == null) {
            throw new GcodeParserException("Internal parser error: missing data. " + commandString);
        }
        if (command.point == null) {
            // No point data associated with this command (Maybe just setting feed rate), leave it as-is
            return Collections.singletonList(commandString);
        }

        Position start = state.currentPoint;
        Position end = command.point.point();
        List<Double> splits = splitParameters(start, end);

        // Modal words stay on the original command unless the move is actually split.
        if (splits.isEmpty()) {
            return Collections.singletonList(commandString);
        }

        if (sc.remainder.length() > 0) {
            results.add(sc.remainder);
        }
        Position current = start;
        for (double k : splits) {
            Position next = new Position(
                    start.x + k * (end.x - start.x),
                    start.y + k * (end.y - start.y),
                    start.z + k * (end.z - start.z),
                    start.getUnits());
            results.add(GcodePreprocessorUtils.generateLineFromPoints(
                    command.code, current, next, command.state.inAbsoluteMode, null));
            current = next;
        }
        results.add(GcodePreprocessorUtils.generateLineFromPoints(
                command.code, current, end, command.state.inAbsoluteMode, null));
        return results;
    }
}
