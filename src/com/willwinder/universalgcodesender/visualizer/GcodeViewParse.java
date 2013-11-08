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

import com.willwinder.universalgcodesender.GcodePreprocessorUtils;
import java.util.ArrayList;
import java.util.List;
import javax.vecmath.Point3d;


public class GcodeViewParse {
    // Configurable values
    // TODO: ....could this be dynamic or user selectable?
    private int arcResolution = 5;

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
    
    public List<LineSegment> toObj(List<String> gcode)
    {
        double speed = 2; //DEFAULTS to 2

        Point3d next = new Point3d();
        Point3d center = new Point3d(0.0, 0.0, 0.0);
        Point3d last = new Point3d(0.0, 0.0, 0.0);
        double parsedF;
        double parsedR;
        int gCode, mCode;
        int lastGCode = -1;
        List<Integer> l;

        long startTime = System.currentTimeMillis();
        
        for(String s : gcode)
        {       
            String command = GcodePreprocessorUtils.removeComment(s);
            
            // Parse out gcode values
            List<String> sarr = GcodePreprocessorUtils.splitCommand(command);
            parsedF = GcodePreprocessorUtils.parseCoord(sarr, 'F');
            parsedR = GcodePreprocessorUtils.parseCoord(sarr, 'R');
            
            next = GcodePreprocessorUtils.updatePointWithCommand(
                    sarr, last, this.absoluteMode);
            
            // Centerpoint in case of arc
            center =
                    GcodePreprocessorUtils.updateCenterWithCommand(
                    sarr, last, absoluteIJK);

            // Feed
            if(!Double.isNaN(parsedF)) {
                speed = parsedF;
            }
                      
            // Save any updated bounaries.
            testExtremes(next);
            
            // Check multiple matches on one line in case of state commands:
            l = GcodePreprocessorUtils.parseGCodes(command);
            gCode = -1;
            for (Integer i : l) {
                gCode = i;
                handleGCode(gCode, last, center, next, parsedR);
            }
            
            // Check multiple matches on one line in case of state commands:
            l = GcodePreprocessorUtils.parseMCodes(command);
            mCode = -1;
            for (Integer i : l) {
                mCode = i;
                handleMCode(mCode, last, center, next);
            }
           
            // If there isn't a new code, use the last code.
            if (gCode == -1 && mCode == -1 && lastGCode != -1) {
                gCode = lastGCode;
                handleGCode(gCode, last, center, next, parsedR);
            }
            
            // Save the last commands.
            if (gCode != -1) {
                lastGCode = gCode;
            }
            
            last.set(next);
        }
        
        if (this.debug) {
            long endTime = System.currentTimeMillis();
            System.out.println("Duration = " + (endTime - startTime) + "ms");
        }

        return lines;
    }
    
    private void handleGCode(int code, final Point3d start, final Point3d center, final Point3d end, final double R) {
        LineSegment ls;
        switch (code) {
            case 0:
                ls = new LineSegment(start, end, currentLine++);
                ls.setIsFastTraverse(true);
                if ((start.x == end.x) && (start.y == end.y) && (start.z != end.z)) {
                    ls.setIsZMovement(true);
                }
                this.queueLine(ls);
                break;

            case 1:
                ls = new LineSegment(start, end, currentLine++);
                if ((start.x == end.x) && (start.y == end.y) && (start.z != end.z)) {
                    ls.setIsZMovement(true);
                }                ls.isFastTraverse();
                this.queueLine(ls);
                break;
            
            case 2:
            case 3:
                boolean clockwise = true;
                if (code == 3) {
                    clockwise = false;
                }

                double radius = 0;
                Point3d arcCenter = center;
                
                // If R was specified and IJK were not, convert R to IJK
                if (R != 0 && center.x == 0 && center.y == 0) {
                    radius = R;
                    arcCenter = GcodePreprocessorUtils.convertRToCenter(
                            start, end, R, absoluteIJK, clockwise);
                }
                
                // Generate points along the arc
                List<Point3d> points = 
                        GcodePreprocessorUtils.generatePointsAlongArcBDring(
                        start, end, arcCenter, clockwise, radius, arcResolution);
                
                // Create line segments from points.
                Point3d startPoint = null;
                for (Point3d nextPoint : points) {
                    this.testExtremes(nextPoint);
                    if (startPoint != null) {
                        this.queueArcLine(startPoint, nextPoint);
                    }
                    startPoint = nextPoint;
                }
                
                currentLine++;
                break;
                
            case 20:
                break;
                
            case 21:
                break;
                
            case 90:
                absoluteMode = true;
                break;
            case 91:
                absoluteMode = false;
                break;
        }
    }
    
    private void handleMCode(int code, final Point3d start, final Point3d center, final Point3d endpoint) {
        switch (code) {
            case 0:
            case 1:
                break;
            case 2:
            case 3:
                break;
                
            case 90:
                break;
            case 91:
                break;
        }
    }

    // This one doesn't work right.
    private void addArcSegmentsReplicatorG(final Point3d start, final Point3d endpoint, final Point3d center, boolean clockwise) {
        // System.out.println("Arc from " + current.toString() + " to " +
        // endpoint.toString() + " with center " + center);
        Point3d current = new Point3d(start.x, start.y, start.z);
        
        // angle variables.
        double angleA;
        double angleB;
        double angle;
        double radius;
        double length;

        // delta variables.
        double aX;
        double aY;
        double bX;
        double bY;

        // figure out our deltas
        aX = start.x - center.x;
        aY = start.y - center.y;
        bX = endpoint.x - center.x;
        bY = endpoint.y - center.y;

        // Clockwise
        if (clockwise) {
                angleA = Math.atan2(bY, bX);
                angleB = Math.atan2(aY, aX);
        }
        // Counterclockwise
        else {
                angleA = Math.atan2(aY, aX);
                angleB = Math.atan2(bY, bX);
        }

        // Make sure angleB is always greater than angleA
        // and if not add 2PI so that it is (this also takes
        // care of the special case of angleA == angleB,
        // ie we want a complete circle)
        if (angleB <= angleA) {
                angleB += 2 * Math.PI;
        }
        angle = angleB - angleA;
        // calculate a couple useful things.
        radius = Math.sqrt(aX * aX + aY * aY);
        length = radius * angle;

        // for doing the actual move.
        int steps;
        int s;
        int step;

        // Maximum of either 2.4 times the angle in radians
        // or the length of the curve divided by the curve section constant
        //steps = (int) Math.ceil(Math.max(angle * 2.4, length / curveSection));
        steps = 3;
        
        // this is the real draw action.
        Point3d newPoint = new Point3d();
        double arcStartZ = start.z;
        double fraction;
        for (s = 1; s <= steps; s++) {
                // Forwards for CCW, backwards for CW
                if (!clockwise) {
                    step = s;
                } else {
                    step = steps - s;
                }

                fraction = (double) step / steps;
                // calculate our waypoint.
                newPoint.x = center.x + radius
                                * Math.cos(angleA + angle * fraction);
                newPoint.y = center.y + radius
                                * Math.sin(angleA + angle * fraction);
                newPoint.z = arcStartZ + (endpoint.z - arcStartZ) * fraction;

                //System.out.println("    "+newPoint.toString());

                // start the move
                //setTarget(newPoint);
                this.queueArcLine(current, newPoint);
                current = newPoint;
        }
        this.queueArcLine(current, endpoint);
    }
    
    private void queuePoint(final Point3d point) {
        if (lastPoint != null) {
            lines.add(new LineSegment(lastPoint, point, currentLine));
        }
        //lastPoint = point;
        lastPoint.set(point);
    }
    
    private void queueLine(LineSegment line) {
        lines.add(line);
    }
    
    private void queueArcLine(final Point3d start, final Point3d end) {
        LineSegment ls = new LineSegment(start, end, currentLine);
        ls.setIsArc(true);
        lines.add(ls);
    }
}