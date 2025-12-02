package com.willwinder.universalgcodesender.gcode.processors;

import com.google.common.collect.Iterables;
import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.Code;
import static com.willwinder.universalgcodesender.gcode.util.Code.G0;
import static com.willwinder.universalgcodesender.gcode.util.Code.G1;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserUtils;
import com.willwinder.universalgcodesender.model.Position;

import java.util.Collections;
import java.util.List;

/**
 * Simplifies the line segments so that lines that are shorter than the given min segment length will be ignored.
 */
public class SimplifyProcessor implements CommandProcessor {
    final double minSegmentLength;

    /**
     * Creates a processor which will filter out line segments that are shorter than given threshold.
     *
     * @param minSegmentLength the minimum length of a single line.
     */
    public SimplifyProcessor(double minSegmentLength) {
        this.minSegmentLength = minSegmentLength;
    }

    private Code hasLine(List<GcodeParser.GcodeMeta> commands) {
        if (commands == null) return null;
        for (GcodeParser.GcodeMeta command : commands) {
            switch (command.code) {
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
        if (minSegmentLength == 0.0) {
            return Collections.singletonList(commandString);
        }

        List<GcodeParser.GcodeMeta> commands = GcodeParserUtils.processCommand(commandString, 0, state);
        Code code = hasLine(commands);
        if (code == null) {
            return Collections.singletonList(commandString);
        }

        GcodeParser.GcodeMeta command = Iterables.getLast(commands);
        if (command == null || command.point == null) {
            return Collections.singletonList(commandString);
        }

        // line length
        Position start = state.currentPoint;
        Position end = command.point.point();
        double length = start.distanceXYZ(end);

        // Check if line needs splitting.
        if (length >= this.minSegmentLength || Double.isNaN(length)) {
            return Collections.singletonList(commandString);
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public String getHelp() {
        return "Simplifies the gcode making sure that the line segments are not shorter than threshold";
    }
}
