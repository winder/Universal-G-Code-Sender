/*
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
import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils.SplitCommand;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserUtils;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import com.willwinder.universalgcodesender.utils.AutoLevelArcMode;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Shifts Z to a scanned surface in one pass. A straight move is split only where it crosses
 * from one surface triangle into another, so a sparse scan does not chop every line.
 * Arcs are either expanded into line segments or kept, with Z shifted only at the start and end.
 * Plain linear moves are read directly so a program rebuild does not parse each line
 * once per stage.
 */
public class AutoLevelProcessor implements CommandProcessor {
    private static final double Z_EPSILON = 1e-6;
    private static final double POINT_EPSILON_MM = 1e-4;

    private final ArcExpander arcExpander;
    private final AutoLevelArcMode arcMode;
    private final double materialSurfaceHeightMM;
    private final List<Position> surfacePoints;
    private final DelaunaySurfaceInterpolator surface;
    private final Units surfaceMeshUnits;
    private final Position lastLeveledPoint = new Position(Double.NaN, Double.NaN, Double.NaN, Units.MM);
    private boolean hasLeveledPoint;
    private CommandProcessorList legacy;

    public AutoLevelProcessor(double arcTolerance, double materialSurfaceHeightMM, List<Position> surfacePoints) {
        this(arcTolerance, materialSurfaceHeightMM, surfacePoints, AutoLevelArcMode.LINE_SEGMENTS);
    }

    public AutoLevelProcessor(double arcTolerance, double materialSurfaceHeightMM, List<Position> surfacePoints, AutoLevelArcMode arcMode) {
        if (surfacePoints == null || surfacePoints.isEmpty()) {
            throw new IllegalArgumentException("Surface mesh is required.");
        }
        this.materialSurfaceHeightMM = materialSurfaceHeightMM;
        this.surfacePoints = List.copyOf(surfacePoints);
        this.arcMode = arcMode == null ? AutoLevelArcMode.LINE_SEGMENTS : arcMode;
        this.arcExpander = new ArcExpander(true, arcTolerance, GcodePreprocessorUtils.getDecimalFormatter(), true);
        this.surface = new DelaunaySurfaceInterpolator(this.surfacePoints);
        this.surfaceMeshUnits = surface.getUnits();
    }

    @Override
    public List<String> processCommand(String command, GcodeState state) throws GcodeParserException {
        if (command == null || command.isEmpty() || state.currentPoint == null) {
            return Collections.singletonList(command);
        }

        LinearMove move = LinearMove.tryParse(command, state.currentPoint, state.inAbsoluteMode, state.currentMotionMode);
        if (move != null) {
            List<String> leveled = levelMove(command, state.currentPoint, move.end, move.motion, state.inAbsoluteMode);
            remember(move.end);
            return leveled;
        }
        if (containsArcWord(command) || state.currentMotionMode == Code.G2 || state.currentMotionMode == Code.G3) {
            if (arcMode == AutoLevelArcMode.KEEP_ARCS) {
                return levelArc(command, state);
            }
            List<String> expanded = arcExpander.processCommand(command, state);
            if (expanded.size() == 1 && command.equals(expanded.get(0))) {
                return expanded;
            }
            return levelExpanded(expanded, state);
        }
        List<String> leveled = legacy(command, state);
        rememberParsedEnd(command, state);
        return leveled;
    }

    @Override
    public void reset() {
        hasLeveledPoint = false;
        if (legacy != null) {
            legacy.reset();
        }
    }

    @Override
    public String getHelp() {
        if (arcMode == AutoLevelArcMode.KEEP_ARCS) {
            return "Levels Z to a scanned surface. Arcs are kept and Z is shifted at the start and end.";
        }
        return "Levels Z to a scanned surface, expanding arcs and splitting moves where the surface slope changes.";
    }

    /**
     * Keeps the G2/G3 command, including I/J/K or R. Z at the end is shifted onto the surface.
     * Z at the start is shifted too, by a linear move, when an earlier leveled move has not
     * already left the tool there. The controller then changes Z linearly along the arc.
     */
    private List<String> levelArc(String command, GcodeState state) throws GcodeParserException {
        if (state.currentPoint == null) {
            return Collections.singletonList(command);
        }
        GcodeMeta arc = findArc(GcodeParserUtils.processCommand(command, 0, state, true));
        if (arc == null || arc.point == null) {
            return Collections.singletonList(command);
        }

        Position start = state.currentPoint;
        Position end = arc.point.point();
        List<String> out = new ArrayList<>();
        String motion = command;
        SplitCommand split = GcodePreprocessorUtils.extractMotion(arc.code, command);
        if (split != null && split.remainder.length() > 0) {
            out.add(split.remainder);
            motion = split.extracted;
        }
        if (!containsArcWord(motion)) {
            motion = arc.code + " " + motion;
        }
        if (!alreadyLeveled(start)) {
            String correction = startCorrection(start, state.inAbsoluteMode);
            if (correction != null) {
                out.add(correction);
            }
        }
        out.add(applyZ(motion, start, end, state.inAbsoluteMode));
        remember(end);
        return out;
    }

    private static GcodeMeta findArc(List<GcodeMeta> commands) {
        if (commands == null) {
            return null;
        }
        GcodeMeta arc = null;
        for (GcodeMeta command : commands) {
            if (command.point != null && command.point.isArc()) {
                arc = command;
            }
        }
        return arc;
    }

    /**
     * A Z-only feed move that puts the tool on the surface at the arc start.
     * Returns null when that Z is already the programmed Z.
     */
    private String startCorrection(Position start, boolean absolute) {
        if (!isFinite(start)) {
            return null;
        }
        double offset = zOffsetAt(start);
        if (!Double.isFinite(offset) || Math.abs(offset) < Z_EPSILON) {
            return null;
        }
        if (absolute) {
            return applyZ("G1", start, start, true);
        }
        return GcodePreprocessorUtils.overridePosition("G1", PartialPosition.builder(start.getUnits()).setZ(offset).build());
    }

    private void remember(Position programmed) {
        if (!isFinite(programmed)) {
            return;
        }
        Position millimeters = programmed.getPositionIn(Units.MM);
        lastLeveledPoint.x = millimeters.x;
        lastLeveledPoint.y = millimeters.y;
        lastLeveledPoint.z = millimeters.z;
        hasLeveledPoint = true;
    }

    private void rememberParsedEnd(String command, GcodeState state) {
        try {
            List<GcodeMeta> metas = GcodeParserUtils.processCommand(command, 0, state, true);
            if (metas == null) {
                return;
            }
            for (GcodeMeta meta : metas) {
                if (meta.point != null && meta.point.point() != null) {
                    remember(meta.point.point());
                }
            }
        } catch (GcodeParserException ignored) {
            // The command was already emitted. Keep the previous leveled point.
        }
    }

    private boolean alreadyLeveled(Position point) {
        if (!hasLeveledPoint || !isFinite(point)) {
            return false;
        }
        Position millimeters = point.getPositionIn(Units.MM);
        return Math.abs(millimeters.x - lastLeveledPoint.x) <= POINT_EPSILON_MM
                && Math.abs(millimeters.y - lastLeveledPoint.y) <= POINT_EPSILON_MM
                && Math.abs(millimeters.z - lastLeveledPoint.z) <= POINT_EPSILON_MM;
    }

    private static boolean isFinite(Position point) {
        return point != null
                && Double.isFinite(point.getX())
                && Double.isFinite(point.getY())
                && Double.isFinite(point.getZ());
    }

    private List<String> levelExpanded(List<String> expanded, GcodeState state) {
        List<String> out = new ArrayList<>(expanded.size());
        Position cursor = state.currentPoint;
        for (String segment : expanded) {
            LinearMove move = LinearMove.tryParse(segment, cursor, state.inAbsoluteMode, state.currentMotionMode);
            if (move == null) {
                out.add(segment);
                continue;
            }
            out.addAll(levelMove(segment, cursor, move.end, move.motion, state.inAbsoluteMode));
            cursor = move.end;
        }
        return out;
    }

    private List<String> levelMove(String command, Position start, Position end, Code motion, boolean absolute) {
        Position startOnSurface = start.getPositionIn(surfaceMeshUnits);
        Position endOnSurface = end.getPositionIn(surfaceMeshUnits);
        List<Double> splits = surface.crossingParameters(
                startOnSurface.x, startOnSurface.y, endOnSurface.x, endOnSurface.y);
        if (splits.isEmpty()) {
            return Collections.singletonList(applyZ(command, start, end, absolute));
        }

        List<String> results = new ArrayList<>();
        SplitCommand split = GcodePreprocessorUtils.extractMotion(motion, command);
        if (split != null && split.remainder.length() > 0) {
            results.add(split.remainder);
        }

        Position current = start;
        // Z is applied to the coordinates as they are written. Formatting rounds those
        // coordinates, and the next segment starts from the rounded point.
        Position parsed = start;
        for (double k : splits) {
            Position next = new Position(
                    start.x + k * (end.x - start.x),
                    start.y + k * (end.y - start.y),
                    start.z + k * (end.z - start.z),
                    start.getUnits());
            String segment = GcodePreprocessorUtils.generateLineFromPoints(motion, current, next, absolute, null);
            Position parsedEnd = parsedEnd(segment, parsed, absolute, motion, next);
            results.add(applyZ(segment, parsed, parsedEnd, absolute));
            parsed = parsedEnd;
            current = next;
        }
        String last = GcodePreprocessorUtils.generateLineFromPoints(motion, current, end, absolute, null);
        results.add(applyZ(last, parsed, parsedEnd(last, parsed, absolute, motion, end), absolute));
        return results;
    }

    private static Position parsedEnd(String segment, Position start, boolean absolute, Code motion, Position fallback) {
        LinearMove parsed = LinearMove.tryParse(segment, start, absolute, motion);
        return parsed == null ? fallback : parsed.end;
    }

    private String applyZ(String command, Position start, Position end, boolean absolute) {
        double zPointOffset = absolute ? zOffsetAt(end) : zOffsetAt(end) - zOffsetAt(start);

        double newZ = end.getZ() + zPointOffset;
        PartialPosition.Builder overrideZ = PartialPosition.builder(end.getUnits());
        if (absolute) {
            overrideZ.setZ(newZ);
        } else {
            overrideZ.setZ(newZ - start.getZ());
        }
        return GcodePreprocessorUtils.overridePosition(command, overrideZ.build());
    }

    /**
     * Surface correction added to a programmed Z, in that point's units.
     * Zero outside the scanned area.
     */
    private double zOffsetAt(Position point) {
        Position position = point.getPositionIn(surfaceMeshUnits);
        double materialSurfaceHeight = materialSurfaceHeightMM * UnitUtils.scaleUnits(Units.MM, surfaceMeshUnits);
        return (surface.surfaceHeightAt(position.x, position.y) - materialSurfaceHeight)
                * UnitUtils.scaleUnits(surfaceMeshUnits, point.getUnits());
    }

    private List<String> legacy(String command, GcodeState state) throws GcodeParserException {
        if (legacy == null) {
            legacy = new CommandProcessorList();
            legacy.add(arcExpander);
            legacy.add(new LineSplitterTriangleIntersections(surface));
            legacy.add(new MeshLeveler(materialSurfaceHeightMM, surfacePoints));
        }
        return legacy.processCommand(command, state);
    }

    private static boolean containsArcWord(String command) {
        int length = command.length();
        for (int i = 0; i < length; i++) {
            char c = command.charAt(i);
            if (c != 'G' && c != 'g') {
                continue;
            }
            int j = i + 1;
            while (j < length && command.charAt(j) == ' ') {
                j++;
            }
            while (j < length && command.charAt(j) == '0') {
                j++;
            }
            if (j >= length) {
                continue;
            }
            char motion = command.charAt(j);
            if (motion != '2' && motion != '3') {
                continue;
            }
            int after = j + 1;
            if (after < length) {
                char next = command.charAt(after);
                if (next >= '0' && next <= '9') {
                    continue;
                }
            }
            return true;
        }
        return false;
    }

    /**
     * A G0/G1 with only axis words and an optional feed. Anything else returns null
     * so the caller can use the existing processors.
     */
    private static final class LinearMove {
        final Code motion;
        final Position end;

        LinearMove(Code motion, Position end) {
            this.motion = motion;
            this.end = end;
        }

        static LinearMove tryParse(String command, Position start, boolean absolute, Code modalMotion) {
            int length = command.length();
            boolean sawAxis = false;
            boolean sawX = false;
            boolean sawY = false;
            boolean sawZ = false;
            boolean sawF = false;
            double x = 0;
            double y = 0;
            double z = 0;
            Code motion = null;

            int i = 0;
            while (i < length) {
                char c = command.charAt(i);
                if (c == ' ' || c == '\t') {
                    i++;
                    continue;
                }
                char word = c >= 'a' && c <= 'z' ? (char) (c - 32) : c;
                if (word < 'A' || word > 'Z') {
                    return null;
                }
                i++;
                int numberStart = i;
                if (i < length) {
                    char sign = command.charAt(i);
                    if (sign == '+' || sign == '-') {
                        i++;
                    }
                }
                int digits = i;
                while (i < length) {
                    char d = command.charAt(i);
                    if ((d >= '0' && d <= '9') || d == '.') {
                        i++;
                    } else {
                        break;
                    }
                }
                if (i == digits) {
                    return null;
                }
                double value;
                try {
                    value = Double.parseDouble(command.substring(numberStart, i));
                } catch (NumberFormatException ex) {
                    return null;
                }

                switch (word) {
                    case 'G':
                        if (motion != null) {
                            return null;
                        }
                        if (value == 0d) {
                            motion = Code.G0;
                        } else if (value == 1d) {
                            motion = Code.G1;
                        } else {
                            return null;
                        }
                        break;
                    case 'X':
                        if (sawX) {
                            return null;
                        }
                        sawX = true;
                        sawAxis = true;
                        x = value;
                        break;
                    case 'Y':
                        if (sawY) {
                            return null;
                        }
                        sawY = true;
                        sawAxis = true;
                        y = value;
                        break;
                    case 'Z':
                        if (sawZ) {
                            return null;
                        }
                        sawZ = true;
                        sawAxis = true;
                        z = value;
                        break;
                    case 'F':
                        if (sawF) {
                            return null;
                        }
                        sawF = true;
                        break;
                    default:
                        return null;
                }
            }

            if (!sawAxis) {
                return null;
            }
            if (motion == null) {
                if (modalMotion == Code.G0) {
                    motion = Code.G0;
                } else if (modalMotion == Code.G1) {
                    motion = Code.G1;
                } else {
                    return null;
                }
            }

            Position end = new Position(start);
            if (absolute) {
                if (sawX) {
                    end.x = x;
                }
                if (sawY) {
                    end.y = y;
                }
                if (sawZ) {
                    end.z = z;
                }
            } else {
                if (sawX) {
                    end.x = Double.isNaN(end.x) ? x : end.x + x;
                }
                if (sawY) {
                    end.y = Double.isNaN(end.y) ? y : end.y + y;
                }
                if (sawZ) {
                    end.z = Double.isNaN(end.z) ? z : end.z + z;
                }
            }
            return new LinearMove(motion, end);
        }
    }
}
