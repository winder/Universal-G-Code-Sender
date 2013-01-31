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
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.vecmath.Point3d;

import javax.vecmath.Point3f;


public class GcodeViewParse {
    private static boolean debugVals = false;
    private static float extremes[] = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f}; // -x, -y, -z, x, y, z
    // false = incremental; true = absolute
    boolean absoluteMode = false;

    public GcodeViewParse()
    {

    }

    public float[] getExtremes()
    {
        return extremes;
    }
    
    private void testExtremes(Point3f p3f)
    {
        testExtremes(p3f.x, p3f.y, p3f.z);
    }
    
    private void testExtremes(float x, float y, float z)
    {
        if(x < extremes[0])
        {
            extremes[0] = x;
        }
        if(x > extremes[3])
        {
            extremes[3] = x;
        }
        if(y < extremes[1])
        {
            extremes[1] = y;
        }
        if(y > extremes[4])
        {
            extremes[4] = y;
        }
        if(z < extremes[2])
        {
            extremes[2] = z;
        }
        if(z > extremes[5])
        {
            extremes[5] = z;
        }
    }
    public ArrayList<LineSegment> toObj(ArrayList<String> gcode)
    {
        float speed = 2; //DEFAULTS to 2
        Point3f lastPoint = null;
        Point3f curPoint = null;
        int curLayer = 0;
        int curToolhead = 0;
        float parsedX, parsedY, parsedZ, parsedF, parsedI, parsedJ;
        float tolerance = .0002f;
        ArrayList<LineSegment> lines = new ArrayList<LineSegment>();
        float[] lastCoord = { 0.0f, 0.0f, 0.0f};
        boolean currentExtruding = false;
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
            
            //System.out.println(Arrays.toString(sarr));
            if(!Float.isNaN(parsedX))
            {
                if (!this.absoluteMode)
                    parsedX += lastCoord[0];
                lastCoord[0] = parsedX;
            }
            if(!Float.isNaN(parsedY))
            {
                if (!this.absoluteMode)
                    parsedY += lastCoord[1];
                lastCoord[1] = parsedY;
            }
            if(!Float.isNaN(parsedZ))
            {

                if (!(Math.abs(parsedZ - lastCoord[2]) <= tolerance))
                {
                    curLayer++;
                }

                if (!this.absoluteMode)
                    parsedZ += lastCoord[2];
                lastCoord[2] = parsedZ;
            }
            if(!Float.isNaN(parsedF))
            {
                speed = parsedF;
            }
            
            curPoint = new Point3f(lastCoord[0], lastCoord[1], lastCoord[2]);
            
            // Straight lines.
            if (s.matches(".*G0.*") || s.matches(".*G1.*")) 
            {

                if(!(Float.isNaN(lastCoord [0]) || Float.isNaN(lastCoord [1]) || Float.isNaN(lastCoord [2])))
                {
                    if(debugVals)
                    {
                        System.out.println(lastCoord[0] + "," + lastCoord [1] + "," + lastCoord[2] + ", speed =" + speed + 
                                        ", layer=" + curLayer);
                    }
                    
                    if(currentExtruding && curLayer > 5)
                    {
                        testExtremes(curPoint);
                    }
                    if(lastPoint != null)
                    {
                        lines.add(new LineSegment(lastPoint, curPoint, curLayer, speed, curToolhead, currentExtruding));
                    }
                    lastPoint = curPoint;
                }
            }
            
            // Arc lines
            if (s.matches(".*G2.*") || s.matches(".*G3.*")) {
                int gCode = 2;
                if (s.matches(".*G3.*")) {
                    gCode = 3;
                }
                
                // call our arc drawing function.
                if (!Float.isNaN(parsedI) && !Float.isNaN(parsedJ)) {
                    
                    // our centerpoint
                    Point3f center = new Point3f();
                    center.x = lastCoord[0] + parsedI;
                    center.y = lastCoord[1] + parsedJ;
                    center.z = 0;
                    
                    // draw the arc itself.
                    if (lastPoint!= null) {
                        if (gCode == 2)
                            lines.addAll( segmentArc(lastPoint, center, curPoint, true) );
                        else
                            lines.addAll( segmentArc(lastPoint, center, curPoint, false) );
                    }
                }
                
                lastPoint = curPoint;
            }
            
            // Absolute Positioning
            if (s.matches(".*G90.*")) {
                    absoluteMode = true;
            }

            // Incremental Positioning
            if (s.matches(".*G91.*")) {
                    absoluteMode = false;
            }
        }
        return lines;
    }
    
    
    private ArrayList<LineSegment> segmentArc(Point3f start, Point3f center, Point3f endpoint, boolean clockwise) {
        System.out.println("Arc from " + start.toString() + " to " +
                           endpoint.toString() + " with center " + center);
        ArrayList<LineSegment> lines = new ArrayList<LineSegment>();
        Point3f current = new Point3f();
        current.x = start.x;
        current.y = start.y;
        current.z = start.z;
        
        // angle variables.
        float angleA;
        float angleB;
        float angle;
        float radius;
        float length;

        // delta variables.
        float aX;
        float aY;
        float bX;
        float bY;

        // figure out our deltas
        aX = current.x - center.x;
        aY = current.y - center.y;
        bX = endpoint.x - center.x;
        bY = endpoint.y - center.y;

        // Clockwise
        if (clockwise) {
                angleA = (float)Math.atan2(bY, bX);
                angleB = (float)Math.atan2(aY, aX);
        }
        // Counterclockwise
        else {
                angleA = (float)Math.atan2(aY, aX);
                angleB = (float)Math.atan2(bY, bX);
        }

        // Make sure angleB is always greater than angleA
        // and if not add 2PI so that it is (this also takes
        // care of the special case of angleA == angleB,
        // ie we want a complete circle)
        if (angleB <= angleA)
                angleB += 2 * Math.PI;
        angle = angleB - angleA;
        // calculate a couple useful things.
        radius = (float)Math.sqrt(aX * aX + aY * aY);
        length = radius * angle;

        // for doing the actual move.
        int steps;
        int s;
        int step;

        // Maximum of either 2.4 times the angle in radians
        // or the length of the curve divided by the curve section constant
        steps = (int) Math.ceil(Math.max(angle * 2.4, length / 0.02));
        steps = 35;
        // this is the real draw action.
        Point3f newPoint = new Point3f();
        float arcStartZ = current.z;
        for (s = 1; s <= steps; s++) {
                // Forwards for CCW, backwards for CW
                if (!clockwise)
                        step = s;
                else
                        step = steps - s;

                // calculate our waypoint.
                newPoint.x = (float)center.x + radius
                                * (float)Math.cos(angleA + angle * ((float) step / steps));
                newPoint.y = (float)center.y + radius
                                * (float)Math.sin(angleA + angle * ((float) step / steps));
                newPoint.z = arcStartZ + (endpoint.z - arcStartZ) * s / steps;

                // start the move
                //lines.add(new LineSegment(current, newPoint, 0, 0));
                
                current.x = newPoint.x;
                current.y = newPoint.y;
                current.z = newPoint.z;
        }
        lines.add(new LineSegment(start, endpoint, 0, 0));
        return lines;
    }

    private float parseCoord(String[] sarr, char c)
    {
        for(String t : sarr)
        {
            if(t.matches("\\s*[" + c + "]\\s*-*[\\d|\\.]+"))
            {
                //System.out.println("te : " + t);
                return Float.parseFloat(t.substring(1,t.length()));
            }
        }
        return Float.NaN;
    }
    
}