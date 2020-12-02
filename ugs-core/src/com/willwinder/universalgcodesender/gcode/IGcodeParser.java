package com.willwinder.universalgcodesender.gcode;

import com.willwinder.universalgcodesender.gcode.GcodeParser.GcodeMeta;
import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import java.util.List;
import com.willwinder.universalgcodesender.gcode.processors.CommandProcessor;

/**
 * Gcode parser interface.
 *
 * @author wwinder
 */
public interface IGcodeParser {

    /**
     * @return the number of command processors that have been added.
     */
    int numCommandProcessors();

    /**
     * Add a preprocessor to use with the preprocessCommand method.
     * @param p - a processor to be added
     */
    void addCommandProcessor(CommandProcessor p);

    /**
     * Removes a processor to not be used anymore
     * @param p - a processor to remove
     */
    void removeCommandProcessor(CommandProcessor p);

    /**
     * Clear out any processors that have been added.
     */
    void clearCommandProcessors();

    /**
     * Add a string of command(s) for parsing.
     * @param command
     * @return PointSegment representing the last command.
     */
    List<GcodeMeta> addCommand(String command) throws GcodeParserException;

    /**
     * Add a string of command(s) and a line number associated with that string.
     * @param command
     * @param lineNumber
     * @return PointSegment(s) representing the command.
     * @throws GcodeParserException
     */
    List<GcodeMeta> addCommand(String command, int lineNumber) throws GcodeParserException;

    /**
     * The state of the machine as of the last command.
     * @return GcodeState
     */
    GcodeState getCurrentState();

    /**
     * The current stats of the gcode file.
     * @return GcodeStats
     */
    GcodeStats getCurrentStats();

    /**
     * Preprocesses a gcode string and returns one or more strings with the
     * postprocessed gcode commands. Each of the ICommandProcessors should be
     * applied to the string in the order they were given to the parser.
     * @param command gcode string to process
     * @return a collection of postprocessed commands
     * @throws GcodeParserException 
     */
    List<String> preprocessCommand(String command, GcodeState initial) throws GcodeParserException;
}
