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

import com.willwinder.universalgcodesender.gcode.processors.ICommandProcessor;
import com.willwinder.universalgcodesender.types.PointSegment;

import java.text.DecimalFormat;
import java.util.*;
import javax.vecmath.Point3d;

/**
 *
 * @author wwinder
 */
public class GcodeParser implements IGcodeParser {

    // Current state
    private GcodeState state;

    // Last two commands.
    private PointSegment latest;
    private PointSegment secondLatest;

    private final ArrayList<ICommandProcessor> processors = new ArrayList<>();
    
    public GcodeParser() {
        this.state = new GcodeState();
        this.reset();
    }

    /**
     * @return the number of command processors that have been added.
     */
    public int numCommandProcessors() {
        return this.processors.size();
    }

    /**
     * Add a preprocessor to use with the preprocessCommand method.
     */
    public void addCommandProcessor(ICommandProcessor p) {
        this.processors.add(p);
    }

    /**
     * Clear out any processors that have been added.
     */
    public void resetCommandProcessors() {
        this.processors.clear();
    }

    // Resets the current state.
    private void reset() {
        this.state.currentPoint = new Point3d();
        latest = new PointSegment(this.state.currentPoint, -1);
        secondLatest = null;
    }
    
    /**
     * Add a command to be processed with no line number association.
     */
    @Override
    public List<PointSegment> addCommand(String command) throws GcodeParserException {
        return addCommand(command, this.state.commandNumber++);
    }

    /**
     * Add a command to be processed with a line number.
     * @throws GcodeParserException If the command is too long throw an exception
     */
    @Override
    public List<PointSegment> addCommand(String command, int line) throws GcodeParserException {
        //String stripped = GcodePreprocessorUtils.removeComment(command);
        List<String> commands = this.preprocessCommand(command);
        List<PointSegment> results = new ArrayList<>();
        for (String c: commands) {
            List<String> args = GcodePreprocessorUtils.splitCommand(c);
            List<PointSegment> points = this.addCommand(args, line);
            if (points != null) {
                results.addAll(points);
            }
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
    @Override
    public GcodeState getCurrentState() {
        return this.state;
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

        secondLatest = latest;
        latest = ps;

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

        secondLatest = latest;
        latest = ps;

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

    /**
     * Preprocesses a command. Does not update state.
     */
    @Override
    public List<String> preprocessCommand(String command) throws GcodeParserException {
        List<String> result = new ArrayList<>();
        result.add(command);
        for (ICommandProcessor p : processors) {
            // Process each command in the list and add results to the end.
            // Don't re-process the results with the same preprocessor.
            for (int i = result.size(); i > 0; i--) {
                result.addAll(p.processCommand(result.remove(0), state));
            }
        }

        return result;
    }
    
    /**
     * Expands the last point in the list if it is an arc according to the
     * the parsers settings.
     */
    /*
    private List<PointSegment> expandArc() {
        PointSegment startSegment = secondLatest;
        PointSegment lastSegment = latest;

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
        int num = latest.getLineNumber();
        latest = secondLatest;
        secondLatest = null;
                
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
            secondLatest = latest;
            latest = temp;
            psl.add(temp);
        }

        // Update the new endpoint.
        this.state.currentPoint = latest.point();

        return psl;
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
    */
}
