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

import java.util.Collections;
import java.util.List;

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
    private final Position[][] surfaceMesh;
    private Position lowerLeft;
    private Position topRight;
    private final int xLen, yLen;
    private final double resolution;
    private final Units surfaceMeshUnits;

    /**
     * @param materialSurfaceHeightMM Z height used in offset.
     * @param surfaceMesh             2D array in the format Position[x][y]
     */
    public MeshLeveler(double materialSurfaceHeightMM, Position[][] surfaceMesh) {
        if (surfaceMesh == null) {
            throw new IllegalArgumentException("Surface mesh is required.");
        }

        this.materialSurfaceHeightMM = materialSurfaceHeightMM;
        this.yLen = surfaceMesh[0].length;
        this.xLen = surfaceMesh.length;

        validateMesh(surfaceMesh);
        this.surfaceMesh = surfaceMesh;
        this.surfaceMeshUnits = surfaceMesh[0][0].getUnits();
        this.resolution = Math.max(
                surfaceMesh[1][0].x - surfaceMesh[0][0].x,
                surfaceMesh[0][1].y - surfaceMesh[0][0].y);

        recalculateBounds();
    }

    private void recalculateBounds() {
        this.lowerLeft = surfaceMesh[0][0];
        this.topRight = new Position(this.lowerLeft);

        for (Position[] row : surfaceMesh) {
            for (Position cell : row) {
                this.topRight.setX(Math.max(cell.getX(), this.topRight.getX()));
                this.topRight.setY(Math.max(cell.getY(), this.topRight.getY()));
            }
        }
    }

    private void validateMesh(Position[][] surfaceMesh) {
        // Validate that points form a rectangular 2D array.
        for (Position[] arr : surfaceMesh) {
            if (arr.length != yLen) {
                throw new IllegalArgumentException(ERROR_MESH_SHAPE);
            }
        }

        // Need at least 4 points for bilinear interpolation.
        if (xLen < 2 || yLen < 2) {
            throw new IllegalArgumentException(ERROR_NOT_ENOUGH_SAMPLES);
        }

        // Verify that x points are aligned and y points are ascending.
        for (int xIdx = 0; xIdx < xLen; xIdx++) {
            double xCoord = surfaceMesh[xIdx][0].x;
            double yCoord = surfaceMesh[xIdx][0].y;
            for (int yIdx = 0; yIdx < yLen; yIdx++) {
                if (surfaceMesh[xIdx][yIdx].x != xCoord) {
                    String err = "@ " + xIdx + ", " + yIdx + ": (" + xCoord + " != " + surfaceMesh[xIdx][yIdx].x + ")";
                    throw new IllegalArgumentException(ERROR_X_ALIGNMENT + err);
                }
                if (yCoord > surfaceMesh[xIdx][yIdx].y) {
                    String err = "@ " + xIdx + ", " + yIdx + ": (" + yCoord + " !<= " + surfaceMesh[xIdx][yIdx].y + ")";
                    throw new IllegalArgumentException(ERROR_Y_ASCENTION + err);
                }
                yCoord = surfaceMesh[xIdx][yIdx].y;
            }
        }

        // Verify that y points are aligned and x points are ascending.
        for (int yIdx = 0; yIdx < yLen; yIdx++) {
            double xCoord = surfaceMesh[0][yIdx].x;
            double yCoord = surfaceMesh[0][yIdx].y;
            for (int xIdx = 0; xIdx < xLen; xIdx++) {
                if (surfaceMesh[xIdx][yIdx].y != yCoord) {
                    String err = "@ " + xIdx + ", " + yIdx + ": (" + yCoord + " != " + surfaceMesh[xIdx][yIdx].y + ")";
                    throw new IllegalArgumentException(ERROR_Y_ALIGNMENT + err);
                }
                if (xCoord > surfaceMesh[xIdx][yIdx].x) {
                    String err = "@ " + xIdx + ", " + yIdx + ": (" + xCoord + " !<= " + surfaceMesh[xIdx][yIdx].x + ")";
                    throw new IllegalArgumentException(ERROR_X_ASCENTION + err);
                }
                xCoord = surfaceMesh[xIdx][yIdx].x;
            }
        }
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

    protected Position[][] findBoundingArea(double x, double y) {
        double xOffset = x - this.lowerLeft.x;
        double yOffset = y - this.lowerLeft.y;

        int xIdx = (int) ((xOffset == 0) ? 0 : (xOffset / this.resolution));
        int yIdx = (int) ((yOffset == 0) ? 0 : (yOffset / this.resolution));

        // Clamp bounds
        xIdx = Math.min(xIdx, this.xLen - 2);
        yIdx = Math.min(yIdx, this.yLen - 2);
        xIdx = Math.max(xIdx, 0);
        yIdx = Math.max(yIdx, 0);

        return new Position[][]{
                {this.surfaceMesh[xIdx][yIdx], this.surfaceMesh[xIdx][yIdx + 1]},
                {this.surfaceMesh[xIdx + 1][yIdx], this.surfaceMesh[xIdx + 1][yIdx + 1]}
        };
    }

    /**
     * Get the surface height from the mesh and returns it in the surface mesh units.
     * <p>
     * Bilinear interpolation:
     * http://supercomputingblog.com/graphics/coding-bilinear-interpolation/
     */
    protected double surfaceHeightAt(double x, double y) {
        // Do not adjust Z outside the probed area
        if (x < lowerLeft.getX() || x > topRight.getX() || y < lowerLeft.getY() || y > topRight.getY()) {
            return 0;
        }

        Position[][] q = findBoundingArea(x, y);

        Position Q11 = q[0][0];
        Position Q21 = q[1][0];
        Position Q12 = q[0][1];
        Position Q22 = q[1][1];

        double x1 = Q11.x;
        double x2 = Q21.x;
        double y1 = Q11.y;
        double y2 = Q12.y;

        double R1 = ((x2 - x) / (x2 - x1)) * Q11.z + ((x - x1) / (x2 - x1)) * Q21.z;
        double R2 = ((x2 - x) / (x2 - x1)) * Q12.z + ((x - x1) / (x2 - x1)) * Q22.z;

        return ((y2 - y) / (y2 - y1)) * R1 + ((y - y1) / (y2 - y1)) * R2;
    }

    @Override
    public String getHelp() {
        return null;
    }
}
