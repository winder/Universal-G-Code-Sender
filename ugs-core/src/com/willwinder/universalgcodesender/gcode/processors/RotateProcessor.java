/*
    Copyright 2020 Will Winder

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
    along with UGS. If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserUtils;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils.normalizeCommand;

/**
 * A processor that will rotate the model around the given center position using the given rotation in radians.
 * If the commands have arcs they will be expanded to line segments first.
 *
 * @author Joacim Breiler
 */
public class RotateProcessor implements CommandProcessor {

    private ArcExpander arcExpander;
    private Position center;
    private double rotation;

    public RotateProcessor() {
        init(new Position(0, 0, 0, UnitUtils.Units.MM), 0);
    }

    public RotateProcessor(Position center, double rotation) {
        init(center, rotation);
    }

    private void init(Position center, double rotation) {
        this.center = center;
        this.rotation = rotation;
        this.arcExpander = new ArcExpander(true, 0.1);
    }

    public Position getCenter() {
        return center;
    }

    public void setCenter(Position center) {
        this.center = center;
    }

    public double getRotation() {
        return rotation;
    }

    public void setRotation(double rotation) {
        this.rotation = rotation;
    }

    @Override
    public List<String> processCommand(String command, GcodeState state) throws GcodeParserException {
        // Ignore this processor if no rotation should be made
        if (rotation == 0) {
            return Collections.singletonList(command);
        }

        List<String> commands = expandArcsToLines(command, state);
        return commands.stream()
                .map(c -> {
                    try {
                        return rotateCommandCoordinates(c, state);
                    } catch (GcodeParserException e) {
                        throw new RuntimeException("Could not rotate the given command coordinate", e);
                    }
                })
                .flatMap(Collection::stream).collect(Collectors.toList());
    }

    private List<String> expandArcsToLines(String command, GcodeState state) throws GcodeParserException {
        return arcExpander.processCommand(command, state);
    }

    private List<String> rotateCommandCoordinates(String command, GcodeState state) throws GcodeParserException {
        List<GcodeParser.GcodeMeta> gcodeMetas = GcodeParserUtils.processCommand(command, 0, state);
        if (!isMovement(gcodeMetas)) {
            return Collections.singletonList(command);
        }
        return gcodeMetas.stream()
                .map(gcodeMeta -> {
                    UnitUtils.Units currentUnits = UnitUtils.Units.getUnits(gcodeMeta.state.units);
                    Position start = state.currentPoint
                            .getPositionIn(currentUnits);

                    Position end = gcodeMeta.point.point()
                            .getPositionIn(currentUnits);
                    end = end.rotate(center.getPositionIn(currentUnits), rotation);

                    String adjustedCommand = GcodePreprocessorUtils.generateLineFromPoints(
                            gcodeMeta.code, start, end, gcodeMeta.state.inAbsoluteMode, null);

                    try {
                        return normalizeCommand(adjustedCommand, gcodeMeta.state);
                    } catch (GcodeParserException e) {
                        return adjustedCommand;
                    }
                })
                .collect(Collectors.toList());
    }

    private boolean isMovement(List<GcodeParser.GcodeMeta> commands) {
        if (commands == null) return false;
        boolean hasLine = false;
        for (GcodeParser.GcodeMeta command : commands) {
            switch (command.code) {
                case G0:
                case G1:
                case G2:
                case G3:
                    hasLine = true;
                    break;
            }
        }
        return hasLine;
    }

    @Override
    public String getHelp() {
        return "Rotates the model 180 degrees";
    }
}
