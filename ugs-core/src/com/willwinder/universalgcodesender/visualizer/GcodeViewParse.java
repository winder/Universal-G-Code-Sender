/*
 * Gcode parser that creates an array of line segments which can be drawn.
 *
 * Created on Jan 29, 2013
 */

/*
    Copywrite 2013-2016 Noah Levy, William Winder

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
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.gcode.processors.CommandSplitter;
import com.willwinder.universalgcodesender.gcode.processors.CommentProcessor;
import com.willwinder.universalgcodesender.gcode.processors.WhitespaceProcessor;
import com.willwinder.universalgcodesender.gcode.util.PlaneFormatter;
import com.willwinder.universalgcodesender.types.PointSegment;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3d;


public class GcodeViewParse {

    // false = incremental; true = absolute
    boolean absoluteMode = true;
    static boolean absoluteIJK = false;

    // Parsed object
    private final Point3d min;
    private final Point3d max;
    private final List<LineSegment> lines;
    
    // Debug
    private final boolean debug = true;
    
    public GcodeViewParse()
    {
        min = new Point3d();
        max = new Point3d();
        lines = new ArrayList<>();
    }

    public Point3d getMinimumExtremes()
    {
        return min;
    }
    
    public Point3d getMaximumExtremes()
    {
        return max;
    }
    
    /**
     * Test a point and update min/max coordinates if appropriate.
     */
    private void testExtremes(final Point3d p3d)
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
     * Create a gcode parser with required configuration.
     */
    private static GcodeParser getParser(double arcSegmentLength) {
        GcodeParser gp = new GcodeParser();
        gp.addCommandProcessor(new WhitespaceProcessor());
        gp.addCommandProcessor(new CommentProcessor());
        gp.addCommandProcessor(new CommandSplitter());
        //gp.addCommandProcessor(new ArcExpander(true, arcSegmentLength, 4));
        return gp;
    }

    /**
     * Almost the same as toObjRedux, convert gcode to a LineSegment collection.
     * I've tried refactoring this, but the function is so small that merging
     * toObjFromReader and toObjRedux adds more complexity than having these two
     * methods.
     * 
     * @param gcode commands to visualize.
     * @param arcSegmentLength length of line segments when expanding an arc.
     */
    public List<LineSegment> toObjFromReader(GcodeStreamReader reader,
            double arcSegmentLength) throws IOException, GcodeParserException {
        lines.clear();
        GcodeParser gp = getParser(arcSegmentLength);

        // Save the state
        Point3d start = new Point3d();
        Point3d end = new Point3d();

        while (reader.getNumRowsRemaining() > 0) {
            List<String> commands = gp.preprocessCommand(reader.getNextCommand().getCommandString());
            for (String command : commands) {
                List<PointSegment> points = gp.addCommand(command);
                for (PointSegment p : points) {
                    addLinesFromPointSegment(start, end, p, arcSegmentLength, lines);
                }
            }
        }

        return lines;
    }
    
    /**
     * The original (working) gcode to LineSegment collection code.
     * @param gcode commands to visualize.
     * @param arcSegmentLength length of line segments when expanding an arc.
     */
    public List<LineSegment> toObjRedux(List<String> gcode, double arcSegmentLength) throws GcodeParserException {
        GcodeParser gp = getParser(arcSegmentLength);

        lines.clear();

        // Save the state
        Point3d start = new Point3d();
        Point3d end = new Point3d();

        for (String s : gcode) {
            List<String> commands = gp.preprocessCommand(s);
            for (String command : commands) {
                List<PointSegment> points = gp.addCommand(command);
                for (PointSegment p : points) {
                    addLinesFromPointSegment(start, end, p, arcSegmentLength, lines);
                }
            }
        }
        
        return lines;
    }
    
    /**
     * Turns a point segment into one or more LineSegment. Arcs are expanded.
     * Keeps track of the minimum and maximum x/y/z locations.
     */
    private List<LineSegment> addLinesFromPointSegment(Point3d start, Point3d end, PointSegment segment, double arcSegmentLength, List<LineSegment> ret) {
        // For a line segment list ALL arcs must be converted to lines.
        double minArcLength = 0;
        LineSegment ls;
        PointSegment ps = segment;
        ps.convertToMetric();
        
        end.set(ps.point());

        // start is null for the first iteration.
        if (start != null) {
            // Expand arc for graphics.
            if (ps.isArc()) {
                List<Point3d> points =
                    GcodePreprocessorUtils.generatePointsAlongArcBDring(
                        start, end, ps.center(), ps.isClockwise(),
                        ps.getRadius(), minArcLength, arcSegmentLength, new PlaneFormatter(ps.getPlaneState()));
                // Create line segments from points.
                if (points != null) {
                    Point3d startPoint = start;
                    for (Point3d nextPoint : points) {
                        ls = new LineSegment(startPoint, nextPoint, ps.getLineNumber());
                        ls.setIsArc(ps.isArc());
                        ls.setIsFastTraverse(ps.isFastTraverse());
                        ls.setIsZMovement(ps.isZMovement());
                        this.testExtremes(nextPoint);
                        ret.add(ls);
                        startPoint = nextPoint;
                    }
                }
            // Line
            } else {
                ls = new LineSegment(start, end, ps.getLineNumber());
                ls.setIsArc(ps.isArc());
                ls.setIsFastTraverse(ps.isFastTraverse());
                ls.setIsZMovement(ps.isZMovement());
                this.testExtremes(end);
                ret.add(ls);
            }
        }
        start.set(end);
        
        return ret;
    }
}