/*
    Copyright 2017-2020 Will Winder

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
package com.willwinder.universalgcodesender.gcode.util;

import com.google.common.collect.Iterables;
import com.willwinder.universalgcodesender.gcode.GcodeParser;
import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import com.willwinder.universalgcodesender.i18n.Localization;
import com.willwinder.universalgcodesender.model.Position;
import com.willwinder.universalgcodesender.model.UnitUtils;
import com.willwinder.universalgcodesender.types.GcodeCommand;
import com.willwinder.universalgcodesender.types.PointSegment;
import com.willwinder.universalgcodesender.utils.GcodeStreamReader;
import com.willwinder.universalgcodesender.utils.IGcodeStreamReader;
import com.willwinder.universalgcodesender.utils.IGcodeWriter;
import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static com.willwinder.universalgcodesender.gcode.util.Code.G20;
import static com.willwinder.universalgcodesender.gcode.util.Code.G21;
import static com.willwinder.universalgcodesender.gcode.util.Code.G90;
import static com.willwinder.universalgcodesender.gcode.util.Code.G90_1;
import static com.willwinder.universalgcodesender.gcode.util.Code.G91;
import static com.willwinder.universalgcodesender.gcode.util.Code.G91_1;
import static com.willwinder.universalgcodesender.gcode.util.Code.ModalGroup.Motion;
import static com.willwinder.universalgcodesender.gcode.util.Code.UNKNOWN;

/**
 *
 * @author wwinder
 */
public class GcodeParserUtils {
    private static final Logger LOGGER = Logger.getLogger(GcodeParserUtils.class.getName());

    /**
     * For backwards compatibility this method calls processCommand with includeNonMotionStates = false.
     */
    public static List<GcodeParser.GcodeMeta> processCommand(String command, int line, final GcodeState inputState)
            throws GcodeParserException {
        return processCommand(command, line, inputState, false);
    }

    /**
     * Process command given an initial state. This method will not modify its
     * input parameters.
     *
     * @param includeNonMotionStates Create gcode meta responses even if there is no motion, for example "F100" will not
     *                               return a GcodeMeta entry unless this flag is set to true.
     */
    public static List<GcodeParser.GcodeMeta> processCommand(String command, int line, final GcodeState inputState,
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
            switch (c.getType()) {
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
        List<GcodeParser.GcodeMeta> results = new ArrayList<>();
        for (Code i : gCodes) {
            if (i == UNKNOWN) {
                LOGGER.warning("An unknown gcode command was detected in: " + command);
            } else {
                GcodeParser.GcodeMeta meta = handleGCode(i, args, line, state);
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
            GcodeParser.GcodeMeta meta = new GcodeParser.GcodeMeta();
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

        boolean zOnly = state.currentPoint.isZMotionTo(nextPoint);
        boolean isRotation = state.currentPoint.hasRotationTo(nextPoint);

        ps.setIsMetric(state.isMetric);
        ps.setIsZMovement(zOnly);
        ps.setIsRotation(isRotation);
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
                    Math.pow(plane.axis0(state.currentPoint) - plane.axis0(center), 2.0)
                            + Math.pow(plane.axis1(state.currentPoint) - plane.axis1(center), 2.0));
        }

        ps.setIsMetric(state.isMetric);
        ps.setArcCenter(center);
        ps.setIsArc(true);
        ps.setRadius(radius);
        ps.setIsClockwise(clockwise);
        ps.setPlaneState(state.plane);

        boolean isRotation = state.currentPoint.hasRotationTo(nextPoint);
        ps.setIsRotation(isRotation);


        // Save off the endpoint.
        state.currentPoint = nextPoint;
        return ps;
    }

    /**
     * Branch parser to handle specific gcode command.
     * <p>
     * A copy of the state object should go in the resulting GcodeMeta object.
     */
    private static GcodeParser.GcodeMeta handleGCode(final Code code, List<String> args, int line, GcodeState state)
            throws GcodeParserException {
        GcodeParser.GcodeMeta meta = new GcodeParser.GcodeMeta();

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
     * Helper method to apply processors to gcode.
     */
    public static void processAndExport(GcodeParser gcp, File input, IGcodeWriter output)
            throws IOException, GcodeParserException {
        try(BufferedReader br = new BufferedReader(new FileReader(input))) {
            if (processAndExportGcodeStream(gcp, br, output)) {
                return;
            }
        }

        try(BufferedReader br = new BufferedReader(new FileReader(input))) {
            processAndExportText(gcp, br, output);
        }
    }

    /**
     * Common logic in processAndExport* methods.
     */
    private static void preprocessAndWrite(GcodeParser gcp, IGcodeWriter gsw, String command, String comment, int idx) throws GcodeParserException {
        if (idx % 100000 == 0) {
            LOGGER.log(Level.FINE, "gcode processing line: " + idx);
        }

        if (StringUtils.isEmpty(command)) {
            gsw.addLine(command, command, comment, idx);
        }
        else {
            // Parse the gcode for the buffer.
            Collection<String> lines = gcp.preprocessCommand(command, gcp.getCurrentState());

            for(String processedLine : lines) {
                gsw.addLine(command, processedLine, comment, idx);
            }

            gcp.addCommand(command);
        }
    }

    /**
     * Attempts to read the input file in GcodeStream format.
     * @return whether or not we succeed processing the file.
     */
    private static boolean processAndExportGcodeStream(GcodeParser gcp, BufferedReader input, IGcodeWriter output)
            throws IOException, GcodeParserException {

        // Preprocess a GcodeStream file.
        try (IGcodeStreamReader gsr = new GcodeStreamReader(input)) {
            int i = 0;
            while (gsr.getNumRowsRemaining() > 0) {
                i++;
                GcodeCommand gc = gsr.getNextCommand();
                preprocessAndWrite(gcp, output, gc.getCommandString(), gc.getComment(), i);
            }

            // Done processing GcodeStream file.
            return true;
        } catch (GcodeStreamReader.NotGcodeStreamFile ex) {
            // File exists, but isn't a stream reader. So go ahead and try parsing it as a raw gcode file.
        }
        return false;
    }

    /**
     * Attempts to read the input file in gcode-text format.
     * @return whether or not we succeed processing the file.
     */
    private static void processAndExportText(GcodeParser gcp, BufferedReader input, IGcodeWriter output)
            throws IOException, GcodeParserException {
        // Preprocess a regular gcode file.
        try(BufferedReader br = input) {
            int i = 0;
            for(String line; (line = br.readLine()) != null; ) {
                i++;

                String comment = GcodePreprocessorUtils.parseComment(line);
                preprocessAndWrite(gcp, output, line, comment, i);
            }
        }
    }
}
