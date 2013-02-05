/*
 * 
 *
 * Created on Jan 29, 2013
 */

/*
    Copywrite 2013 Noah Levy

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

import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.logging.Level;
import java.util.logging.Logger;
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
    boolean absoluteMode = false;
    static boolean absoluteIJK = false;
    
    public GcodeViewParse()
    {
        min = new Point3d();
        max = new Point3d();
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
    
    private void testExtremes(Point3d p3d)
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
        Point3d nextPoint = new Point3d();
        Point3d center = new Point3d();

        int curLayer = 0;
        int curToolhead = 0;
        double parsedX, parsedY, parsedZ, parsedF, parsedI, parsedJ, parsedK;
        double tolerance = .0002f;
        double[] nextCoord = { 0.0f, 0.0f, 0.0f};
        boolean currentExtruding = false;
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
            

            if(!Double.isNaN(parsedX)) {
                if (!this.absoluteMode)
                    parsedX += nextCoord[0];
                nextCoord[0] = parsedX;
            }
            if(!Double.isNaN(parsedY)) {
                if (!this.absoluteMode)
                    parsedY += nextCoord[1];
                nextCoord[1] = parsedY;
            }
            if(!Double.isNaN(parsedZ)) {
                if (!this.absoluteMode)
                    parsedZ += nextCoord[2];
                nextCoord[2] = parsedZ;
            }
            
            if(!Double.isNaN(parsedI)) {
                if (!this.absoluteIJK)
                    parsedI += lastPoint.x;
            } 

            if(!Double.isNaN(parsedJ)) {
                if (!this.absoluteIJK)
                    parsedJ += lastPoint.y;
            }

            if(!Double.isNaN(parsedK)) {
                if (!this.absoluteIJK)
                    parsedK += lastPoint.z;
            }
            
            if(!Double.isNaN(parsedF)) {
                speed = parsedF;
            }
                                            
            // Centerpoint in case of arc
            center.set(0.0, 0.0, 0.0);
            if (!Double.isNaN(parsedI))
                center.x = parsedI;
            if (!Double.isNaN(parsedJ))
                center.y = parsedJ;
            if (!Double.isNaN(parsedK))
                center.z = parsedK;
            
            nextPoint = new Point3d(nextCoord[0], nextCoord[1], nextCoord[2]);

            // Save any updated bounaries.
            testExtremes(nextPoint);
            
            // Check multiple matches on one line in case of state commands:
            matcher = this.gPattern.matcher(s);
            gCode = -1;
            while (matcher.find()) {
                gCode = Integer.parseInt(matcher.group(1));
                System.out.println("Command: G" + gCode + ",   '"+s+"'");
                handleGCode(gCode, lastPoint, center, nextPoint);
            }
            
            // If there isn't a new code, use the last code.
            if (gCode == -1 || lastGCode != -1) {
                gCode = lastGCode;
                System.out.println("(Last) : G" + gCode + ",   '"+s+"'");
                handleGCode(gCode, lastPoint, center, nextPoint);
            }
            
            // Check multiple matches on one line in case of state commands:
            matcher = this.mPattern.matcher(s);
            mCode = -1;
            while (matcher.find()) {
                mCode = Integer.parseInt(matcher.group(1));
                System.out.println("Command: M" + mCode + ",   '"+s+"'");
                handleMCode(mCode, lastPoint, center, nextPoint);
            }

            // Save the last commands.
            lastGCode = gCode;
        }
        return lines;
    }
    
    private void handleGCode(int code, Point3d lastPoint, Point3d center, Point3d endpoint) {
        
        switch (code) {
            case 0:
            case 1:
                this.queuePoint(endpoint);
                break;
            case 2:
            case 3:
                boolean clockwise = true;
                if (code == 3) {
                    clockwise = false;
                }
                
                // draw the arc itself.
                addArcSegments(lastPoint, center, endpoint, clockwise);
                break;
                
            case 90:
                absoluteMode = true;
                break;
            case 91:
                absoluteMode = false;
                break;
        }
    }
    
    private void handleMCode(int code, Point3d lastPoint, Point3d center, Point3d endpoint) {
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
    
    private void addArcSegments(Point3d start, Point3d center, Point3d endpoint, boolean clockwise) {
         //System.out.println("Arc from " + start.toString() + " to " +
         //endpoint.toString() + " with center " + center);
        
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
        steps = (int) Math.ceil(Math.max(angle * 2.4, length / 0.01));

        // this is the real draw action.
        Point3d newPoint = new Point3d();
        double arcStartZ = start.z;
//System.out.println("Starting arc....");
        for (s = 1; s <= steps; s++) {
                // Forwards for CCW, backwards for CW
                if (!clockwise)
                        step = s;
                else
                        step = steps - s;

                // calculate our waypoint.
                newPoint.x = center.x + radius
                                * Math.cos(angleA + angle * ( step / steps));
                newPoint.y = center.y + radius
                                * Math.sin(angleA + angle * ( step / steps));
                newPoint.z = arcStartZ + (endpoint.z - arcStartZ) * s / steps;

                // Add the segment
                this.queuePoint(newPoint);
/*                
System.out.println("    "+newPoint.toString());
        }
System.out.println(endpoint.toString());
boolean yes = false;
while(yes) {try {
                Thread.sleep(1000);
            } catch (InterruptedException ex) {
                Logger.getLogger(GcodeViewParse.class.getName()).log(Level.SEVERE, null, ex);
            }
 * */
                 }

        // Connect the final segment with the end point.
        this.queuePoint(endpoint);
    }
    
    private void queuePoint(Point3d point) {
        if (lastPoint != null) {
            lines.add(new LineSegment(lastPoint, point, 0, 0));
        }
        lastPoint = point;
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