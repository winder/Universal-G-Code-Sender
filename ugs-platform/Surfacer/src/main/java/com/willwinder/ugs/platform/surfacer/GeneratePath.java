/*
 * Copyright (C) 2025 dimic
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.willwinder.ugs.platform.surfacer;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 *
 * @author dimic
 */
public class GeneratePath {

    private Prefs prefs;
    
    double xmin, ymin, xmax, ymax;
    double width, height;
    Box box = new Box();

    private enum Dirs { UP, RIGHT, DOWN, LEFT }

    public void init(Prefs prefs) {
        this.prefs = prefs;
        double toolRad = prefs.toolDiameter() / 2;
        xmin = box.xmin = Math.min(prefs.x0(), prefs.x1()) + toolRad;
        ymin = box.ymin = Math.min(prefs.y0(), prefs.y1()) + toolRad;
        xmax = box.xmax = Math.max(prefs.x0(), prefs.x1()) - toolRad;
        ymax = box.ymax = Math.max(prefs.y0(), prefs.y1()) - toolRad;
        width = xmax - xmin;
        height = ymax - ymin;
    }
    
    static class Point {
        double x, y;
        Point(double x, double y) { this.x = x; this.y = y; }
        @Override
        public String toString() { return String.format("(%.10f, %.10f)", x, y); }
    }

    static class Box {
        double xmin, ymin, xmax, ymax;
        Box(double xmin, double xmax, double ymin, double ymax) {
            this.xmin = xmin;
            this.xmax = xmax;
            this.ymin = ymin;
            this.ymax = ymax;
        }
        Box() {}
    }
    
    private Point[] liangBarskyClip(Point p1, Point p2, Box b) {
        double x0 = p1.x, y0 = p1.y;
        double x1 = p2.x, y1 = p2.y;

        double dx = x1 - x0;
        double dy = y1 - y0;

        double t0 = 0.0;
        double t1 = 1.0;

        double[] p = {-dx, dx, -dy, dy};
        double[] q = {x0 - b.xmin, b.xmax - x0, y0 - b.ymin, b.ymax - y0};

        for (int i = 0; i < 4; i++) {
            if (p[i] == 0) {
                if (q[i] < 0) return null; // Line is parallel and outside
            } else {
                double r = q[i] / p[i];
                if (p[i] < 0) {
                    t0 = Math.max(t0, r);
                } else {
                    t1 = Math.min(t1, r);
                }
            }
        }

        if (t0 > t1) return null; // No visible segment

        Point clippedStart = new Point(x0 + t0 * dx, y0 + t0 * dy);
        Point clippedEnd   = new Point(x0 + t1 * dx, y0 + t1 * dy);
        return new Point[]{clippedStart, clippedEnd};
    }
    
    List<Point> rasterPath() {
        List<Point> path = new ArrayList<>();

        double step = prefs.toolDiameter() * (1 - prefs.overlap());
        double theta = Math.toRadians(prefs.angle());
        double dx = Math.cos(theta), dy = Math.sin(theta);
        double nx = -dy, ny = dx;   // normals
        
        // Project box corners onto normal
        double[] projections = {
            xmin * nx + ymin * ny,
            xmax * nx + ymin * ny,
            xmax * nx + ymax * ny,
            xmin * nx + ymax * ny
        };
        double minProj = Arrays.stream(projections).min().getAsDouble();
        double maxProj = Arrays.stream(projections).max().getAsDouble();

        // if angle is orthogonal, the outer inset box equals the border box,
        // so skip the outer inset box
        if (prefs.angle() == 0 || prefs.angle() == 90) {
            minProj += step;
            maxProj -= step;
        }

        // calculate the step
        double span = maxProj - minProj;
        step = span / (int)Math.ceil(span / step);

        path.add(new Point(xmin, ymin));
        path.add(new Point(xmin, ymax));
        path.add(new Point(xmax, ymax));
        path.add(new Point(xmax, ymin));
        path.add(new Point(xmin, ymin));
        path.add(null);
        
        double x0, y0, lastMax = maxProj, lastMin = minProj;
        Point[] clipped;
        boolean topline = true;
        while (lastMax - lastMin >= step) {
            if (topline) {
                x0 = minProj * nx;
                y0 = minProj * ny;
                minProj += step;
            } else {
                x0 = maxProj * nx;
                y0 = maxProj * ny;
                maxProj -= step;
            }

            Point p1 = new Point(x0 - dx * 1e6, y0 - dy * 1e6);
            Point p2 = new Point(x0 + dx * 1e6, y0 + dy * 1e6);
            clipped = liangBarskyClip(p1, p2, box);
            if (clipped != null) {
                if (prefs.climbCut() ^ topline) {
                    path.add(clipped[1]);
                    path.add(clipped[0]);
                } else {
                    path.add(clipped[0]);
                    path.add(clipped[1]);
                }
                if (topline) lastMin = minProj - step;
                else lastMax = maxProj + step;
                path.add(null);                
            }
            topline = !topline;
        }
        return path.subList(0, path.size()-1);
    }

    
    private boolean spiralMove(List<Point> path, boolean climb, double step, Box b, Dirs d) { 
        boolean positive = d == Dirs.UP || d == Dirs.RIGHT;
        
        if (d == Dirs.UP || d == Dirs.DOWN) {
            double x = positive == climb ? b.xmin : b.xmax;
            double y1 = positive ? b.ymax : b.ymin, y2 = positive ? b.ymin : b.ymax;
            path.add(new Point(x, y1));
            if (b.xmax - b.xmin <= step) {
                double cx = b.xmin + ((positive == climb ? step : -step) + b.xmax - b.xmin) / 2;
                path.add(new Point(cx, y1));
                path.add(new Point(cx, y2));
                return true;
            }
            if (positive == climb) b.xmin += step; else b.xmax -= step;
        } else {
            double x1 = positive ? b.xmax : b.xmin, x2 = positive ? b.xmin : b.xmax;
            double y = positive == climb ? b.ymax : b.ymin;
            path.add(new Point(x1, y));
            if (b.ymax - b.ymin <= step) {
                double cy = b.ymax - ((positive == climb ? step : -step) + b.ymax - b.ymin) / 2;
                path.add(new Point(x1, cy));
                path.add(new Point(x2, cy));
                return true;
            }
            if (positive == climb) b.ymax -= step; else b.ymin += step;
        }
        return false;
    }
    
    List<Point> spiralPath() {
        List<Point> path = new ArrayList<>();
        
        // Current bounds
        Box bounds = new Box(xmin, xmax, ymin, ymax);
        double step  = prefs.toolDiameter() * (1.0 - prefs.overlap());

        // Start at bottom–left corner
        path.add(new Point(bounds.xmin, bounds.ymin));
        
        boolean climb = prefs.climbCut();
        Dirs[] dir = climb
                ? new Dirs[]{ Dirs.UP,    Dirs.RIGHT, Dirs.DOWN, Dirs.LEFT }
                : new Dirs[]{ Dirs.RIGHT, Dirs.UP,    Dirs.LEFT, Dirs.DOWN };
        
        boolean firstCycle = true;
        while (true) {
            for (Dirs d : dir) {
                if (firstCycle && d == Dirs.DOWN && !climb)
                    path.add(new Point(bounds.xmin, bounds.ymin - step));
                else if (firstCycle && d == Dirs.LEFT && climb)
                    path.add(new Point(bounds.xmin - step, bounds.ymin));
                if (spiralMove(path, climb, step, bounds, d)) return path;
            }
            firstCycle = false;
        }
    }

}
