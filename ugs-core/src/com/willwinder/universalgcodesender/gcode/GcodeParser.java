/**
 * Object to parse gcode one command at a time in a way that can be used by any
 * other class which needs to know about the current state at a given command.
 * 
 * This object can be extended by adding in any number of ICommandProcessor
 * objects which are applied to each command in the order they were inserted
 * into the parser. These processors can be as simple as removing whitespace to
 * as complex as expanding a canned cycle or applying an leveling plane.
 */

/*
    Copyright 2013-2017 Will Winder

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

import com.google.common.collect.ImmutableList;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import static com.willwinder.universalgcodesender.gcode.util.Plane.*;
import com.willwinder.universalgcodesender.gcode.processors.ICommandProcessor;
import com.willwinder.universalgcodesender.gcode.processors.Stats;
import com.willwinder.universalgcodesender.gcode.util.PlaneFormatter;
import com.willwinder.universalgcodesender.types.PointSegment;

import java.util.*;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.vecmath.Point3d;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author wwinder
 */
public class GcodeParser implements IGcodeParser {
    private static final Logger logger = Logger.getLogger(GcodeParser.class.getName());

    // Current state
    private GcodeState state;

    private final ArrayList<ICommandProcessor> processors = new ArrayList<>();

    private Stats statsProcessor;

    /**
     * An intermediate object with all metadata for a given point.
     */
    public static class GcodeMeta {
        /**
         * The original command represented by this meta object.
         */
        public String command;

        /**
         * Gcode command in line.
         */
        public String code;

        /**
         * Gcode state after processing the command.
         */
        public GcodeState state;

        /**
         * PointSegments represent the endpoint of a given command.
         */
        public PointSegment point;
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
        this.statsProcessor = new Stats();
    }

    /**
     * Resets the current state.
     */
    public void reset() {
        this.statsProcessor = new Stats();
        this.state.currentPoint = new Point3d();
        this.state.commandNumber = -1;
    }
    
    /**
     * Add a command to be processed with no line number association.
     */
    @Override
    public List<GcodeMeta> addCommand(String command) throws GcodeParserException {
        return addCommand(command, ++this.state.commandNumber);
    }

    /**
     * Add a command to be processed with a line number.
     * @throws GcodeParserException If the command is too long throw an exception
     */
    @Override
    public List<GcodeMeta> addCommand(String command, int line) throws GcodeParserException {
        statsProcessor.processCommand(command, state);
        List<GcodeMeta> results = new ArrayList<>();
        // Add command get meta doesn't update the state, so we need to do that
        // manually.
        //List<String> processedCommands = this.preprocessCommand(command);
        Collection<GcodeMeta> metaObjects = processCommand(command, line, state);
        if (metaObjects != null) {
            for (GcodeMeta c : metaObjects) {
                results.add(c);
                if (c.state != null) {
                    this.state = c.state;
                    // Process stats.
                    statsProcessor.processCommand(command, state);
                }
            }
        }

        return results;
    }
    
    /**
     * Gets the point at the end of the list.
     */
    @Override
    public GcodeState getCurrentState() {
        return this.state;
    }

    @Override
    public GcodeStats getCurrentStats() {
        return statsProcessor;
    }
    
    /**
     * Process commend given an initial state. This method will not modify its
     * input parameters.
     */
    public static List<GcodeMeta> processCommand(String command, int line, final GcodeState inputState)
            throws GcodeParserException {
        String noCommentsCommand = GcodePreprocessorUtils.removeComment(command);
        List<String> args = GcodePreprocessorUtils.splitCommand(noCommentsCommand);
        if (args.isEmpty()) return null;

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
        
        // If there was no command and there are coordinates, add the implicit one to the party.
        if (gCodes.isEmpty() && !StringUtils.isBlank(state.lastGcodeCommand)
                && GcodePreprocessorUtils.hasCoordinates(args)) {
            gCodes.add(state.lastGcodeCommand);
        }
        
        List<GcodeMeta> results = new ArrayList<>();
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

    private static PointSegment addProbePointSegment(Point3d nextPoint, boolean fastTraverse, int line, GcodeState state) {
        PointSegment ps = addLinearPointSegment(nextPoint, fastTraverse, line, state);
        ps.setIsProbe(true);
        return ps;
    }

    /**
     * Create a PointSegment representing the linear command.
     */
    private static PointSegment addLinearPointSegment(Point3d nextPoint, boolean fastTraverse, int line, GcodeState state) {
        if (nextPoint == null) {
            return null;
        }

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

    /**
     * Create a PointSegment representing the arc command.
     */
    private static PointSegment addArcPointSegment(Point3d nextPoint, boolean clockwise, List<String> args, int line, GcodeState state) {
        if (nextPoint == null) {
            return null;
        }

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
     * Branch parser to handle specific gcode command.
     * 
     * A copy of the state object should go in the resulting GcodeMeta object.
     */
    private static final Pattern CODE_PATTERN = Pattern.compile("(?:0?)+(\\d+)");
    private static GcodeMeta handleGCode(final String code, List<String> args, int line, GcodeState state)
            throws GcodeParserException {
        GcodeMeta meta = new GcodeMeta();

        Matcher m = CODE_PATTERN.matcher(code);
        if (!m.matches()) {
            throw new GcodeParserException("Invalid gcode in handleGCode: " + code);
        }

        final String codeChar = m.group(1);

        meta.code = codeChar;

        Point3d nextPoint = null;

        // If it is a movement code make sure it has some coordinates.
        if (GcodePreprocessorUtils.hasCoordinates(args)) {
            nextPoint = GcodePreprocessorUtils.updatePointWithCommand(args, state.currentPoint, state.inAbsoluteMode)
                    .orElseThrow(() -> new GcodeParserException("Invalid: G" + codeChar + " with no coordinates"));
        }

        switch (codeChar) {
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

            // Probe: http://linuxcnc.org/docs/html/gcode/g-code.html#gcode:g38
            case "38.2": // probe toward workpiece, stop on contact, signal error if failure
            case "38.3": // probe toward workpiece, stop on contact
            case "38.4": // probe away from workpiece, stop on loss of contact, signal error if failure
            case "38.5": // probe away from workpiece, stop on loss of contact
                meta.point = addProbePointSegment(nextPoint, true, line, state);
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
        state.lastGcodeCommand = codeChar;
        meta.state = state.copy();
        return meta;
    }

    /**
     * Applies all command processors to a given command and returns the
     * resulting GCode. Does not change the parser state.
     * 
     * TODO: Rather than have a separate 'preprocessCommand' which needs to be
     * followed up with calls to addCommand, it would be great to have addCommand
     * also do the preprocessing. This is challenging because they have different
     * return types.
     * 
     * This is also needed for some very particular processing in GUIBackend which
     * gathers comments as a separate step outside the GcodeParser.
     * 
     * TODO 2: Move this processing logic into another class, or GcodeParserUtils along with testState.
     */
    @Override
    public List<String> preprocessCommand(String command, final GcodeState initialState) throws GcodeParserException {
        List<String> ret = new ArrayList<>();
        ret.add(command);
        GcodeState tempState = null;
        for (ICommandProcessor p : processors) {
            // Reset point segments after each pass. The final pass is what we will return.
            tempState = initialState.copy();
            // Process each command in the list and add results to the end.
            // Don't re-process the results with the same preprocessor.
            for (int i = ret.size(); i > 0; i--) {
                // The arc expander changes the lastGcodeCommand which causes the following to fail:
                // G2 Y-0.7 J-14.7
                // Y28.7 J14.7 (this line treated as a G1)
                tempState.lastGcodeCommand = initialState.lastGcodeCommand;
                List<String> intermediate = p.processCommand(ret.remove(0), tempState);

                // process results to update the state and collect PointSegments
                for(String c : intermediate) {
                    tempState = testState(c, tempState);
                }

                ret.addAll(intermediate);
            }
        }

        return ret;
    }

    /**
     * Helper to statically process the next step in a program without modifying the parser.
     */
    static private GcodeState testState(String command, GcodeState state) throws GcodeParserException {
        GcodeState ret = state;

        // Add command get meta doesn't update the state, so we need to do that manually.
        Collection<GcodeMeta> metaObjects = processCommand(command, 0, state);
        if (metaObjects != null) {
            for (GcodeMeta c : metaObjects) {
                if (c.state != null) {
                    ret = c.state;
                }
            }
        }

        return ret;
    }
}
