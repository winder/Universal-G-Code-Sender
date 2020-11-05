/*
    Copyright 2013-2020 Will Winder

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

import com.google.common.collect.Iterables;
import com.willwinder.universalgcodesender.gcode.processors.CommandProcessor;
import com.willwinder.universalgcodesender.gcode.processors.Stats;
import com.willwinder.universalgcodesender.gcode.util.Code;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.gcode.util.Plane;
import com.willwinder.universalgcodesender.gcode.util.PlaneFormatter;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.types.PointSegment;
import org.apache.commons.lang3.StringUtils;

import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.willwinder.universalgcodesender.gcode.util.Code.*;
import static com.willwinder.universalgcodesender.gcode.util.Code.ModalGroup.Motion;

/**
 * Object to parse gcode one command at a time in a way that can be used by any
 * other class which needs to know about the current state at a given command.
 *
 * This object can be extended by adding in any number of ICommandProcessor
 * objects which are applied to each command in the order they were inserted
 * into the parser. These processors can be as simple as removing whitespace to
 * as complex as expanding a canned cycle or applying an leveling plane.
 *
 * @author wwinder
 */
public class GcodeParser implements IGcodeParser {
    private static final Logger logger = Logger.getLogger(GcodeParser.class.getName());

    // Current state
    private GcodeState state;

    private final List<CommandProcessor> processors = new ArrayList<>();

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
        public Code code;

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
    public void addCommandProcessor(CommandProcessor p) {
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
        this.state = new GcodeState();
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
        Collection<GcodeMeta> metaObjects = processCommand(command, line, state, true);
        if (metaObjects != null) {
            for (GcodeMeta c : metaObjects) {
                if(c.point != null) {
                    results.add(c);
                }
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
     * For backwards compatibility this method calls processCommand with includeNonMotionStates = false.
     */
    public static List<GcodeMeta> processCommand(String command, int line, final GcodeState inputState)
            throws GcodeParserException {
      return processCommand(command, line, inputState, false);
    }
    
    /**
     * Process command given an initial state. This method will not modify its
     * input parameters.
     * 
     * @param includeNonMotionStates Create gcode meta responses even if there is no motion, for example "F100" will not
     * return a GcodeMeta entry unless this flag is set to true.
     */
    public static List<GcodeMeta> processCommand(String command, int line, final GcodeState inputState,
            boolean includeNonMotionStates)
            throws GcodeParserException {
        List<String> args = GcodePreprocessorUtils.splitCommand(command);
        if (args.isEmpty()) return null;

        // Initialize with original state
        GcodeState state = inputState.copy();

        state.commandNumber = line;
        
        // handle M codes.
        Set<Code> mCodes = GcodePreprocessorUtils.getMCodes(args);
        for (Code c : mCodes) {
            switch(c.getType()) {
                case Spindle:
                    state.spindle = c;
                    break;
                case Coolant:
                    state.coolant = c;
                    break;
                default:
                    break;
            }
        }

        List<String> fCodes = GcodePreprocessorUtils.parseCodes(args, 'F');
        if (!fCodes.isEmpty()) {
            try {
                state.speed = Double.parseDouble(Iterables.getOnlyElement(fCodes));
            } catch (IllegalArgumentException e) {
                throw new GcodeParserException("Multiple F-codes on one line.");
            }
        }

        List<String> sCodes = GcodePreprocessorUtils.parseCodes(args, 'S');
        if (!sCodes.isEmpty()) {
            try {
                state.spindleSpeed = Double.parseDouble(Iterables.getOnlyElement(sCodes));
            } catch (IllegalArgumentException e) {
                throw new GcodeParserException("Multiple S-codes on one line.");
            }
        }
        
        // Gather G codes.
        Set<Code> gCodes = GcodePreprocessorUtils.getGCodes(args);
        
        boolean hasAxisWords = GcodePreprocessorUtils.hasAxisWords(args);

        // Error to mix group 1 (Motion) and certain group 0 (NonModal) codes (G10, G28, G30, G92)
        Collection<Code> motionCodes = gCodes.stream()
                .filter(Code::consumesMotion)
                .collect(Collectors.toList());

        // 1 motion code per line.
        if (motionCodes.size() > 1) {
            throw new GcodeParserException(Localization.getString("parser.gcode.multiple-axis-commands")
                    + ": " + StringUtils.join(motionCodes, ", "));
        }

        // If there are axis words and nothing to use them, add the currentMotionMode.
        if (hasAxisWords && motionCodes.isEmpty() && state.currentMotionMode != null) {
            gCodes.add(state.currentMotionMode);
        }

        // Apply each code to the state.
        List<GcodeMeta> results = new ArrayList<>();
        for (Code i : gCodes) {
            if (i == UNKNOWN) {
                logger.warning("An unknown gcode command was detected in: " + command);
            } else {
                GcodeMeta meta = handleGCode(i, args, line, state);
                meta.command = command;
                // Commands like 'G21' don't return a point segment.
                if (meta.point != null) {
                    meta.point.setSpeed(state.speed);
                }
                results.add(meta);
            }
        }

        // Return updated state / command.
        if (results.isEmpty() && includeNonMotionStates) {
          GcodeMeta meta = new GcodeMeta();
          meta.state = state;
          meta.command = command;
          meta.code = state.currentMotionMode;
          return Collections.singletonList(meta);
        }
        
        return results;
    }

    private static PointSegment addProbePointSegment(Position nextPoint, boolean fastTraverse, int line, GcodeState state) {
        PointSegment ps = addLinearPointSegment(nextPoint, fastTraverse, line, state);
        ps.setIsProbe(true);
        return ps;
    }

    /**
     * Create a PointSegment representing the linear command.
     */
    private static PointSegment addLinearPointSegment(Position nextPoint, boolean fastTraverse, int line, GcodeState state) {
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
    private static PointSegment addArcPointSegment(Position nextPoint, boolean clockwise, List<String> args, int line, GcodeState state) {
        if (nextPoint == null) {
            return null;
        }

        PointSegment ps = new PointSegment(nextPoint, line);

        PlaneFormatter plane = new PlaneFormatter(state.plane);
        Position center =
                GcodePreprocessorUtils.updateCenterWithCommand(
                        args, state.currentPoint, nextPoint, state.inAbsoluteIJKMode, clockwise, plane);

        double radius = GcodePreprocessorUtils.parseCoord(args, 'R');

        // Calculate radius if necessary, according to the current G17/18/19 Plane
        if (Double.isNaN(radius)) {

            radius = Math.sqrt(
                    Math.pow(plane.axis0(state.currentPoint)  - plane.axis0(center), 2.0)
                            + Math.pow(plane.axis1(state.currentPoint) - plane.axis1(center), 2.0));
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
    private static GcodeMeta handleGCode(final Code code, List<String> args, int line, GcodeState state)
            throws GcodeParserException {
        GcodeMeta meta = new GcodeMeta();

        meta.code = code;

        Position nextPoint = null;

        // If it is a movement code make sure it has some coordinates.
        if (code.consumesMotion()) {
            nextPoint = GcodePreprocessorUtils.updatePointWithCommand(args, state.currentPoint, state.inAbsoluteMode);

            if (nextPoint == null) {
                if (!code.motionOptional()) {
                    throw new GcodeParserException(
                            Localization.getString("parser.gcode.missing-axis-commands") + ": " + code);
                }
            }
        }

        if (nextPoint == null && meta.point != null) {
            nextPoint = meta.point.point();
        }

        switch (code) {
            case G0:
                meta.point = addLinearPointSegment(nextPoint, true, line, state);
                break;
            case G1:
                meta.point = addLinearPointSegment(nextPoint, false, line, state);
                break;

            // Arc command.
            case G2:
                meta.point = addArcPointSegment(nextPoint, true, args, line, state);
                break;
            case G3:
                meta.point = addArcPointSegment(nextPoint, false, args, line, state);
                break;

            case G17:
            case G18:
            case G19:
            case G17_1:
            case G18_1:
            case G19_1:
                state.plane = Plane.lookup(code);
                break;

            //inch
            case G20:
                state.isMetric = false;
                state.units = G20;
                state.currentPoint = state.currentPoint.getPositionIn(UnitUtils.Units.INCH);
                break;
            //mm
            case G21:
                state.isMetric = true;
                state.units = G21;
                state.currentPoint = state.currentPoint.getPositionIn(UnitUtils.Units.MM);
                break;

            // Probe: http://linuxcnc.org/docs/html/gcode/g-code.html#gcode:g38
            case G38_2: // probe toward workpiece, stop on contact, signal error if failure
            case G38_3: // probe toward workpiece, stop on contact
            case G38_4: // probe away from workpiece, stop on loss of contact, signal error if failure
            case G38_5: // probe away from workpiece, stop on loss of contact
                meta.point = addProbePointSegment(nextPoint, true, line, state);
                break;

            // These are not used in the visualizer.
            case G54:
            case G55:
            case G56:
            case G57:
            case G58:
            case G59:
            case G59_1:
            case G59_2:
            case G59_3:
                state.offset = code;
                break;

            case G90:
                state.inAbsoluteMode = true;
                state.distanceMode = G90;
                break;
            case G91:
                state.inAbsoluteMode = false;
                state.distanceMode = G91;
                break;

            case G90_1:
                state.inAbsoluteIJKMode = true;
                state.arcDistanceMode = G90_1;
                break;
            case G91_1:
                state.inAbsoluteIJKMode = false;
                state.arcDistanceMode = G91_1;
                break;

            case G93:
            case G94:
            case G95:
                state.feedMode = code;
                break;
            default:
                break;
        }
        if (code.getType() == Motion) {
            state.currentMotionMode = code;
        }
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
        GcodeState tempState;
        for (CommandProcessor p : processors) {
            // Reset point segments after each pass. The final pass is what we will return.
            tempState = initialState.copy();
            // Process each command in the list and add results to the end.
            // Don't re-process the results with the same preprocessor.
            for (int i = ret.size(); i > 0; i--) {
                // The arc expander changes the lastGcodeCommand which causes the following to fail:
                // G2 Y-0.7 J-14.7
                // Y28.7 J14.7 (this line treated as a G1)
                tempState.currentMotionMode = initialState.currentMotionMode;
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
