/*
 * Object to parse gcode one command at a time in a way that can be used by any
 * other class which needs to know about the current state at a given command.
 */

/*
    Copywrite 2013-2016 Will Winder

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

import java.text.DecimalFormat;
import java.util.*;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class GcodeParser {

    // Current state
    private GcodeState state;

    // Settings
    private double speedOverride = -1;
    private int truncateDecimalLength = 40;
    private boolean removeAllWhitespace = true;
    private boolean convertArcsToLines = false;
    private double smallArcThreshold = 1.0;
    // Not configurable outside, but maybe it should be.
    private double smallArcSegmentLength = 0.3;
    private final int maxCommandLength = 50;
    
    // The gcode.
    List<PointSegment> points;
    
    public GcodeParser() {
        this.state = new GcodeState();
        this.reset();
    }

    public boolean getConvertArcsToLines() {
        return convertArcsToLines;
    }

    public void setConvertArcsToLines(boolean convertArcsToLines) {
        this.convertArcsToLines = convertArcsToLines;
    }

    public boolean getRemoveAllWhitespace() {
        return removeAllWhitespace;
    }

    public void setRemoveAllWhitespace(boolean removeAllWhitespace) {
        this.removeAllWhitespace = removeAllWhitespace;
    }

    public double getSmallArcSegmentLength() {
        return smallArcSegmentLength;
    }

    public void setSmallArcSegmentLength(double smallArcSegmentLength) {
        this.smallArcSegmentLength = smallArcSegmentLength;
    }

    public double getSmallArcThreshold() {
        return smallArcThreshold;
    }

    public void setSmallArcThreshold(double smallArcThreshold) {
        this.smallArcThreshold = smallArcThreshold;
    }

    public double getSpeedOverride() {
        return speedOverride;
    }

    public void setSpeedOverride(double speedOverride) {
        this.speedOverride = speedOverride;
    }

    public int getTruncateDecimalLength() {
        return truncateDecimalLength;
    }

    public void setTruncateDecimalLength(int truncateDecimalLength) {
        this.truncateDecimalLength = truncateDecimalLength;
    }

    // Resets the current state.
    private void reset() {
        this.state.currentPoint = new Point3d();
        this.points = new ArrayList<>();
        // The unspoken home location.
        this.points.add(new PointSegment(this.state.currentPoint, -1));
    }
    
    /**
     * Add a command to be processed with no line number association.
     */
    public List<PointSegment> addCommand(String command) throws GcodeParserException {
        return addCommand(command, this.state.commandNumber++);
    }

    /**
     * Add a command to be processed with a line number.
     * @throws Exception If the command is too long throw an exception
     */
    public List<PointSegment> addCommand(String command, int line) throws GcodeParserException {
        //String stripped = GcodePreprocessorUtils.removeComment(command);
        List<String> commands = this.preprocessCommand(command);
        List<PointSegment> results = new ArrayList<>();
        for (String c: commands) {
            List<String> args = GcodePreprocessorUtils.splitCommand(c);
            results.addAll(this.addCommand(args, line));
        }
        return results;
    }
    
    /**
     * Add a command which has already been broken up into its arguments.
     */
    private List<PointSegment> addCommand(List<String> args, int line) {
        if (args.isEmpty()) {
            return null;
        }
        return processCommand(args, line);
    }

    /**
     * Warning, this should only be used when modifying live gcode, such as when
     * expanding an arc or canned cycle into line segments.
     */
    private void setLastGcodeCommand(String num) {
        this.state.lastGcodeCommand = num;
    }
    
    /**
     * Gets the point at the end of the list.
     */
    public GcodeState getCurrentState() {
        return this.state;
    }
    
    /**
     * Expands the last point in the list if it is an arc according to the
     * the parsers settings.
     */
    private List<PointSegment> expandArc() {
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
        List<Point3d> expandedPoints =
                GcodePreprocessorUtils.generatePointsAlongArcBDring(
                        start, end, center, clockwise, radius,
                        smallArcThreshold, smallArcSegmentLength);
        
        // Validate output of expansion.
        if (expandedPoints == null) {
            return null;
        }
        
        // Remove the last point now that we're about to expand it.
        int num = this.points.remove(this.points.size() - 1).getLineNumber();
                
        // Initialize return value
        List<PointSegment> psl = new ArrayList<>();

        // Create line segments from points.
        PointSegment temp;
        // skip first element.
        Iterator<Point3d> psi = expandedPoints.listIterator(1);
        while (psi.hasNext()) {
            temp = new PointSegment(psi.next(), num);
            temp.setIsMetric(lastSegment.isMetric());

            // Add new points.
            this.points.add(temp);
            psl.add(temp);
        }

        // Update the new endpoint.
        this.state.currentPoint = this.points.get(this.points.size() - 1).point();

        return psl;
    }
    
    public List<PointSegment> getPointSegmentList() {
        return this.points;
    }

    private List<PointSegment> processCommand(List<String> args, int line) {
        List<PointSegment> results = new ArrayList<>();
        
        // handle M codes.
        //codes = GcodePreprocessorUtils.parseCodes(args, 'M');
        //handleMCode(for each codes);

        List<String> fCodes = GcodePreprocessorUtils.parseCodes(args, 'F');
        if (!fCodes.isEmpty()) {
            state.speed = Double.parseDouble(fCodes.remove(fCodes.size()-1));
        }
        
        // handle G codes.
        List<String> gCodes = GcodePreprocessorUtils.parseCodes(args, 'G');
        
        // If there was no command, add the implicit one to the party.
        if (gCodes.isEmpty() && state.lastGcodeCommand != null && !state.lastGcodeCommand.isEmpty()) {
            gCodes.add(state.lastGcodeCommand);
        }
        
        for (String i : gCodes) {
            PointSegment ps = handleGCode(i, args, line);
            // Commands like 'G21' don't return a point segment.
            if (ps != null) {
                ps.setSpeed(state.speed);
                results.add(ps);
            }
        }
        

        return results;
    }

    private PointSegment addLinearPointSegment(Point3d nextPoint, boolean fastTraverse, int line) {
        PointSegment ps = new PointSegment(nextPoint, line);

        boolean zOnly = false;

        // Check for z-only
        if ((this.state.currentPoint.x == nextPoint.x) &&
                (this.state.currentPoint.y == nextPoint.y) &&
                (this.state.currentPoint.z != nextPoint.z)) {
            zOnly = true;
        }

        ps.setIsMetric(this.state.isMetric);
        ps.setIsZMovement(zOnly);
        ps.setIsFastTraverse(fastTraverse);
        this.points.add(ps);

        // Save off the endpoint.
        this.state.currentPoint = nextPoint;
        return ps;
    }

    private PointSegment addArcPointSegment(Point3d nextPoint, boolean clockwise, List<String> args, int line) {
        PointSegment ps = new PointSegment(nextPoint, line);

        Point3d center =
                GcodePreprocessorUtils.updateCenterWithCommand(
                        args, this.state.currentPoint, nextPoint, this.state.inAbsoluteIJKMode, clockwise);

        double radius = GcodePreprocessorUtils.parseCoord(args, 'R');

        // Calculate radius if necessary.
        if (Double.isNaN(radius)) {
            radius = Math.sqrt(
                    Math.pow(this.state.currentPoint.x - center.x, 2.0)
                            + Math.pow(this.state.currentPoint.y - center.y, 2.0));
        }

        ps.setIsMetric(this.state.isMetric);
        ps.setArcCenter(center);
        ps.setIsArc(true);
        ps.setRadius(radius);
        ps.setIsClockwise(clockwise);
        this.points.add(ps);

        // Save off the endpoint.
        this.state.currentPoint = nextPoint;
        return ps;
    }

    private PointSegment handleGCode(String code, List<String> args, int line) {
        PointSegment ps = null;
        Point3d nextPoint = 
            GcodePreprocessorUtils.updatePointWithCommand(
            args, this.state.currentPoint, this.state.inAbsoluteMode);

        if (code.length() > 1 && code.startsWith("0"))
            code = code.substring(1);

        switch (code) {
            case "0":
                ps = addLinearPointSegment(nextPoint, true, line);
                break;
            case "1":
                ps = addLinearPointSegment(nextPoint, false, line);
                break;

            // Arc command.
            case "2":
                ps = addArcPointSegment(nextPoint, true, args, line);
                break;
            case "3":
                ps = addArcPointSegment(nextPoint, false, args, line);
                break;

            case "20":
                //inch
                this.state.isMetric = false;
                break;
            case "21":
                //mm
                this.state.isMetric = true;
                break;

            case "90":
                this.state.inAbsoluteMode = true;
                break;
            case "90.1":
                this.state.inAbsoluteIJKMode = true;
                break;

            case "91":
                this.state.inAbsoluteMode = false;
                break;
            case "91.1":
                this.state.inAbsoluteIJKMode = false;
                break;
            default:
                break;
        }
        this.state.lastGcodeCommand = code;
        return ps;
    }

    private List<String> preprocessCommands(Collection<String> commands) throws GcodeParserException {
        int count = commands.size();
        int interval = count / 1000;
        List<String> result = new ArrayList<>(count);

        int i = 0;
        double row = 0;
        for (String command : commands) {
            i++;
            row++;
            if (i >= interval) {
                System.out.println("row " + (int)row + " of " + count);
                i = 0;
            }
            result.addAll(preprocessCommand(command));
        }

        return result;
    }

    /**
     * Preprocesses a command. Does not update state.
     */
    public List<String> preprocessCommand(String command) throws GcodeParserException {
        List<String> result = new ArrayList<>();

        // Remove comments from command.
        String newCommand = GcodePreprocessorUtils.removeComment(command);
        String rawCommand = newCommand;

        if (removeAllWhitespace) {
            newCommand = GcodePreprocessorUtils.removeAllWhitespace(newCommand);
        }
        
        newCommand = GcodePreprocessorUtils.removeM30(newCommand);

        if (newCommand.length() > 0) {

            // Override feed speed
            if (speedOverride > 0) {
                newCommand = GcodePreprocessorUtils.overrideSpeed(newCommand, speedOverride);
            }

            if (truncateDecimalLength > 0) {
                newCommand = GcodePreprocessorUtils.truncateDecimals(truncateDecimalLength, newCommand);
            }

            // If this is enabled we need to parse the gcode as we go along.
            if (convertArcsToLines) { // || this.expandCannedCycles) {
                List<String> arcLines = convertArcsToLines(newCommand);
                if (arcLines != null) {
                    result.addAll(arcLines);
                } else {
                    result.add(newCommand);
                }
            } else {
                result.add(newCommand);
            }
        }

        // Check command length
        for (String c : result) {
            if (c.length() > maxCommandLength) {
                throw new GcodeParserException("Command '"+c+"' is too long: " + c.length() + " > " + maxCommandLength);
            }
        }

        return result;
    }

    private List<String> convertArcsToLines(String command) throws GcodeParserException {

        List<String> result = null;

        // Save off the start of the arc for later.
        Point3d start = new Point3d(this.state.currentPoint);

        List<PointSegment> ps = addCommand(command);

        if (ps == null || ps.size() != 1 || !ps.get(0).isArc()) {
            return result;
        }

        List<PointSegment> psl = expandArc();

        if (psl == null) {
            return result;
        }

        int index;
        StringBuilder sb;

        // Create the commands...
        result = new ArrayList<>(psl.size());


        // Setup decimal formatter.
        sb = new StringBuilder("#.");
        for (index = 0; index < truncateDecimalLength; index++) {
            sb.append("#");
        }
        DecimalFormat df = new DecimalFormat(sb.toString());
        index = 0;

        // Create an array of new commands out of the of the segments in psl.
        // Don't add them to the gcode parser since it is who expanded them.
        for (PointSegment segment : psl) {
            Point3d end = segment.point();
            result.add(GcodePreprocessorUtils.generateG1FromPoints(start, end, this.state.inAbsoluteMode, df));
            start = segment.point();
        }

        return result;
    }
}
