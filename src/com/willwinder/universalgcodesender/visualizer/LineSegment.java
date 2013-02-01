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

import javax.vecmath.Point3d;
 
public class LineSegment {

    private int layer;
    private int toolhead = 0; //DEFAULT TOOLHEAD ASSUMED TO BE 0!
    private double speed;
    private Point3d first, second;
    private boolean isExtruding;

    public LineSegment (Point3d a,Point3d b, int layernum, double speedz)
    {
        first = a;
        second = b;
        layer = layernum;
        speed = speedz;
    }
    public LineSegment (Point3d a,Point3d b, int layernum, double speedz, boolean extrudz)
    {
        first = a;
        second = b;
        layer = layernum;
        speed = speedz;
        isExtruding = extrudz;
    }
    public LineSegment(double x1, double y1, double z1, double x2, double y2, double z2, int layernum, double speedz)
    {
        first = new Point3d(x1, y1, z1);
        second = new Point3d(x2, y2, z2);
        layernum = layer;
        speed = speedz;
    }
    public LineSegment (Point3d a,Point3d b, int layernum, double speedz, int toolheadz)
    {
        first = a;
        second = b;
        layer = layernum;
        speed = speedz;
        toolhead = toolheadz;
    }
    public LineSegment(double x1, double y1, double z1, double x2, double y2, double z2, int layernum, double speedz, int toolheadz)
    {
        first = new Point3d(x1, y1, z1);
        second = new Point3d(x2, y2, z2);
        layernum = layer;
        speed = speedz;
        toolhead = toolheadz;
    }
    public LineSegment (Point3d a,Point3d b, int layernum, double speedz, int toolheadz, boolean extrudz)
    {
        first = a;
        second = b;
        layer = layernum;
        speed = speedz;
        toolhead = toolheadz;
        isExtruding = extrudz;
    }
    public LineSegment(double x1, double y1, double z1, double x2, double y2, double z2, int layernum, double speedz, int toolheadz, boolean extrudz)
    {
        first = new Point3d(x1, y1, z1);
        second = new Point3d(x2, y2, z2);
        layernum = layer;
        speed = speedz;
        toolhead = toolheadz;
        isExtruding = extrudz;
    }
    
    public Point3d[] getPointArray()
    {
        Point3d[] pointarr = { first, second };
        return pointarr;
    }
    
    public double[] getPoints()
    {
        double[] points = {first.x, first.y, first.z , second.x, second.y, second.z };
        return points;
    }
    
    public Point3d getStart() {
        return this.first;
    }

    public Point3d getEnd() {
        return this.second;
    }
    
    public int getToolhead()
    {
        return toolhead;
    }
    
    public double getSpeed()
    {
        return speed;
    }
    
    public int getLayer()
    {
        return layer;
    }
    
    public boolean getExtruding()
    {
        return isExtruding;
    }
}