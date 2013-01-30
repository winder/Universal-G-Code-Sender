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

import javax.vecmath.Point3f;


public class GcodeViewParse {
    private static boolean debugVals = false;
    private static float extremes[] = {0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f}; // -x, -y, -z, x, y, z
    
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
        float parsedX, parsedY, parsedZ, parsedF;
        float tolerance = .0002f;
        ArrayList<LineSegment> lines = new ArrayList<LineSegment>();
        float[] lastCoord = { 0.0f, 0.0f, 0.0f};
        boolean currentExtruding = false;
        for(String s : gcode)
        {
            if(s.matches(".*M101.*"))
            {
                currentExtruding = true;
            }
            if(s.matches(".*M103.*"))
            {
                currentExtruding = false;
            }
            if(s.matches("\\(\\</layer\\>\\)"))
            {
                curLayer++; 
            }
            if(s.matches(".*T0.*"))
            {
                curToolhead = 0;
            }
            if(s.matches(".*T1.*"))
            {
                curToolhead = 1;
            }
            // Straight lines.
            if (s.matches(".*G0.*") || s.matches(".*G1.*") || s.matches(".*G2.*") || s.matches(".*G3.*")) 
            {
                String[] sarr = s.split(" ");
                parsedX = parseCoord(sarr, 'X');
                parsedY = parseCoord(sarr, 'Y');
                parsedZ = parseCoord(sarr, 'Z');
                parsedF = parseCoord(sarr, 'F');

                //System.out.println(Arrays.toString(sarr));
                if(!Float.isNaN(parsedX))
                {
                    lastCoord[0] = parsedX;
                }
                if(!Float.isNaN(parsedY))
                {
                    lastCoord[1] = parsedY;
                }
                if(!Float.isNaN(parsedZ))
                {
                    
                    if (!(Math.abs(parsedZ - lastCoord[2]) <= tolerance))
                    {
                        curLayer++;
                    }
                    
                    lastCoord[2] = parsedZ;
                }
                if(!Float.isNaN(parsedF))
                {
                    speed = parsedF;
                }
                if(!(Float.isNaN(lastCoord [0]) || Float.isNaN(lastCoord [1]) || Float.isNaN(lastCoord [2])))
                {
                    if(debugVals)
                    {
                        System.out.println(lastCoord[0] + "," + lastCoord [1] + "," + lastCoord[2] + ", speed =" + speed + 
                                        ", layer=" + curLayer);
                    }
                    curPoint = new Point3f(lastCoord[0], lastCoord[1], lastCoord[2]);
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
                
        
            }
        }
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
            } else {
                System.out.println("No match: " + t);
            }
        }
        return Float.NaN;
    }
}