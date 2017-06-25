/**
 * Adjust Z heights based on a provided surface mesh.
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
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.model.UnitUtils.Units;
import java.util.Collections;
import java.util.List;
import javax.vecmath.Point2d;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class MeshLeveler implements ICommandProcessor {
    final private double materialSurfaceHeight;
    final private Point3d[][] surfaceMesh;
    final private Point3d lowerLeft;
    final private Point3d upperRight;
    final private int xLen, yLen;
    final private Point2d meshDimensions;
    final private double resolution;

    // Used during processing.
    private double lastZHeight;
    private Units unit;

    public final static String ERROR_MESH_SHAPE= "Surface mesh must be a rectangular 2D array.";
    public final static String ERROR_NOT_ENOUGH_SAMPLES = "Need at least 2 samples along each axis.";
    public final static String ERROR_X_ALIGNMENT = "Unaligned x coordinate in surface grid.";
    public final static String ERROR_Y_ALIGNMENT = "Unaligned x coordinate in surface grid.";
    public final static String ERROR_Y_ASCENTION = "Found a y coordinate that isn't ascending.";
    public final static String ERROR_X_ASCENTION = "Found a x coordinate that isn't ascending.";

    public final static String ERROR_UNEXPECTED_ARC = "The mesh leveler cannot process arcs. Enable the arc expander.";
    public final static String ERROR_MISSING_POINT_DATA = "Internal parser error: missing data.";

    /**
     * @param materialSurfaceHeight Z height used in offset.
     * @param surfaceMesh 2D array in the format Position[x][y]
     */
    public MeshLeveler(double materialSurfaceHeightMM, Point3d[][] surfaceMesh, Units unit) {
        if (surfaceMesh == null) {
            throw new IllegalArgumentException("Surface mesh is required.");
        }

        // Validate that points form a rectangular 2D array.
        this.yLen = surfaceMesh[0].length;
        this.xLen = surfaceMesh.length;
        for (Point3d[] arr : surfaceMesh) {
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
            double yCoord = surfaceMesh[xIdx][0].y - 1;
            for (int yIdx = 0; yIdx < yLen; yIdx++) {
                if (surfaceMesh[xIdx][yIdx].x != xCoord) {
                    throw new IllegalArgumentException(ERROR_X_ALIGNMENT);
                }
                if (surfaceMesh[xIdx][yIdx].y <= yCoord) {
                    throw new IllegalArgumentException(ERROR_Y_ASCENTION);
                }
            }
        }

        // Verify that y points are aligned and x points are ascending.
        for (int yIdx = 0; yIdx < yLen; yIdx++) {
            double xCoord = surfaceMesh[0][yIdx].x - 1;
            double yCoord = surfaceMesh[0][yIdx].y;
            for (int xIdx = 0; xIdx < xLen; xIdx++) {
                if (surfaceMesh[xIdx][yIdx].y != yCoord) {
                    throw new IllegalArgumentException(ERROR_Y_ALIGNMENT);
                }
                if (surfaceMesh[xIdx][yIdx].x <= xCoord) {
                    throw new IllegalArgumentException(ERROR_X_ASCENTION);
                }
            }
        }

        this.unit = unit;
        this.materialSurfaceHeight = materialSurfaceHeightMM;
        this.surfaceMesh = surfaceMesh;
        this.resolution = Math.max(
                surfaceMesh[1][0].x-surfaceMesh[0][0].x,
                surfaceMesh[0][1].y-surfaceMesh[0][0].y);

        this.lowerLeft = surfaceMesh[0][0];
        this.upperRight = surfaceMesh[xLen-1][yLen-1];
        this.meshDimensions = new Point2d(
                this.upperRight.x - this.lowerLeft.x,
                this.upperRight.y - this.lowerLeft.y);
    }

    private boolean hasJustLines(List<GcodeMeta> commands) throws GcodeParserException {
        if (commands == null) return false;
        boolean hasLine = false;
        for (GcodeMeta command : commands) {
            if (command.code.equals("0") || command.code.equals("1") && command.point != null) {
                hasLine = true;
            }
            else if (command.code.equals("2") || command.code.equals("3")) {
                throw new GcodeParserException(ERROR_UNEXPECTED_ARC);
            }
        }
        return hasLine;
    }

    @Override
    public List<String> processCommand(final String commandString, GcodeState state) throws GcodeParserException {
        List<GcodeMeta> commands = GcodeParser.processCommand(commandString, 0, state);

        // If there are no lines, return unmodified input.
        if (!hasJustLines(commands)) {
            return Collections.singletonList(commandString);
        }

        if (commands.size() > 1) {
            throw new GcodeParserException(Localization.getString("parser.processor.general.multiple-commands"));
        }

        GcodeMeta command = commands.get(0);

        if (command == null || command.point == null) {
            throw new GcodeParserException(ERROR_MISSING_POINT_DATA);
        }

        Point3d start = state.currentPoint;
        Point3d end = command.point.point();

        if (start.z != end.z) {
            this.lastZHeight = end.z;
        }

        // Get offset relative to the expected surface height.
        // Visualizer normalizes everything to MM but probe mesh might be INCH
        double probeScaleFactor = UnitUtils.scaleUnits(UnitUtils.Units.MM, this.unit);
        double zScaleFactor = UnitUtils.scaleUnits(UnitUtils.Units.MM, state.isMetric ? Units.MM : Units.INCH);
        double zPointOffset =
                surfaceHeightAt(end.x / zScaleFactor, end.y / zScaleFactor) -
                (this.materialSurfaceHeight / probeScaleFactor);
        zPointOffset *= zScaleFactor;


        // Update z coordinate.
        end.z = this.lastZHeight + zPointOffset;
        //end.z /= resultScaleFactor;

        String adjustedCommand = GcodePreprocessorUtils.generateLineFromPoints(
                "G" + command.code, start, end, command.state.inAbsoluteMode, null);
        return Collections.singletonList(adjustedCommand);
    }

    protected Point3d[][] findBoundingArea(double x, double y) throws GcodeParserException {
        /*
        if (x < this.lowerLeft.x || x > this.upperRight.x || y < this.lowerLeft.y || y > this.upperRight.y) {
            throw new GcodeParserException("Coordinate out of bounds.");
        }
        */

        double xOffset = x - this.lowerLeft.x;
        double yOffset = y - this.lowerLeft.y;

        int xIdx = (int) ((xOffset == 0) ? 0 : (xOffset / this.resolution));
        int yIdx = (int) ((yOffset == 0) ? 0 : (yOffset / this.resolution));

        // Clamp bounds
        xIdx = Math.min(xIdx, this.xLen - 2);
        yIdx = Math.min(yIdx, this.yLen - 2);
        xIdx = Math.max(xIdx, 0);
        yIdx = Math.max(yIdx, 0);

        return new Point3d[][] {
            {this.surfaceMesh[xIdx  ][yIdx], this.surfaceMesh[xIdx  ][yIdx+1]},
            {this.surfaceMesh[xIdx+1][yIdx], this.surfaceMesh[xIdx+1][yIdx+1]}
        };
    }

    /**
     * Bilinear interpolation:
     * http://supercomputingblog.com/graphics/coding-bilinear-interpolation/
     */
    protected double surfaceHeightAt(double x, double y) throws GcodeParserException {
        Point3d[][] q = findBoundingArea(x, y);

        Point3d Q11 = q[0][0];
        Point3d Q21 = q[1][0];
        Point3d Q12 = q[0][1];
        Point3d Q22 = q[1][1];

        /*
        // This check doesn't work properly because I chose to clamp bounds
        if (Q11.x > x || Q12.x > x || Q21.x < x || Q22.x < x ||
            Q12.y < y || Q22.y < y || Q11.y > y || Q21.y > y) {
            throw new GcodeParserException("Problem detected getting surface height. Please submit file to github for analysis.");
        }
        */

        double x1 = Q11.x;
        double x2 = Q21.x;
        double y1 = Q11.y;
        double y2 = Q12.y;

        double R1 = ((x2 - x)/(x2 - x1)) * Q11.z + ((x - x1)/(x2 - x1)) * Q21.z;
        double R2 = ((x2 - x)/(x2 - x1)) * Q12.z + ((x - x1)/(x2 - x1)) * Q22.z;
        
        double P = ((y2 - y)/(y2 - y1)) * R1 + ((y - y1)/(y2 - y1)) * R2;

        return P;
    }

    @Override
    public String getHelp() {
        return null;
    }
}
