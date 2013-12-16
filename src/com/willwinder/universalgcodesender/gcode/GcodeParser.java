/*
 * Object to parse gcode one command at a time in a way that can be used by any
 * other class which needs to know about the current state at a given command.
 */

/*
    Copywrite 2013 Will Winder

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
package com.willwinder.universalgcodesender.gcode;

import com.willwinder.universalgcodesender.types.PointSegment;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class GcodeParser {
    // Current state
    private boolean isMetric = true;
    private boolean isAbsoluteMode = true;
    private boolean isAbsoluteIJKMode = false;
    private int lastGcodeCommand = -1;
    private Point3d currentPoint = null;
    private int commandNumber = 0;
    
    // The gcode.
    List<PointSegment> points;
    
    public GcodeParser() {
        this.reset();
    }
    
    // Resets the current state.
    final public void reset() {
        this.currentPoint = new Point3d();
        this.points = new ArrayList<PointSegment>();
        // The unspoken home location.
        this.points.add(new PointSegment(this.currentPoint, -1));
    }
    
    /**
     * Add a command to be processed.
     */
    public PointSegment addCommand(String command) {
        String stripped = GcodePreprocessorUtils.removeComment(command);
        List<String> args = GcodePreprocessorUtils.splitCommand(stripped);
        return this.addCommand(args);
    }
    
    /**
     * Add a command which has already been broken up into its arguments.
     */
    public PointSegment addCommand(List<String> args) {
        if (args.isEmpty()) {
            return null;
        }
        return processCommand(args);
    }

    /**
     * Warning, this should only be used when modifying live gcode, such as when
     * expanding an arc or canned cycle into line segments.
     */
    private void setLastGcodeCommand(int num) {
        this.lastGcodeCommand = num;
    }
    
    /**
     * Gets the point at the end of the list.
     */
    public Point3d getCurrentPoint() {
        return currentPoint;
    }
    
    /**
     * Expands the last point in the list if it is an arc according to the
     * provided parameters.
     */
    public List<PointSegment> expandArcWithParameters(double minLengthMM, double segmentLengthMM, int roundTo) {
        PointSegment startSegment = this.points.get(this.points.size() - 2);
        PointSegment lastSegment = this.points.get(this.points.size() - 1);

        // Can only expand arcs.
        if (!lastSegment.isArc()) {
            return null;
        }
        
        // Get precalculated stuff.
        Point3d start     = startSegment.point();
        Point3d end       = lastSegment.point();
        Point3d center    = lastSegment.center();
        double radius     = lastSegment.getRadius();
        boolean clockwise = lastSegment.isClockwise();

        //
        // Start expansion.
        //
        List<Point3d> expandedPoints;
        boolean withoutThreshhold = false;
        
        if (withoutThreshhold) {
            expandedPoints = GcodePreprocessorUtils.generatePointsAlongArcBDring(
                                start, end, center, clockwise, radius, 20);
        }
        else {
            // Calculate radius if necessary.
            if (radius == 0) {
                radius = Math.sqrt(Math.pow(start.x - center.x, 2.0) + Math.pow(start.y - center.y, 2.0));
            }

            // Calculate angles from center.
            double startAngle = GcodePreprocessorUtils.getAngle(center, start);
            double endAngle = GcodePreprocessorUtils.getAngle(center, end);


            // Fix semantics, if the angle ends at 0 it really should end at 360.
            if (endAngle == 0) {
                    endAngle = Math.PI * 2;
            }

            // Calculate distance along arc.
            double sweep;
            if (!clockwise && endAngle < startAngle) {
                sweep = ((Math.PI * 2 - startAngle) + endAngle);
            } else if (clockwise && endAngle > startAngle) {
                sweep = ((Math.PI * 2 - endAngle) + startAngle);
            } else {
                sweep = Math.abs(endAngle - startAngle);
            }

            // Convert units.
            double distance = sweep * radius;
            double radiusInMM = radius;
            if (this.isMetric == false) {
                distance *= 25.4;
                radiusInMM *= 25.4;
            }

            // If this arc doesn't meet the minimum threshold, don't expand.
            if (distance > minLengthMM) {
                return null;
            }

            // mm_per_arc_segment calculation isn't working
            //double mm_per_arc_segment = Math.sqrt(4*arcTolerance*(2*radiusInMM-arcTolerance));
            
            double mm_per_arc_segment = segmentLengthMM;
            int numPoints = (int)Math.ceil(distance/mm_per_arc_segment);

            expandedPoints = GcodePreprocessorUtils.generatePointsAlongArcBDring(
                            start, end, center, clockwise, radius, 
                            startAngle, endAngle, sweep, numPoints);
        }
        
        // Validate output of expansion.
        if (expandedPoints == null) {
            return null;
        }
        
        // Remove the last point now that we're about to expand it.
        this.points.remove(this.points.size() - 1);
        commandNumber--;        
                
        // Initialize return value
        List<PointSegment> psl = new ArrayList<PointSegment>();

        // Create line segments from points.
        PointSegment temp;
        // skip first element.
        Iterator<Point3d> psi = expandedPoints.listIterator(1);
        while (psi.hasNext()) {
            temp = new PointSegment(psi.next(), commandNumber++);
            temp.setIsMetric(lastSegment.isMetric());

            // Add new points.
            this.points.add(temp);
            psl.add(temp);
        }

        // Update the new endpoint.
        this.currentPoint = this.points.get(this.points.size() - 1).point();

        return psl;
    }
    
    public List<PointSegment> getPointSegmentList() {
        return this.points;
    }

    private PointSegment processCommand(List<String> args) {
        List<Integer> gCodes;
        PointSegment ps = null;
        
        // handle M codes.
        //codes = GcodePreprocessorUtils.parseCodes(args, 'M');
        //handleMCode(for each codes);
        
        // handle G codes.
        gCodes = GcodePreprocessorUtils.parseCodes(args, 'G');
        
        // If there was no command, add the implicit one to the party.
        if (gCodes.isEmpty() && lastGcodeCommand != -1) {
            gCodes.add(lastGcodeCommand);
        }
        
        for (Integer i : gCodes) {
            ps = handleGCode(i, args);
        }
        
        return ps;
    }
    
    private PointSegment handleGCode(int code, List<String> args) {
        PointSegment ps = null;
        Point3d nextPoint = 
            GcodePreprocessorUtils.updatePointWithCommand(
            args, this.currentPoint, this.isAbsoluteMode);

        switch (code) {
            case 0:
            case 1:
                ps = new PointSegment(nextPoint, commandNumber++);
                
                boolean zOnly = false;
                                
                // Check for z-only
                if ((this.currentPoint.x == nextPoint.x) &&
                    (this.currentPoint.y == nextPoint.y) &&
                    (this.currentPoint.z != nextPoint.z)) {
                    zOnly = true;
                }
                
                ps.setIsMetric(this.isMetric);
                ps.setIsZMovement(zOnly);
                ps.setIsFastTraverse(code == 0);
                this.points.add(ps);
                
                // Save off the endpoint.
                this.currentPoint = nextPoint;
                break;

            // Arc command.
            case 2:
            case 3:
                ps = new PointSegment(nextPoint, commandNumber++);
                
                boolean clockwise = true;
                if (code == 3) {
                    clockwise = false;
                }

                Point3d center = 
                        GcodePreprocessorUtils.updateCenterWithCommand(
                        args, this.currentPoint, nextPoint, this.isAbsoluteIJKMode, clockwise);

                double radius = GcodePreprocessorUtils.parseCoord(args, 'R');

                // Calculate radius if necessary.
                if (Double.isNaN(radius)) {
                    radius = Math.sqrt(
                            Math.pow(this.currentPoint.x - center.x, 2.0) 
                            + Math.pow(this.currentPoint.y - center.y, 2.0));
                }
                
                ps.setIsMetric(this.isMetric);
                ps.setArcCenter(center);
                ps.setIsArc(true);
                ps.setRadius(radius);
                ps.setIsClockwise(clockwise);
                this.points.add(ps);

                // Save off the endpoint.
                this.currentPoint = nextPoint;
                break;

            case 20:
                //inch
                this.isMetric = false;
                break;
            case 21:
                //mm
                this.isMetric = true;
                break;

            case 90:
                this.isAbsoluteMode = true;
                break;
            case 91:
                this.isAbsoluteMode = false;
                break;
        }
        this.lastGcodeCommand = code;
        return ps;
    }
}
