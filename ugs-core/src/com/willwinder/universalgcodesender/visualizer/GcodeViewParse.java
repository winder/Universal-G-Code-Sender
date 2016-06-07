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
import com.willwinder.universalgcodesender.gcode.GcodeParserException;
import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.types.GcodeCommand;
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
    
    private void testExtremes(final Point3d p3d)
    {
        testExtremes(p3d.x, p3d.y, p3d.z);
    }
    
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

    public List<LineSegment> toObjFromReader(GcodeStreamReader reader,
            double arcSegmentLength) throws IOException, GcodeParserException {
        lines.clear();
        GcodeParser gp = new GcodeParser();

        // Save the state
        Point3d start = new Point3d();
        Point3d end = new Point3d();

        while (reader.getNumRowsRemaining() > 0) {
            GcodeCommand c = reader.getNextCommand();
            List<PointSegment> points = gp.addCommand(c.getCommandString(), c.getCommandNumber());
            for (PointSegment p : points) {
                addLinesFromPointSegment(start, end, p, arcSegmentLength, lines);
            }
        }

        return lines;
    }
    
    public List<LineSegment> toObjRedux(List<String> gcode, double arcSegmentLength) throws GcodeParserException {
        GcodeParser gp = new GcodeParser();
        lines.clear();

        // Save the state
        Point3d start = new Point3d();
        Point3d end = new Point3d();

        for (String s : gcode) {
            gp.addCommand(s);
            List<PointSegment> points = gp.addCommand(s);
            for (PointSegment p : points) {
                addLinesFromPointSegment(start, end, p, arcSegmentLength, lines);
            }
        }
        
        return lines;
    }
    
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
                        ps.getRadius(), minArcLength, arcSegmentLength);
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