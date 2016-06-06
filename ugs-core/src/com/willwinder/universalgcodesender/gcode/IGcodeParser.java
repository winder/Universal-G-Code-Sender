/**
 * Gcode parser interface.
 */
package com.willwinder.universalgcodesender.gcode;

import com.willwinder.universalgcodesender.types.PointSegment;

/**
 *
 * @author wwinder
 */
public interface IGcodeParser {
    /**
     * Add a string of command(s) for parsing.
     * @param command
     * @return PointSegment representing the last command.
     */
    public PointSegment addCommand(String command) throws GcodeParserException;

    /**
     * Add a string of command(s) and a line number associated with that string.
     * @param command
     * @param lineNumber
     * @return PointSegment representing the last command.
     */
    public PointSegment addCommand(String command, int lineNumber) throws GcodeParserException;

    /**
     * The state of the machine as of the last command.
     * @return GcodeState
     */
    public GcodeState getCurrentState();
}
