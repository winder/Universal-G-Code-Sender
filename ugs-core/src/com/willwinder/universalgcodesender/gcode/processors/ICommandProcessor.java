/**
 * Used by the gcode parser to preprocess commands.
 */
package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.gcode.util.GcodeParserException;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import java.util.List;

/**
 *
 * @author wwinder
 */
public interface ICommandProcessor {
    /**
     * Given a command and the current state of a program returns a replacement
     * list of commands.
     * @param command Input gcode.
     * @param state State of the gcode parser when the command will run.
     * @return One or more gcode commands to replace the original command with.
     */
    public List<String> processCommand(String command, GcodeState state) throws GcodeParserException;
}
