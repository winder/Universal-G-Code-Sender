/*
 * Gcode parser that creates an array of line segments which can be drawn.
 *
 * Created on Jan 29, 2013
 */

/*
    Copyright 2013-2017 Noah Levy, William Winder

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

package com.willwinder.universalgcodesender.visualizer;

import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.GcodeParser.GcodeMeta;
import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.gcode.processors.CommentProcessor;
import com.willwinder.universalgcodesender.gcode.processors.WhitespaceProcessor;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.gcode.util.PlaneFormatter;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.types.PointSegment;
import com.willwinder.universalgcodesender.utils.GUIHelpers;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


public class GcodeViewParse {
    Logger LOGGER = Logger.getLogger(GcodeViewParse.class.getName());

    // Parsed object
    private final Position min;
    private final Position max;
    private final List<LineSegment> lines;

    public GcodeViewParse()
    {
        min = new Position(UnitUtils.Units.MM);
        max = new Position(UnitUtils.Units.MM);
        lines = new ArrayList<>();
    }

    public Position getMinimumExtremes()
    {
        return min;
    }
    
    public Position getMaximumExtremes()
    {
        return max;
    }
    
    /**
     * Test a point and update min/max coordinates if appropriate.
     */
    private void testExtremes(final Position p3d)
    {
        testExtremes(p3d.x, p3d.y, p3d.z);
    }
    
    /**
     * Test a point and update min/max coordinates if appropriate.
     */
    private void testExtremes(double x, double y, double z)
    {
        if(x < min.x) {
            min.x = x;
        }
        if(x > max.x) {
            max.x = x;
        }
        if(y < min.y) {
            min.y = y;
        }
        if(y > max.y) {
            max.y = y;
        }
        if(z < min.z) {
            min.z = z;
        }
        if(z > max.z) {
            max.z = z;
        }
    }

    /**
     * TODO: LineSegment is little more than a PointSegment which also has the beginning. Since they are never used
     *       without an entire list of LineSegments there is no need to store the beginning. So...
     * TODO: Remove LineSegment, use PointSegment everywhere instead. The biggest difference is that the PointSegment
     *       also stores the PlaneState, that can be removed when things are converted to Point3d for final rendering.
     */

    /**
     * Create a gcode parser with required configuration.
     */
    private static GcodeParser getParser(double arcSegmentLength) {
        GcodeParser gp = new GcodeParser();
        gp.addCommandProcessor(new CommentProcessor());
        gp.addCommandProcessor(new WhitespaceProcessor());
        //gp.addCommandProcessor(new ArcExpander(true, arcSegmentLength, 4));
        return gp;
    }

    /**
     * Almost the same as toObjRedux, convert gcode to a LineSegment collection.
     * I've tried refactoring this, but the function is so small that merging
     * toObjFromReader and toObjRedux adds more complexity than having these two
     * methods.
     * 
     * @param reader a stream with commands to parse.
     * @param arcSegmentLength length of line segments when expanding an arc.
     */
    public List<LineSegment> toObjFromReader(IGcodeStreamReader reader,
                                             double arcSegmentLength) throws IOException, GcodeParserException {
        lines.clear();
        GcodeParser gp = getParser(arcSegmentLength);

        // Save the state
        Position start = new Position(gp.getCurrentState().getUnits());

        while (reader.getNumRowsRemaining() > 0) {
            GcodeCommand commandObject = reader.getNextCommand();
            List<String> commands = gp.preprocessCommand(commandObject.getCommandString(), gp.getCurrentState());
            for (String command : commands) {
                List<GcodeMeta> points = gp.addCommand(command, commandObject.getCommandNumber());
                for (GcodeMeta meta : points) {
                    if (meta.point != null) {
                        addLinesFromPointSegment(start, meta.point, arcSegmentLength, lines);
                        start = meta.point.point();
                    }
                }
            }
        }

        return lines;
    }
    
    /**
     * The original (working) gcode to LineSegment collection code.
     *
     * @param gcode commands to visualize.
     * @param arcSegmentLength length of line segments when expanding an arc.
     */
    public List<LineSegment> toObjRedux(List<String> gcode, double arcSegmentLength) throws GcodeParserException {
        GcodeParser gp = getParser(arcSegmentLength);

        lines.clear();

        // Save the state
        Position start = new Position(gp.getCurrentState().getUnits());

        for (String s : gcode) {
            List<String> commands = gp.preprocessCommand(s, gp.getCurrentState());
            for (String command : commands) {
                List<GcodeMeta> points = gp.addCommand(command);
                for (GcodeMeta meta : points) {
                    if (meta.point != null) {
                        addLinesFromPointSegment(start, meta.point, arcSegmentLength, lines);
                        // if the last set point is in a different or unknown unit, crate a new point-instance with the correct unit set
                        if (start.getUnits() != UnitUtils.Units.MM && gp.getCurrentState().isMetric){
                            start=new Position(
                                    meta.point.point().x,
                                    meta.point.point().y,
                                    meta.point.point().z,
                                    meta.point.point().a,
                                    meta.point.point().b,
                                    meta.point.point().c,
                                    gp.getCurrentState().isMetric ? UnitUtils.Units.MM : UnitUtils.Units.INCH
                                    );
                        } else {
                            // ...otherwise recycle the old instance and just update the x,y,z coords
                            start.set(meta.point.point());
                        }
                    }
                }
            }
        }
        
        return lines;
    }
    
    /**
     * Helper to create a line segment with flags initialized.
     */
    private static LineSegment createLineSegment(Position a, Position b, PointSegment meta) {
      LineSegment ls = new LineSegment(a, b, meta.getLineNumber());
      ls.setIsArc(meta.isArc());
      ls.setIsFastTraverse(meta.isFastTraverse());
      ls.setIsZMovement(meta.isZMovement());
      ls.setIsRotation(meta.isRotation());
      return ls;
    }

    /**
     * Turns a point segment into one or more LineSegment. Arcs are expanded.
     * Keeps track of the minimum and maximum x/y/z locations.
     */
    private void addLinesFromPointSegment(final Position start, final PointSegment endSegment, double arcSegmentLength, List<LineSegment> ret) {
        // For a line segment list ALL arcs must be converted to lines.
        double minArcLength = 0;
        LineSegment ls;
        endSegment.convertToMetric();

        try {
            // start is null for the first iteration.
            if (start != null) {
                // Expand arc for graphics.
                if (endSegment.isArc()) {
                    List<Position> points =
                        GcodePreprocessorUtils.generatePointsAlongArcBDring(
                            start, endSegment.point(), endSegment.center(), endSegment.isClockwise(),
                            endSegment.getRadius(), minArcLength, arcSegmentLength, new PlaneFormatter(endSegment.getPlaneState()));
                    // Create line segments from points.
                    if (points != null) {
                        Position startPoint = start;
                        for (Position nextPoint : points) {
                            ret.add(createLineSegment(startPoint, nextPoint, endSegment));
                            this.testExtremes(nextPoint);
                            startPoint = nextPoint;
                        }
                    }
                // Line
                } else {
                    ret.add(createLineSegment(start, endSegment.point(), endSegment));
                    this.testExtremes(endSegment.point());
                }
            }
        } catch (Exception e) {
            String message = endSegment.getLineNumber() + ": " + e.getMessage();
            GUIHelpers.displayErrorDialog(message, true);
            LOGGER.log(Level.SEVERE, message, e);
        }
    }
}
