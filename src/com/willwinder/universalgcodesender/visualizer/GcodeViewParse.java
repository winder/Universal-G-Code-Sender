/*
 * Gcode parser that creates an array of line segments which can be drawn.
 *
 * Created on Jan 29, 2013
 */

/*
    Copywrite 2013 Noah Levy, William Winder

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
import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.types.PointSegment;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3d;


public class GcodeViewParse {

    // false = incremental; true = absolute
    boolean absoluteMode = true;
    static boolean absoluteIJK = false;

    // Parsed object
    private Point3d min;
    private Point3d max;
    private List<LineSegment> lines;
    
    // Parsing state.
    private Point3d lastPoint;
    private int currentLine = 0;    // for assigning line numbers to segments.
      
    // Debug
    private boolean debug = true;
    
    public GcodeViewParse()
    {
        min = new Point3d();
        max = new Point3d();
        lastPoint = new Point3d();
        lines = new ArrayList<LineSegment>();
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
    
    public List<LineSegment> toObjRedux(List<String> gcode, double minArcLength, double arcSegmentLength) {
        GcodeParser gp = new GcodeParser();
        for (String s : gcode) {
            gp.addCommand(s);
        }
        
        return getLinesFromParser(gp, minArcLength, arcSegmentLength);
    }
    
    private List<LineSegment> getLinesFromParser(GcodeParser gp, double minArcLength, double arcSegmentLength) {
        List<PointSegment> psl = gp.getPointSegmentList();
        
        Point3d start = null;
        Point3d end = null;
        LineSegment ls;
        int num = 0;
        for (PointSegment segment : psl) {
            PointSegment ps = segment;
            ps.convertToMetric();
            
            end = ps.point();
            

            // start is null for the first iteration.
            if (start != null) {
                // Expand arc for graphics.
                if (ps.isArc()) {
                    List<Point3d> points =
                        GcodePreprocessorUtils.generatePointsAlongArcBDring(
                        start, end, ps.center(), ps.isClockwise(), ps.getRadius(), minArcLength, arcSegmentLength);
                    // Create line segments from points.
                    Point3d startPoint = start;
                    for (Point3d nextPoint : points) {
                        ls = new LineSegment(startPoint, nextPoint, num);
                        ls.setIsArc(ps.isArc());
                        ls.setIsFastTraverse(ps.isFastTraverse());
                        ls.setIsZMovement(ps.isZMovement());
                        this.testExtremes(nextPoint);
                        lines.add(ls);
                        startPoint = nextPoint;
                    }
                // Line
                } else {
                    ls = new LineSegment(start, end, num++);
                    ls.setIsArc(ps.isArc());
                    ls.setIsFastTraverse(ps.isFastTraverse());
                    ls.setIsZMovement(ps.isZMovement());
                    this.testExtremes(end);
                    lines.add(ls);
                }
            }
            start = end;
        }
        
        return lines;
    }
}