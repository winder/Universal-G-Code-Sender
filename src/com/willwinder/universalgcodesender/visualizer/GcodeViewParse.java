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

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.vecmath.Point3d;


public class GcodeViewParse {
    private static boolean debugVals = false;
    private Point3d min;
    private Point3d max;
    private ArrayList<LineSegment> lines;
    
    private Point3d lastPoint;
    private Pattern gPattern = null;
    private Pattern mPattern = null;
    
    private static String gCommand = "[Gg]0*(\\d+)";
    private static String mCommand = "[Mm]0*(\\d+)";
    
    // false = incremental; true = absolute
    boolean absoluteMode = true;
    static boolean absoluteIJK = false;
    
    public GcodeViewParse()
    {
        min = new Point3d();
        max = new Point3d();
        lastPoint = new Point3d();
        lines = new ArrayList<LineSegment>();

        this.gPattern = Pattern.compile(gCommand);
        this.mPattern = Pattern.compile(mCommand);
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
    
    public ArrayList<LineSegment> toObj(ArrayList<String> gcode)
    {
        double speed = 2; //DEFAULTS to 2
        double[] newCoord = { 0.0f, 0.0f, 0.0f};

        Point3d next = new Point3d();
        Point3d center = new Point3d(0.0, 0.0, 0.0);
        Point3d last = new Point3d(0.0, 0.0, 0.0);
        double parsedX, parsedY, parsedZ, parsedF, parsedI, parsedJ, parsedK;
        int gCode = -1;
        int mCode = -1;
        int lastGCode = -1;
        Matcher matcher;

        for(String s : gcode)
        {            
            // Parse out gcode values
            String[] sarr = s.split(" ");
            parsedX = parseCoord(sarr, 'X');
            parsedY = parseCoord(sarr, 'Y');
            parsedZ = parseCoord(sarr, 'Z');
            parsedF = parseCoord(sarr, 'F');
            parsedI = parseCoord(sarr, 'I');
            parsedJ = parseCoord(sarr, 'J');
            parsedK = parseCoord(sarr, 'K');

            // At this point next == last
            if(!Double.isNaN(parsedX)) {
                if (!this.absoluteMode)
                    parsedX += last.x;
                next.x = parsedX;
            }
            if(!Double.isNaN(parsedY)) {
                if (!this.absoluteMode)
                    parsedY += last.y;
                next.y = parsedY;
            }
            if(!Double.isNaN(parsedZ)) {
                if (!this.absoluteMode)
                    parsedZ += last.z;
                next.z = parsedZ;
            }
            
            
            if(!Double.isNaN(parsedF)) {
                speed = parsedF;
            }
                      
                        
            // Centerpoint in case of arc
            center.set(0.0, 0.0, 0.0);
            if (!Double.isNaN(parsedI)) {
                if (!this.absoluteIJK) {
                    center.x = last.x + parsedI;
                } else {
                    center.x = parsedI;
                }
            }

            if (!Double.isNaN(parsedJ)) {
                if (!this.absoluteIJK) {
                    center.y = last.y + parsedJ;
                } else {
                    center.y = parsedJ;
                }
            }

            if (!Double.isNaN(parsedK)) {
                if (!this.absoluteIJK) {
                    center.z = last.z + parsedK;
                } else {
                    center.z = parsedK;
                }
            }
            
            // Save any updated bounaries.
            testExtremes(next);
            
            // Check multiple matches on one line in case of state commands:
            matcher = this.gPattern.matcher(s);
            gCode = -1;
            while (matcher.find()) {
                gCode = Integer.parseInt(matcher.group(1));
                handleGCode(gCode, last, center, next);
            }
            
            // Check multiple matches on one line in case of state commands:
            matcher = this.mPattern.matcher(s);
            mCode = -1;
            while (matcher.find()) {
                mCode = Integer.parseInt(matcher.group(1));
                handleMCode(mCode, last, center, next);
            }
           
            // If there isn't a new code, use the last code.
            if (gCode == -1 && mCode == -1 && lastGCode != -1) {
                gCode = lastGCode;
                handleGCode(gCode, last, center, next);
            }
            
            // Save the last commands.
            if (gCode != -1) {
                lastGCode = gCode;
            }
            
            last.set(next);
        }
        return lines;
    }
    
    private void handleGCode(int code, final Point3d start, final Point3d center, final Point3d end) {
        
        switch (code) {
            case 0:
            case 1:
                //this.queuePoint(endpoint);
                this.queueLine(start, end);
                break;
            case 2:
            case 3:
                boolean clockwise = true;
                if (code == 3) {
                    clockwise = false;
                }

                // draw the arc itself.
                //addArcSegmentsReplicatorG(start, end, center, clockwise);
                addArcSegmentsBDring(start, end, center, clockwise);
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
    
    
    /** Return the angle when going from p1 to p2.
     */
    private double getAngle(final Point3d p1, final Point3d p2) {
        double deltaX = p2.x - p1.x;
        double deltaY = p2.y - p1.y;

        double angle = 0.0;

        if (deltaX != 0) {			// prevent div by 0
            // it helps to know what quadrant you are in
            if (deltaX > 0 && deltaY >= 0)  // 0 - 90
                angle = Math.atan(deltaY/deltaX);
            else if (deltaX < 0 && deltaY >= 0) // 90 to 180
                angle = Math.PI - Math.abs(Math.atan(deltaY/deltaX));
            else if (deltaX < 0 && deltaY < 0) // 180 - 270
                angle = Math.PI + Math.abs(Math.atan(deltaY/deltaX));
            else if (deltaX > 0 && deltaY < 0) // 270 - 360
                angle = Math.PI * 2 - Math.abs(Math.atan(deltaY/deltaX));
        }
        else {
            // 90 deg
            if (deltaY > 0) {
                angle = Math.PI / 2.0;
            }
            // 270 deg
            else {
                angle = Math.PI * 3.0 / 2.0;
            }
        }
      
        return angle;
    }
    
    private void addArcSegmentsBDring(final Point3d p1, final Point3d p2, final Point3d center, boolean isCw) {
        int numPoints = 15;  // TODO: ....could this be dynamic or user selectable?		
        double radius;
        Point3d lineStart = new Point3d(p1.x, p1.y, p1.z);
        Point3d lineEnd = new Point3d(p2.x, p2.y, p2.z);
        double sweep;
        double angle;

        //use pythag theorum...to get the radius
        radius = Math.sqrt(Math.pow(p1.x - center.x, 2.0) + Math.pow(p1.y - center.y, 2.0));

        double startAngle = getAngle(center, p1);
        double endAngle = getAngle(center, p2);

        // if it ends at 0 it really should end at 360
        if (endAngle == 0) {
                endAngle = Math.PI * 2;
        }

        if (!isCw && endAngle < startAngle) {
            sweep = ((Math.PI * 2 - startAngle) + endAngle);
        } else if (isCw && endAngle > startAngle) {
            sweep = ((Math.PI * 2 - endAngle) + startAngle);
        } else {
            sweep = Math.abs(endAngle - startAngle);
        }
        
        double zIncrement = (p2.z - p1.z) / numPoints;
        for(int i=0; i<numPoints; i++)
        {
            if (isCw) {
                angle = (startAngle - i * sweep/numPoints);
            } else {
                angle = (startAngle + i * sweep/numPoints);
            }

            if (angle >= Math.PI * 2) {
                angle = angle - Math.PI * 2;
            }

            lineEnd.x = Math.cos(angle) * radius + center.x;
            lineEnd.y = Math.sin(angle) * radius + center.y;
            lineEnd.z += zIncrement;

            this.testExtremes(lineEnd);
            
            this.queueLine(lineStart, lineEnd);

            lineStart.set(lineEnd);
        }
        
        this.queueLine(lineEnd, p2);
        //this.queuePoint(lineStart, p2);
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
        if (angleB <= angleA)
                angleB += 2 * Math.PI;
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
                if (!clockwise)
                        step = s;
                else
                        step = steps - s;

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
                this.queueLine(current, newPoint);
                current = newPoint;
        }
        this.queueLine(current, endpoint);
    }
    
    private void queuePoint(final Point3d point) {
        if (lastPoint != null) {
            lines.add(new LineSegment(lastPoint, point, 0, 0));
        }
        //lastPoint = point;
        lastPoint.set(point);
    }
    
    private void queueLine(final Point3d start, final Point3d end) {
        lines.add(new LineSegment(start, end, 0, 0));
    }
    
    private double parseCoord(String[] sarr, char c)
    {
        for(String t : sarr)
        {
            if(t.matches("\\s*[" + c + "]\\s*-*[\\d|\\.]+"))
            {
                //System.out.println("te : " + t);
                return Double.parseDouble(t.substring(1,t.length()));
            }
        }
        return Double.NaN;
    }
    
    private int getGCode(String str) {
        Matcher matcher = this.gPattern.matcher(str);

        if (matcher.find()) {
            return Integer.parseInt(matcher.group(1));
        }
        return -1;
    }    
}