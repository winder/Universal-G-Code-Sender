/**
 * Gcode parser interface.
 */
package com.willwinder.universalgcodesender.gcode;

import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.gcode.processors.ICommandProcessor;
import com.willwinder.universalgcodesender.types.PointSegment;
import java.util.List;

/**
 *
 * @author wwinder
 */
public interface IGcodeParser {

    /**
     * @return the number of command processors that have been added.
     */
    public int numCommandProcessors();

    /**
     * Add a preprocessor to use with the preprocessCommand method.
     */
    public void addCommandProcessor(ICommandProcessor p);

    /**
     * Clear out any processors that have been added.
     */
    public void resetCommandProcessors();

    /**
     * Add a string of command(s) for parsing.
     * @param command
     * @return PointSegment representing the last command.
     */
    public List<PointSegment> addCommand(String command) throws GcodeParserException;

    /**
     * Add a string of command(s) and a line number associated with that string.
     * @param command
     * @param lineNumber
     * @return PointSegment(s) representing the command.
     * @throws GcodeParserException
     */
    public List<PointSegment> addCommand(String command, int lineNumber) throws GcodeParserException;

    /**
     * The state of the machine as of the last command.
     * @return GcodeState
     */
    public GcodeState getCurrentState();

    /**
     * Preprocesses a gcode string and returns one or more strings with the
     * postprocessed gcode commands. Each of the ICommandProcessors should be
     * applied to the string in the order they were given to the parser.
     * @param command gcode string to process
     * @return a collection of postprocessed commands
     * @throws GcodeParserException 
     */
    public List<String> preprocessCommand(String command) throws GcodeParserException;
}
