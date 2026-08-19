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
package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.gcode.GcodeParser.GcodeMeta;
import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserUtils;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.Axis;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Compensates for the mechanical backlash of a machine.
 *
 * <p>When an axis changes direction an extra movement is inserted which only takes up the play
 * of that axis, this keeps the play from being distributed along the following movement which
 * would round off corners. The following movements are shifted with the backlash so that the
 * tool ends up at the intended position.
 *
 * <p>The position of the play is unknown when the first movement of an axis is made, it is
 * therefore assumed that it needs to be compensated. Rapid movements are used to wind up the axes
 * that have not moved yet in a known direction, taking up their play while the tool isn't cutting
 * instead of in the middle of a cut.
 *
 * <p>Arcs are expanded to line segments as their direction changes along the arc.
 *
 * @author Joacim Breiler
 */
public class BacklashCompensator implements CommandProcessor {
    private static final double MINIMUM_MOVEMENT_MM = 0.0001;
    // Winding up in the positive direction will never push the tool into the material
    private static final int WIND_UP_DIRECTION = 1;
    private static final List<Axis> COMPENSATED_AXES = Arrays.asList(Axis.X, Axis.Y, Axis.Z);

    private final Position backlash;
    private final ArcExpander arcExpander;
    private final DecimalFormat decimalFormat = new DecimalFormat("#.#########", Localization.dfs);
    private final Map<Axis, Integer> compensatedDirection = new EnumMap<>(Axis.class);
    private final Map<Axis, Double> offset = new EnumMap<>(Axis.class);

    public BacklashCompensator(Position backlash, double arcSegmentLengthMM) {
        this.backlash = backlash.getPositionIn(Units.MM);
        this.arcExpander = new ArcExpander(true, arcSegmentLengthMM);
    }

    @Override
    public List<String> processCommand(String command, GcodeState state) throws GcodeParserException {
        if (!hasBacklash()) {
            return Collections.singletonList(command);
        }

        List<String> result = new ArrayList<>();
        GcodeState currentState = state;
        for (String expandedCommand : expandArcsToLines(command, state)) {
            List<GcodeMeta> gcodeMetas = GcodeParserUtils.processCommand(expandedCommand, 0, currentState, true);
            result.addAll(compensateCommand(expandedCommand, gcodeMetas, currentState));
            currentState = getStateAfterCommand(gcodeMetas, currentState);
        }

        return result;
    }

    @Override
    public void reset() {
        compensatedDirection.clear();
        offset.clear();
    }

    @Override
    public String getHelp() {
        return Localization.getString("sender.help.backlash") + "\n"
                + Localization.getString("sender.backlash.amount")
                + ": X" + decimalFormat.format(backlash.getX())
                + " Y" + decimalFormat.format(backlash.getY())
                + " Z" + decimalFormat.format(backlash.getZ());
    }

    private boolean hasBacklash() {
        return COMPENSATED_AXES.stream().anyMatch(axis -> getBacklash(axis) > 0);
    }

    private List<String> expandArcsToLines(String command, GcodeState state) throws GcodeParserException {
        return arcExpander.processCommand(command, state);
    }

    private List<String> compensateCommand(String command, List<GcodeMeta> gcodeMetas, GcodeState state) {
        GcodeMeta movement = getMovement(gcodeMetas);
        if (movement == null || state.currentPoint == null) {
            return Collections.singletonList(command);
        }

        Map<Axis, Double> previousOffset = new EnumMap<>(offset);
        updateOffsets(state.currentPoint, movement.point.point());

        List<String> result = new ArrayList<>();
        createTakeUpCommand(command, movement, state.currentPoint, previousOffset).ifPresent(result::add);

        // In relative mode the take up command has already moved the axes with the changed offset
        result.add(movement.state.inAbsoluteMode ? offsetAxisWords(command, movement.state) : command);

        createWindUpCommand(command, movement).ifPresent(result::add);
        return result;
    }

    // Only moves the axes that changed direction, the tool will stand still as long as the
    // configured backlash matches the play of the machine
    private Optional<String> createTakeUpCommand(String command, GcodeMeta movement, Position start, Map<Axis, Double> previousOffset) {
        Units units = Units.getUnits(movement.state.units);
        double scale = UnitUtils.scaleUnits(Units.MM, units);
        Position startPosition = start.getPositionIn(units);

        Map<Axis, Double> axisValues = new EnumMap<>(Axis.class);
        COMPENSATED_AXES.stream()
                .filter(axis -> getOffset(offset, axis) != getOffset(previousOffset, axis))
                .forEach(axis -> axisValues.put(axis, movement.state.inAbsoluteMode
                        ? startPosition.get(axis) + (getOffset(offset, axis) * scale)
                        : (getOffset(offset, axis) - getOffset(previousOffset, axis)) * scale));

        return createMovementCommand(command, movement, axisValues);
    }

    // Takes up the play of the axes that has not moved yet, only done on rapid movements as the
    // tool would otherwise be displaced with the unknown play in the middle of a cut
    private Optional<String> createWindUpCommand(String command, GcodeMeta movement) {
        if (movement.code != Code.G0) {
            return Optional.empty();
        }

        Units units = Units.getUnits(movement.state.units);
        double scale = UnitUtils.scaleUnits(Units.MM, units);
        Position endPosition = movement.point.point().getPositionIn(units);

        Map<Axis, Double> axisValues = new EnumMap<>(Axis.class);
        COMPENSATED_AXES.stream()
                .filter(axis -> getBacklash(axis) > 0)
                .filter(axis -> !compensatedDirection.containsKey(axis))
                .filter(axis -> !Double.isNaN(endPosition.get(axis)))
                .forEach(axis -> {
                    updateOffset(axis, WIND_UP_DIRECTION);
                    axisValues.put(axis, movement.state.inAbsoluteMode
                            ? endPosition.get(axis) + (getOffset(offset, axis) * scale)
                            : getOffset(offset, axis) * scale);
                });

        return createMovementCommand(command, movement, axisValues);
    }

    private Optional<String> createMovementCommand(String command, GcodeMeta movement, Map<Axis, Double> axisValues) {
        if (axisValues.isEmpty()) {
            return Optional.empty();
        }

        StringBuilder result = new StringBuilder(movement.code.toString());
        axisValues.forEach((axis, value) -> result.append(axis.name()).append(decimalFormat.format(value)));
        getFeedRateWord(command).ifPresent(result::append);
        return Optional.of(result.toString());
    }

    private Optional<String> getFeedRateWord(String command) {
        return GcodePreprocessorUtils.splitCommand(command).stream()
                .filter(word -> Character.toUpperCase(word.charAt(0)) == 'F')
                .findFirst();
    }

    private void updateOffsets(Position start, Position end) {
        Position startInMillimeters = start.getPositionIn(Units.MM);
        Position endInMillimeters = end.getPositionIn(Units.MM);
        COMPENSATED_AXES.forEach(axis -> updateOffset(axis,
                getDirection(startInMillimeters.get(axis), endInMillimeters.get(axis))));
    }

    private void updateOffset(Axis axis, int direction) {
        if (direction == 0) {
            return;
        }

        // The position of the play is unknown on the first movement, assume it needs to be taken up
        compensatedDirection.putIfAbsent(axis, direction);
        boolean needsCompensation = compensatedDirection.get(axis) == direction;
        offset.put(axis, needsCompensation ? direction * getBacklash(axis) : 0d);
    }

    private int getDirection(double start, double end) {
        if (Double.isNaN(start) || Double.isNaN(end) || Math.abs(end - start) < MINIMUM_MOVEMENT_MM) {
            return 0;
        }

        return end > start ? 1 : -1;
    }

    private String offsetAxisWords(String command, GcodeState state) {
        double scale = UnitUtils.scaleUnits(Units.MM, Units.getUnits(state.units));

        StringBuilder result = new StringBuilder();
        for (String word : GcodePreprocessorUtils.splitCommand(command)) {
            Axis axis = getAxis(word);
            Double value = axis == null ? null : parseValue(word);
            if (value == null) {
                result.append(word);
                continue;
            }

            result.append(word.charAt(0)).append(decimalFormat.format(value + (getOffset(offset, axis) * scale)));
        }

        return result.toString();
    }

    private GcodeMeta getMovement(List<GcodeMeta> gcodeMetas) {
        // Movements in machine coordinates are not tracked by the parser so they can't be compensated
        if (gcodeMetas == null || gcodeMetas.stream().anyMatch(meta -> meta.code == Code.G53)) {
            return null;
        }

        return gcodeMetas.stream()
                .filter(meta -> meta.point != null && (meta.code == Code.G0 || meta.code == Code.G1))
                .reduce((first, second) -> second)
                .orElse(null);
    }

    private GcodeState getStateAfterCommand(List<GcodeMeta> gcodeMetas, GcodeState state) {
        if (gcodeMetas == null) {
            return state;
        }

        return gcodeMetas.stream()
                .map(meta -> meta.state)
                .filter(Objects::nonNull)
                .reduce((first, second) -> second)
                .orElse(state);
    }

    private Axis getAxis(String word) {
        return switch (Character.toUpperCase(word.charAt(0))) {
            case 'X' -> Axis.X;
            case 'Y' -> Axis.Y;
            case 'Z' -> Axis.Z;
            default -> null;
        };
    }

    private Double parseValue(String word) {
        try {
            return Double.valueOf(word.substring(1));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    private double getBacklash(Axis axis) {
        double value = backlash.get(axis);
        return Double.isNaN(value) ? 0 : Math.abs(value);
    }

    private double getOffset(Map<Axis, Double> offsets, Axis axis) {
        return offsets.getOrDefault(axis, 0d);
    }
}
