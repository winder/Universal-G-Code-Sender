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

import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils.SplitCommand;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Expands arcs, splits long moves, and shifts Z to a scanned surface in one pass.
 * Plain linear moves are read directly so a program rebuild does not parse each line
 * once per stage.
 */
public class AutoLevelProcessor implements CommandProcessor {
    private final ArcExpander arcExpander;
    private final double maxSegmentLength;
    private final double materialSurfaceHeightMM;
    private final List<Position> surfacePoints;
    private final DelaunaySurfaceInterpolator surface;
    private final Units surfaceMeshUnits;
    private CommandProcessorList legacy;

    public AutoLevelProcessor(double arcTolerance, double maxSegmentLength, double materialSurfaceHeightMM, List<Position> surfacePoints) {
        if (surfacePoints == null || surfacePoints.isEmpty()) {
            throw new IllegalArgumentException("Surface mesh is required.");
        }
        this.maxSegmentLength = maxSegmentLength;
        this.materialSurfaceHeightMM = materialSurfaceHeightMM;
        this.surfacePoints = List.copyOf(surfacePoints);
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
            return levelMove(command, state.currentPoint, move.end, move.motion, state.inAbsoluteMode);
        }
        if (containsArcWord(command) || state.currentMotionMode == Code.G2 || state.currentMotionMode == Code.G3) {
            List<String> expanded = arcExpander.processCommand(command, state);
            if (expanded.size() == 1 && command.equals(expanded.get(0))) {
                return expanded;
            }
            return levelExpanded(expanded, state);
        }
        return legacy(command, state);
    }

    @Override
    public String getHelp() {
        return "Levels Z to a scanned surface, expanding arcs and splitting long moves in one pass.";
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
        double length = start.distanceXYZ(end);
        if (!(length > maxSegmentLength)) {
            return Collections.singletonList(applyZ(command, start, end, absolute));
        }

        List<String> results = new ArrayList<>();
        SplitCommand split = GcodePreprocessorUtils.extractMotion(motion, command);
        if (split != null && split.remainder.length() > 0) {
            results.add(split.remainder);
        }

        int numSegments = (int) Math.ceil(length / maxSegmentLength);
        double segmentLength = length / Math.ceil(length / maxSegmentLength);
        Position current = start;
        // Z is applied to the coordinates as they are written. Formatting rounds those
        // coordinates, and the next segment starts from the rounded point.
        Position parsed = start;
        for (int i = 1; i < numSegments; i++) {
            double k = 1 / (length / (i * segmentLength));
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
        double zPointOffset;
        if (absolute) {
            Position position = end.getPositionIn(surfaceMeshUnits);
            double materialSurfaceHeight = materialSurfaceHeightMM * UnitUtils.scaleUnits(Units.MM, surfaceMeshUnits);
            zPointOffset = (surface.surfaceHeightAt(position.x, position.y) - materialSurfaceHeight)
                    * UnitUtils.scaleUnits(surfaceMeshUnits, end.getUnits());
        } else {
            Position startInMesh = start.getPositionIn(surfaceMeshUnits);
            Position endInMesh = end.getPositionIn(surfaceMeshUnits);
            double startHeight = surface.surfaceHeightAt(startInMesh.x, startInMesh.y);
            double endHeight = surface.surfaceHeightAt(endInMesh.x, endInMesh.y);
            zPointOffset = (endHeight - startHeight) * UnitUtils.scaleUnits(surfaceMeshUnits, end.getUnits());
        }

        double newZ = end.getZ() + zPointOffset;
        PartialPosition.Builder overrideZ = PartialPosition.builder(end.getUnits());
        if (absolute) {
            overrideZ.setZ(newZ);
        } else {
            overrideZ.setZ(newZ - start.getZ());
        }
        return GcodePreprocessorUtils.overridePosition(command, overrideZ.build());
    }

    private List<String> legacy(String command, GcodeState state) throws GcodeParserException {
        if (legacy == null) {
            legacy = new CommandProcessorList();
            legacy.add(arcExpander);
            legacy.add(new LineSplitter(maxSegmentLength));
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
