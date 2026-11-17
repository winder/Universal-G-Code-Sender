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

import com.google.common.collect.ImmutableList;
import com.willwinder.universalgcodesender.gcode.GcodeParser.GcodeMeta;
import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserUtils;
import com.willwinder.universalgcodesender.model.PartialPosition;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Adjust Z heights based on a provided surface mesh.
 *
 * @author wwinder
 */
public class MeshLeveler implements CommandProcessor {
    public final static String ERROR_MESH_SHAPE = "Surface mesh must be a rectangular 2D array.";
    public final static String ERROR_NOT_ENOUGH_SAMPLES = "Need at least 2 samples along each axis.";
    public final static String ERROR_X_ALIGNMENT = "Unaligned x coordinate in surface grid.";
    public final static String ERROR_Y_ALIGNMENT = "Unaligned y coordinate in surface grid.";
    public final static String ERROR_Y_ASCENTION = "Found a y coordinate that isn't ascending.";
    public final static String ERROR_X_ASCENTION = "Found a x coordinate that isn't ascending.";
    public final static String ERROR_UNEXPECTED_ARC = "The mesh leveler cannot process arcs. Enable the arc expander.";
    public final static String ERROR_MISSING_POINT_DATA = "Internal parser error: missing data. ";
    private final double materialSurfaceHeightMM;
    private final DelaunaySurfaceInterpolator surface;
    private final Units surfaceMeshUnits;

    /**
     * @param materialSurfaceHeightMM Z height used in offset.
     * @param surfaceMesh             2D array in the format Position[x][y]
     */
    public MeshLeveler(double materialSurfaceHeightMM, Position[][] surfaceMesh) {
        this(materialSurfaceHeightMM, flatten(surfaceMesh));
    }

    /**
     * @param materialSurfaceHeightMM Z height used in offset.
     * @param surfacePoints           arbitrary surface samples in one unit system
     */
    public MeshLeveler(double materialSurfaceHeightMM, List<Position> surfacePoints) {
        if (surfacePoints == null || surfacePoints.isEmpty()) {
            throw new IllegalArgumentException("Surface mesh is required.");
        }
        this.materialSurfaceHeightMM = materialSurfaceHeightMM;
        this.surface = new DelaunaySurfaceInterpolator(surfacePoints);
        this.surfaceMeshUnits = surface.getUnits();
    }

    private static List<Position> flatten(Position[][] surfaceMesh) {
        if (surfaceMesh == null || surfaceMesh.length == 0 || surfaceMesh[0] == null) {
            throw new IllegalArgumentException("Surface mesh is required.");
        }
        return Arrays.stream(surfaceMesh)
                .flatMap(Arrays::stream)
                .collect(Collectors.toList());
    }

    private boolean ensureJustLines(List<GcodeMeta> commands) throws GcodeParserException {
        if (commands == null) return false;
        boolean hasLine = false;
        for (GcodeMeta command : commands) {
            switch (command.code) {
                case G0:
                case G1:
                    hasLine = true;
                    break;
                case G2:
                case G3:
                    throw new GcodeParserException(ERROR_UNEXPECTED_ARC);
            }
        }
        return hasLine;
    }

    @Override
    public List<String> processCommand(final String commandString, GcodeState state) throws GcodeParserException {
        List<GcodeMeta> commands = GcodeParserUtils.processCommand(commandString, 0, state);

        // If there are no lines, return unmodified input.
        if (!ensureJustLines(commands)) {
            return Collections.singletonList(commandString);
        }

        ImmutableList.Builder<String> adjustedCommands = ImmutableList.builder();
        for (GcodeMeta command : commands) {
            if (command == null) {
                throw new GcodeParserException(ERROR_MISSING_POINT_DATA + commandString);
            }
            if (command.point == null) {
                adjustedCommands.add(commandString);
                continue;
            }

            Position start = state.currentPoint;
            Position end = command.point.point();

            // Get offset relative to the expected surface height.
            // Visualizer normalizes everything to MM but probe mesh might be INCH
            double zPointOffset;
            if (state.inAbsoluteMode) {
                Position position = end.getPositionIn(surfaceMeshUnits);
                double materialSurfaceHeight = this.materialSurfaceHeightMM * UnitUtils.scaleUnits(Units.MM, surfaceMeshUnits);
                zPointOffset = (surfaceHeightAt(position.x, position.y) - materialSurfaceHeight) * UnitUtils.scaleUnits(surfaceMeshUnits, end.getUnits());
            } else {
                // TODO: If the first move in the gcode file is relative it won't properly take the materialSurfaceHeight
                // into account. To fix the CommandProcessor needs to inject an adjustment before that first relative move
                // happens. Until that happens the user must make sure the materialSurfaceHeight is zero.

                // In relative mode we only need to adjust by the z delta between the starting and ending point
                Position startPositionInMeshUnits = start.getPositionIn(surfaceMeshUnits);
                double startHeight = surfaceHeightAt(startPositionInMeshUnits.x, startPositionInMeshUnits.y);
                Position endPositionInMeshUnits = end.getPositionIn(surfaceMeshUnits);
                double endHeight = surfaceHeightAt(endPositionInMeshUnits.x, endPositionInMeshUnits.y);
                zPointOffset = (endHeight - startHeight) * UnitUtils.scaleUnits(surfaceMeshUnits, end.getUnits());
            }

            // Update z coordinate.
            double newZ = end.getZ() + zPointOffset;

            PartialPosition.Builder overrideZ = PartialPosition.builder(end.getUnits());
            if (command.state.inAbsoluteMode) {
                overrideZ.setZ(newZ);
            } else {
                overrideZ.setZ(newZ - start.getZ());
            }
            String adjustedCommand = GcodePreprocessorUtils.overridePosition(commandString, overrideZ.build());
            adjustedCommands.add(adjustedCommand);
        }

        return adjustedCommands.build();
    }

    /**
     * Get the surface height and return it in the surface mesh units.
     */
    protected double surfaceHeightAt(double x, double y) {
        return surface.surfaceHeightAt(x, y);
    }

    @Override
    public String getHelp() {
        return null;
    }
}
