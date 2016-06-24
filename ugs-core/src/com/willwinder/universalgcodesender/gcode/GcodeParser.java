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

import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import static com.willwinder.universalgcodesender.gcode.util.Plane.*;
import com.willwinder.universalgcodesender.gcode.processors.ICommandProcessor;
import com.willwinder.universalgcodesender.gcode.util.PlaneFormatter;
import com.willwinder.universalgcodesender.types.PointSegment;

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


    /**
     * An intermediate object with all metadata for a given point.
     */
    private static class GcodeMeta {
        public String command;

        // Gcode state after processing the command.
        public GcodeState state;

        // There should be a point OR a collection of points. Not both.
        // PointSegments represent the endpoint of a given command.
        public PointSegment point;
        public List<PointSegment> points;
    }
    
    /**
     * Constructor.
     */
    public GcodeParser() {
        this.state = new GcodeState();
        this.reset();
    }

    /**
     * @return the number of command processors that have been added.
     */
    @Override
    public int numCommandProcessors() {
        return this.processors.size();
    }

    /**
     * Add a preprocessor to use with the preprocessCommand method.
     */
    @Override
    public void addCommandProcessor(ICommandProcessor p) {
        this.processors.add(p);
    }

    /**
     * Clear out any processors that have been added.
     */
    @Override
    public void resetCommandProcessors() {
        this.processors.clear();
    }

    /**
     * Resets the current state.
     */
    public void reset() {
        this.state.currentPoint = new Point3d();
        this.state.commandNumber = -1;
        latest = new PointSegment(this.state.currentPoint, -1);
        secondLatest = null;
    }
    
    /**
     * Add a command to be processed with no line number association.
     */
    @Override
    public List<PointSegment> addCommand(String command) throws GcodeParserException {
        return addCommand(command, ++this.state.commandNumber);
    }

    /**
     * Add a command to be processed with a line number.
     * @throws GcodeParserException If the command is too long throw an exception
     */
    @Override
    public List<PointSegment> addCommand(String command, int line) throws GcodeParserException {
        List<PointSegment> results = new ArrayList<>();
        // Add command get meta doesn't update the state, so we need to do that
        // manually.
        //List<String> processedCommands = this.preprocessCommand(command);
        Collection<GcodeMeta> metaObjects = addCommandGetMeta(command, line, state);
        if (metaObjects != null) {
            for (GcodeMeta c : metaObjects) {
                if (c.point != null)
                    results.add(c.point);
                if (c.points != null)
                    results.addAll(c.points);
                if (c.state != null)
                    this.state = c.state;
            }
        }

        for (PointSegment ps : results) {
            secondLatest = latest;
            latest = ps;
        }
        return results;
    }

    /**
     * Get point segment and corresponding gcode states, does not modify current
     * state.
     */
    private static List<GcodeMeta> addCommandGetMeta(String command, int line, final GcodeState state) throws GcodeParserException {
        List<GcodeMeta> results = new ArrayList<>();
        List<String> args = GcodePreprocessorUtils.splitCommand(command);
        if (args.isEmpty()) return null;
        return processCommand(command, args, line, state);
    }
    
    /**
     * Gets the point at the end of the list.
     */
    @Override
    public GcodeState getCurrentState() {
        return this.state;
    }
    
    private static List<GcodeMeta> processCommand(String command, List<String> args, int line, final GcodeState inputState) {
        List<GcodeMeta> results = new ArrayList<>();
        
        GcodeState state = inputState.copy();
        state.commandNumber = line;
        
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
            GcodeMeta meta = handleGCode(i, args, line, state);
            meta.command = command;
            // Commands like 'G21' don't return a point segment.
            if (meta.point != null) {
                meta.point.setSpeed(state.speed);
            }
            results.add(meta);
        }
        
        return results;
    }

    private static PointSegment addLinearPointSegment(Point3d nextPoint, boolean fastTraverse, int line, GcodeState state) {
        PointSegment ps = new PointSegment(nextPoint, line);

        boolean zOnly = false;

        // Check for z-only
        if ((state.currentPoint.x == nextPoint.x) &&
                (state.currentPoint.y == nextPoint.y) &&
                (state.currentPoint.z != nextPoint.z)) {
            zOnly = true;
        }

        ps.setIsMetric(state.isMetric);
        ps.setIsZMovement(zOnly);
        ps.setIsFastTraverse(fastTraverse);

        // Save off the endpoint.
        state.currentPoint = nextPoint;
        return ps;
    }

    private static PointSegment addArcPointSegment(Point3d nextPoint, boolean clockwise, List<String> args, int line, GcodeState state) {
        PointSegment ps = new PointSegment(nextPoint, line);

        Point3d center =
                GcodePreprocessorUtils.updateCenterWithCommand(
                        args, state.currentPoint, nextPoint, state.inAbsoluteIJKMode, clockwise, new PlaneFormatter(state.plane));

        double radius = GcodePreprocessorUtils.parseCoord(args, 'R');

        // Calculate radius if necessary.
        if (Double.isNaN(radius)) {
            radius = Math.sqrt(
                    Math.pow(state.currentPoint.x - center.x, 2.0)
                            + Math.pow(state.currentPoint.y - center.y, 2.0));
        }

        ps.setIsMetric(state.isMetric);
        ps.setArcCenter(center);
        ps.setIsArc(true);
        ps.setRadius(radius);
        ps.setIsClockwise(clockwise);
        ps.setPlaneState(state.plane);

        // Save off the endpoint.
        state.currentPoint = nextPoint;
        return ps;
    }

    /**
     * A copy of the state object should go in the resulting GcodeMeta object.
     */
    private static GcodeMeta handleGCode(String code, List<String> args, int line, GcodeState state) {
        GcodeMeta meta = new GcodeMeta();

        Point3d nextPoint = 
            GcodePreprocessorUtils.updatePointWithCommand(
            args, state.currentPoint, state.inAbsoluteMode);

        if (code.length() > 1 && code.startsWith("0"))
            code = code.substring(1);

        switch (code) {
            case "0":
                meta.point = addLinearPointSegment(nextPoint, true, line, state);
                break;
            case "1":
                meta.point = addLinearPointSegment(nextPoint, false, line, state);
                break;

            // Arc command.
            case "2":
                meta.point = addArcPointSegment(nextPoint, true, args, line, state);
                break;
            case "3":
                meta.point = addArcPointSegment(nextPoint, false, args, line, state);
                break;

            case "17":
                state.plane = XY;
                break;

            case "18":
                state.plane = ZX;
                break;

            case "19":
                state.plane = YZ;
                break;

            case "17.1":
                state.plane = UV;
                break;

            case "18.1":
                state.plane = WU;
                break;

            case "19.1":
                state.plane = VW;
                break;

            case "20":
                //inch
                state.isMetric = false;
                break;
            case "21":
                //mm
                state.isMetric = true;
                break;

            case "90":
                state.inAbsoluteMode = true;
                break;
            case "90.1":
                state.inAbsoluteIJKMode = true;
                break;

            case "91":
                state.inAbsoluteMode = false;
                break;
            case "91.1":
                state.inAbsoluteIJKMode = false;
                break;
            default:
                break;
        }
        state.lastGcodeCommand = code;
        meta.state = state.copy();
        return meta;
    }

    /**
     * Applies all command processors to a given command and returns the
     * resulting GCode. Does not change the parser state.
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
