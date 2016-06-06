/*
 * Used by the gcode parser to preprocess commands.
 */
package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.gcode.GcodeState;
import java.util.List;

/**
 *
 * @author wwinder
 */
public interface ICommendProcessor {
    /**
     * Given a command and the current state of a program returns a replacement
     * list of commands.
     */
    public List<String> processCommand(String command, GcodeState state);
}
