/**
 * Removes all unnecessary whitespace.
 */
package com.willwinder.universalgcodesender.gcode.processors;

import com.willwinder.universalgcodesender.gcode.GcodePreprocessorUtils;
import com.willwinder.universalgcodesender.gcode.GcodeState;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author wwinder
 */
public class WhitespaceProcessor implements ICommandProcessor {

    @Override
    public List<String> processCommand(String command, GcodeState state) {
        ArrayList<String> result = new ArrayList<>();
        result.add(GcodePreprocessorUtils.removeAllWhitespace(command));
        return result;
    }
}
